package com.sinabro.backend.user.entity;


import jakarta.persistence.*;
import lombok.*;
import java.sql.Timestamp;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "character_selection")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CharacterSelection {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "child_id")
    private String childId;

    @Column(name = "character_id")
    private String characterId;

    @CreationTimestamp
    @Column(name = "created_at")
    private Timestamp createdAt;

    // 읽기 전용 연관관계(탐색용 필드) — DB 컬럼 추가 아님!
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "character_id",
            referencedColumnName = "character_id",
            insertable = false,
            updatable = false
    )
    private CharacterInfo character; // selection.getCharacter().getCharacterName() 등으로 읽기
}
