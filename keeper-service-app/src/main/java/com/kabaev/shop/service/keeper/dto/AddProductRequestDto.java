package com.kabaev.shop.service.keeper.dto;

import com.kabaev.shop.service.keeper.domain.Product;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.math.BigDecimal;

public record AddProductRequestDto(
        @Size(min = 10, max = 100)
        @NotNull
        String name,
        @Size(min = 10, max = 1000)
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
