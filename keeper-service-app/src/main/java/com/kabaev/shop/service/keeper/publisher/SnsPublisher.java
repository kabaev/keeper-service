package com.kabaev.shop.service.keeper.publisher;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;

@Component
public class SnsPublisher {

    private final SnsClient snsClient;
    private final String snsTopicArn;

    public SnsPublisher(
            SnsClient snsClient,
            @Value("${sns.topic.arn}") String snsTopicArn) {
        this.snsClient = snsClient;
        this.snsTopicArn = snsTopicArn;
    }

    public void sendInTopic(String code) {
        PublishRequest request = PublishRequest.builder()
                .messageGroupId("GroupId")
                .messageDeduplicationId(code)
                .message(code)
                .topicArn(snsTopicArn)
                .build();
        snsClient.publish(request);
    }

}
