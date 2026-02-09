package com.example.extensionblocker.service;

import com.example.extensionblocker.domain.CustomExtension;
import com.example.extensionblocker.domain.FixedExtension;
import com.example.extensionblocker.dto.CustomExtensionDto;
import com.example.extensionblocker.dto.CustomExtensionRequestListDto;
import com.example.extensionblocker.dto.FixedExtensionDto;
import com.example.extensionblocker.repository.CustomExtensionRepository;
import com.example.extensionblocker.repository.FixedExtensionRepository;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.extensionblocker.dto.CustomExtensionBatchResultDto;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Set; // Added for HashSet
import java.util.HashSet; // Added for HashSet
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
     * 여러 개의 새로운 커스텀 확장자를 추가합니다.
     * 각 확장자는 유효성 검사를 거치며, 중복되거나 전체 개수 제한을 초과하는 확장자는 추가되지 않습니다.
     *
     * @param extensionNames 추가할 커스텀 확장자 이름 목록 (CustomExtensionNameDto 객체)
     * @return 성공적으로 추가된 커스텀 확장자들의 DTO 목록
     * @throws IllegalStateException    커스텀 확장자 개수 제한(200개)을 초과한 경우
     */
    @Transactional
    public CustomExtensionBatchResultDto addCustomExtensions(List<CustomExtensionRequestListDto.CustomExtensionNameDto> extensionNames) {
        List<CustomExtensionDto> successfulAdditions = new ArrayList<>();
        int successCount = 0;
        int failedCount = 0;
        Map<String, String> failedExtensionsWithReasons = new HashMap<>();

        int currentCustomExtensionCount = (int) customExtensionRepository.count();
        final int MAX_CUSTOM_EXTENSIONS = 200;

        Set<String> processedNamesInBatch = new HashSet<>();

        for (CustomExtensionRequestListDto.CustomExtensionNameDto extDto : extensionNames) {
            String cleanedName = extDto.getName().toLowerCase().trim();

            if (processedNamesInBatch.contains(cleanedName)) {
                failedCount++;
                failedExtensionsWithReasons.put(cleanedName, "요청 내 중복된 확장자입니다.");
                continue;
            }
            processedNamesInBatch.add(cleanedName);

            if (currentCustomExtensionCount >= MAX_CUSTOM_EXTENSIONS) {
                failedCount++;
                failedExtensionsWithReasons.put(cleanedName, "등록 개수(" + MAX_CUSTOM_EXTENSIONS + "개)를 초과하였습니다.");
                continue;
            }

            if (cleanedName.isEmpty() || cleanedName.length() > 20) {
                failedCount++;
                failedExtensionsWithReasons.put(cleanedName, "글자수가 1글자에서 20자 사이여야 합니다.");
                continue;
            }

            if (fixedExtensionRepository.findByName(cleanedName).isPresent()) {
                failedCount++;
                failedExtensionsWithReasons.put(cleanedName, "고정 확장자와 중복됩니다.");
                continue;
            }

            if (customExtensionRepository.existsByName(cleanedName)) {
                failedCount++;
                failedExtensionsWithReasons.put(cleanedName, "이미 존재하는 커스텀 확장자입니다.");
                continue;
            }

            CustomExtension customExtension = CustomExtension.builder().name(cleanedName).build();
            CustomExtension savedExtension = customExtensionRepository.save(customExtension);
            successfulAdditions.add(CustomExtensionDto.fromEntity(savedExtension));
            successCount++;
            currentCustomExtensionCount++;
        }
        return new CustomExtensionBatchResultDto(successfulAdditions, successCount, failedCount, failedExtensionsWithReasons);
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