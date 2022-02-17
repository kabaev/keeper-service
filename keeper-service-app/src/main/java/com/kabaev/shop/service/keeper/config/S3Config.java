package com.kabaev.shop.service.keeper.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
public class S3Config {

    private final String bucketName;
    private final String regionName;

    public S3Config(
            @Value("${s3.bucket.name}") String bucketName,
            @Value("${s3.region.name}") String regionName) {
        this.bucketName = bucketName;
        this.regionName = regionName;
    }

    @Bean
    public S3Client s3Client() {
        Region region = Region.of(regionName);
        return S3Client.builder()
                .region(region)
                .build();
    }

}
