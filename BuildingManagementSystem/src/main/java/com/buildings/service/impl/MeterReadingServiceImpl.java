package com.buildings.service.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.buildings.dto.request.service.MeterReadingCreateRequest;
import com.buildings.dto.request.service.MeterReadingUpdateRequest;
import com.buildings.dto.response.service.MeterReadingResponse;
import com.buildings.dto.response.service.OldIndexResponse;
import com.buildings.dto.response.service.PeriodSummaryResponse;
import com.buildings.entity.Apartment;
import com.buildings.entity.MeterReading;
import com.buildings.entity.User;
import com.buildings.entity.enums.MeterReadingStatus;
import com.buildings.exception.AppException;
import com.buildings.exception.ErrorCode;
import com.buildings.mapper.MeterReadingMapper;
import com.buildings.repository.ApartmentRepository;
import com.buildings.repository.MeterReadingRepository;
import com.buildings.repository.ServiceRepository;
import com.buildings.repository.UserRepository;
import com.buildings.service.MeterReadingService;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class MeterReadingServiceImpl implements MeterReadingService {

    private final MeterReadingRepository meterReadingRepository;
    private final ServiceRepository serviceRepository;
    private final ApartmentRepository apartmentRepository;
    private final UserRepository userRepository;
    private final MeterReadingMapper meterReadingMapper;

    private static final DateTimeFormatter PERIOD_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM");
    // Cảnh báo nếu tiêu thụ tăng hơn 300% so với tháng trước
    private static final double HIGH_USAGE_THRESHOLD = 3.0;

    @Override
    public MeterReadingResponse create(MeterReadingCreateRequest request, String photoUrl, UUID takenById) {
        // Validate apartment
        Apartment apartment = apartmentRepository.findById(request.getApartmentId())
                .orElseThrow(() -> new AppException(ErrorCode.APARTMENT_NOT_FOUND));

        // Validate service
        com.buildings.entity.Service service = serviceRepository.findById(request.getServiceId())
                .orElseThrow(() -> new AppException(ErrorCode.SERVICE_NOT_FOUND));

        // Check duplicate
        if (meterReadingRepository.existsByApartmentIdAndServiceIdAndPeriod(
                request.getApartmentId(), request.getServiceId(), request.getPeriod())) {
            throw new AppException(ErrorCode.DUPLICATE_METER_READING);
        }

        // Get old index from previous month if not provided
        BigDecimal oldIndex = request.getOldIndex();
        if (oldIndex == null) {
            oldIndex = getPreviousNewIndex(request.getApartmentId(), request.getServiceId(), request.getPeriod());
        }

        // Validate index
        if (!Boolean.TRUE.equals(request.getIsMeterReset()) &&
                request.getNewIndex().compareTo(oldIndex) < 0) {
            throw new AppException(ErrorCode.INVALID_METER_INDEX);
        }

        // Calculate consumption
        BigDecimal consumption = calculateConsumption(oldIndex, request.getNewIndex(), request.getIsMeterReset());

        // Create entity
        MeterReading meterReading = meterReadingMapper.toEntity(request);
        meterReading.setApartment(apartment);
        meterReading.setService(service);
        meterReading.setOldIndex(oldIndex);
        meterReading.setConsumption(consumption);
        meterReading.setPhotoUrl(photoUrl);
        meterReading.setTakenAt(LocalDateTime.now());
        meterReading.setStatus(MeterReadingStatus.DRAFT);

        // Set taken by user
        if (takenById != null) {
            User takenBy = userRepository.findById(takenById).orElse(null);
            meterReading.setTakenBy(takenBy);
        }

        meterReading = meterReadingRepository.save(meterReading);

        log.info("Created meter reading for apartment: {}, service: {}, period: {}",
                apartment.getCode(), service.getCode(), request.getPeriod());

        // Đưa isHighUsage vào response
        MeterReadingResponse response = meterReadingMapper.toResponse(meterReading);
        response.setIsHighUsage(isHighUsageConsumption(request.getApartmentId(), request.getServiceId(),
                request.getPeriod(), consumption));
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public MeterReadingResponse getById(UUID id) {
        MeterReading meterReading = meterReadingRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new AppException(ErrorCode.METER_READING_NOT_FOUND));
        return meterReadingMapper.toResponse(meterReading);
    }

    @Override
    public MeterReadingResponse update(UUID id, MeterReadingUpdateRequest request, String newPhotoUrl) {
        MeterReading meterReading = meterReadingRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.METER_READING_NOT_FOUND));

        // Check if locked
        if (meterReading.getStatus() == MeterReadingStatus.LOCKED) {
            throw new AppException(ErrorCode.METER_READING_LOCKED);
        }

        // Update fields
        meterReadingMapper.updateEntity(meterReading, request);

        // Update photo if provided
        if (newPhotoUrl != null) {
            meterReading.setPhotoUrl(newPhotoUrl);
        }

        // Recalculate consumption if indexes changed
        BigDecimal oldIndex = request.getOldIndex() != null ? request.getOldIndex() : meterReading.getOldIndex();
        BigDecimal newIndex = request.getNewIndex() != null ? request.getNewIndex() : meterReading.getNewIndex();
        Boolean isMeterReset = request.getIsMeterReset() != null ? request.getIsMeterReset()
                : meterReading.getIsMeterReset();

        // Validate index
        if (!Boolean.TRUE.equals(isMeterReset) && newIndex.compareTo(oldIndex) < 0) {
            throw new AppException(ErrorCode.INVALID_METER_INDEX);
        }

        meterReading.setOldIndex(oldIndex);
        meterReading.setNewIndex(newIndex);
        meterReading.setConsumption(calculateConsumption(oldIndex, newIndex, isMeterReset));

        meterReading = meterReadingRepository.save(meterReading);

        log.info("Updated meter reading: {}", id);
        // Đưa isHighUsage vào response
        MeterReadingResponse response = meterReadingMapper.toResponse(meterReading);
        response.setIsHighUsage(isHighUsageConsumption(meterReading.getApartmentId(), meterReading.getServiceId(),
                meterReading.getPeriod(), meterReading.getConsumption()));
        return response;
    }

    @Override
    public void delete(UUID id) {
        MeterReading meterReading = meterReadingRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.METER_READING_NOT_FOUND));

        if (meterReading.getStatus() == MeterReadingStatus.LOCKED) {
            throw new AppException(ErrorCode.METER_READING_LOCKED);
        }

        meterReading.setIsDeleted(true);
        meterReadingRepository.save(meterReading);

        log.info("Soft deleted meter reading: {}", id);
    }

    // ==================== QUERIES ====================

    @Override
    @Transactional(readOnly = true)
    public List<MeterReadingResponse> getByApartment(UUID apartmentId) {
        return meterReadingMapper.toResponseList(
                meterReadingRepository.findByApartmentIdOrderByPeriodDesc(apartmentId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<MeterReadingResponse> getByApartmentAndService(UUID apartmentId, UUID serviceId) {
        return meterReadingMapper.toResponseList(
                meterReadingRepository.findByApartmentIdAndServiceIdOrderByPeriodDesc(apartmentId, serviceId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<MeterReadingResponse> getByPeriod(String period) {
        return meterReadingMapper.toResponseList(
                meterReadingRepository.findByPeriodOrderByApartmentId(period));
    }

    @Override
    @Transactional(readOnly = true)
    public List<MeterReadingResponse> getByPeriodAndService(String period, UUID serviceId) {
        return meterReadingMapper.toResponseList(
                meterReadingRepository.findByPeriodAndServiceIdOrderByApartmentId(period, serviceId));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MeterReadingResponse> search(UUID apartmentId, UUID serviceId, String period,
            MeterReadingStatus status, Pageable pageable) {
        return meterReadingRepository.findWithFilters(apartmentId, serviceId, period, status, pageable)
                .map(meterReadingMapper::toResponse);
    }

    // ==================== BUSINESS LOGIC ====================

    @Override
    @Transactional(readOnly = true)
    public OldIndexResponse getOldIndexSuggestion(UUID apartmentId, UUID serviceId, String period) {
        // Validate service
        com.buildings.entity.Service service = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new AppException(ErrorCode.SERVICE_NOT_FOUND));

        // Calculate previous period
        YearMonth currentYM = YearMonth.parse(period.trim(), PERIOD_FORMATTER);
        YearMonth previousYM = currentYM.minusMonths(1);
        String previousPeriod = previousYM.format(PERIOD_FORMATTER);

        // Get previous reading
        BigDecimal suggestedOldIndex = getPreviousNewIndex(apartmentId, serviceId, period);
        boolean hasPrevious = meterReadingRepository
                .findByApartmentIdAndServiceIdAndPeriod(apartmentId, serviceId, previousPeriod)
                .isPresent();

        return OldIndexResponse.builder()
                .apartmentId(apartmentId)
                .serviceId(serviceId)
                .serviceName(service.getName())
                .currentPeriod(period)
                .previousPeriod(previousPeriod)
                .suggestedOldIndex(suggestedOldIndex)
                .hasPreviousReading(hasPrevious)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getPreviousNewIndex(UUID apartmentId, UUID serviceId, String period) {
        return meterReadingRepository
                .findTopByApartmentIdAndServiceIdAndPeriodLessThanOrderByPeriodDesc(apartmentId, serviceId, period)
                .map(MeterReading::getNewIndex)
                .orElse(BigDecimal.ZERO);
    }

    @Override
    public BigDecimal calculateConsumption(BigDecimal oldIndex, BigDecimal newIndex, Boolean isMeterReset) {
        if (Boolean.TRUE.equals(isMeterReset)) {
            // Nếu reset đồng hồ, tiêu thụ = chỉ số mới (giả sử đồng hồ mới bắt đầu từ 0)
            return newIndex;
        }
        return newIndex.subtract(oldIndex);
    }

    // ==================== STATUS MANAGEMENT ====================

    @Override
    public MeterReadingResponse confirm(UUID id) {
        MeterReading meterReading = meterReadingRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.METER_READING_NOT_FOUND));

        if (meterReading.getStatus() == MeterReadingStatus.LOCKED) {
            throw new AppException(ErrorCode.METER_READING_LOCKED);
        }

        meterReading.setStatus(MeterReadingStatus.CONFIRMED);
        meterReading = meterReadingRepository.save(meterReading);

        log.info("Confirmed meter reading: {}", id);
        return meterReadingMapper.toResponse(meterReading);
    }

    @Override
    public MeterReadingResponse lock(UUID id) {
        MeterReading meterReading = meterReadingRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.METER_READING_NOT_FOUND));

        meterReading.setStatus(MeterReadingStatus.LOCKED);
        meterReading = meterReadingRepository.save(meterReading);

        log.info("Locked meter reading: {}", id);
        return meterReadingMapper.toResponse(meterReading);
    }

    @Override
    @Transactional(readOnly = true)
    public long countByPeriodAndStatus(String period, MeterReadingStatus status) {
        return meterReadingRepository.countByPeriodAndStatus(period, status);
    }

    // Thống kê tổng hợp theo kỳ
    @Override
    @Transactional(readOnly = true)
    public PeriodSummaryResponse getPeriodSummary(String period) {
        List<MeterReading> readings = meterReadingRepository.findByPeriodOrderByApartmentId(period);

        if (readings.isEmpty()) {
            return PeriodSummaryResponse.builder()
                    .period(period)
                    .totalReadings(0L)
                    .build();
        }

        long draftCount = readings.stream()
                .filter(r -> r.getStatus() == MeterReadingStatus.DRAFT).count();
        long confirmedCount = readings.stream()
                .filter(r -> r.getStatus() == MeterReadingStatus.CONFIRMED).count();
        long lockedCount = readings.stream()
                .filter(r -> r.getStatus() == MeterReadingStatus.LOCKED).count();

        BigDecimal totalConsumption = readings.stream()
                .map(MeterReading::getConsumption)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal avgConsumption = totalConsumption.divide(
                BigDecimal.valueOf(readings.size()), 2, java.math.RoundingMode.HALF_UP);

        BigDecimal maxConsumption = readings.stream()
                .map(MeterReading::getConsumption)
                .max(BigDecimal::compareTo).orElse(BigDecimal.ZERO);

        BigDecimal minConsumption = readings.stream()
                .map(MeterReading::getConsumption)
                .min(BigDecimal::compareTo).orElse(BigDecimal.ZERO);

        // Dùng avgConsumption nhân 3 làm ngưỡng cảnh báo highUsage tổng hợp
        long highUsageCount = readings.stream()
                .filter(r -> r.getConsumption().compareTo(
                        avgConsumption.multiply(BigDecimal.valueOf(HIGH_USAGE_THRESHOLD))) > 0)
                .count();

        long completedCount = confirmedCount + lockedCount;
        double completionRate = readings.isEmpty() ? 0.0
                : (double) completedCount / readings.size() * 100.0;

        return PeriodSummaryResponse.builder()
                .period(period)
                .totalReadings(readings.size())
                .draftCount(draftCount)
                .confirmedCount(confirmedCount)
                .lockedCount(lockedCount)
                .totalConsumption(totalConsumption)
                .avgConsumption(avgConsumption)
                .maxConsumption(maxConsumption)
                .minConsumption(minConsumption)
                .highUsageCount(highUsageCount)
                .completionRate(completionRate)
                .build();
    }

    // Helper: kiểm tra high usage so với tháng trước
    private boolean isHighUsageConsumption(UUID apartmentId, UUID serviceId, String period, BigDecimal consumption) {
        BigDecimal previousConsumption = meterReadingRepository
                .findTopByApartmentIdAndServiceIdAndPeriodLessThanOrderByPeriodDesc(apartmentId, serviceId, period)
                .map(MeterReading::getConsumption)
                .orElse(null);

        if (previousConsumption == null || previousConsumption.compareTo(BigDecimal.ZERO) == 0) {
            return false;
        }

        // Cảnh báo nếu consumption tăng hơn HIGH_USAGE_THRESHOLD lần so với tháng trước
        BigDecimal threshold = previousConsumption.multiply(BigDecimal.valueOf(HIGH_USAGE_THRESHOLD));
        return consumption.compareTo(threshold) > 0;
    }
}
