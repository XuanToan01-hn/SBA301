package com.buildings.entity.enums;

/**
 * Phương thức tính phí dịch vụ
 */
public enum BillingMethod {
    FIXED,  // Cố định (VD: phí gửi xe cố định/tháng)
    AREA,   // Theo diện tích (VD: phí quản lý/m2)
    METER,  // Theo đồng hồ đo (VD: điện, nước - giá đơn)
    TIER    // Theo bậc thang (VD: điện, nước - giá lũy tiến)
}
