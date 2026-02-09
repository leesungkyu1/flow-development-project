package com.example.extensionblocker.controller;

import com.example.extensionblocker.domain.CustomExtension;
import com.example.extensionblocker.domain.FixedExtension;
import com.example.extensionblocker.repository.CustomExtensionRepository;
import com.example.extensionblocker.repository.FixedExtensionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc; // Add back this import
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.example.extensionblocker.dto.CustomExtensionRequestListDto;
import com.example.extensionblocker.dto.CustomExtensionRequestListDto.CustomExtensionNameDto;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ExtensionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private FixedExtensionRepository fixedExtensionRepository;

    @Autowired
    private CustomExtensionRepository customExtensionRepository;

    private FixedExtension batFixedExtension;
    private CustomExtension customPepExtension;

    // Helper method to create CustomExtensionRequestListDto
    private CustomExtensionRequestListDto createCustomExtensionRequestListDto(String... names) {
        List<CustomExtensionNameDto> nameDtos = Arrays.stream(names)
                .map(CustomExtensionNameDto::new)
                .collect(Collectors.toList());
        return new CustomExtensionRequestListDto(nameDtos);
    }

    @BeforeEach
    void setUp() {
        customExtensionRepository.deleteAll();

        batFixedExtension = fixedExtensionRepository.findByName("bat")
                .orElseGet(() -> fixedExtensionRepository.save(
                        FixedExtension.builder()
                                .name("bat")
                                .description("Batch file for Windows command line execution.")
                                .checked(false)
                                .build()));
        fixedExtensionRepository.flush();

        customPepExtension = customExtensionRepository.save(CustomExtension.builder().name("pep").build());
        customExtensionRepository.flush();
    }

    // Fixed Extensions Tests
    @Test
    @DisplayName("GET /api/fixed-extensions - 고정 확장자 전체 조회 성공")
    void getFixedExtensions_success() throws Exception {

        mockMvc.perform(get("/api/fixed-extensions"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(fixedExtensionRepository.count()));
    }

    @Test
    @DisplayName("PUT /api/fixed-extensions/{id} - 고정 확장자 상태 업데이트 성공")
    void updateFixedExtensionStatus_success() throws Exception {
        Map<String, Boolean> updates = new HashMap<>();
        updates.put("checked", true);

        mockMvc.perform(put("/api/fixed-extensions/{id}", batFixedExtension.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updates)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(batFixedExtension.getId()))
                .andExpect(jsonPath("$.data.name").value("bat"))
                .andExpect(jsonPath("$.data.checked").value(true));

        FixedExtension updatedExtension = fixedExtensionRepository.findById(batFixedExtension.getId()).orElseThrow();
        assertThat(updatedExtension.isChecked()).isTrue();
    }

    @Test
    @DisplayName("PUT /api/fixed-extensions/{id} - 유효하지 않은 요청 바디 시 400 반환")
    void updateFixedExtensionStatus_badRequest() throws Exception {
        Map<String, String> updates = new HashMap<>();
        updates.put("invalidField", "true"); // 'checked' key is missing

        mockMvc.perform(put("/api/fixed-extensions/{id}", batFixedExtension.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updates)))
                .andExpect(status().isBadRequest());
    }

    // Custom Extensions Tests
    @Test
    @DisplayName("GET /api/custom-extensions - 커스텀 확장자 전체 조회 성공")
    void getCustomExtensions_success() throws Exception {
        mockMvc.perform(get("/api/custom-extensions"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(customExtensionRepository.count()));
    }

    @Test
    @DisplayName("POST /api/custom-extensions - 커스텀 확장자 추가 성공 (201 Created)")
    void addCustomExtensions_success() throws Exception {
        CustomExtensionRequestListDto requestDto = createCustomExtensionRequestListDto("newext", "another");

        mockMvc.perform(post("/api/custom-extensions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.successCount").value(2))
                .andExpect(jsonPath("$.data.failedCount").value(0))
                .andExpect(jsonPath("$.data.successfulAdditions").isArray())
                .andExpect(jsonPath("$.data.successfulAdditions.length()").value(2))
                .andExpect(jsonPath("$.data.successfulAdditions[0].name").value("newext"))
                .andExpect(jsonPath("$.data.successfulAdditions[1].name").value("another"));

        assertThat(customExtensionRepository.findByName("newext")).isPresent();
        assertThat(customExtensionRepository.findByName("another")).isPresent();
    }

    @Test
    @DisplayName("POST /api/custom-extensions - 이미 존재하는 고정 확장자와 중복 시 CustomExtensionBatchResultDto에 실패 기록 (201 Created)")
    void addCustomExtensions_fixedExtensionConflict() throws Exception {
        CustomExtensionRequestListDto requestDto = createCustomExtensionRequestListDto("bat"); // 'bat' is a fixed extension

        mockMvc.perform(post("/api/custom-extensions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated()) // Request itself is processed successfully
                .andExpect(jsonPath("$.data.successCount").value(0))
                .andExpect(jsonPath("$.data.failedCount").value(1))
                .andExpect(jsonPath("$.data.failedExtensionsWithReasons.bat").value("고정 확장자와 중복됩니다."));
    }


    @Test
    @DisplayName("POST /api/custom-extensions - 이미 존재하는 커스텀 확장자와 중복 시 CustomExtensionBatchResultDto에 실패 기록 (201 Created)")
    void addCustomExtensions_customExtensionConflict() throws Exception {
        CustomExtensionRequestListDto requestDto = createCustomExtensionRequestListDto(customPepExtension.getName());

        mockMvc.perform(post("/api/custom-extensions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated()) // Request itself is processed successfully
                .andExpect(jsonPath("$.data.successCount").value(0))
                .andExpect(jsonPath("$.data.failedCount").value(1))
                .andExpect(jsonPath("$.data.failedExtensionsWithReasons.pep").value("이미 존재하는 커스텀 확장자입니다."));
    }


    @Test
    @DisplayName("POST /api/custom-extensions - 이름이 비어있을 경우 CustomExtensionBatchResultDto에 실패 기록 (201 Created)")
    void addCustomExtensions_emptyName_badRequest() throws Exception {
        CustomExtensionRequestListDto requestDto = createCustomExtensionRequestListDto(""); // Empty name

        mockMvc.perform(post("/api/custom-extensions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.successCount").value(0))
                .andExpect(jsonPath("$.data.failedCount").value(1))
                .andExpect(jsonPath("$.data.failedExtensionsWithReasons['names[0].name']").value("확장자 이름은 1자에서 20자 사이여야 합니다."));
    }

    @Test
    @DisplayName("POST /api/custom-extensions - 이름이 20자 초과일 경우 CustomExtensionBatchResultDto에 실패 기록 (201 Created)")
    void addCustomExtensions_nameTooLong_badRequest() throws Exception {
        CustomExtensionRequestListDto requestDto = createCustomExtensionRequestListDto("abcdefghijklmnopqrstuvwx"); // 24 chars

        mockMvc.perform(post("/api/custom-extensions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.successCount").value(0))
                .andExpect(jsonPath("$.data.failedCount").value(1));
    }


    @Test
    @DisplayName("POST /api/custom-extensions - 200개 제한 초과 시 CustomExtensionBatchResultDto에 실패 기록 (201 Created)")
    void addCustomExtensions_limitExceeded_badRequest() throws Exception {
        // Fill up to 200 custom extensions
        for (int i = 0; i < 200; i++) {
            customExtensionRepository.save(CustomExtension.builder().name("test" + i).build());
        }
        customExtensionRepository.flush(); // Ensure data is written to DB

        CustomExtensionRequestListDto requestDto = createCustomExtensionRequestListDto("overlimit");

        mockMvc.perform(post("/api/custom-extensions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated()) // Request itself is processed successfully
                .andExpect(jsonPath("$.data.successCount").value(0))
                .andExpect(jsonPath("$.data.failedCount").value(1))
                .andExpect(jsonPath("$.data.failedExtensionsWithReasons.overlimit").value("등록 개수(200개)를 초과하였습니다."));
    }


    @Test
    @DisplayName("DELETE /api/custom-extensions/{id} - 커스텀 확장자 삭제 성공 (200)")
    void deleteCustomExtension_success() throws Exception {
        mockMvc.perform(delete("/api/custom-extensions/{id}", customPepExtension.getId()))
                .andExpect(status().isOk());

        assertThat(customExtensionRepository.findById(customPepExtension.getId())).isNotPresent();
    }

    @Test
    @DisplayName("DELETE /api/custom-extensions/{id} - 존재하지 않는 커스텀 확장자 삭제 시 400 반환")
    void deleteCustomExtension_notFound() throws Exception {
        mockMvc.perform(delete("/api/custom-extensions/{id}", 9999L))
                .andExpect(status().isBadRequest());
    }
}
