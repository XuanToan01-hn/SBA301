package com.buildings.repository;

import com.buildings.entity.ServiceTariff;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ServiceTariffRepository extends JpaRepository<ServiceTariff, UUID> {

    List<ServiceTariff> findByServiceIdOrderByEffectiveFromDesc(UUID serviceId);


    @Query("SELECT t FROM ServiceTariff t WHERE t.service.id = :serviceId " +
           "AND t.effectiveFrom <= :date " +
           "AND (t.effectiveTo IS NULL OR t.effectiveTo >= :date) " +
           "ORDER BY t.effectiveFrom DESC")
    List<ServiceTariff> findActiveTariffs(@Param("serviceId") UUID serviceId, @Param("date") LocalDate date);

    default Optional<ServiceTariff> findCurrentTariff(UUID serviceId) {
        List<ServiceTariff> tariffs = findActiveTariffs(serviceId, LocalDate.now());
        return tariffs.isEmpty() ? Optional.empty() : Optional.of(tariffs.get(0));
    }

    default Optional<ServiceTariff> findActiveTariff(UUID serviceId, LocalDate date) {
        List<ServiceTariff> tariffs = findActiveTariffs(serviceId, date);
        return tariffs.isEmpty() ? Optional.empty() : Optional.of(tariffs.get(0));
    }

    @Query("SELECT t FROM ServiceTariff t LEFT JOIN FETCH t.tiers WHERE t.id = :id")
    Optional<ServiceTariff> findByIdWithTiers(@Param("id") UUID id);

    @Query("SELECT COUNT(t) > 0 FROM ServiceTariff t WHERE t.service.id = :serviceId " +
           "AND t.id != :excludeId " +
           "AND t.effectiveFrom <= :effectiveTo " +
           "AND (t.effectiveTo IS NULL OR t.effectiveTo >= :effectiveFrom)")
    boolean hasOverlappingTariff(@Param("serviceId") UUID serviceId,
                                  @Param("excludeId") UUID excludeId,
                                  @Param("effectiveFrom") LocalDate effectiveFrom,
                                  @Param("effectiveTo") LocalDate effectiveTo);
}
