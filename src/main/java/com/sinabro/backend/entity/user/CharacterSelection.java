package com.sinabro.backend.entity.user;

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
    private Timestamp createdAt;
}
