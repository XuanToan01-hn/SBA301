package com.buildings.repository;


import com.buildings.entity.Building;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;


@Repository
public interface BuildingRepository extends JpaRepository<Building, UUID> {

    @Query("""
    SELECT b FROM Building b
    WHERE (:search = '' OR LOWER(b.name) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(b.code) LIKE LOWER(CONCAT('%', :search, '%')))
    AND (:apartmentsGenerated IS NULL OR b.apartmentsGenerated = :apartmentsGenerated)
""")
    Page<Building> searchBuildings(
            @Param("search") String search,
            @Param("apartmentsGenerated") Boolean apartmentsGenerated,
            Pageable pageable
    );
    Optional<Building> findByCode(String code);

    Optional<Building> findByName(String name);

    boolean existsByCode(String code);

    boolean existsByName(String name);

    @Query("SELECT CASE WHEN COUNT(b) > 0 THEN true ELSE false END FROM Building b WHERE b.code = :code AND b.id != :id")
    boolean existsByCodeAndIdNot(@Param("code") String code, @Param("id") Long id);

    @Query("SELECT CASE WHEN COUNT(b) > 0 THEN true ELSE false END FROM Building b WHERE b.name = :name AND b.id != :id")
    boolean existsByNameAndIdNot(@Param("name") String name, @Param("id") Long id);

    @Query("SELECT b FROM Building b WHERE " +
            "LOWER(b.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(b.code) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(b.address) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<Building> findAllWithSearch(@Param("search") String search, Pageable pageable);

    @Query("SELECT COUNT(a) FROM Apartment a WHERE a.building.id = :buildingId")
    Long countApartmentsByBuildingId(@Param("buildingId") Long buildingId);

    @Query("SELECT b FROM Building b WHERE b.apartmentsGenerated = true")
    Page<Building> findBuildingsWithGeneratedApartments(Pageable pageable);

    @Query("SELECT b FROM Building b WHERE b.apartmentsGenerated = false")
    Page<Building> findBuildingsWithoutGeneratedApartments(Pageable pageable);
}
