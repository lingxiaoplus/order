package com.yinqian.order.global.security.handler;

import com.yinqian.order.bean.UserInfo;
import com.yinqian.order.global.ContentValue;
import com.yinqian.order.service.UserService;
import com.yinqian.order.utils.CookieUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@Slf4j
@Component
public class JwtRefreshSuccessHandler implements AuthenticationSuccessHandler {
    //刷新间隔5分钟
    private static final int tokenRefreshInterval = 300;

    @Autowired
    private UserService userService;


    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        UserInfo user = (UserInfo) authentication.getPrincipal();
        String generateToken = userService.authEntication(user);
        //设置cookie 移动端无法设置cookie
        CookieUtils.setCookie(request,response, ContentValue.LOGIN_TOKEN_NAME,generateToken,ContentValue.COOKIE_MAXAGE);
        response.setHeader(ContentValue.LOGIN_TOKEN_NAME,generateToken);
    }
    
    protected boolean shouldTokenRefresh(Date issueAt){
        LocalDateTime issueTime = LocalDateTime.ofInstant(issueAt.toInstant(), ZoneId.systemDefault());
        return LocalDateTime.now().minusSeconds(tokenRefreshInterval).isAfter(issueTime);
    }

}