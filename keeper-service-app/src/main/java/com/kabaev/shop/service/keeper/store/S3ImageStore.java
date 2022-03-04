package com.kabaev.shop.service.keeper.store;

import com.kabaev.shop.service.keeper.exception.ImageUploadException;
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

    private final String s3BucketName;
    private final String awsRegionName;
    private final S3Client s3Client;

    public S3ImageStore(
            @Value("${s3.bucket.name}") String s3BucketName,
            @Value("${aws.region.name}") String awsRegionName,
            S3Client s3Client) {
        this.s3BucketName = s3BucketName;
        this.awsRegionName = awsRegionName;
        this.s3Client = s3Client;
    }

    public String saveImageToS3(MultipartFile image, String key) {
        PutObjectRequest objectRequest = PutObjectRequest.builder()
                .bucket(s3BucketName)
                .key(key)
                .contentType(image.getContentType())
                .contentLength(image.getSize())
                .build();

        try {
            s3Client.putObject(objectRequest, RequestBody.fromBytes(image.getBytes()));
        } catch (IOException e) {
            log.error("The image was not saved in the S3 bucket");
            throw new ImageUploadException("The image was not saved in the S3 bucket");
        }

        GetUrlRequest urlRequest = GetUrlRequest.builder()
                .region(Region.of(awsRegionName))
                .bucket(s3BucketName)
                .key(key)
                .build();

        return s3Client.utilities().getUrl(urlRequest).toString();
    }

    public void deleteImageFromS3(String key) {
        DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                .bucket(s3BucketName)
                .key(key)
                .build();
        s3Client.deleteObject(deleteObjectRequest);
    }

}
