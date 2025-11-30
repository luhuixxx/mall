package com.luxiao.mallsecurity.crypto;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.time.Duration;
import java.util.Base64;

@Component
public class RsaCrypto {

    private final StringRedisTemplate redis;

    public RsaCrypto(StringRedisTemplate redis) {
        this.redis = redis;
    }

    private static final String KEY_PRIVATE = "security:rsa:private";
    private static final String KEY_PUBLIC = "security:rsa:public";

    public String decrypt(String base64Cipher) {
        try {
            ensureKeys();
            String privateBase64 = redis.opsForValue().get(KEY_PRIVATE);
            byte[] keyBytes = Base64.getDecoder().decode(privateBase64);
            PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            PrivateKey privateKey = kf.generatePrivate(spec);
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            byte[] data = Base64.getDecoder().decode(base64Cipher);
            byte[] plain = cipher.doFinal(data);
            return new String(plain, java.nio.charset.StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new IllegalArgumentException("密码密文解析失败");
        }
    }

    public String getPublicKeyPem() {
        ensureKeys();
        String pubBase64 = redis.opsForValue().get(KEY_PUBLIC);
        return toPemPublic(pubBase64);
    }

    private void ensureKeys() {
        String priv = redis.opsForValue().get(KEY_PRIVATE);
        String pub = redis.opsForValue().get(KEY_PUBLIC);
        if (priv == null || pub == null) {
            try {
                KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
                gen.initialize(2048);
                KeyPair kp = gen.generateKeyPair();
                PrivateKey privateKey = kp.getPrivate();
                PublicKey publicKey = kp.getPublic();
                String privB64 = Base64.getEncoder().encodeToString(privateKey.getEncoded());
                String pubB64 = Base64.getEncoder().encodeToString(publicKey.getEncoded());
                redis.opsForValue().set(KEY_PRIVATE, privB64, Duration.ofDays(1));
                redis.opsForValue().set(KEY_PUBLIC, pubB64, Duration.ofDays(1));
            } catch (Exception e) {
                throw new RuntimeException("RSA密钥生成失败", e);
            }
        }
    }

    private String toPemPublic(String base64) {
        StringBuilder sb = new StringBuilder();
        sb.append("-----BEGIN PUBLIC KEY-----\n");
        for (int i = 0; i < base64.length(); i += 64) {
            int end = Math.min(i + 64, base64.length());
            sb.append(base64, i, end).append('\n');
        }
        sb.append("-----END PUBLIC KEY-----");
        return sb.toString();
    }
}
