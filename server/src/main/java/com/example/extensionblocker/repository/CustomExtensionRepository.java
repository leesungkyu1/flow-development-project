package com.example.extensionblocker.repository;

import com.example.extensionblocker.domain.CustomExtension;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * {@link CustomExtension} 엔티티에 대한 데이터 접근(Repository) 인터페이스.
 * Spring Data JPA를 통해 데이터베이스 CRUD 작업을 수행합니다.
 */
@Repository
public interface CustomExtensionRepository extends JpaRepository<CustomExtension, Long> {
    /**
     * 이름으로 커스텀 확장자를 조회합니다.
     *
     * @param name 조회할 확장자 이름
     * @return 해당 이름을 가진 {@link CustomExtension} (Optional)
     */
    Optional<CustomExtension> findByName(String name);

    /**
     * 특정 이름의 커스텀 확장자가 존재하는지 확인합니다.
     *
     * @param name 존재 여부를 확인할 확장자 이름
     * @return 해당 이름을 가진 확장자가 존재하면 true, 그렇지 않으면 false
     */
    boolean existsByName(String name);

    /**
     * 현재 저장된 커스텀 확장자의 총 개수를 반환합니다.
     *
     * @return 커스텀 확장자의 총 개수
     */
    long count();
}
