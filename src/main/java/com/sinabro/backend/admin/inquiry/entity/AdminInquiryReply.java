package com.sinabro.backend.admin.inquiry.entity;

import com.sinabro.backend.admin.user.entity.AdminUser;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "inquiry_reply",
        indexes = {
                @Index(name = "idx_reply_inquiry_id", columnList = "inquiry_id"),
                @Index(name = "idx_reply_user_id", columnList = "user_id"),
                @Index(name = "idx_reply_created_date", columnList = "reply_created_date")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"inquiry", "user"})
public class AdminInquiryReply {
    // 답변 ID (PK)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reply_id")
    private Long id;

    // 답변 내용 (TEXT)
    @Lob
    @Column(name = "reply_content", nullable = false, columnDefinition = "TEXT")
    private String content;

    // 작성일 (DEFAULT CURRENT_TIMESTAMP)
    @Column(name = "reply_created_date", nullable = false,
            columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdDate;

    // 수정일 (NULL 허용, DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP)
    @Column(name = "reply_update_date",
            columnDefinition = "TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private LocalDateTime updateDate;

    // 어떤 문의에 대한 답변인지 FK → inquiry.inquiry_id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inquiry_id", referencedColumnName = "inquiry_id", nullable = false)
    private AdminInquiry inquiry;

    // 답변 작성자 (부모 ID) FK → user.user_id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "user_id", nullable = false)
    private AdminUser user;

    //create, update preupdate
    // ====== Lifecycle ======
    @PrePersist
    public void prePersist() {
        if (this.createdDate == null) this.createdDate = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updateDate = LocalDateTime.now();
    }
}
