package com.sinabro.backend.stage.repository;

import com.sinabro.backend.stage.entity.ChildFruitStatus;
import com.sinabro.backend.stage.entity.ChildFruitStatusId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 🌱 ChildFruitStatusRepository
 * - 자녀별 열매 활성 상태 관리용 레포지토리
 * - Child_Fruit_Status 테이블 접근 전용
 */
@Repository
public interface ChildFruitStatusRepository extends JpaRepository<ChildFruitStatus, ChildFruitStatusId> {

    // ✅ 이 메서드를 새로 추가해줘!
    // findBy + (복합키 필드명) + _ + (복합키 클래스 안의 필드명)
    // -> findById_ChildId
    // 이렇게 하면 "id 필드 안에 있는 childId로 찾아줘" 라는 정확한 명령이 돼.
    List<ChildFruitStatus> findById_ChildId(String childId);

    /**
     * 🔍 자녀-열매 조합으로 현재 활성 상태 조회
     * @param childId 자녀 ID
     * @param fruitId 열매 ID
     * @return ChildFruitStatus 존재 시 Optional로 반환
     */
    @Query("SELECT cfs FROM ChildFruitStatus cfs " +
            "WHERE cfs.id.childId = :childId AND cfs.id.fruitId = :fruitId")
    Optional<ChildFruitStatus> findByChildIdAndFruitId(
            @Param("childId") String childId,
            @Param("fruitId") String fruitId
    );

    /**
     * ✅ 자녀-열매 조합 존재 여부 확인
     * @param childId 자녀 ID
     * @param fruitId 열매 ID
     * @return 존재 여부 (true/false)
     */
    boolean existsById_ChildIdAndId_FruitId(String childId, String fruitId);

    /**
     * 🌳 열매 활성화 (UPDATE is_active=TRUE)
     * - 이미 존재하는 경우 상태만 업데이트
     */
    @Modifying
    @Query("UPDATE ChildFruitStatus cfs SET cfs.isActive = TRUE " +
            "WHERE cfs.id.childId = :childId AND cfs.id.fruitId = :fruitId")
    int activateFruit(
            @Param("childId") String childId,
            @Param("fruitId") String fruitId
    );
}
