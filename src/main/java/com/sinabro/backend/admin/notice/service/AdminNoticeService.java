package com.sinabro.backend.admin.notice.service;

import com.sinabro.backend.admin.notice.dto.*;
import com.sinabro.backend.admin.notice.entity.AdminNotice;
import com.sinabro.backend.admin.notice.repository.AdminNoticeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminNoticeService {

    private final AdminNoticeRepository adminNoticeRepository;

    // 목록 조회
    public List<AdminNoticeListItemDto> getNoticeList() {
        return adminNoticeRepository.findAll().stream()
                .map(notice -> AdminNoticeListItemDto.builder()
                        .id(notice.getId())
                        .title(notice.getTitle())
                        .createdDate(notice.getCreatedDate())
                        .noticeType(notice.getNoticeType())
                        .build())
                .collect(Collectors.toList());
    }

    // 상세 조회
    public AdminNoticeDetailDto getNoticeDetail(Long id) {
        AdminNotice notice = adminNoticeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("공지사항을 찾을 수 없습니다. id=" + id));

        return AdminNoticeDetailDto.builder()
                .id(notice.getId())
                .title(notice.getTitle())
                .content(notice.getContent())
                .noticeType(notice.getNoticeType())
                .createdDate(notice.getCreatedDate())
                .updatedDate(notice.getUpdatedDate())
                .build();
    }

    // 등록
    public Long createNotice(AdminNoticeCreateRequest request) {
        AdminNotice notice = AdminNotice.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .noticeType(request.getNoticeType())
                .build();

        return adminNoticeRepository.save(notice).getId();
    }

    // 수정
    public void updateNotice(Long id, AdminNoticeUpdateRequest request) {
        AdminNotice notice = adminNoticeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("공지사항을 찾을 수 없습니다. id=" + id));

        notice.setTitle(request.getTitle());
        notice.setContent(request.getContent());
        notice.setNoticeType(request.getNoticeType());

        adminNoticeRepository.save(notice); // @PreUpdate로 updatedDate 자동 반영됨
    }

    // 삭제
    public void deleteNotice(Long id) {
        if (!adminNoticeRepository.existsById(id)) {
            throw new IllegalArgumentException("공지사항을 찾을 수 없습니다. id=" + id);
        }
        adminNoticeRepository.deleteById(id);
    }

}