package com.buildings.repository;

import com.buildings.dto.request.Auth.AuthenticationRequest;
import com.buildings.entity.User;
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
public interface UserRepository extends JpaRepository<User, UUID> {

    @Query("SELECT u FROM User u " +
            "LEFT JOIN FETCH u.userRoles ur " +
            "LEFT JOIN FETCH ur.role r " +
            "LEFT JOIN FETCH ur.building b " +
            "WHERE u.email = :email")
    Optional<User> findByEmailWithRoles(@Param("email") String email);

    Optional<User> findByEmail(String email);

    Optional<User> findById(UUID id);

    @Query("SELECT DISTINCT u FROM User u " +
            "LEFT JOIN u.userRoles ur " +
            "LEFT JOIN ur.role r " +
            "WHERE (:name IS NULL OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :name, '%'))) " +
            "AND (:roleCode IS NULL OR r.code = :roleCode)")
    Page<User> findByNameAndRole(@Param("name") String name,
                                 @Param("roleCode") String roleCode,
                                 Pageable pageable);

    @Query("SELECT u FROM User u WHERE " +
            "LOWER(u.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "u.phone LIKE CONCAT('%', :keyword, '%')")
    List<User> searchUsers(@Param("keyword") String keyword);
}
