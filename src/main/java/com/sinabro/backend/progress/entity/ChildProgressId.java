package com.sinabro.backend.progress.entity;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;


// 복합 키를 위한 ID 클래스
// 1. Serializable 구현은 필수!
// 2. equals와 hashCode 메서드를 구현해야 해 (Lombok이 대신 해줌)
// 3. 기본 생성자가 꼭 있어야 해
@NoArgsConstructor
@EqualsAndHashCode
@AllArgsConstructor
public class ChildProgressId implements Serializable {
    private String childId;
    private Category category;
}