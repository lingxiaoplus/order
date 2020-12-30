package com.yinqian.order.global.security;

import com.yinqian.order.global.ContentValue;
import com.yinqian.order.global.SecurityProperties;
import com.yinqian.order.global.security.filter.UrlMetadataSourceFilter;
import com.yinqian.order.global.security.handler.AuthFailHandler;
import com.yinqian.order.global.security.handler.AuthSuccessHandler;
import com.yinqian.order.global.security.handler.RestAccessDeniedHandler;
import com.yinqian.order.global.security.handler.TokenClearLogoutHandler;
import com.yinqian.order.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.ObjectPostProcessor;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.access.intercept.FilterSecurityInterceptor;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.List;

//@EnableGlobalMethodSecurity(prePostEnabled = true) //开启注解 判断用户对某个控制层的方法是否具有访问权限
@EnableWebSecurity
@EnableConfigurationProperties({SecurityProperties.class})
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private AuthSuccessHandler successHandler;

    @Autowired
    private AuthFailHandler failHandler;

    @Autowired
    private RestAccessDeniedHandler accessDeniedHandler;
    @Autowired
    private JwtAuthenticationProvider jwtAuthenticationProvider;
    @Autowired
    private TokenClearLogoutHandler tokenClearLogoutHandler;
    @Autowired
    private UrlMetadataSourceFilter metadataSource;
    @Autowired
    private UrlAccessDecisionManager manager;
    @Autowired
    private UserService userService;
    @Autowired
    private SecurityProperties securityProperties;


    /**
     * WebSecurity 全局请求忽略规则配置（比如说静态文件，比如说注册页面）、全局HttpFirewall配置
     * @param web
     * @throws Exception
     */
    @Override
    public void configure(WebSecurity web) throws Exception {
        //静态资源无需认证
        List<String> ignorePaths = securityProperties.getIgnorePaths();
        web.ignoring().antMatchers(ignorePaths.toArray(new String[ignorePaths.size()]));
    }

    /**
     * 具体的权限控制规则
     * @param http
     * @throws Exception
     */
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        //super.configure(http);

        http.authorizeRequests()
                //.antMatchers("/admin/login/*","/swagger-ui.html","/swagger-resources/*", "/webjars/*","/v2/api-docs*","/doc.html").permitAll()
                .withObjectPostProcessor(new ObjectPostProcessor<FilterSecurityInterceptor>() {
                    @Override
                    public <O extends FilterSecurityInterceptor> O postProcess(O o) {
                        o.setSecurityMetadataSource(metadataSource);
                        o.setAccessDecisionManager(manager);
                        return o;
                    }
                })
                .anyRequest().authenticated()  //默认其它的请求都需要认证，这里一定要添加
                .and()
                .cors()  //支持跨域
                .and()
                //CRSF禁用，因为不使用session
                .csrf().disable()
                //禁用session
                .sessionManagement().disable()
                .formLogin().disable() //禁用form登录
                //添加header设置，支持跨域和ajax请求
                .addFilterAfter(corsFilter(), CorsFilter.class)
                //添加登录filter
                .apply(new LoginConfigure<>()).loginHandler(successHandler,failHandler)
                .and()
                .apply(new JwtRequestConfigure<>()).and()
                //添加token的filter
                //.apply(new JwtLoginConfigurer<>()).tokenValidSuccessHandler(successHandler).permissiveRequestUrls("/logout")
                //使用默认的logoutFilter
                .logout()
                .logoutUrl("/logout")
                //logout时清除token
                .addLogoutHandler(tokenClearLogoutHandler)
                //logout成功后返回200
                .logoutSuccessHandler(new HttpStatusReturningLogoutSuccessHandler())
                .and()
                .sessionManagement()
                .disable()
                .exceptionHandling()
                .accessDeniedHandler(accessDeniedHandler);
    }

    /**
     * 配置全局的认证相关的信息
     * @param auth
     * @throws Exception
     */
    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userService);
        auth.authenticationProvider(daoAuthenticationProvider()).authenticationProvider(jwtAuthenticationProvider);
    }

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }
    @Bean("daoAuthenticationProvider")
    protected AuthenticationProvider daoAuthenticationProvider(){
        //这里会默认使用BCryptPasswordEncoder比对加密后的密码，注意要跟createUser时保持一致
        DaoAuthenticationProvider daoProvider = new DaoAuthenticationProvider();
        daoProvider.setUserDetailsService(userService);
        return daoProvider;
    }


    @Bean
    public CorsFilter corsFilter(){
        //1.添加cors配置信息
        CorsConfiguration config = new CorsConfiguration();
        //.1 允许的域，不要写*
        List<String> allowedOrigins = securityProperties.getAllowedOrigins();
        allowedOrigins.forEach(config::addAllowedOrigin);
        //.2是否发送cookie信息
        config.setAllowCredentials(true);
        //.3允许的请求方式
        List<String> allowedMethods = securityProperties.getAllowedMethods();
        allowedMethods.forEach(config::addAllowedMethod);
        //.4允许的头信息
        config.addAllowedHeader("*");
        config.addExposedHeader(ContentValue.LOGIN_TOKEN_NAME);
        //.5添加有效时长
        config.setMaxAge(3600L);

        //2.添加映射路径
        UrlBasedCorsConfigurationSource configurationSource = new UrlBasedCorsConfigurationSource();
        configurationSource.registerCorsConfiguration("/**",config);

        //3.返回新的CorsFilter
        return new CorsFilter(configurationSource);
    }

}
