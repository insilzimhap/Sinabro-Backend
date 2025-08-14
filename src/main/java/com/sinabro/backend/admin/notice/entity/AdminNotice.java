package com.sinabro.backend.admin.notice.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notice")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminNotice {
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
