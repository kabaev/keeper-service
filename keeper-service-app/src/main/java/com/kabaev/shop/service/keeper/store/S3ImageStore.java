package com.kabaev.shop.service.keeper.store;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetUrlRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;

@Component
@Slf4j
public class S3ImageStore {

    private final String bucketName;
    private final String regionName;
    private final S3Client s3Client;

    public S3ImageStore(
            @Value("${s3.bucket.name}") String bucketName,
            @Value("${s3.region.name}") String regionName,
            S3Client s3Client) {
        this.bucketName = bucketName;
        this.regionName = regionName;
        this.s3Client = s3Client;
    }

    public String saveImageToS3(MultipartFile image, String key) {
        PutObjectRequest objectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .contentType(image.getContentType())
                .contentLength(image.getSize())
                .build();

        try {
            s3Client.putObject(objectRequest, RequestBody.fromBytes(image.getBytes()));
        } catch (IOException e) {
            log.error("The images wasn't saved in the S3 bucket");
            throw new RuntimeException("IOException was thrown");
        }

        GetUrlRequest urlRequest = GetUrlRequest.builder()
                .region(Region.of(regionName))
                .bucket(bucketName)
                .key(key)
                .build();

        return s3Client.utilities().getUrl(urlRequest).toString();
    }

    public void deleteImageFromS3(String key) {
        DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();
        s3Client.deleteObject(deleteObjectRequest);
    }

}
