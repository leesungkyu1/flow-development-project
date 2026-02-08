package com.example.extensionblocker.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

/**
 * 사용자 정의(커스텀) 확장자를 나타내는 엔티티.
 * 사용자가 직접 추가하고 관리할 수 있는 확장자 목록을 저장합니다.
 */
@Entity
@Table(name = "custom_extension", uniqueConstraints = {
        @UniqueConstraint(columnNames = "name")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomExtension {

    /**
     * 사용자 정의 확장자의 고유 식별자.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 사용자 정의 확장자의 이름 (예: "xyz", "abc").
     * 데이터베이스에서 고유해야 하며 최대 20자까지 가능합니다.
     */
    @Column(nullable = false, length = 20)
    private String name;
}
