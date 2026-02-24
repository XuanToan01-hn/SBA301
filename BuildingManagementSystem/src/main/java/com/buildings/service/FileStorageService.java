package com.buildings.service;

import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {

    /**
     * Lưu file và trả về URL path
     * @param file File cần lưu
     * @param subDirectory Thư mục con (VD: "meter-photos")
     * @return URL path của file đã lưu
     */
    String saveFile(MultipartFile file, String subDirectory);

    /**
     * Xóa file theo URL path
     * @param fileUrl URL path của file cần xóa
     */
    void deleteFile(String fileUrl);

    /**
     * Kiểm tra file có tồn tại không
     * @param fileUrl URL path của file
     * @return true nếu file tồn tại
     */
    boolean fileExists(String fileUrl);
}
