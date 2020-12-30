package com.yinqian.order.service;

import com.yinqian.order.bean.UserInfo;
import org.springframework.security.core.userdetails.UserDetailsService;

/**
 * @author renml
 * @date 2020/12/29 14:32
 */
public interface UserService extends UserDetailsService {
    String authEntication(UserInfo user);

    UserInfo verify(String token);
}
