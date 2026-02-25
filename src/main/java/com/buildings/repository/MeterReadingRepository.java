package com.buildings.repository;

import com.buildings.entity.MeterReading;
import com.buildings.entity.enums.MeterReadingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MeterReadingRepository extends JpaRepository<MeterReading, UUID> {

    Optional<MeterReading> findByApartmentIdAndServiceIdAndPeriod(UUID apartmentId, UUID serviceId, String period);

    boolean existsByApartmentIdAndServiceIdAndPeriod(UUID apartmentId, UUID serviceId, String period);

    Optional<MeterReading> findTopByApartmentIdAndServiceIdAndPeriodLessThanOrderByPeriodDesc(
            UUID apartmentId, UUID serviceId, String period);

    List<MeterReading> findByApartmentIdOrderByPeriodDesc(UUID apartmentId);

    List<MeterReading> findByApartmentIdAndServiceIdOrderByPeriodDesc(UUID apartmentId, UUID serviceId);

    List<MeterReading> findByPeriodOrderByApartmentId(String period);

    List<MeterReading> findByPeriodAndServiceIdOrderByApartmentId(String period, UUID serviceId);

    List<MeterReading> findByStatus(MeterReadingStatus status);

    List<MeterReading> findByPeriodAndStatus(String period, MeterReadingStatus status);

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


    long countByPeriodAndStatus(String period, MeterReadingStatus status);

    @Query("SELECT mr FROM MeterReading mr " +
           "LEFT JOIN FETCH mr.apartment " +
           "LEFT JOIN FETCH mr.service " +
           "LEFT JOIN FETCH mr.takenBy " +
           "WHERE mr.id = :id")
    Optional<MeterReading> findByIdWithDetails(@Param("id") UUID id);
}
