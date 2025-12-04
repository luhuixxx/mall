package com.luxiao.mallorder.vo;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class OrderStatsResp {
    private long totalOrders;
    private long paidOrders;
    private long todayOrders;
    private BigDecimal todayAmount;
    private BigDecimal last7DaysAmount;
}

