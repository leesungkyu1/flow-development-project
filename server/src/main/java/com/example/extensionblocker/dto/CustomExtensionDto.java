package com.example.extensionblocker.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomExtensionDto {
    private Long id;
    private String name;

    public static CustomExtensionDto fromEntity(com.example.extensionblocker.domain.CustomExtension entity) {
        return CustomExtensionDto.builder()
                .id(entity.getId())
                .name(entity.getName())
                .build();
    }
}
