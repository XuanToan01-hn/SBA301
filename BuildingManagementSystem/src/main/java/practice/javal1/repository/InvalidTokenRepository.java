// InvalidTokenRepository.java
package practice.javal1.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import practice.javal1.entity.InvalidTokenEntity;

@Repository
public interface InvalidTokenRepository extends JpaRepository<InvalidTokenEntity, String> {
    boolean existsById(String id);
}
