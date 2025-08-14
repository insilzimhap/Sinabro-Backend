package com.sinabro.backend.admin.inquiry.entity;

import com.sinabro.backend.admin.user.entity.AdminUser;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "inquiry",
        indexes = {
                @Index(name = "idx_inquiry_created_at", columnList = "inquiry_created_at"),
                @Index(name = "idx_inquiry_status", columnList = "inquiry_status"),
                @Index(name = "idx_inquiry_user_id", columnList = "user_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"parent", "replies"})
public class AdminInquiry {
    // 문의 ID (PK)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "inquiry_id")
    private Long id;

    // 문의 제목
    @Column(name = "inquiry_title", length = 255, nullable = false)
    private String title;

    // 문의 내용 (TEXT)
    @Lob
    @Column(name = "inquiry_content", nullable = false, columnDefinition = "TEXT")
    private String content;

    // 작성일 (DEFAULT CURRENT_TIMESTAMP)
    @Column(name = "inquiry_created_at", nullable = false,
            columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;

    // 수정일 (NULL 허용, DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP)
    @Column(name = "inquiry_update_at",
            columnDefinition = "TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private LocalDateTime updatedAt;

    // 문의 작성자 (부모) FK → user.user_id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "user_id", nullable = false)
    private AdminUser parent;

    // 답변 상태 (ENUM('답변 전','답변 완료')를 String으로 매핑)
    @Builder.Default
    @Column(name = "inquiry_status", length = 20, nullable = false,
            columnDefinition = "ENUM('답변 전','답변 완료') DEFAULT '답변 전'")
    private String status = "답변 전";

    // 문의 ↔ 답변 (1:N)
    @OneToMany(mappedBy = "inquiry", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<AdminInquiryReply> replies = new ArrayList<>();

    // create, update preupdate
    // ====== Lifecycle ======
    @PrePersist
    public void prePersist() {
        if (this.createdAt == null) this.createdAt = LocalDateTime.now();
        if (this.status == null || this.status.isBlank()) this.status = "답변 전";
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // ====== 편의 메서드 ======
    public void addReply(AdminInquiryReply reply) {
        replies.add(reply);
        reply.setInquiry(this);
    }

    public void removeReply(AdminInquiryReply reply) {
        replies.remove(reply);
        reply.setInquiry(null);
    }

}
