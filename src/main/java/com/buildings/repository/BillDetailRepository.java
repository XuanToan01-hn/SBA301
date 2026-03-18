package com.buildings.repository;

import com.buildings.entity.BillDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface BillDetailRepository extends JpaRepository<BillDetail, UUID> {

    @Query("SELECT CASE WHEN COUNT(d) > 0 THEN true ELSE false END FROM BillDetail d " +
            "JOIN d.bill b " +
            "JOIN b.apartment a " +
            "WHERE a.id = :apartmentId " +
            "AND LOWER(d.description) LIKE LOWER(CONCAT('%', :requestCode, '%'))")
    boolean existsMaintenanceChargeByApartmentAndRequestCode(
            @Param("apartmentId") UUID apartmentId,
            @Param("requestCode") String requestCode
    );
}
