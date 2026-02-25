package com.buildings.repository;

import com.buildings.entity.Bill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface BillRepository extends JpaRepository<Bill, UUID> {
    Optional<Bill> findById(UUID billId);

    @Query(value = "select * from bills where id = :id", nativeQuery = true)
    Optional<Bill> findNative(@Param("id") String id);


}
