package com.sinabro.backend.user.repository;
import com.sinabro.backend.user.entity.Child;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.List;

/**
 * [공용 레포] Child 엔티티
 * - 부모별 자녀 조회/정렬/삭제 (admin에서 쓰던 메서드 포함)
 */
public interface ChildRepository extends JpaRepository<Child, String> {
    // 단건 조회
    Optional<Child> findByChildId(String childId);

    // 부모 user_id로 자녀 전체 조회, 자녀 목록 (프로필 선택 화면) (+admin)
    List<Child> findByParent_UserId(String userId);

    // 소유권 확인용
    boolean existsByChildIdAndParent_UserId(String childId, String parentUserId);

    Optional<Child> findByChildIdAndParent_UserId(String childId, String parentUserId);


    // admin - 자녀 목록 정렬 (최신순)
    List<Child> findByParent_UserIdOrderByChildCreateDateDesc(String userId);

    // admin - 부모 삭제 전에 자녀 전부 제거할 때 사용 (CASCADE가 없어서)
    long deleteByParent_UserId(String parentUserId);
}
