package com.kabaev.shop.service.keeper.publisher;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;

@Component
public class SnsPublisher {

    private final String regionName;
    private final String topicArn;

    public SnsPublisher(
            @Value("${s3.region.name}") String regionName,
            @Value("${sns.topic.arn}") String topicArn) {
        this.regionName = regionName;
        this.topicArn = topicArn;
    }

    public void sendInTopic(String code) {
        Region region = Region.of(regionName);
        SnsClient snsClient = SnsClient.builder()
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
