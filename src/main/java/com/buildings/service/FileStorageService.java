package com.buildings.service;

import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {

    /**
     * Upload file lên MinIO.
     *
     * @param file   file cần upload
     * @param folder thư mục con trong bucket (ví dụ: "maintenance", "progress")
     * @return objectName (path trong bucket), dùng để build URL hoặc xóa sau này
     */
    String uploadFile(MultipartFile file, String folder) throws Exception;

    String saveFile(MultipartFile file, String subDirectory);

    /**
     * Xóa file khỏi MinIO theo objectName.
     *
     * @param objectName path trong bucket (trả về từ uploadFile)
     */
    void deleteFile(String objectName) throws Exception;

    /**
     * Build public URL từ objectName.
     */
    String getFileUrl(String objectName);

    boolean fileExists(String fileUrl);

}
