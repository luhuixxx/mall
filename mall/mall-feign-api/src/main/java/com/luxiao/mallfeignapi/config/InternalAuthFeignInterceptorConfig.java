package com.luxiao.mallfeignapi.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

@Configuration
public class InternalAuthFeignInterceptorConfig {

    @Value("${spring.application.name}")
    private String serviceName;

    @Value("${security.internal.secret:}")
    private String internalSecret;

    @Bean
    public RequestInterceptor internalAuthRequestInterceptor() {
        return new RequestInterceptor() {
            @Override
            public void apply(RequestTemplate template) {
                if (internalSecret == null || internalSecret.isEmpty()) {
                    return;
                }
                long ts = Instant.now().toEpochMilli();
                String payload = serviceName + ":" + ts;
                String sign = hmacSha256Hex(internalSecret, payload);
                template.header("X-Internal-Service", serviceName);
                template.header("X-Internal-Timestamp", String.valueOf(ts));
                template.header("X-Internal-Sign", sign);
                template.header("X-Auth-Identity", "SERVICE");
            }
        };
    }

    private String hmacSha256Hex(String secret, String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] bytes = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(bytes.length * 2);
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            return "";
        }
    }
}

