package com.buildings.service.impl;

import com.buildings.configuration.MinioConfig;
import com.buildings.service.FileStorageService;
import io.minio.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class MinioStorageServiceImpl implements FileStorageService {

    private final MinioClient minioClient;
    private final MinioConfig minioConfig;

    @Override
    public String uploadFile(MultipartFile file, String folder) throws Exception {
        ensureBucketExists();

        String originalFilename = file.getOriginalFilename() != null
                ? file.getOriginalFilename()
                : "file";
        String objectName = folder + "/" + UUID.randomUUID().toString() + "_" + originalFilename;

        minioClient.putObject(PutObjectArgs.builder()
                .bucket(minioConfig.getBucket())
                .object(objectName)
                .stream(file.getInputStream(), file.getSize(), -1)
                .contentType(file.getContentType())
                .build());

        log.info("Uploaded file to MinIO: {}", objectName);
        return objectName;
    }

    @Override
    public void deleteFile(String objectName) throws Exception {
        minioClient.removeObject(RemoveObjectArgs.builder()
                .bucket(minioConfig.getBucket())
                .object(objectName)
                .build());
        log.info("Deleted file from MinIO: {}", objectName);
    }

    @Override
    public String getFileUrl(String objectName) {
        return minioConfig.getEndpoint() + "/" + minioConfig.getBucket() + "/" + objectName;
    }

    private void ensureBucketExists() throws Exception {
        boolean exists = minioClient.bucketExists(
                BucketExistsArgs.builder().bucket(minioConfig.getBucket()).build());
        if (!exists) {
            minioClient.makeBucket(
                    MakeBucketArgs.builder().bucket(minioConfig.getBucket()).build());
            log.info("Created MinIO bucket: {}", minioConfig.getBucket());
        }
    }
}
