package com.kabaev.shop.service.keeper.controller;

import com.kabaev.shop.service.keeper.domain.Product;
import com.kabaev.shop.service.keeper.repository.ProductRepository;
import com.kabaev.shop.service.keeper.publisher.SnsPublisher;
import com.kabaev.shop.service.keeper.store.S3ImageStore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping(path = "api/v1/products")
@Slf4j
public class ProductController {

    private final ProductRepository productRepository;
    private final SnsPublisher snsPublisher;
    private final S3ImageStore s3ImageStore;

    public ProductController(
            ProductRepository productRepository,
            SnsPublisher snsPublisher,
            S3ImageStore s3ImageStore) {
        this.productRepository = productRepository;
        this.snsPublisher = snsPublisher;
        this.s3ImageStore = s3ImageStore;
    }

    @GetMapping
    public List<Product> getAllProducts() {
        log.debug("Displaying all products");
        return productRepository.findAll();
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Transactional
    public Product save(@RequestPart Product product, @RequestPart MultipartFile image) {
        log.debug("Attempt to save a new product: {}", product);
        if (product.getId() != null) {
            throw new IllegalArgumentException("Product Id shouldn't be specified explicitly");
        }

        log.debug("Finding out whether the product exists in the database: {}", product);
        Optional<Product> productInDatabase = productRepository.findByName(product.getName());
        if (productInDatabase.isPresent()) {
            throw new IllegalArgumentException("Product with given name already exist");
        }

        product.setCode(UUID.randomUUID().toString());

        if (image != null) {
            log.debug("Setting up image URL in the product instance");
            String imageUrl = s3ImageStore.saveImageToS3(image, product.getName());
            product.setImageUrl(imageUrl);
        }

        log.debug("Saving the product in the database");
        Product savedProduct = productRepository.save(product);

        log.debug("Sending the product to the topic: {}", savedProduct);
        snsPublisher.sendInTopic(product.getCode());

        return savedProduct;
    }

}
