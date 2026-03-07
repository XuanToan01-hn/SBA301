package com.buildings.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.*;
import lombok.experimental.SuperBuilder;

import com.buildings.entity.enums.MeterReadingStatus;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * Entity ghi chỉ số công tơ hàng tháng cho căn hộ
 */
@Entity
@Table(name = "meter_readings", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"apartment_id", "service_id", "period"}, name = "unique_reading")
})
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class MeterReading extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "apartment_id", nullable = false)
    private Apartment apartment;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "apartment_id", insertable = false, updatable = false, length = 36)
    private UUID apartmentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_id", nullable = false)
    private Service service;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "service_id", insertable = false, updatable = false, length = 36)
    private UUID serviceId;

    @Column(nullable = false, length = 7)
    private String period; // Định dạng YYYY-MM (VD: 2024-01)

    @Column(name = "old_index", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal oldIndex = BigDecimal.ZERO; // Chỉ số cũ (lấy từ tháng trước)

    @Column(name = "new_index", nullable = false, precision = 10, scale = 2)
    private BigDecimal newIndex; // Chỉ số mới

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal consumption; // Tiêu thụ = new_index - old_index

    @Column(name = "is_meter_reset")
    @Builder.Default
    private Boolean isMeterReset = false; // True nếu thay đồng hồ hoặc quay vòng về 0

    @Column(name = "photo_url", length = 255)
    private String photoUrl; // URL ảnh chụp công tơ

    @Column(name = "taken_at")
    private LocalDateTime takenAt; // Thời điểm ghi chỉ số

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "taken_by")
    private User takenBy; // Người ghi chỉ số

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "taken_by", insertable = false, updatable = false, length = 36)
    private UUID takenById;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    @Builder.Default
    private MeterReadingStatus status = MeterReadingStatus.DRAFT;

    @Column(columnDefinition = "TEXT")
    private String note; // Ghi chú
}