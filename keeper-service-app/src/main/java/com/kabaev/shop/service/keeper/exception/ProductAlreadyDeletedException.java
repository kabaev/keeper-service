package com.kabaev.shop.service.keeper.exception;

public class ProductAlreadyDeletedException extends RuntimeException {

    public ProductAlreadyDeletedException(String message) {
        super(message);
    }

}

