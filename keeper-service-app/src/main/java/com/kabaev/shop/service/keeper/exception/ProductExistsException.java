package com.kabaev.shop.service.keeper.exception;

public class ProductExistsException extends RuntimeException {

    public ProductExistsException(String message) {
        super(message);
    }

}

