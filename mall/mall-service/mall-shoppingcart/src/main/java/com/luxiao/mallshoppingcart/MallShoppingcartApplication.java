package com.luxiao.mallshoppingcart;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication(scanBasePackages = "com.luxiao")
@EnableDiscoveryClient
public class MallShoppingcartApplication {

    public static void main(String[] args) {
        SpringApplication.run(MallShoppingcartApplication.class, args);
    }

}
