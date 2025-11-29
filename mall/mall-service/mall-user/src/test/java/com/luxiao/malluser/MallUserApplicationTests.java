package com.luxiao.malluser;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;


class MallUserApplicationTests {

    public static void main(String[] args) {
        System.out.println(new BCryptPasswordEncoder().encode("nacos123456"));
    }
}
