package com.buildings.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.buildings.entity.ServiceTariff;

@Repository
public interface ServiceTariffRepository extends JpaRepository<ServiceTariff, UUID> {

    List<ServiceTariff> findByServiceIdOrderByEffectiveFromDesc(UUID serviceId);

    /**
     * Tìm biểu giá đang hiệu lực cho một dịch vụ tại một ngày cụ thể
     */
    @Query("SELECT t FROM ServiceTariff t WHERE t.service.id = :serviceId " +
           "AND t.effectiveFrom <= :date " +
           "AND (t.effectiveTo IS NULL OR t.effectiveTo >= :date) " +
           "ORDER BY t.effectiveFrom DESC")
    List<ServiceTariff> findActiveTariffs(@Param("serviceId") UUID serviceId, @Param("date") LocalDate date);

    /**
     * Lấy biểu giá hiện tại (đang hiệu lực)
     */
    default Optional<ServiceTariff> findCurrentTariff(UUID serviceId) {
        List<ServiceTariff> tariffs = findActiveTariffs(serviceId, LocalDate.now());
        return tariffs.isEmpty() ? Optional.empty() : Optional.of(tariffs.get(0));
    }

    /**
     * Tìm biểu giá đang hiệu lực tại một thời điểm cụ thể
     */
    default Optional<ServiceTariff> findActiveTariff(UUID serviceId, LocalDate date) {
        List<ServiceTariff> tariffs = findActiveTariffs(serviceId, date);
        return tariffs.isEmpty() ? Optional.empty() : Optional.of(tariffs.get(0));
    }

    @Query("SELECT t FROM ServiceTariff t LEFT JOIN FETCH t.tiers WHERE t.id = :id")
    Optional<ServiceTariff> findByIdWithTiers(@Param("id") UUID id);

    /**
     * Kiểm tra xem có biểu giá nào bị trùng thời gian không
     */
    @Query("SELECT COUNT(t) > 0 FROM ServiceTariff t WHERE t.service.id = :serviceId " +
           "AND t.id != :excludeId " +
           "AND t.effectiveFrom <= :effectiveTo " +
           "AND (t.effectiveTo IS NULL OR t.effectiveTo >= :effectiveFrom)")
    boolean hasOverlappingTariff(@Param("serviceId") UUID serviceId,
                                  @Param("excludeId") UUID excludeId,
                                  @Param("effectiveFrom") LocalDate effectiveFrom,
                                  @Param("effectiveTo") LocalDate effectiveTo);
}
