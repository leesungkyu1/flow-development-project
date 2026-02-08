package com.example.extensionblocker.service;

import com.example.extensionblocker.domain.CustomExtension;
import com.example.extensionblocker.domain.FixedExtension;
import com.example.extensionblocker.dto.CustomExtensionDto;
import com.example.extensionblocker.dto.FixedExtensionDto;
import com.example.extensionblocker.repository.CustomExtensionRepository;
import com.example.extensionblocker.repository.FixedExtensionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

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

    @Test
    @DisplayName("커스텀 확장자 추가 - 성공")
    void addCustomExtension_success() {
        when(customExtensionRepository.count()).thenReturn(0L);
        when(fixedExtensionRepository.findByName(anyString())).thenReturn(Optional.empty());
        when(customExtensionRepository.existsByName(anyString())).thenReturn(false);
        when(customExtensionRepository.save(any(CustomExtension.class))).thenReturn(customPep);

        CustomExtensionDto result = extensionService.addCustomExtension("pep");

        assertThat(result.getName()).isEqualTo("pep");
        verify(customExtensionRepository, times(1)).count();
        verify(fixedExtensionRepository, times(1)).findByName("pep");
        verify(customExtensionRepository, times(1)).existsByName("pep");
        verify(customExtensionRepository, times(1)).save(any(CustomExtension.class));
    }

    @Test
    @DisplayName("커스텀 확장자 추가 - 입력값 앞뒤 공백 제거 및 소문자 변환")
    void addCustomExtension_trimAndLowercase() {
        when(customExtensionRepository.count()).thenReturn(0L);
        when(fixedExtensionRepository.findByName(anyString())).thenReturn(Optional.empty());
        when(customExtensionRepository.existsByName(anyString())).thenReturn(false);
        when(customExtensionRepository.save(any(CustomExtension.class))).thenReturn(CustomExtension.builder().id(1L).name("new").build());

        CustomExtensionDto result = extensionService.addCustomExtension(" NEW ");

        assertThat(result.getName()).isEqualTo("new");
        verify(fixedExtensionRepository, times(1)).findByName("new");
        verify(customExtensionRepository, times(1)).existsByName("new");
        verify(customExtensionRepository, times(1)).save(any(CustomExtension.class));
    }

    @Test
    @DisplayName("커스텀 확장자 추가 - 이름이 비어있음")
    void addCustomExtension_emptyName() {
        assertThatThrownBy(() -> extensionService.addCustomExtension(" "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("커스텀 확장자의 글자수가 1글자에서 20자까지 입력가능합니다.");
        verify(customExtensionRepository, never()).count();
        verify(fixedExtensionRepository, never()).findByName(anyString());
        verify(customExtensionRepository, never()).existsByName(anyString());
        verify(customExtensionRepository, never()).save(any(CustomExtension.class));
    }

    @Test
    @DisplayName("커스텀 확장자 추가 - 이름이 20자 초과")
    void addCustomExtension_nameTooLong() {
        assertThatThrownBy(() -> extensionService.addCustomExtension("abcdefghijklmnopqrstuvwxyz"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("커스텀 확장자의 글자수가 1글자에서 20자까지 입력가능합니다.");
        verify(customExtensionRepository, never()).count();
        verify(fixedExtensionRepository, never()).findByName(anyString());
        verify(customExtensionRepository, never()).existsByName(anyString());
        verify(customExtensionRepository, never()).save(any(CustomExtension.class));
    }

    @Test
    @DisplayName("커스텀 확장자 추가 - 200개 제한 초과")
    void addCustomExtension_limitExceeded() {
        when(customExtensionRepository.count()).thenReturn(200L);

        assertThatThrownBy(() -> extensionService.addCustomExtension("new"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("커스텀 확장자의 등록 개수가 200개를 초과하였습니다.");
        verify(customExtensionRepository, times(1)).count();
        verify(fixedExtensionRepository, never()).findByName(anyString());
        verify(customExtensionRepository, never()).existsByName(anyString());
        verify(customExtensionRepository, never()).save(any(CustomExtension.class));
    }

    @Test
    @DisplayName("커스텀 확장자 추가 - 이미 존재하는 고정 확장자와 중복")
    void addCustomExtension_fixedExtensionExists() {
        when(customExtensionRepository.count()).thenReturn(0L);
        when(fixedExtensionRepository.findByName("bat")).thenReturn(Optional.of(fixedBat));

        assertThatThrownBy(() -> extensionService.addCustomExtension("bat"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("확장자 'bat' 는 중복되었습니다.");
        verify(customExtensionRepository, times(1)).count();
        verify(fixedExtensionRepository, times(1)).findByName("bat");
        verify(customExtensionRepository, never()).existsByName(anyString());
        verify(customExtensionRepository, never()).save(any(CustomExtension.class));
    }

    @Test
    @DisplayName("커스텀 확장자 추가 - 이미 존재하는 커스텀 확장자와 중복")
    void addCustomExtension_customExtensionExists() {
        when(customExtensionRepository.count()).thenReturn(0L);
        when(fixedExtensionRepository.findByName("pep")).thenReturn(Optional.empty());
        when(customExtensionRepository.existsByName("pep")).thenReturn(true);

        assertThatThrownBy(() -> extensionService.addCustomExtension("pep"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("확장자 'pep' 는 중복되었습니다.");
        verify(customExtensionRepository, times(1)).count();
        verify(fixedExtensionRepository, times(1)).findByName("pep");
        verify(customExtensionRepository, times(1)).existsByName("pep");
        verify(customExtensionRepository, never()).save(any(CustomExtension.class));
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
