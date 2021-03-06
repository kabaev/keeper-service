package com.kabaev.shop.service.keeper.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

import java.net.URI;

@Configuration
public class S3Config {

    private final String awsRegionName;
    private final String s3Endpoint;

    public S3Config(
            @Value("${aws.region.name}") String awsRegionName,
            @Value("${s3.endpoint}") String s3Endpoint) {
        this.awsRegionName = awsRegionName;
        this.s3Endpoint = s3Endpoint;
    }

    @Bean
    public S3Client s3Client() {
        Region region = Region.of(awsRegionName);
        return S3Client.builder()
                .endpointOverride(URI.create(s3Endpoint))
                .region(region)
                .build();
    }

}
