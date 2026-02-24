package com.buildings.entity;

import java.time.LocalDateTime;
import java.util.List;

import com.buildings.entity.enums.MaintenanceCategory;
import com.buildings.entity.enums.PaymentStatus;
import com.buildings.entity.enums.RequestPriority;
import com.buildings.entity.enums.RequestScope;
import com.buildings.entity.enums.RequestStatus;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "maintenance_requests")
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class MaintenanceRequest extends BaseEntity{

    @Column(unique = true, nullable = false)
    private String code;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String title;

    private Boolean isBillable;// có tính phí hay không (có những trường hợp hỏng hóc do tòa nhà, không phải do người dân)

    private LocalDateTime preferredTime;// tgian người gửi yêu cầu mong muốn
    private LocalDateTime startedAt;// tgian bắt đầu thực hiện
    private LocalDateTime finishedAt;// tgian hoàn thành
    private LocalDateTime closedAt;// tgian đóng (hoàn thành mọi thủ tục giấy tờ/thanh toán)

    @Enumerated(EnumType.STRING)
    private RequestScope scope;

    @Enumerated(EnumType.STRING)
    private MaintenanceCategory category;

    @Enumerated(EnumType.STRING)
    private RequestStatus requestStatus;

    @Enumerated(EnumType.STRING)
    private RequestPriority priority;

    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus;

    @ManyToOne
    @JoinColumn(name = "requester_id")
    private User requester;

    @ManyToOne
    @JoinColumn(name = "staff_id")
    private User staff;

    @ManyToOne
    @JoinColumn(name = "apartment_id")
    private Apartment apartment;

    @ManyToOne
    @JoinColumn(name = "building_id")
    private Building building;

    @OneToMany(mappedBy = "maintenanceRequest")
    private List<MaintenanceQuotation> quotations;

    @OneToMany(mappedBy = "maintenanceRequest")
    private List<MaintenanceResource> resources;

}
