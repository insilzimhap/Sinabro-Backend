package com.sinabro.backend.entity.user;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "characters")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Character {
    @Id
    @Column(name = "character_id")
    private String characterId;

    @Column(name = "character_name")
    private String characterName;

    @Column(name = "image_url")
    private String imageUrl;
}
