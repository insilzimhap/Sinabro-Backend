package com.sinabro.backend.user.child.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "characters") // MySQL 테이블명: characters
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CharacterInfo {

    @Id
    @Column(name = "character_id", length = 50, nullable = false)
    private String characterId;

    @Column(name = "character_name", length = 255, nullable = false)
    private String characterName;

    @Column(name = "image_url", length = 255)
    private String imageUrl;
}
