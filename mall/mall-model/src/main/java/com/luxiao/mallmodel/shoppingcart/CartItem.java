package com.luxiao.mallmodel.shoppingcart;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@TableName("cart_item")
public class CartItem {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;
private Long spuId;
private String imageUrl;
    private Long skuId;

    private Integer quantity;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedTime;

    @TableField(fill = FieldFill.INSERT)
    private String createdUser;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String updatedUser;
}