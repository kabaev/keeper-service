package com.kabaev.shop.service.keeper.dto;

import com.kabaev.shop.service.keeper.domain.Product;

import java.math.BigDecimal;

public record AddProductResponseDto(
        String code,
        String name,
        String description,
        BigDecimal price,
        boolean deleted) {

    public AddProductResponseDto(Product product) {
        this(
                product.getCode(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.isDeleted()
        );
    }

}
