package com.buildings.repository;

import com.buildings.entity.ServiceTariffTier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ServiceTariffTierRepository extends JpaRepository<ServiceTariffTier, UUID> {

    List<ServiceTariffTier> findByTariffIdOrderByMinValAsc(UUID tariffId);

    void deleteByTariffId(UUID tariffId);
}
