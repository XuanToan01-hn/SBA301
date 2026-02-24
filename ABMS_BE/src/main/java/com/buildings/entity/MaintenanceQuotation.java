package com.buildings.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.buildings.entity.enums.QuotationStatus;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "maintenance_quotations")
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class MaintenanceQuotation extends BaseEntity {

    private String code;
    private String title;

    @Enumerated(EnumType.STRING)
    private QuotationStatus status;

    @Column(columnDefinition = "TEXT")
    private String description;// staff note

    @Column(columnDefinition = "TEXT")
    private String note;// cư dân note

    @Column(precision = 15, scale = 2)
    private BigDecimal totalAmount;

    private LocalDateTime validUntil;// báo giá có hiệu lực đến... (nếu không phản hồi từ Status => EXPIRED)

    @ManyToOne
    @JoinColumn(name = "maintenance_request_id")
    private MaintenanceRequest maintenanceRequest;

    @OneToMany(mappedBy = "quotation")
    private List<MaintenanceItem> items;

}
