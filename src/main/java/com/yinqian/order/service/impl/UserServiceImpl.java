package com.yinqian.order.service.impl;

import com.yinqian.order.bean.UserInfo;
import com.yinqian.order.service.UserService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * @author renml
 * @date 2020/12/29 16:06
 */
@Service
public class UserServiceImpl implements UserService {
    @Override
    public String authEntication(UserInfo user) {
        return null;
    }

    @Override
    public UserInfo verify(String token) {
        return null;
    }

    @Override
    public UserDetails loadUserByUsername(String s) throws UsernameNotFoundException {
        return null;
    }
}
