package com.buildings.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.buildings.entity.Service;
import com.buildings.entity.enums.BillingMethod;

@Repository
public interface ServiceRepository extends JpaRepository<Service, UUID> {

    Optional<Service> findByCode(String code);

    boolean existsByCode(String code);

    List<Service> findByIsRecurring(Boolean isRecurring);

    List<Service> findByBillingMethod(BillingMethod billingMethod);

    @Query("SELECT s FROM Service s WHERE (s.isDeleted = false OR s.isDeleted IS NULL) AND s.isActive = true")
    List<Service> findAllActive();

    @Query("SELECT s FROM Service s LEFT JOIN FETCH s.tariffs WHERE s.id = :id")
    Optional<Service> findByIdWithTariffs(@Param("id") UUID id);
}
