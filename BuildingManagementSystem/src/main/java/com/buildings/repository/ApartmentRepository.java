package com.buildings.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.buildings.entity.Apartment;

@Repository
public interface ApartmentRepository extends JpaRepository<Apartment, UUID> {

    Optional<Apartment> findByCode(String code);

    boolean existsByCode(String code);
}
