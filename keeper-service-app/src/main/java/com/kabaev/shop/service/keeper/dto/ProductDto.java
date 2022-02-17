package com.kabaev.shop.service.keeper.dto;

import com.kabaev.shop.service.keeper.domain.Product;

import java.math.BigDecimal;
import java.util.List;

public record ProductDto(
        String code,
        String name,
        String description,
        BigDecimal price,
        List<String> imageUriList) {

    public ProductDto(Product product) {
        this(
                product.getCode(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getImageUriList()
        );
    }

}
