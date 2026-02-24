package com.buildings.service.impl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

import com.buildings.exception.AppException;
import com.buildings.exception.ErrorCode;
import com.buildings.service.FileStorageService;

@Service
@Slf4j
public class FileStorageServiceImpl implements FileStorageService {

    @Value("${app.file.upload-dir:uploads}")
    private String uploadDir;

    @Value("${app.file.base-url:/uploads}")
    private String baseUrl;

    private Path uploadPath;

    @PostConstruct
    public void init() {
        uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(uploadPath);
            log.info("Upload directory initialized: {}", uploadPath);
        } catch (IOException e) {
            log.error("Could not create upload directory: {}", uploadPath, e);
            throw new AppException(ErrorCode.FILE_UPLOAD_FAILED);
        }
    }

    @Override
    public String saveFile(MultipartFile file, String subDirectory) {
        if (file == null || file.isEmpty()) {
            return null;
        }

        try {
            // Validate file
            String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
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
    public void deleteFile(String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty()) {
            return;
        }

        try {
            // Convert URL to file path
            String relativePath = fileUrl.replace(baseUrl, "");
            if (relativePath.startsWith("/")) {
                relativePath = relativePath.substring(1);
            }

            Path filePath = uploadPath.resolve(relativePath);

            if (Files.exists(filePath)) {
                Files.delete(filePath);
                log.info("File deleted: {}", fileUrl);
            }

        } catch (IOException e) {
            log.error("Failed to delete file: {}", fileUrl, e);
        }
    }

    @Override
    public boolean fileExists(String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty()) {
            return false;
        }

        String relativePath = fileUrl.replace(baseUrl, "");
        if (relativePath.startsWith("/")) {
            relativePath = relativePath.substring(1);
        }

        Path filePath = uploadPath.resolve(relativePath);
        return Files.exists(filePath);
    }

    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf("."));
    }
}
