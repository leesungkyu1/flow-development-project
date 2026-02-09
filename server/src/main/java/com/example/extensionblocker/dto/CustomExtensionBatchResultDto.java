package com.example.extensionblocker.dto;

import lombok.*;

import java.util.List;
import java.util.Map;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomExtensionBatchResultDto {
    private List<CustomExtensionDto> successfulAdditions;
    private int successCount;
    private int failedCount;
    private Map<String, String> failedExtensionsWithReasons;
}
