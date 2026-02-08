package com.example.extensionblocker.controller;

import com.example.extensionblocker.common.ApiResponse;
import com.example.extensionblocker.domain.CustomExtension;
import com.example.extensionblocker.domain.FixedExtension;
import com.example.extensionblocker.dto.CustomExtensionDto;
import com.example.extensionblocker.dto.FixedExtensionDto;
import com.example.extensionblocker.service.ExtensionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 파일 확장자 차단 관련 API 요청을 처리하는 REST 컨트롤러.
 * 고정 확장자 및 커스텀 확장자 관리 기능을 제공합니다.
 */
@RestController
@RequestMapping("/api")
public class ExtensionController {

    private final ExtensionService extensionService;

    /**
     * ExtensionController의 생성자.
     * ExtensionService를 주입받습니다.
     *
     * @param extensionService 확장자 관련 비즈니스 로직을 처리하는 서비스
     */
    public ExtensionController(ExtensionService extensionService) {
        this.extensionService = extensionService;
    }

    /**
     * 모든 고정 확장자 목록을 조회합니다.
     *
     * @return 고정 확장자 목록을 포함하는 ApiResponse
     */
    @GetMapping("/fixed-extensions")
    public ApiResponse<List<FixedExtensionDto>> getFixedExtensions() {
        return ApiResponse.success(extensionService.getAllFixedExtensions());
    }

    /**
     * 특정 고정 확장자의 상태(체크 여부)를 업데이트합니다.
     *
     * @param id     업데이트할 고정 확장자의 ID
     * @param updates 업데이트할 상태 정보 (예: {"checked": true})
     * @return 업데이트된 고정 확장자를 포함하는 ResponseEntity
     */
    @PutMapping("/fixed-extensions/{id}")
    public ResponseEntity<ApiResponse<FixedExtensionDto>> updateFixedExtensionStatus(@PathVariable Long id, @RequestBody Map<String, Boolean> updates) {
        Boolean checked = updates.get("checked");
        if (checked == null) {
            return new ResponseEntity<>(ApiResponse.error("BAD_REQUEST", "Checked status missing."), HttpStatus.BAD_REQUEST);
        }
        FixedExtensionDto updatedExtension = extensionService.updateFixedExtension(id, checked);
        return new ResponseEntity<>(ApiResponse.success(updatedExtension), HttpStatus.OK);
    }

    /**
     * 모든 커스텀 확장자 목록을 조회합니다.
     *
     * @return 커스텀 확장자 목록을 포함하는 ApiResponse
     */
    @GetMapping("/custom-extensions")
    public ApiResponse<List<CustomExtensionDto>> getCustomExtensions() {
        return ApiResponse.success(extensionService.getAllCustomExtensions());
    }

    /**
     * 새로운 커스텀 확장자를 추가합니다.
     * 확장자 이름은 비어 있을 수 없으며, 중복되거나 200개 제한을 초과할 수 없습니다.
     *
     * @param payload 추가할 확장자 이름을 포함하는 맵 (예: {"name": "xyz"})
     * @return 새로 추가된 커스텀 확장자를 포함하는 ResponseEntity
     */
    @PostMapping("/custom-extensions")
    public ResponseEntity<ApiResponse<CustomExtensionDto>> addCustomExtension(@RequestBody Map<String, String> payload) {
        String name = payload.get("name");
        if (name == null || name.isBlank()) {
            return new ResponseEntity<>(ApiResponse.error("BAD_REQUEST", "Extension name cannot be empty."), HttpStatus.BAD_REQUEST);
        }
        CustomExtensionDto newExtension = extensionService.addCustomExtension(name);
        return new ResponseEntity<>(ApiResponse.success(newExtension), HttpStatus.CREATED);
    }

    /**
     * 특정 커스텀 확장자를 삭제합니다.
     *
     * @param id 삭제할 커스텀 확장자의 ID
     * @return 삭제 성공 시 응답 본문이 없는 ResponseEntity
     */
    @DeleteMapping("/custom-extensions/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCustomExtension(@PathVariable Long id) {
        extensionService.removeCustomExtension(id);
        return new ResponseEntity<>(ApiResponse.success(), HttpStatus.OK);
    }
}
