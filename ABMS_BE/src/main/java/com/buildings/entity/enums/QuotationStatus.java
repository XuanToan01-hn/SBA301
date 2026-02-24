package com.buildings.entity.enums;

public enum QuotationStatus {
    DRAFT,      // Đang soạn thảo
    SENT,       // Đã gửi cho cư dân, chờ duyệt
    APPROVED,   // Cư dân đã đồng ý
    REJECTED,   // Cư dân từ chối
    CANCELLED,  // Hủy bỏ
    EXPIRED
}
