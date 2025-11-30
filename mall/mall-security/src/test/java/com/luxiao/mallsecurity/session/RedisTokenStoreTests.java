package com.luxiao.mallsecurity.session;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.luxiao.mallsecurity.model.AuthSession;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

public class RedisTokenStoreTests {
    @Test
    void saveGetDelete() throws Exception {
        StringRedisTemplate template = Mockito.mock(StringRedisTemplate.class);
        ValueOperations<String, String> ops = Mockito.mock(ValueOperations.class);
        Mockito.when(template.opsForValue()).thenReturn(ops);
        ObjectMapper om = new ObjectMapper();
        RedisTokenStore store = new RedisTokenStore(template, om);
        AuthSession s = new AuthSession(1L, "admin", List.of(new SimpleGrantedAuthority("product:read")), Instant.now(), Instant.now().plusSeconds(60));
        store.save("t", s, Duration.ofSeconds(60));
        Mockito.verify(ops, Mockito.times(1)).set(Mockito.anyString(), Mockito.anyString(), Mockito.any());
        Mockito.when(ops.get(Mockito.anyString())).thenReturn(om.writeValueAsString(s));
        AuthSession loaded = store.get("t");
        Assertions.assertEquals("admin", loaded.getUsername());
        store.delete("t");
        Mockito.verify(template, Mockito.atLeastOnce()).delete(Mockito.anyString());
    }
}
