package com.luxiao.malluser.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.luxiao.mallmodel.user.User;
import com.luxiao.malluser.dto.UserLoginReq;
import com.luxiao.malluser.dto.LoginResp;
import com.luxiao.malluser.dto.UserRegisterReq;
import com.luxiao.malluser.dto.UserUpdateReq;

public interface UserService extends IService<User> {

    User register(UserRegisterReq req);

    String login(UserLoginReq req);

    User updateProfile(Long id, UserUpdateReq req);

    Page<User> pageUsers(int page, int size, String username, String email);
}
