package com.luxiao.malluser;

import com.luxiao.malluser.dto.UserRegisterReq;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class UserDtoTests {
    @Test
    void userRegisterReq_basicFields() {
        UserRegisterReq req = new UserRegisterReq();
        req.setName("n");
        req.setUsername("u");
        req.setPassword("p");
        req.setEmail("e@example.com");
        assertEquals("n", req.getName());
        assertEquals("u", req.getUsername());
        assertEquals("p", req.getPassword());
        assertEquals("e@example.com", req.getEmail());
    }
}

