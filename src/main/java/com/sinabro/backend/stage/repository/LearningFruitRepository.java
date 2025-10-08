package com.sinabro.backend.stage.repository;

import com.sinabro.backend.stage.entity.LearningFruit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
// JpaRepository를 상속받고, 제네릭 타입으로 <엔티티 클래스, PK의 타입>을 지정해줘.
// LearningFruit의 PK인 fruit_id가 String 타입이므로 String을 사용했어.
public interface LearningFruitRepository extends JpaRepository<LearningFruit, String> {

    // Spring Data JPA가 메서드 이름을 분석해서 자동으로 쿼리를 만들어주기 때문에
    // 기본적인 CRUD(Create, Read, Update, Delete)는 따로 코드를 작성할 필요가 없어.
    // 예를 들어, findById(), findAll(), save(), deleteById() 같은 메서드들을 바로 사용할 수 있어.

}