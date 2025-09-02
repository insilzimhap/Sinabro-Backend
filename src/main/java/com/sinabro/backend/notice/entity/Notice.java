package com.sinabro.backend.notice.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * 공지 도메인 엔티티 (공용)
 * - admin/app 양쪽에서 사용
 */

@Entity
@Table(name = "notice")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notice {
    // 공지 ID (PK)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notice_id")
    private Long id;

    // 제목
    @Column(name = "title", length = 255, nullable = false)
    private String title;

    // 내용 (TEXT)
    @Lob
    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    // 유형 (예: 이벤트, 점검 등)
    @Column(name = "notice_type", length = 50)
    private String noticeType;

    // 조회수 (기본 0) — SQL: BIGINT NOT NULL DEFAULT 0
    // - DB default가 0이지만, JPA persist 시 명확성을 위해 필드 기본값도 0L로 둠
    @Builder.Default
    @Column(name = "view_count", nullable = false)
    private Long viewCount = 0L;

    // 작성일 (DEFAULT CURRENT_TIMESTAMP)
    @Column(name = "notice_created_date", nullable = false,
            columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdDate;

    // 수정일 (NULL 허용, DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP)
    @Column(name = "notice_updated_date",
            columnDefinition = "TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private LocalDateTime updatedDate;

    // 최초 등록 시 자동 생성
    @PrePersist
    public void prePersist() {
        this.createdDate = LocalDateTime.now();
    }

    // 수정 시 자동 업데이트
    @PreUpdate
    public void preUpdate() {
        this.updatedDate = LocalDateTime.now();
    }
}
