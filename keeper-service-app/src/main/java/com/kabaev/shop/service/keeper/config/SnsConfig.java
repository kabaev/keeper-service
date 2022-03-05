package com.kabaev.shop.service.keeper.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sns.SnsClient;

import java.net.URI;

@Configuration
public class SnsConfig {

    private final String awsRegionName;
    private final String snsEndpoint;

    public SnsConfig(
            @Value("${aws.region.name}") String awsRegionName,
            @Value("${sns.endpoint}") String snsEndpoint) {
        this.awsRegionName = awsRegionName;
        this.snsEndpoint = snsEndpoint;
    }

    @Bean
    public SnsClient snsClient() {
        Region region = Region.of(awsRegionName);
        return SnsClient.builder()
                .endpointOverride(URI.create(snsEndpoint))
                .region(region)
                .build();
    }

}
