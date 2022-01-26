package com.kabaev.shop.service.keeper.service;

import com.kabaev.shop.service.keeper.domain.Product;
import com.kabaev.shop.service.keeper.repository.ProductRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public void addProduct(List<Product> list) {
        productRepository.saveAll(list);
    }

}
