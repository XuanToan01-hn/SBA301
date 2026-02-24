package com.buildings.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.buildings.entity.ServiceTariffTier;

@Repository
public interface ServiceTariffTierRepository extends JpaRepository<ServiceTariffTier, UUID> {

    /**
     * Lấy danh sách bậc thang của một biểu giá, sắp xếp theo minVal tăng dần
     */
    List<ServiceTariffTier> findByTariffIdOrderByMinValAsc(UUID tariffId);

    void deleteByTariffId(UUID tariffId);
}
