package com.luxiao.malluser.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class UserUpdateBalanceReq {
    @NotNull
    @DecimalMin(value = "0", inclusive = true)
    private BigDecimal balance;
}

