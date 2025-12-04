package com.luxiao.mallfeignapi.user;

import com.luxiao.mallcommon.api.ApiResponse;
import com.luxiao.mallfeignapi.config.InternalAuthFeignInterceptorConfig;
import com.luxiao.mallmodel.user.User;
import com.luxiao.mallmodel.user.dto.UserAdjustBalanceReq;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "mall-user",configuration = InternalAuthFeignInterceptorConfig.class)
public interface UserApi {

    @PutMapping("/api/user/{id}/balance/adjust")
    ResponseEntity<ApiResponse<User>> adjustBalance(@PathVariable("id") Long id, @RequestBody UserAdjustBalanceReq req);
}

