package com.luxiao.mallcommon.handler;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import jakarta.servlet.http.HttpServletRequest;

@Component
public class MyMetaObjectHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        this.strictInsertFill(metaObject, "createdTime", LocalDateTime::now, LocalDateTime.class);
        this.strictInsertFill(metaObject, "updatedTime", LocalDateTime::now, LocalDateTime.class);
        String operator = currentOperator();
        this.strictInsertFill(metaObject, "createdUser", () -> operator, String.class);
        this.strictInsertFill(metaObject, "updatedUser", () -> operator, String.class);
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        this.strictUpdateFill(metaObject, "updatedTime", LocalDateTime::now, LocalDateTime.class);
        String operator = currentOperator();
        this.strictUpdateFill(metaObject, "updatedUser", () -> operator, String.class);
    }

    private String currentOperator() {
        try {
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                HttpServletRequest req = attrs.getRequest();
                String identity = req.getHeader("X-Auth-Identity");
                String userId = req.getHeader("X-Auth-UserId");
                if (identity != null && userId != null) {
                    return identity + ":" + userId;
                }
            }
        } catch (Exception ignored) {}
        return "system";
    }
}
