package com.luxiao.mallmodel.order;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.FieldFill;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@TableName("`order`") // `order`是关键字，需要特殊处理
public class Order {

    @TableId(type = IdType.NONE) // 订单ID不自增，由程序生成
    private Long id;

    private String name;

    private String phone;

    private String address;

    private Integer status;

    private Integer totalQuantity;

    private BigDecimal amount;

    private BigDecimal discount;

    private String trackingNumber;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedTime;

    @TableField(fill = FieldFill.INSERT)
    private String createdUser;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String updatedUser;
}