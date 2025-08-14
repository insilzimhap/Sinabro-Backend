package com.sinabro.backend.admin.user.service;


import com.sinabro.backend.admin.user.dto.AdminChildDetailDto;
import com.sinabro.backend.admin.user.dto.AdminUserDetailDto;
import com.sinabro.backend.admin.user.dto.AdminUserDto;
import com.sinabro.backend.admin.user.repository.AdminUserRepository;

import com.sinabro.backend.admin.user.dto.AdminChildDto;
import com.sinabro.backend.admin.user.entity.AdminChild;
import com.sinabro.backend.admin.user.repository.AdminChildRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor   // 롬복: final 필드들로 생성자 자동 생성
public class AdminUserService {
    private final AdminUserRepository adminUserRepository;
    private final AdminChildRepository adminChildRepository;

    public List<AdminUserDto> getAllUsers() {
        return adminUserRepository.findAll().stream()
                .map(user -> new AdminUserDto(
                        user.getId(),
                        user.getName(),
                        user.getEmail(),
                        user.getPhoneNumber(),
                        user.getRole(),
                        user.getCreateDate()
                ))
                .collect(Collectors.toList());
    }

    // ✅ 부모 ID로 자녀 목록 조회
    public List<AdminChildDto> getChildrenByParent(String parentUserId) {
        return adminChildRepository.findByParent_Id(parentUserId)
                .stream()
                .map(this::toChildDto)
                .toList();
    }

    private AdminChildDto toChildDto(AdminChild c) {
        return new AdminChildDto(
                c.getParent().getId(),
                c.getId(),
                c.getName(),
                c.getAge(),
                c.getPassword(),
                c.getLevel()
        );
    }

    // ✅ 부모 상세 + 자녀 목록
    public AdminUserDetailDto getUserDetail(String userId) {
        var parent = adminUserRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("parent not found: " + userId));

        var children = adminChildRepository.findByParent_Id(userId).stream()
                .map(c -> new AdminUserDetailDto.AdminChildRowDto(
                        c.getId(), c.getName(), c.getAge(), c.getLevel()
                ))
                .toList();

        var parentDto = new AdminUserDetailDto.ParentDto(
                parent.getId(), parent.getEmail(), parent.getName(), parent.getPassword()
        );

        return new AdminUserDetailDto(parentDto, children);
    }

    // ✅ (옵션) 자녀 상세
    public AdminChildDetailDto getChildDetail(String childId) {
        var c = adminChildRepository.findById(childId)
                .orElseThrow(() -> new IllegalArgumentException("child not found: " + childId));

        return new AdminChildDetailDto(
                c.getId(), c.getName(), c.getAge(), c.getLevel(), c.getPassword()
        );
    }
}
