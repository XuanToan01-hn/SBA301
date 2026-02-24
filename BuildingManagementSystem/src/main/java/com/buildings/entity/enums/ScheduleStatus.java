package com.buildings.entity.enums;

public enum ScheduleStatus {
    PROPOSED,       // Đã đề xuất, chờ phía còn lại phản hồi
    CONFIRMED,      // Đã xác nhận lịch
    REJECTED,       // Bị từ chối
    CANCELLED,      // Hủy
    COUNTER_PROPOSED // Đề xuất lại lịch khác
}
