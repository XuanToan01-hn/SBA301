package com.buildings.service.impl;

import com.buildings.configuration.MinioConfig;
import com.buildings.exception.AppException;
import com.buildings.exception.ErrorCode;
import com.buildings.service.FileStorageService;
import io.minio.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;
import java.nio.file.Paths;
import jakarta.annotation.PostConstruct;

@Service
@RequiredArgsConstructor
@Slf4j
public class MinioStorageServiceImpl implements FileStorageService {

    private final MinioClient minioClient;
    private final MinioConfig minioConfig;
    @Value("${app.file.upload-dir:uploads}")
    private String uploadDir;

    @Value("${app.file.base-url:/uploads}")
    private String baseUrl;

    private Path uploadPath;

    @PostConstruct
    public void initUploadPath() {
        try {
            this.uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
            Files.createDirectories(this.uploadPath);
            log.info("Upload directory initialized at {}", this.uploadPath);
        } catch (IOException e) {
            log.error("Failed to initialize upload directory: {}", uploadDir, e);
            throw new AppException(ErrorCode.FILE_UPLOAD_FAILED);
        }
    }

    @Override
    public String uploadFile(MultipartFile file, String folder) throws Exception {
        ensureBucketExists();

        String originalFilename = StringUtils.cleanPath(
                file.getOriginalFilename() != null ? file.getOriginalFilename() : "file");
        if (originalFilename.contains("..")) {
            throw new AppException(ErrorCode.FILE_UPLOAD_FAILED);
        }

        String extension = getFileExtension(originalFilename);
        String objectName = folder + "/" + UUID.randomUUID() + extension;

        String contentType = StringUtils.hasText(file.getContentType())
                ? file.getContentType()
                : "application/octet-stream";

        minioClient.putObject(PutObjectArgs.builder()
                .bucket(minioConfig.getBucket())
                .object(objectName)
                .stream(file.getInputStream(), file.getSize(), -1)
                .contentType(contentType)
                .build());

        log.info("Uploaded file to MinIO: {}", objectName);
        return objectName;
    }
    @Override
    public String saveFile(MultipartFile file, String subDirectory) {
        if (file == null || file.isEmpty()) {
            return null;
        }

        try {
            // Validate file
            String originalFilename = StringUtils.cleanPath(
                    file.getOriginalFilename() != null ? file.getOriginalFilename() : "file");
            if (originalFilename.contains("..")) {
                throw new AppException(ErrorCode.FILE_UPLOAD_FAILED);
            }

            // Create subdirectory if needed
            Path targetDir = uploadPath;
            if (subDirectory != null && !subDirectory.isEmpty()) {
                targetDir = uploadPath.resolve(subDirectory);
                Files.createDirectories(targetDir);
            }

            // Generate unique filename
            String extension = getFileExtension(originalFilename);
            String newFilename = UUID.randomUUID().toString() + extension;

            // Save file
            Path targetPath = targetDir.resolve(newFilename);
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

            // Return URL path
            String urlPath = baseUrl;
            if (subDirectory != null && !subDirectory.isEmpty()) {
                urlPath += "/" + subDirectory;
            }
            urlPath += "/" + newFilename;

            log.info("File saved: {} -> {}", originalFilename, urlPath);
            return urlPath;

        } catch (IOException e) {
            log.error("Failed to save file", e);
            throw new AppException(ErrorCode.FILE_UPLOAD_FAILED);
        }
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
    public String getFileUrl(String objectNameOrUrl) {
        String objectName = extractObjectName(objectNameOrUrl);
        String publicBase = getPublicEndpointBase();
        return publicBase + "/" + minioConfig.getBucket() + "/" + objectName;
    }

    @Override
    public boolean fileExists(String objectNameOrUrl) {
        if (objectNameOrUrl == null || objectNameOrUrl.isEmpty()) {
            return false;
        }

        String objectName = extractObjectName(objectNameOrUrl);

        try {
            minioClient.statObject(StatObjectArgs.builder()
                    .bucket(minioConfig.getBucket())
                    .object(objectName)
                    .build());
            return true;
        } catch (Exception e) {
            return false;
        }
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

    private String getPublicEndpointBase() {
        String configuredPublicEndpoint = minioConfig.getPublicEndpoint();
        String endpoint = StringUtils.hasText(configuredPublicEndpoint)
                ? configuredPublicEndpoint
                : minioConfig.getEndpoint();
        return endpoint.replaceAll("/+$", "");
    }

    private String getInternalEndpointBase() {
        return minioConfig.getEndpoint().replaceAll("/+$", "");
    }

    private String extractObjectName(String objectNameOrUrl) {
        String value = StringUtils.hasText(objectNameOrUrl) ? objectNameOrUrl.trim() : "";
        if (value.isEmpty()) {
            return value;
        }

        String bucketPrefixFromInternal = getInternalEndpointBase() + "/" + minioConfig.getBucket() + "/";
        if (value.startsWith(bucketPrefixFromInternal)) {
            return value.substring(bucketPrefixFromInternal.length());
        }

        String bucketPrefixFromPublic = getPublicEndpointBase() + "/" + minioConfig.getBucket() + "/";
        if (value.startsWith(bucketPrefixFromPublic)) {
            return value.substring(bucketPrefixFromPublic.length());
        }

        String bucketPathPrefix = "/" + minioConfig.getBucket() + "/";
        if (value.startsWith(bucketPathPrefix)) {
            return value.substring(bucketPathPrefix.length());
        }

        return value;
    }

    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf("."));
    }
}
