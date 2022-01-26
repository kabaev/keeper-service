package com.kabaev.shop.service.keeper.controller;

import com.kabaev.shop.service.keeper.domain.Product;
import com.kabaev.shop.service.keeper.service.ProductService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping(path = "api/v1/products")
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public List<Product> getAllProductsBut() {
        productService.addProduct(
                List.of(
                        new Product("Timur"),
                        new Product("Max")
                )
        );
        return productService.getAllProducts();
    }

}
