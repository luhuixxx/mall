package com.luxiao.malluser.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.luxiao.mallmodel.user.User;
import com.luxiao.malluser.dto.UserLoginReq;
import com.luxiao.malluser.dto.UserRegisterReq;
import com.luxiao.malluser.dto.UserUpdateReq;
import com.luxiao.malluser.dto.UserChangePasswordReq;
import com.luxiao.malluser.dto.UserUpdateBalanceReq;
import com.luxiao.mallmodel.user.dto.UserAdjustBalanceReq;

public interface UserService extends IService<User> {

    User register(UserRegisterReq req);

    String login(UserLoginReq req);

    User updateProfile(Long id, UserUpdateReq req);

    Page<User> pageUsers(int page, int size, String username, String email);

    void changePassword(Long id, UserChangePasswordReq req);

    void resetPassword(Long id);

    User updateBalance(Long id, UserUpdateBalanceReq req);

    User adjustBalance(Long id, UserAdjustBalanceReq req);

    long countAllUsers();
    long countNewUsersLast7Days();
}
