package com.example.extensionblocker.service;

import com.example.extensionblocker.domain.CustomExtension;
import com.example.extensionblocker.domain.FixedExtension;
import com.example.extensionblocker.dto.CustomExtensionDto;
import com.example.extensionblocker.dto.FixedExtensionDto;
import com.example.extensionblocker.repository.CustomExtensionRepository;
import com.example.extensionblocker.repository.FixedExtensionRepository;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 파일 확장자 관련 비즈니스 로직을 처리하는 서비스 클래스.
 * 고정 확장자와 커스텀 확장자의 조회, 추가, 수정, 삭제 기능을 제공합니다.
 */
@Service
@CacheConfig(cacheNames = "fixedExtensions")
public class ExtensionService {

    private final FixedExtensionRepository fixedExtensionRepository;
    private final CustomExtensionRepository customExtensionRepository;

    /**
     * ExtensionService의 생성자.
     * FixedExtensionRepository와 CustomExtensionRepository를 주입받습니다.
     *
     * @param fixedExtensionRepository 고정 확장자 데이터 접근을 위한 리포지토리
     * @param customExtensionRepository 커스텀 확장자 데이터 접근을 위한 리포지토리
     */
    public ExtensionService(FixedExtensionRepository fixedExtensionRepository, CustomExtensionRepository customExtensionRepository) {
        this.fixedExtensionRepository = fixedExtensionRepository;
        this.customExtensionRepository = customExtensionRepository;
    }

    /**
     * 모든 고정 확장자 목록을 조회합니다.
     * 이 메서드의 결과는 캐시됩니다.
     *
     * @return 고정 확장자 목록
     */
    @Cacheable
    public List<FixedExtensionDto> getAllFixedExtensions() {
        return fixedExtensionRepository.findAll().stream()
                .map(FixedExtensionDto::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * 특정 고정 확장자의 차단 상태를 업데이트합니다.
     * 업데이트 시 캐시된 모든 고정 확장자 데이터는 무효화됩니다.
     *
     * @param id      업데이트할 고정 확장자의 ID
     * @param checked 차단 상태 (true: 차단, false: 차단 해제)
     * @return 업데이트된 고정 확장자 객체
     * @throws IllegalArgumentException 해당 ID의 고정 확장자를 찾을 수 없을 경우
     */
    @Transactional
    @CacheEvict(allEntries = true)
    public FixedExtensionDto updateFixedExtension(Long id, boolean checked) {
        FixedExtension fixedExtension = fixedExtensionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("고정 확장자를 찾을 수 없습니다."));
        fixedExtension.setChecked(checked);
        return FixedExtensionDto.fromEntity(fixedExtensionRepository.save(fixedExtension));
    }

    /**
     * 새로운 커스텀 확장자를 추가합니다.
     * 확장자 이름은 1자에서 20자 사이여야 하며, 기존 확장자와 중복될 수 없고,
     * 전체 커스텀 확장자 개수가 200개를 초과할 수 없습니다.
     *
     * @param name 추가할 커스텀 확장자 이름 (자동으로 소문자로 변환 및 공백 제거)
     * @return 새로 추가된 커스텀 확장자 객체
     * @throws IllegalArgumentException 확장자 이름이 유효하지 않거나 이미 존재하는 경우
     * @throws IllegalStateException    커스텀 확장자 개수 제한(200개)을 초과한 경우
     */
    @Transactional
    public CustomExtensionDto addCustomExtension(String name) {
        String cleanedName = name.toLowerCase().trim();

        if (cleanedName.isEmpty() || cleanedName.length() > 20) {
            throw new IllegalArgumentException("커스텀 확장자의 글자수가 1글자에서 20자까지 입력가능합니다.");
        }

        if (customExtensionRepository.count() >= 200) {
            throw new IllegalStateException("커스텀 확장자의 등록 개수가 200개를 초과하였습니다.");
        }

        if (fixedExtensionRepository.findByName(cleanedName).isPresent() || customExtensionRepository.existsByName(cleanedName)) {
            throw new IllegalArgumentException("확장자 '" + cleanedName + "' 는 중복되었습니다.");
        }

        CustomExtension customExtension = CustomExtension.builder().name(cleanedName).build();
        return CustomExtensionDto.fromEntity(customExtensionRepository.save(customExtension));
    }

    /**
     * 특정 ID의 커스텀 확장자를 삭제합니다.
     *
     * @param id 삭제할 커스텀 확장자의 ID
     * @throws IllegalArgumentException 해당 ID의 커스텀 확장자를 찾을 수 없을 경우
     */
    @Transactional
    public void removeCustomExtension(Long id) {
        if (!customExtensionRepository.existsById(id)) {
            throw new IllegalArgumentException("커스텀 확장자 id : " + id + " 를 찾을 수 없습니다.");
        }
        customExtensionRepository.deleteById(id);
    }

    /**
     * 모든 커스텀 확장자 목록을 조회합니다.
     *
     * @return 커스텀 확장자 목록
     */
    public List<CustomExtensionDto> getAllCustomExtensions() {
        return customExtensionRepository.findAll().stream().map(CustomExtensionDto::fromEntity).collect(Collectors.toList());
    }
}
