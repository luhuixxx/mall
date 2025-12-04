package com.luxiao.mallmodel.user.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class UserAdjustBalanceReq {
    @NotNull
    private BigDecimal delta;
}

