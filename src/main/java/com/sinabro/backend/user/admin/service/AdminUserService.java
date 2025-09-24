package com.sinabro.backend.user.admin.service;


import com.sinabro.backend.user.admin.dto.*;
import com.sinabro.backend.user.entity.CharacterInfo;
import com.sinabro.backend.user.entity.User;
import com.sinabro.backend.user.entity.Child;


import com.sinabro.backend.user.repository.CharacterSelectionRepository;
import com.sinabro.backend.user.repository.UserRepository;
import com.sinabro.backend.user.repository.ChildRepository;

import jakarta.persistence.criteria.JoinType;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Comparator;
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

    // ✅ 공용 레포로 교체 (이름만 소문자 카멜케이스로 정리)
    private final UserRepository userRepository;
    private final ChildRepository childRepository;
    private final CharacterSelectionRepository characterSelectionRepository;

    /**
     * ✅ 전체 부모(사용자) 목록 조회
     * - AdminUserDto로 가볍게 매핑 (목록 테이블 렌더링용)
     * - 정렬이 필요하면 Repository 쿼리 메서드에서 order by 추가를 권장
     */
    public List<AdminUserDto> getAllUsers() {
        return userRepository.findAllByOrderByUserCreateDateDesc().stream()
                .map(user -> new AdminUserDto(
                        user.getUserId(),
                        user.getUserName(),
                        user.getUserEmail(),
                        user.getUserPhoneNum(),
                        user.getRole(),
                        user.getUserCreateDate()
                ))
                .collect(Collectors.toList());
    }

    /**
     * ✅ 부모 ID로 자녀 목록 조회
     * - /api/admin/users/{parentUserId}/children 에서 사용
     */
    public List<AdminChildDto> getChildrenByParent(String parentUserId) {
        // ✔ 공용 레포 메서드 사용: findByParent_UserId
        // ✔ 정렬 메서드가 없으므로 메모리에서 최신순 정렬(Child.createDate 내림차순)
        return childRepository.findByParent_UserId(parentUserId).stream()
                .sorted(Comparator.comparing(
                        Child::getChildCreateDate,
                        Comparator.nullsLast(Comparator.naturalOrder())
                ).reversed())
                .map(this::toChildDto)
                .collect(Collectors.toList());
    }

    /**
     * 내부 매퍼: AdminChild → AdminChildDto
     * - 프론트가 리스트로 뿌리기 쉽게 기본 필드만 노출
     */
    private AdminChildDto toChildDto(Child c) {
        return new AdminChildDto(
                c.getParent().getUserId(), // 부모 user_id
                c.getChildId(),            // child_id
                c.getChildName(),          // 자녀 이름
                c.getChildAge(),           // 자녀 나이
                c.getChildPw(),            // 자녀 비밀번호(정책상 노출이 필요 없으면 DTO에서 제거 고려)
                c.getChildLevel()          // 자녀 레벨
        );
    }

    /**
     * ✅ 부모 상세 + 자녀 목록
     * - 부모 단건 정보를 ParentDto로, 자녀 리스트를 ChildRowDto로 묶어서 전달
     * - AdminUserDetailDto 구조는 기존 설계 유지
     */
    public AdminUserDetailDto getUserDetail(String userId) {
        var parent = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("parent not found: " + userId));

        var children = childRepository.findByParent_UserId(userId).stream()
                .map(c -> new AdminUserDetailDto.AdminChildRowDto(
                        c.getChildId(), c.getChildName(), c.getChildAge(), c.getChildLevel()
                ))
                .collect(Collectors.toList());

        // ✔ Timestamp → LocalDateTime 변환 (DTO가 LocalDateTime을 기대)
        LocalDateTime createDate = null;
        Timestamp ts = parent.getUserCreateDate();
        if (ts != null) createDate = ts.toLocalDateTime();

        var parentDto = new AdminUserDetailDto.ParentDto(
                parent.getUserId(),
                parent.getUserEmail(),
                parent.getUserName(),
                parent.getUserPhoneNum(), // ✅ 추가
                createDate                 // ✅ 추가
        );
        return new AdminUserDetailDto(parentDto, children);
    }

    /**
     * ✅ (옵션) 자녀 상세
     * - 단건 상세 화면이 필요할 때 사용
     */
    public AdminChildDetailDto getChildDetail(String childId) {
        var c = childRepository.findById(childId)
                .orElseThrow(() -> new IllegalArgumentException("child not found: " + childId));

        // 자녀의 최근 캐릭터 1건 조회
        var selOpt = characterSelectionRepository.findTopByChildIdOrderByCreatedAtDesc(childId);

        // selection → (읽기 전용 ManyToOne) character 조인으로 바로 이름 뽑기
        String characterName = selOpt
                .map(sel -> sel.getCharacter())              // CharacterInfo (LAZY)
                .map(CharacterInfo::getCharacterName)        // 이름만
                .orElse(null);                               // 선택 없으면 null

        return AdminChildDetailDto.builder()
                .childId(c.getChildId())
                .name(c.getChildName())
                .nickname(c.getChildNickname())
                .age(c.getChildAge())
                .level(c.getChildLevel())
                .characterName(characterName)
                .build();
    }

    /** 부모 정보 수정 (200 OK, body 없음) */
    @Transactional
    public void updateUser(String userId, AdminUserUpdateRequest req) {
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("parent not found: " + userId));

        // ✔ 공용 엔티티 세터명으로 교체
        user.setUserName(req.getName());
        user.setUserEmail(req.getEmail());
        user.setUserPhoneNum(req.getPhoneNumber());

        // 영속 상태이므로 save() 생략 가능하지만, 명시하고 싶으면 유지
        userRepository.save(user);
    }

    /** 부모 삭제: selection(모든 자녀들) → children → parent */
    @Transactional
    public boolean deleteUser(String userId) {
        // 부모 존재 체크 (먼저 확인)
        if (!userRepository.existsById(userId)) {
            return false; // 404
        }

        // 1) 부모의 모든 자녀 조회
        var kids = childRepository.findByParent_UserId(userId);

        if (!kids.isEmpty()) {
            // 2) 자녀들의 selection 먼저 삭제
            var childIds = kids.stream().map(Child::getChildId).toList();
            characterSelectionRepository.deleteByChildIdIn(childIds);

            // 3) 자녀 삭제
            childRepository.deleteAll(kids);
            // (기존 deleteByParent_Id(userId) 대신 명시적으로 순서를 보장)
        }

        // 4) 부모 삭제
        userRepository.deleteById(userId);
        return true; // 204
    }

    /** 자녀 단건 삭제 (자녀 상세 화면의 "자녀 삭제하기" 버튼): selection 먼저 → child */
    @Transactional
    public boolean deleteChild(String childId) {
        if (!childRepository.existsById(childId)) {
            return false; // 404 신호
        }
        characterSelectionRepository.deleteByChildId(childId);
        childRepository.deleteById(childId);
        return true; // 204
    }

    /** 회원 검색 (부모 목록 반환)
     *  field: parentName | childName | email | all
     *  q: 검색어(부분일치)
     *  startDate/endDate: 가입일 범위 필터(옵션)
     */
    public List<AdminUserDto> searchUsers(
            String q,
            String field,                 // all | parentName | childName | email
            LocalDate startDate,
            LocalDate endDate
    ) {
        Specification<User> spec = Specification.where(null);

        // ✅ 날짜 범위 (둘 다 optional)
        if (startDate != null || endDate != null) {
            var start = (startDate != null) ? startDate.atStartOfDay()
                    : LocalDate.MIN.atStartOfDay();
            var end   = (endDate != null)   ? endDate.atTime(LocalTime.MAX)
                    : LocalDate.MAX.atTime(LocalTime.MAX);
            spec = spec.and((root, cq, cb) -> cb.between(root.get("userCreateDate"), start, end));
        }

        // ✅ 검색어
        boolean hasQ = (q != null && !q.isBlank());
        String qLower = hasQ ? q.trim().toLowerCase() : null;
        String f = (field == null || field.isBlank()) ? "all" : field;

        if (hasQ) {
            spec = spec.and((root, cq, cb) -> {
                // 자녀 조인(필요한 경우) + 중복 방지
                switch (f) {
                    case "parentName" -> {
                        return cb.like(cb.lower(root.get("userName")), "%" + qLower + "%");
                    }
                    case "email" -> {
                        return cb.like(cb.lower(root.get("userEmail")), "%" + qLower + "%");
                    }
                    case "childName" -> {
                        var children = root.join("children", JoinType.LEFT);
                        cq.distinct(true);
                        return cb.like(cb.lower(children.get("childName")), "%" + qLower + "%");
                    }
                    case "all" -> {
                        var children = root.join("children", JoinType.LEFT);
                        cq.distinct(true);
                        return cb.or(
                                cb.like(cb.lower(root.get("userName")), "%" + qLower + "%"),
                                cb.like(cb.lower(root.get("userEmail")), "%" + qLower + "%"),
                                cb.like(cb.lower(children.get("childName")), "%" + qLower + "%")
                        );
                    }
                    default -> {
                        // 알 수 없는 field → 부모이름으로 폴백
                        return cb.like(cb.lower(root.get("userName")), "%" + qLower + "%");
                    }
                }
            });
        }

        return userRepository.findAll(spec, Sort.by(Sort.Direction.DESC, "userCreateDate"))
                .stream()
                .map(user -> new AdminUserDto(
                        user.getUserId(),
                        user.getUserName(),
                        user.getUserEmail(),
                        user.getUserPhoneNum(),
                        user.getRole(),
                        user.getUserCreateDate()
                ))
                .toList();
    }
}