package com.kabaev.shop.service.keeper.store;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetUrlRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;

@Component
@Slf4j
public class S3ImageStore {

    private final String bucketName;
    private final String regionName;

    public S3ImageStore(
            @Value("${s3.bucket.name}") String bucketName,
            @Value("${s3.region.name}") String regionName) {
        this.bucketName = bucketName;
        this.regionName = regionName;
    }

    public String saveImageToS3(MultipartFile image, String key) {
        Region region = Region.of(regionName);
        S3Client s3 = S3Client.builder()
                .region(region)
                .build();

        PutObjectRequest objectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();

        try {
            s3.putObject(objectRequest, RequestBody.fromBytes(image.getBytes()));
        } catch (IOException e) {
            log.error("The images wasn't saved in the S3 bucket");
            throw new RuntimeException("IOException was thrown");
        }

        GetUrlRequest urlRequest = GetUrlRequest.builder()
                .region(region)
                .bucket(bucketName)
                .key(key)
                .build();

        return s3.utilities().getUrl(urlRequest).toString();
    }

}
