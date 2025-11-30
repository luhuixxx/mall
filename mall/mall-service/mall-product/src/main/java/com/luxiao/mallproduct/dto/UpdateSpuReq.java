package com.luxiao.mallproduct.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateSpuReq {
    @NotBlank
    private String name;
    private String description;
    private String imageUrl;
}

