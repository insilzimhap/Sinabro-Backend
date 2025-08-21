package com.sinabro.backend.user.child.repository;
import com.sinabro.backend.user.child.entity.Child;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.List;

public interface ChildRepository extends JpaRepository<Child, String> {
    Optional<Child> findByChildId(String childId);

    // ✅ 추가: 부모 user_id로 자녀 전체 조회
    List<Child> findByParent_UserId(String userId);
}
