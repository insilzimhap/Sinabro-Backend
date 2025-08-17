package com.sinabro.backend.admin.user.service;


import com.sinabro.backend.admin.user.dto.AdminChildDetailDto;
import com.sinabro.backend.admin.user.dto.AdminUserDetailDto;
import com.sinabro.backend.admin.user.dto.AdminUserDto;
import com.sinabro.backend.admin.user.repository.AdminUserRepository;

import com.sinabro.backend.admin.user.dto.AdminChildDto;
import com.sinabro.backend.admin.user.entity.AdminChild;
import com.sinabro.backend.admin.user.repository.AdminChildRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 관리자 > 회원(부모/자녀) 관리 서비스
 * - 목록/상세/자녀조회 등 “읽기” 위주
 * - ⚠️ 수정/삭제는 이후 단계에서 DTO/검증 정책 확정 후 추가
 */
@Service
@RequiredArgsConstructor   // 롬복: final 필드들로 생성자 자동 생성
@Transactional(readOnly = true) // 기본은 읽기 트랜잭션
public class AdminUserService {

    private final AdminUserRepository adminUserRepository;
    private final AdminChildRepository adminChildRepository;

    /**
     * ✅ 전체 부모(사용자) 목록 조회
     * - AdminUserDto로 가볍게 매핑 (목록 테이블 렌더링용)
     * - 정렬이 필요하면 Repository 쿼리 메서드에서 order by 추가를 권장
     */
    public List<AdminUserDto> getAllUsers() {
        return adminUserRepository.findAll().stream()
                .map(user -> new AdminUserDto(
                        user.getId(),          // user_id
                        user.getName(),        // user_name
                        user.getEmail(),       // user_email
                        user.getPhoneNumber(), // user_phone_num (NULL 가능)
                        user.getRole(),        // role
                        user.getCreateDate()   // user_create_date (Timestamp)
                ))
                .collect(Collectors.toList());
    }

    /**
     * ✅ 부모 ID로 자녀 목록 조회
     * - /api/admin/users/{parentUserId}/children 에서 사용
     */
    public List<AdminChildDto> getChildrenByParent(String parentUserId) {
        return adminChildRepository.findByParent_Id(parentUserId)
                .stream()
                .map(this::toChildDto)
                .collect(Collectors.toList());
    }

    /**
     * 내부 매퍼: AdminChild → AdminChildDto
     * - 프론트가 리스트로 뿌리기 쉽게 기본 필드만 노출
     */
    private AdminChildDto toChildDto(AdminChild c) {
        return new AdminChildDto(
                c.getParent().getId(), // 부모 user_id
                c.getId(),             // child_id
                c.getName(),           // 자녀 이름
                c.getAge(),            // 자녀 나이
                c.getPassword(),       // 자녀 비밀번호(정책상 노출이 필요 없으면 DTO에서 제거 고려)
                c.getLevel()           // 자녀 레벨
        );
    }

    /**
     * ✅ 부모 상세 + 자녀 목록
     * - 부모 단건 정보를 ParentDto로, 자녀 리스트를 ChildRowDto로 묶어서 전달
     * - AdminUserDetailDto 구조는 기존 설계 유지
     */
    public AdminUserDetailDto getUserDetail(String userId) {
        var parent = adminUserRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("parent not found: " + userId));

        var children = adminChildRepository.findByParent_Id(userId).stream()
                .map(c -> new AdminUserDetailDto.AdminChildRowDto(
                        c.getId(), c.getName(), c.getAge(), c.getLevel()
                ))
                .collect(Collectors.toList());

        var parentDto = new AdminUserDetailDto.ParentDto(
                parent.getId(),
                parent.getEmail(),
                parent.getName(),
                parent.getPassword() // ⚠️ 비밀번호는 화면 정책에 따라 제거/마스킹 고려
        );

        return new AdminUserDetailDto(parentDto, children);
    }

    /**
     * ✅ (옵션) 자녀 상세
     * - 단건 상세 화면이 필요할 때 사용
     */
    public AdminChildDetailDto getChildDetail(String childId) {
        var c = adminChildRepository.findById(childId)
                .orElseThrow(() -> new IllegalArgumentException("child not found: " + childId));

        return new AdminChildDetailDto(
                c.getId(),
                c.getName(),
                c.getAge(),
                c.getLevel(),
                c.getPassword() // ⚠️ 마찬가지로 노출 정책 검토 필요
        );
    }

    // ===========================
    // 이후 단계 (예고/주석만)
    // ===========================
    // @Transactional
    // public void updateUser(String userId, AdminUserUpdateRequest req) { ... }
    //
    // @Transactional
    // public void deleteUser(String userId) { ... }
    //
    // public List<AdminUserDto> searchUsers(String q) { ... } // Repository에 쿼리 메서드 추가 후
}
