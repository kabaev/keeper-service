package com.kabaev.shop.service.keeper.dto;

import com.kabaev.shop.service.keeper.domain.Product;
import com.sun.istack.NotNull;

import java.math.BigDecimal;

public record AddProductRequestDto(
        @NotNull
        String name,
        @NotNull
        String description,
        @NotNull
        BigDecimal price) {

    public AddProductRequestDto(Product product) {
        this(
                product.getName(),
                product.getDescription(),
                product.getPrice()
        );
    }

}
