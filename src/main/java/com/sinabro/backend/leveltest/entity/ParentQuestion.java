package com.sinabro.backend.leveltest.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class ParentQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int questionOrder;
    private String questionText;

    @OneToMany(mappedBy = "parentQuestion", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ParentOption> options = new ArrayList<>();

    public void addOption(String optionText) {
        ParentOption option = new ParentOption();
        option.setOptionText(optionText);
        option.setParentQuestion(this);
        this.options.add(option);
    }
}
