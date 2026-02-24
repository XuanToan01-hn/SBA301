package com.buildings.controller;

import com.buildings.dto.ApiResponse;
import com.buildings.dto.response.FileUploadResponse;
import com.buildings.service.FileStorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/files")
@RequiredArgsConstructor
@Tag(name = "File Upload", description = "Upload và xóa file trên MinIO storage")
public class FileUploadController {

    private final FileStorageService fileStorageService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "Upload file",
            description = "Upload file lên MinIO. Trả về URL và objectName để dùng cho các API khác hoặc để xóa sau.")
    public ApiResponse<FileUploadResponse> uploadFile(
            @RequestPart("file") MultipartFile file,
            @RequestParam(defaultValue = "general") String folder) throws Exception {

        String objectName = fileStorageService.uploadFile(file, folder);
        String url = fileStorageService.getFileUrl(objectName);

        return ApiResponse.<FileUploadResponse>builder()
                .result(FileUploadResponse.builder()
                        .url(url)
                        .objectName(objectName)
                        .fileName(file.getOriginalFilename())
                        .size(file.getSize())
                        .contentType(file.getContentType())
                        .build())
                .build();
    }

    @DeleteMapping
    @Operation(
            summary = "Xóa file",
            description = "Xóa file khỏi MinIO theo objectName (lấy từ response của API upload).")
    public ApiResponse<String> deleteFile(@RequestParam String objectName) throws Exception {
        fileStorageService.deleteFile(objectName);
        return ApiResponse.<String>builder()
                .result("File deleted successfully: " + objectName)
                .build();
    }
}
