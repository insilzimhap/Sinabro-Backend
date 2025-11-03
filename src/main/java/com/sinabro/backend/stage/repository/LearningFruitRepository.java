package com.sinabro.backend.stage.repository;

import com.sinabro.backend.stage.entity.LearningFruit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sinabro.backend.progress.entity.Category;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.*;

@Repository
// JpaRepository를 상속받고, 제네릭 타입으로 <엔티티 클래스, PK의 타입>을 지정해줘.
// LearningFruit의 PK인 fruit_id가 String 타입이므로 String을 사용했어.
public interface LearningFruitRepository extends JpaRepository<LearningFruit, String> {

    // Spring Data JPA가 메서드 이름을 분석해서 자동으로 쿼리를 만들어주기 때문에
    // 기본적인 CRUD(Create, Read, Update, Delete)는 따로 코드를 작성할 필요가 없어.
    // 예를 들어, findById(), findAll(), save(), deleteById() 같은 메서드들을 바로 사용할 수 있어.

    /**
     * [듣기 게임용] 특정 단계(stage)의 모든 열매 조회
     * - category='LISTENING_GAME' + stageId 기준으로 순서대로 반환
     */
    List<LearningFruit> findByCategoryAndStageIdOrderBySequenceInStage(Category category, String stageId);

    /**
     * [공통]
     * 🔍 Stage + 순서로 단일 열매 찾기
     * @param stageId Stage ID
     * @param sequenceInStage 나무 내 순서
     * @return 열매 Optional
     */
    Optional<LearningFruit> findByStageIdAndSequenceInStage(String stageId, int sequenceInStage);

    /**
     * [게임 완료 후] 다음 열매 활성화
     * - setter가 없는 경우 @Modifying 쿼리로 직접 활성화 처리
     */
    @Modifying(clearAutomatically = true)
    @Query("UPDATE LearningFruit f SET f.isActive = true WHERE f.fruitId = :fruitId")
    void activateByFruitId(@Param("fruitId") String fruitId);


    /**
     * 🌳 특정 Stage의 모든 열매 조회 (순서 포함)
     * @param stageId Stage ID
     * @return 해당 스테이지의 열매 리스트
     */
    List<LearningFruit> findByStageIdOrderBySequenceInStage(String stageId);

    /**
     * 🔍 카테고리 & Stage 조합으로 조회 (듣기/쓰기 등 구분용)
     */
    List<LearningFruit> findByCategoryAndStageIdOrderBySequenceInStage(Enum<?> category, String stageId);


}