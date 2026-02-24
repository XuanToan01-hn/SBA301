package com.buildings.entity.enums;

/**
 * Trạng thái ghi chỉ số công tơ
 */
public enum MeterReadingStatus {
    DRAFT,      // Bản nháp - có thể sửa
    CONFIRMED,  // Đã xác nhận - chờ khóa
    LOCKED      // Đã khóa - không thể sửa
}
