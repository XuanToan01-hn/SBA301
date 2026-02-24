package com.buildings.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FileUploadResponse {
    private String url;
    private String objectName;
    private String fileName;
    private Long size;
    private String contentType;
}
