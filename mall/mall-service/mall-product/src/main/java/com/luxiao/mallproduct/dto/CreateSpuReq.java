package com.luxiao.mallproduct.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateSpuReq {
    @NotBlank
    private String name;
    private String description;
    private String imageUrl;
}

