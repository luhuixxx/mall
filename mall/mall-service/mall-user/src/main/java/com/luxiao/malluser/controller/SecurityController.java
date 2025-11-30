package com.luxiao.malluser.controller;

import com.luxiao.mallcommon.api.ApiResponse;
import com.luxiao.mallsecurity.crypto.RsaCrypto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/security")
@Tag(name = "Security APIs")
public class SecurityController {

    private final RsaCrypto rsaCrypto;

    public SecurityController(RsaCrypto rsaCrypto) {
        this.rsaCrypto = rsaCrypto;
    }

    @GetMapping("/public-key")
    @Operation(summary = "获取RSA公钥")
    public ResponseEntity<ApiResponse<String>> publicKey() {
        return ResponseEntity.ok(ApiResponse.ok(rsaCrypto.getPublicKeyPem()));
    }
}

