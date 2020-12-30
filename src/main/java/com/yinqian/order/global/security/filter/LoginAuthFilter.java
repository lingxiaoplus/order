package com.yinqian.order.global.security.filter;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.yinqian.order.exception.OrderException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.util.StreamUtils;
import org.springframework.util.StringUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * security是默认使用form提交表单登录的，我们要定义一个Filter来拦截/login
 * 从json body中提取用户名和密码
 *
 * {@link AbstractAuthenticationProcessingFilter}处理所有HTTP Request和Response对象，并将其封装成AuthenticationMananger可以处理的Authentication
 *
 */
public class LoginAuthFilter extends AbstractAuthenticationProcessingFilter {

    public LoginAuthFilter() {
        super(new AntPathRequestMatcher("/user/login", "POST"));
    }

    /**
     * 从json body中提取出username和password提交给AuthenticationManager
     * @return
     * @throws AuthenticationException
     * @throws IOException
     * @throws ServletException
     */
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException, IOException, ServletException {
        //从json中获取username和password
        String body = StreamUtils.copyToString(request.getInputStream(), StandardCharsets.UTF_8);
        String username = "", password = "";
        if(StringUtils.hasText(body)) {
            JsonObject jsonObject = new JsonParser().parse(body).getAsJsonObject();
            if (jsonObject == null){
                throw new OrderException("请传入登录参数");
            }
            username = jsonObject.get("account").getAsString();
            password = jsonObject.get("password").getAsString();
        }
        username = username.trim();
        //封装到token中提交
        UsernamePasswordAuthenticationToken authRequest = new UsernamePasswordAuthenticationToken(
                username, password);
        return getAuthenticationManager().authenticate(authRequest);
    }
}