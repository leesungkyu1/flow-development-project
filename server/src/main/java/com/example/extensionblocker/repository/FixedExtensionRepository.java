package com.example.extensionblocker.repository;

import com.example.extensionblocker.domain.FixedExtension;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * {@link FixedExtension} 엔티티에 대한 데이터 접근(Repository) 인터페이스.
 * Spring Data JPA를 통해 데이터베이스 CRUD 작업을 수행합니다.
 */
@Repository
public interface FixedExtensionRepository extends JpaRepository<FixedExtension, Long> {
    /**
     * 이름으로 고정 확장자를 조회합니다.
     *
     * @param name 조회할 확장자 이름
     * @return 해당 이름을 가진 {@link FixedExtension} (Optional)
     */
    Optional<FixedExtension> findByName(String name);
}
