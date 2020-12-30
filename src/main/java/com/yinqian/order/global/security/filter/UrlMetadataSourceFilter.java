package com.yinqian.order.global.security.filter;

import com.yinqian.order.global.ContentValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.access.SecurityConfig;
import org.springframework.security.access.SecurityMetadataSource;
import org.springframework.security.web.FilterInvocation;
import org.springframework.security.web.access.intercept.FilterInvocationSecurityMetadataSource;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.CollectionUtils;

import java.util.Collection;
import java.util.List;

/**
 * {@link SecurityMetadataSource}
 * @author lingxiao
 * 动态获取url权限配置  通过当前的请求地址，获取该地址需要的用户角色
 */
@Component
public class UrlMetadataSourceFilter implements FilterInvocationSecurityMetadataSource {

    /*@Autowired
    private MenuService menuService;*/
    private AntPathMatcher antPathMatcher = new AntPathMatcher();

    @Override
    public Collection<ConfigAttribute> getAttributes(Object object) throws IllegalArgumentException {
        //获取请求地址
        String requestUrl = ((FilterInvocation) object).getRequestUrl();
        /*List<Menu> menus = menuService.selectAll();
        for (Menu menu: menus) {
            if (antPathMatcher.match(menu.getUrl(),requestUrl)){
                List<Role> roles = menuService.getRolesByMenu(menu.getId());
                //如果role是空的，说明这个menu没有和role绑定
                if (CollectionUtils.isEmpty(roles)) {
                    break;
                }
                int size = CollectionUtils.isEmpty(roles)?0:roles.size();
                String[] values = new String[size];
                for (int i = 0; i < size; i++) {
                    //这个url需要的角色权限列表
                    values[i] = roles.get(i).getRoleTag();
                }
                return SecurityConfig.createList(values);
            }
        }*/
        //没有匹配到资源，都需要登录访问
        return SecurityConfig.createList(ContentValue.ROLE_LOGIN);
    }

    @Override
    public Collection<ConfigAttribute> getAllConfigAttributes() {
        return null;
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return FilterInvocation.class.isAssignableFrom(clazz);
    }
}