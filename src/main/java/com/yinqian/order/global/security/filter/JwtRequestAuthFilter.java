package com.yinqian.order.global.security.filter;

import com.yinqian.order.bean.UserInfo;
import com.yinqian.order.global.ContentValue;
import com.yinqian.order.global.security.JwtAuthenticationToken;
import com.yinqian.order.global.security.handler.AuthFailHandler;
import com.yinqian.order.global.security.handler.JwtRefreshSuccessHandler;
import com.yinqian.order.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.util.matcher.RequestHeaderRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 用户除登录之外的请求，都要求必须携带JWT Token
 * 这个Filter对这些请求做一个拦截  提取header中的token
 * OncePerRequestFilter过滤器保证一次请求只调用一次doFilterInternal方法;如内部的forward不会再多执行一次
 */
//@Component
public class JwtRequestAuthFilter extends OncePerRequestFilter {

    private final RequestHeaderRequestMatcher requestHeaderRequestMatcher = new RequestHeaderRequestMatcher(ContentValue.LOGIN_TOKEN_NAME);

    @Autowired
    private UserService userService;

    /*@Autowired
    private AuthSuccessHandler successHandler;*/
    @Autowired
    private AuthFailHandler failHandler;
    @Autowired
    private JwtRefreshSuccessHandler successHandler;

    private AuthenticationManager authenticationManager;

    private List<RequestMatcher> permissiveRequestMatchers = new ArrayList<>();

    protected String getJwtToken(HttpServletRequest request){
        String token = request.getHeader(ContentValue.LOGIN_TOKEN_NAME);
        return token;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        //header没带token的，直接放过，因为部分url匿名用户也可以访问
        //如果需要不支持匿名用户的请求没带token，这里放过也没问题，因为SecurityContext中没有认证信息，后面会被权限控制模块拦截
        if (!requestHeaderRequestMatcher.matches(request)){
            filterChain.doFilter(request,response);
            return;
        }
        Authentication authResult = null;
        AuthenticationException failed = null;
        String token = getJwtToken(request);
        if (StringUtils.isBlank(token)) {
            //如果token的长度是0
            failed = new InsufficientAuthenticationException("JWT is Empty");
        }else {
            UserInfo user = userService.verify(token);
            JwtAuthenticationToken authenticationToken = new JwtAuthenticationToken(user);
            authResult = authenticationManager.authenticate(authenticationToken);
        }
        if (authResult != null){
            //成功的回调方法
            SecurityContextHolder.getContext().setAuthentication(authResult);
            successHandler.onAuthenticationSuccess(request, response, authResult);
        }else {
            //失败
            SecurityContextHolder.clearContext();
            failHandler.onAuthenticationFailure(request, response, failed);
            return;
        }
        filterChain.doFilter(request,response);
    }

    /**
     * manager需要在configuration中获取
     * @param authenticationManager
     */
    public void setAuthenticationManager(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }


    protected boolean permissiveRequest(HttpServletRequest request) {
        if(permissiveRequestMatchers == null)
            return false;
        for(RequestMatcher permissiveMatcher : permissiveRequestMatchers) {
            if(permissiveMatcher.matches(request))
                return true;
        }
        return false;
    }

}