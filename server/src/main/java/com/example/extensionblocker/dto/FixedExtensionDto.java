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
public class FixedExtensionDto {
    private Long id;
    private String name;
    private String description;
    private boolean checked;

    public static FixedExtensionDto fromEntity(com.example.extensionblocker.domain.FixedExtension entity) {
        return FixedExtensionDto.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .checked(entity.isChecked())
                .build();
    }
}
