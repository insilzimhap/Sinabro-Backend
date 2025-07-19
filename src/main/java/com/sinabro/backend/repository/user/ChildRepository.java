package com.sinabro.backend.repository.user;
import com.sinabro.backend.entity.user.Child;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ChildRepository extends JpaRepository<Child, String> {
    Optional<Child> findByChildId(String childId);
}
