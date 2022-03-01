package com.kabaev.shop.service.keeper.publisher;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;

import java.net.URI;

@Component
public class SnsPublisher {

    private final String regionName;
    private final String topicArn;
    private final String awsEndpoint;

    public SnsPublisher(
            @Value("${s3.region.name}") String regionName,
            @Value("${sns.topic.arn}") String topicArn,
            @Value("${sns.endpoint}") String awsEndpoint) {
        this.regionName = regionName;
        this.topicArn = topicArn;
        this.awsEndpoint = awsEndpoint;
    }

    public void sendInTopic(String code) {
        Region region = Region.of(regionName);
        SnsClient snsClient = SnsClient.builder()
                .endpointOverride(URI.create(awsEndpoint))
                .region(region)
                .build();
        PublishRequest request = PublishRequest.builder()
                .message(code)
                .topicArn(topicArn)
                .messageGroupId(code)
                .messageDeduplicationId(code)
                .build();
        snsClient.publish(request);
    }

}
