package com.buildings.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.buildings.entity.MeterReading;
import com.buildings.entity.enums.MeterReadingStatus;

@Repository
public interface MeterReadingRepository extends JpaRepository<MeterReading, UUID> {

    /**
     * Tìm ghi chỉ số theo căn hộ, dịch vụ và kỳ
     */
    Optional<MeterReading> findByApartmentIdAndServiceIdAndPeriod(UUID apartmentId, UUID serviceId, String period);

    /**
     * Kiểm tra đã tồn tại ghi chỉ số cho căn hộ, dịch vụ, kỳ chưa
     */
    boolean existsByApartmentIdAndServiceIdAndPeriod(UUID apartmentId, UUID serviceId, String period);

    /**
     * Lấy chỉ số mới của tháng trước để làm chỉ số cũ cho tháng hiện tại
     * Logic: Tìm bản ghi có period < currentPeriod, sắp xếp giảm dần và lấy bản đầu tiên
     */
    Optional<MeterReading> findTopByApartmentIdAndServiceIdAndPeriodLessThanOrderByPeriodDesc(
            UUID apartmentId, UUID serviceId, String period);

    /**
     * Danh sách ghi chỉ số của một căn hộ
     */
    List<MeterReading> findByApartmentIdOrderByPeriodDesc(UUID apartmentId);

    /**
     * Danh sách ghi chỉ số của một căn hộ theo dịch vụ
     */
    List<MeterReading> findByApartmentIdAndServiceIdOrderByPeriodDesc(UUID apartmentId, UUID serviceId);

    /**
     * Danh sách ghi chỉ số theo kỳ (tất cả căn hộ)
     */
    List<MeterReading> findByPeriodOrderByApartmentId(String period);

    /**
     * Danh sách ghi chỉ số theo kỳ và dịch vụ
     */
    List<MeterReading> findByPeriodAndServiceIdOrderByApartmentId(String period, UUID serviceId);

    /**
     * Danh sách ghi chỉ số theo trạng thái
     */
    List<MeterReading> findByStatus(MeterReadingStatus status);

    /**
     * Danh sách ghi chỉ số theo kỳ và trạng thái
     */
    List<MeterReading> findByPeriodAndStatus(String period, MeterReadingStatus status);

    /**
     * Phân trang danh sách ghi chỉ số với filter
     */
    @Query("SELECT mr FROM MeterReading mr " +
           "WHERE (:apartmentId IS NULL OR mr.apartment.id = :apartmentId) " +
           "AND (:serviceId IS NULL OR mr.service.id = :serviceId) " +
           "AND (:period IS NULL OR mr.period = :period) " +
           "AND (:status IS NULL OR mr.status = :status) " +
           "ORDER BY mr.period DESC, mr.apartment.code ASC")
    Page<MeterReading> findWithFilters(
            @Param("apartmentId") UUID apartmentId,
            @Param("serviceId") UUID serviceId,
            @Param("period") String period,
            @Param("status") MeterReadingStatus status,
            Pageable pageable);

    /**
     * Đếm số ghi chỉ số theo kỳ và trạng thái
     */
    long countByPeriodAndStatus(String period, MeterReadingStatus status);

    /**
     * Lấy ghi chỉ số với thông tin chi tiết (eager fetch)
     */
    @Query("SELECT mr FROM MeterReading mr " +
           "LEFT JOIN FETCH mr.apartment " +
           "LEFT JOIN FETCH mr.service " +
           "LEFT JOIN FETCH mr.takenBy " +
           "WHERE mr.id = :id")
    Optional<MeterReading> findByIdWithDetails(@Param("id") UUID id);
}
