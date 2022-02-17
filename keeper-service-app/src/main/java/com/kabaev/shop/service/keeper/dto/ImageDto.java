package com.kabaev.shop.service.keeper.dto;

import com.kabaev.shop.service.keeper.domain.Image;

public record ImageDto(
        String uri,
        String key) {

    public ImageDto(Image image) {
        this(image.getUri(), image.getKey());
    }

}
