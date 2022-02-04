package com.kabaev.shop.service.keeper.service;

import com.kabaev.shop.service.keeper.domain.Product;
import com.kabaev.shop.service.keeper.repository.ProductRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;
import software.amazon.awssdk.services.sns.model.PublishResponse;

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

    public void pubTopic(SnsClient snsClient, String message, String topicArn) {
        PublishRequest request = PublishRequest.builder()
                .message(message)
                .topicArn(topicArn)
                .messageGroupId("groupId")
                .messageDeduplicationId("deduplicationId")
                .build();
        PublishResponse result = snsClient.publish(request);
    }

}
