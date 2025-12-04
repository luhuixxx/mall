package com.luxiao.mallshoppingcart;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication(scanBasePackages = "com.luxiao")
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.luxiao.mallfeignapi")
public class MallShoppingcartApplication {

    public static void main(String[] args) {
        SpringApplication.run(MallShoppingcartApplication.class, args);
    }

}
