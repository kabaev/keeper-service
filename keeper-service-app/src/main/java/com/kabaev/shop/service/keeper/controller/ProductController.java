package com.kabaev.shop.service.keeper.controller;

import com.kabaev.shop.service.keeper.domain.Product;
import com.kabaev.shop.service.keeper.service.ProductService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.util.List;

@RestController
@RequestMapping(path = "api/v1/products")
public class ProductController {

    private final ProductService productService;
    private final String bucketName;
    private final String regionName;

    public ProductController(
            ProductService productService,
            @Value("${s3.bucket.name}") String bucketName,
            @Value("${s3.region.name}") String regionName) {
        this.productService = productService;
        this.bucketName = bucketName;
        this.regionName = regionName;
    }

    @GetMapping
    public List<Product> getAllProducts() {
        productService.addProduct(
                List.of(
                        new Product("Timur"),
                        new Product("Max")
                )
        );

        return productService.getAllProducts();
    }

    @GetMapping("/s3")
    public String addToS3() {
        System.out.println(bucketName);
        System.out.println(regionName);
        Region region = Region.of(regionName);
        S3Client s3 = S3Client.builder()
                .region(region)
                .build();
        PutObjectRequest objectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key("k234k234l23j4j234lj24")
                .build();

        s3.putObject(objectRequest, RequestBody.fromString("some content"));

        return "added";
    }

}
