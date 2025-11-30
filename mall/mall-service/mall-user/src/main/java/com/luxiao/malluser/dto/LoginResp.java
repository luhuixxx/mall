package com.luxiao.malluser.dto;

import com.luxiao.mallmodel.user.User;

public class LoginResp {
    private String token;
    private User user;

    public LoginResp() {}

    public LoginResp(String token, User user) {
        this.token = token;
        this.user = user;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}

