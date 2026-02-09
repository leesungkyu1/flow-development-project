package com.example.extensionblocker.service;

import com.example.extensionblocker.domain.CustomExtension;
import com.example.extensionblocker.domain.FixedExtension;
import com.example.extensionblocker.dto.CustomExtensionDto;
import com.example.extensionblocker.dto.FixedExtensionDto;
import com.example.extensionblocker.repository.CustomExtensionRepository;
import com.example.extensionblocker.repository.FixedExtensionRepository;
import com.example.extensionblocker.dto.CustomExtensionRequestListDto;
import com.example.extensionblocker.dto.CustomExtensionRequestListDto.CustomExtensionNameDto;
import com.example.extensionblocker.dto.CustomExtensionBatchResultDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.ArgumentCaptor;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.Map;
import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExtensionServiceTest {

    @Mock
    private FixedExtensionRepository fixedExtensionRepository;

    @Mock
    private CustomExtensionRepository customExtensionRepository;

    @InjectMocks
    private ExtensionService extensionService;

    private FixedExtension fixedBat;
    private CustomExtension customPep;

    // Helper method to create CustomExtensionNameDto list
    private List<CustomExtensionNameDto> createCustomExtensionNameDtos(String... names) {
        return Arrays.stream(names)
                .map(CustomExtensionNameDto::new)
                .collect(Collectors.toList());
    }

    @BeforeEach
    void setUp() {
        fixedBat = FixedExtension.builder()
                .id(1L)
                .name("bat")
                .description("Batch file")
                .checked(false)
                .build();
        customPep = CustomExtension.builder().id(1L).name("pep").build();
    }

    @Test
    @DisplayName("고정 확장자 전체 조회 - 성공")
    void getAllFixedExtensions_success() {
        List<FixedExtension> fixedExtensions = Arrays.asList(fixedBat,
                FixedExtension.builder()
                        .id(2L)
                        .name("exe")
                        .description("Executable file")
                        .checked(true)
                        .build());

        when(fixedExtensionRepository.findAll()).thenReturn(fixedExtensions);

        List<FixedExtensionDto> result = extensionService.getAllFixedExtensions();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo("bat");
        verify(fixedExtensionRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("고정 확장자 상태 업데이트 - 성공")
    void updateFixedExtension_success() {
        when(fixedExtensionRepository.findById(1L)).thenReturn(Optional.of(fixedBat));
        when(fixedExtensionRepository.save(any(FixedExtension.class))).thenReturn(fixedBat);

        FixedExtensionDto updated = extensionService.updateFixedExtension(1L, true);

        assertThat(updated.isChecked()).isTrue();
        verify(fixedExtensionRepository, times(1)).findById(1L);
        verify(fixedExtensionRepository, times(1)).save(fixedBat);
    }

    @Test
    @DisplayName("고정 확장자 상태 업데이트 - 존재하지 않는 확장자")
    void updateFixedExtension_notFound() {
        when(fixedExtensionRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> extensionService.updateFixedExtension(99L, true))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("고정 확장자를 찾을 수 없습니다.");
        verify(fixedExtensionRepository, times(1)).findById(99L);
        verify(fixedExtensionRepository, never()).save(any(FixedExtension.class));
    }

    // New tests for addCustomExtensions
    @Test
    @DisplayName("커스텀 확장자 여러 개 추가 - 성공")
    void addCustomExtensions_success() {
        List<CustomExtensionNameDto> names = createCustomExtensionNameDtos("newext", "another");
        when(customExtensionRepository.count()).thenReturn(0L);
        when(fixedExtensionRepository.findByName(anyString())).thenReturn(Optional.empty());
        when(customExtensionRepository.existsByName(anyString())).thenReturn(false);
        when(customExtensionRepository.save(any(CustomExtension.class)))
                .thenReturn(CustomExtension.builder().id(2L).name("newext").build())
                .thenReturn(CustomExtension.builder().id(3L).name("another").build());

        CustomExtensionBatchResultDto result = extensionService.addCustomExtensions(names);

        assertThat(result.getSuccessCount()).isEqualTo(2);
        assertThat(result.getFailedCount()).isEqualTo(0);
        assertThat(result.getSuccessfulAdditions()).hasSize(2);
        assertThat(result.getSuccessfulAdditions().get(0).getName()).isEqualTo("newext");
        assertThat(result.getSuccessfulAdditions().get(1).getName()).isEqualTo("another");
        verify(customExtensionRepository, times(1)).count();
        verify(fixedExtensionRepository, times(2)).findByName(anyString());
        verify(customExtensionRepository, times(2)).existsByName(anyString());
        verify(customExtensionRepository, times(2)).save(any(CustomExtension.class));
    }

    @Test
    @DisplayName("커스텀 확장자 여러 개 추가 - 일부 중복 및 고정 확장자명 포함")
    void addCustomExtensions_partialSuccess_withDuplicatesAndFixed() {
        List<CustomExtensionNameDto> names = createCustomExtensionNameDtos("newext", "pep", "bat", "yetanother");
        when(customExtensionRepository.count()).thenReturn(0L); // Initial count

        // "newext"
        when(fixedExtensionRepository.findByName("newext")).thenReturn(Optional.empty());
        when(customExtensionRepository.existsByName("newext")).thenReturn(false);
        when(customExtensionRepository.save(any(CustomExtension.class)))
                .thenReturn(CustomExtension.builder().id(2L).name("newext").build())
                .thenReturn(CustomExtension.builder().id(3L).name("yetanother").build());


        // "pep" (duplicate custom)
        when(fixedExtensionRepository.findByName("pep")).thenReturn(Optional.empty());
        when(customExtensionRepository.existsByName("pep")).thenReturn(true);

        // "bat" (fixed extension)
        when(fixedExtensionRepository.findByName("bat")).thenReturn(Optional.of(fixedBat));

        // "yetanother" (already stubbed save for "yetanother" above, this is just to ensure it's not double-stubbed)
        when(fixedExtensionRepository.findByName("yetanother")).thenReturn(Optional.empty());
        when(customExtensionRepository.existsByName("yetanother")).thenReturn(false);

        CustomExtensionBatchResultDto result = extensionService.addCustomExtensions(names);

        assertThat(result.getSuccessCount()).isEqualTo(2);
        assertThat(result.getFailedCount()).isEqualTo(2);
        assertThat(result.getSuccessfulAdditions()).hasSize(2);
        assertThat(result.getSuccessfulAdditions().stream().map(CustomExtensionDto::getName)).containsExactlyInAnyOrder("newext", "yetanother");
        
        Map<String, String> expectedFailures = new HashMap<>();
        expectedFailures.put("pep", "이미 존재하는 커스텀 확장자입니다.");
        expectedFailures.put("bat", "고정 확장자와 중복됩니다.");
        assertThat(result.getFailedExtensionsWithReasons()).containsExactlyInAnyOrderEntriesOf(expectedFailures);

        verify(customExtensionRepository, times(1)).count();
        verify(fixedExtensionRepository, times(4)).findByName(anyString());
        verify(customExtensionRepository, times(3)).existsByName(anyString());
        verify(customExtensionRepository, times(2)).save(any(CustomExtension.class));
    }

    @Test
    @DisplayName("커스텀 확장자 여러 개 추가 - 200개 제한 초과 (중간에 제한 도달)")
    void addCustomExtensions_limitExceeded_midBatch() {
        List<CustomExtensionNameDto> names = createCustomExtensionNameDtos("ext1", "ext2", "ext3");
        when(customExtensionRepository.count()).thenReturn(199L); // Already 199 exists

        // "ext1" will be saved
        when(fixedExtensionRepository.findByName("ext1")).thenReturn(Optional.empty());
        when(customExtensionRepository.existsByName("ext1")).thenReturn(false);
        when(customExtensionRepository.save(any(CustomExtension.class)))
                .thenReturn(CustomExtension.builder().id(2L).name("ext1").build());

        // "ext2" will hit the limit (199 + 1 = 200)
        // No explicit stub for findByName("ext2") and existsByName("ext2") because they won't be called.


        CustomExtensionBatchResultDto result = extensionService.addCustomExtensions(names);

        assertThat(result.getSuccessCount()).isEqualTo(1);
        assertThat(result.getFailedCount()).isEqualTo(2);
        assertThat(result.getSuccessfulAdditions()).hasSize(1);
        assertThat(result.getSuccessfulAdditions().get(0).getName()).isEqualTo("ext1");
        assertThat(result.getFailedExtensionsWithReasons()).containsEntry("ext2", "등록 개수(200개)를 초과하였습니다.");
        assertThat(result.getFailedExtensionsWithReasons()).containsEntry("ext3", "등록 개수(200개)를 초과하였습니다.");

        verify(customExtensionRepository, times(1)).count();
        verify(fixedExtensionRepository, times(1)).findByName(anyString()); // Only for ext1
        verify(customExtensionRepository, times(1)).existsByName(anyString()); // Only for ext1
        verify(fixedExtensionRepository, never()).findByName("ext2"); // Should not be called for ext2
        verify(customExtensionRepository, never()).existsByName("ext2"); // Should not be called for ext2
        verify(fixedExtensionRepository, never()).findByName("ext3"); // Should not be called for ext3
        verify(customExtensionRepository, never()).existsByName("ext3"); // Should not be called for ext3
        verify(customExtensionRepository, times(1)).save(any(CustomExtension.class)); // only ext1 saved
    }

    @Test
    @DisplayName("커스텀 확장자 여러 개 추가 - 요청 내 중복된 확장자")
    void addCustomExtensions_duplicatesInBatch() {
        List<CustomExtensionNameDto> names = createCustomExtensionNameDtos("newext", "test", "TEST", "another");
        when(customExtensionRepository.count()).thenReturn(0L);
        when(fixedExtensionRepository.findByName(anyString())).thenReturn(Optional.empty());
        when(customExtensionRepository.existsByName(anyString())).thenReturn(false);
        when(customExtensionRepository.save(any(CustomExtension.class)))
                .thenReturn(CustomExtension.builder().id(2L).name("newext").build())
                .thenReturn(CustomExtension.builder().id(3L).name("test").build())
                .thenReturn(CustomExtension.builder().id(4L).name("another").build());

        CustomExtensionBatchResultDto result = extensionService.addCustomExtensions(names);

        assertThat(result.getSuccessCount()).isEqualTo(3); // newext, test, another
        assertThat(result.getFailedCount()).isEqualTo(1); // TEST (duplicate of test)
        assertThat(result.getSuccessfulAdditions()).hasSize(3);
        assertThat(result.getSuccessfulAdditions().stream().map(CustomExtensionDto::getName)).containsExactlyInAnyOrder("newext", "test", "another");
        assertThat(result.getFailedExtensionsWithReasons()).containsEntry("test", "요청 내 중복된 확장자입니다."); // "TEST" is cleaned to "test"

        verify(customExtensionRepository, times(1)).count();
        verify(fixedExtensionRepository, times(3)).findByName(anyString());
        verify(customExtensionRepository, times(3)).existsByName(anyString());
        verify(customExtensionRepository, times(3)).save(any(CustomExtension.class));
    }

    @Test
    @DisplayName("커스텀 확장자 전체 조회 - 성공")
    void getAllCustomExtensions_success() {
        List<CustomExtension> customExtensions = Arrays.asList(customPep,
                CustomExtension.builder().id(2L).name("svg").build());
        when(customExtensionRepository.findAll()).thenReturn(customExtensions);

        List<CustomExtensionDto> result = extensionService.getAllCustomExtensions();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo("pep");
        verify(customExtensionRepository, times(1)).findAll();
    }
}