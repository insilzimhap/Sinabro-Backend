package com.sinabro.backend.progress.entity;

import com.sinabro.backend.user.entity.Child;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.sql.Timestamp;

@Entity
@Table(name = "progress")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class Progress {

    // child_id = PK + FK(Child.child_id)
    @Id
    @Column(name = "child_id", length = 255, nullable = false)
    private String childId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "child_id", referencedColumnName = "child_id", insertable = false, updatable = false)
    @OnDelete(action = OnDeleteAction.CASCADE) // 자녀 삭제 시 진행도 삭제
    private Child child;

    // 최고/최근 단계 (각 카테고리별)
    @Column(name = "best_ws_stage_id", length = 10)
    private String bestWsStageId;

    @Column(name = "best_ls_stage_id", length = 10)
    private String bestLsStageId;

    @Column(name = "last_ws_stage_id", length = 10)
    private String lastWsStageId;

    @Column(name = "last_ls_stage_id", length = 10)
    private String lastLsStageId;

    // 게임 쪽은 일단 보류값, 스키마는 유지
    @Column(name = "best_wg_stage_id", length = 10)
    private String bestWgStageId;

    @Column(name = "best_lg_stage_id", length = 10)
    private String bestLgStageId;

    @Column(name = "last_wg_stage_id", length = 10)
    private String lastWgStageId;

    @Column(name = "last_lg_stage_id", length = 10)
    private String lastLgStageId;

    // 마지막 학습 시간
    @CreationTimestamp
    @Column(name = "last_played", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private Timestamp lastPlayed;
}
