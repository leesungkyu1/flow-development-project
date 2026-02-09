package com.example.extensionblocker.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CustomExtensionRequestListDto {

    @Valid
    @Size(max = 200, message = "한 번에 최대 200개의 확장자만 추가할 수 있습니다.")
    private List<CustomExtensionNameDto> names;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CustomExtensionNameDto {

        @NotEmpty(message = "확장자 이름은 비어 있을 수 없습니다.")
        @Size(min = 1, max = 20, message = "확장자 이름은 1자에서 20자 사이여야 합니다.")
        @Pattern(regexp = "^[a-zA-Z0-9]*$", message = "확장자 이름은 영문자와 숫자만 포함할 수 있습니다.")
        private String name;
    }
}
