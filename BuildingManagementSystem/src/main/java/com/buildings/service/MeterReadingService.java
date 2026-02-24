package com.buildings.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.buildings.dto.request.service.MeterReadingCreateRequest;
import com.buildings.dto.request.service.MeterReadingUpdateRequest;
import com.buildings.dto.response.service.MeterReadingResponse;
import com.buildings.dto.response.service.OldIndexResponse;
import com.buildings.dto.response.service.PeriodSummaryResponse;
import com.buildings.entity.enums.MeterReadingStatus;

public interface MeterReadingService {

    // CRUD
    MeterReadingResponse create(MeterReadingCreateRequest request, String photoUrl, UUID takenById);

    MeterReadingResponse getById(UUID id);

    MeterReadingResponse update(UUID id, MeterReadingUpdateRequest request, String newPhotoUrl);

    void delete(UUID id);

    // Queries
    List<MeterReadingResponse> getByApartment(UUID apartmentId);

    List<MeterReadingResponse> getByApartmentAndService(UUID apartmentId, UUID serviceId);

    List<MeterReadingResponse> getByPeriod(String period);

    List<MeterReadingResponse> getByPeriodAndService(String period, UUID serviceId);

    Page<MeterReadingResponse> search(UUID apartmentId, UUID serviceId, String period,
            MeterReadingStatus status, Pageable pageable);

    // Business logic
    /**
     * Lấy chỉ số cũ suggested cho một căn hộ, dịch vụ và kỳ
     * Chỉ số cũ = Chỉ số mới của tháng trước
     */
    OldIndexResponse getOldIndexSuggestion(UUID apartmentId, UUID serviceId, String period);

    /**
     * Lấy chỉ số mới của tháng trước để làm chỉ số cũ
     */
    BigDecimal getPreviousNewIndex(UUID apartmentId, UUID serviceId, String period);

    /**
     * Tính tiêu thụ = new_index - old_index
     */
    BigDecimal calculateConsumption(BigDecimal oldIndex, BigDecimal newIndex, Boolean isMeterReset);

    // Status management
    MeterReadingResponse confirm(UUID id);

    MeterReadingResponse lock(UUID id);

    // Statistics
    long countByPeriodAndStatus(String period, MeterReadingStatus status);

    // Thống kê tổng hợp theo kỳ
    PeriodSummaryResponse getPeriodSummary(String period);
}
