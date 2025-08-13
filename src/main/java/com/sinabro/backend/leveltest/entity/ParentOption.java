package com.sinabro.backend.leveltest.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class ParentOption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String optionText;

    @ManyToOne
    @JoinColumn(name = "parent_question_id")
    private ParentQuestion parentQuestion;
}
