package com.example.extensionblocker.domain;

import jakarta.persistence.*;
import lombok.*;

/**
 * 고정(사전 정의된) 확장자를 나타내는 엔티티.
 * 파일 업로드 시 차단하거나 허용할 수 있는 미리 정의된 확장자 목록을 관리합니다.
 */
@Entity
@Table(name = "fixed_extension", uniqueConstraints = {
        @UniqueConstraint(columnNames = "name")
})
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Setter
public class FixedExtension {

    /**
     * 고정 확장자의 고유 식별자.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 고정 확장자의 이름 (예: "exe", "bat").
     * 데이터베이스에서 고유해야 합니다.
     */
    @Column(nullable = false, length = 10)
    private String name;

    /**
     * 고정 확장자에 대한 설명.
     */
    @Column(nullable = false, length = 255)
    private String description;

    /**
     * 파일의 매직 넘버 (파일 형식 식별을 위한 바이너리 시그니처).
     * 확장자 변조 방지에 활용될 수 있습니다.
     */
    @Column
    private String magicNumber;

    /**
     * 해당 고정 확장자가 현재 차단되어 있는지 여부.
     */
    @Column(nullable = false)
    private boolean checked;
}
