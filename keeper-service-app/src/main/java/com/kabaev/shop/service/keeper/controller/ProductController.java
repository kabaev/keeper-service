package com.kabaev.shop.service.keeper.controller;

import com.kabaev.shop.service.keeper.domain.Image;
import com.kabaev.shop.service.keeper.domain.Product;
import com.kabaev.shop.service.keeper.dto.*;
import com.kabaev.shop.service.keeper.exception.ImageUploadException;
import com.kabaev.shop.service.keeper.exception.ProductExistsException;
import com.kabaev.shop.service.keeper.exception.ProductNotFoundException;
import com.kabaev.shop.service.keeper.repository.ProductRepository;
import com.kabaev.shop.service.keeper.publisher.SnsPublisher;
import com.kabaev.shop.service.keeper.store.S3ImageStore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
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
    public ProductDtoList getAllProducts() {
        log.debug("Returning all products");
        List<Product> products = productRepository.findAll();
        List<ProductDto> productDtoList = products.stream()
                .map(ProductDto::new)
                .toList();
        return new ProductDtoList(productDtoList);
    }

    @GetMapping("/{code}")
    public ProductDto getProductDtoByCode(@PathVariable("code") String code) {
        log.debug("Returning product with code = {}", code);
        Product product = productRepository.findByCode(code)
                .orElseThrow(() -> new ProductNotFoundException("There is no product with the code: " + code));
        return new ProductDto(product);
    }

    @DeleteMapping("/{code}")
    @Transactional
    public boolean deleteProductByCode(@PathVariable("code") String code) {
        log.debug("Deleting product with code = {}", code);
        Product product = productRepository.findByCode(code)
                .orElseThrow(() -> new ProductNotFoundException("There is no product with the code: " + code));
        List<Image> images = product.getImages();
        if (images != null) {
            images.stream()
                    .map(Image::getKey)
                    .forEach(s3ImageStore::deleteImageFromS3);
        }

        log.debug("Sending the product code to the topic: {}", code);
        snsPublisher.sendInTopic(product.getCode());

        productRepository.delete(product);
        return true;
    }

    @PostMapping
    @Transactional
    public AddProductResponseDto save(@Validated @RequestBody AddProductRequestDto requestDto) {
        log.debug("Attempt to save a new product: {}", requestDto);
        log.debug("Finding out whether the product exists in the database: {}", requestDto);
        Optional<Product> productInDatabase = productRepository.findByName(requestDto.name());
        if (productInDatabase.isPresent()) {
            throw new ProductExistsException("Product with given name already exist: " + requestDto.name());
        }

        Product productToSave = new Product();
        productToSave.setCode(generateUniqueIdentifier());
        productToSave.setName(requestDto.name());
        productToSave.setDescription(requestDto.description());
        productToSave.setPrice(requestDto.price());

        log.debug("Sending the product code to the topic: {}", productToSave.getCode());
        snsPublisher.sendInTopic(productToSave.getCode());

        Product saved = productRepository.save(productToSave);

        return new AddProductResponseDto(saved);
    }

    @PostMapping(value = "/{productCode}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Transactional
    public ImageDto upload(
            @PathVariable("productCode") String productCode,
            @RequestPart(name = "image") MultipartFile image) {

        if (image.isEmpty()) {
            throw new ImageUploadException("Image is not included in the request");
        }

        Product product = productRepository.findByCode(productCode)
                .orElseThrow(() -> new ProductNotFoundException("There is no product with the code: " + productCode));

        String imageKey = generateUniqueIdentifier();
        String imageUri = s3ImageStore.saveImageToS3(image, imageKey);

        Image imageToSave = new Image();
        imageToSave.setKey(imageKey);
        imageToSave.setUri(imageUri);
        product.addImageToProduct(imageToSave);
        productRepository.saveAndFlush(product);

        log.debug("Sending the product code to the topic: {}", productCode);
        snsPublisher.sendInTopic(productCode);

        return new ImageDto(imageToSave);
    }

    public String generateUniqueIdentifier() {
        return UUID.randomUUID().toString();
    }

}
