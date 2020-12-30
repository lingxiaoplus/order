package com.yinqian.order.global.security;

import com.yinqian.order.exception.OrderException;
import com.yinqian.order.exception.ExceptionEnum;
import com.yinqian.order.global.ContentValue;
import com.yinqian.order.global.security.filter.UrlMetadataSourceFilter;
import org.springframework.security.access.AccessDecisionManager;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Iterator;

/**
 * @author lingxiao
 * 负责做出最终的访问控制决策
 */
@Component
public class UrlAccessDecisionManager implements AccessDecisionManager {
    /**
     * 访问控制决策
     * @param authentication 保存了当前登录用户的角色信息，
     * @param object
     * @param configAttributes getAttributes方法传来的，表示当前请求需要的角色（可能有多个）
     * @throws AccessDeniedException
     * @throws InsufficientAuthenticationException
     * {@link UrlMetadataSourceFilter}  这里面拦截url，判断没有匹配到资源，都需要登录访问
     */
    @Override
    public void decide(Authentication authentication, Object object, Collection<ConfigAttribute> configAttributes) throws AccessDeniedException, AuthenticationException {
        Iterator<ConfigAttribute> iterator = configAttributes.iterator();
        while (iterator.hasNext()){
            ConfigAttribute attribute = iterator.next();
            //当前请求需要的权限
            String needRole = attribute.getAttribute();
            if (ContentValue.ROLE_LOGIN.equals(needRole)){
                if (authentication instanceof AnonymousAuthenticationToken){
                    throw new BadCredentialsException("未登录");
                }else {
                    return;
                }
            }
            Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
            for (GrantedAuthority authority: authorities) {
                if (authority.getAuthority().equals(needRole)) {
                    return;
                }
            }
            //权限不足
            throw new OrderException(ExceptionEnum.ACCESS_DENIED_ERROR);
        }
    }

    /**
     * 是否支持处理传递的ConfigAttribute
     * @param attribute 可以自定义SecurityMetadataSource
     * @return
     */
    @Override
    public boolean supports(ConfigAttribute attribute) {
        return true;
    }

    /**
     * 确认class是否为AccessDecisionManager
     * @param clazz
     * @return
     */
    @Override
    public boolean supports(Class<?> clazz) {
        return true;
    }
}