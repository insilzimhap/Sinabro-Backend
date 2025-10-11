package com.sinabro.backend.reward.entity;

import java.io.Serializable;
import lombok.AllArgsConstructor; // ✅ import 추가
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor // ✅ 이 어노테이션을 추가하면 돼!
@EqualsAndHashCode
public class ChildStickerId implements Serializable {
    private String childId;
    private String stickerId;
}