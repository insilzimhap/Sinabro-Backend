package com.sinabro.backend.notice.admin.service;

import com.sinabro.backend.notice.admin.dto.*;
import com.sinabro.backend.notice.admin.dto.AdminNoticeCreateRequest;
import com.sinabro.backend.notice.admin.dto.AdminNoticeUpdateRequest;
import com.sinabro.backend.notice.entity.Notice;
import com.sinabro.backend.notice.repository.NoticeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminNoticeService {

    private final NoticeRepository noticeRepository;

    /**
     * ✅ 목록(전체)
     */
    public List<AdminNoticeListItemDto> getNoticeList() {
        return noticeRepository.findAll(Sort.by(Sort.Direction.DESC, "createdDate"))
                .stream()
                .map(n -> AdminNoticeListItemDto.builder()
                        .id(n.getId())
                        .title(n.getTitle())
                        .createdDate(n.getCreatedDate())
                        .noticeType(n.getNoticeType())
                        .build())
                .toList();
    }

    // 상세 조회
    public AdminNoticeDetailDto getNoticeDetail(Long id) {
        Notice notice = noticeRepository.findById(id)
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
        Notice notice = Notice.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .noticeType(request.getNoticeType())
                .build();

        return noticeRepository.save(notice).getId();
    }

    // 수정
    public void updateNotice(Long id, AdminNoticeUpdateRequest request) {
        Notice notice = noticeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("공지사항을 찾을 수 없습니다. id=" + id));

        notice.setTitle(request.getTitle());
        notice.setContent(request.getContent());
        notice.setNoticeType(request.getNoticeType());

        noticeRepository.save(notice); // @PreUpdate로 updatedDate 자동 반영됨
    }

    // 삭제
    public void deleteNotice(Long id) {
        if (!noticeRepository.existsById(id)) {
            throw new IllegalArgumentException("공지사항을 찾을 수 없습니다. id=" + id);
        }
        noticeRepository.deleteById(id);
    }

    /**
     * ✅ 검색/필터
     */
    public List<AdminNoticeListItemDto> searchNotices(
            String q,
            String noticeType,
            LocalDate startDate,
            LocalDate endDate,
            String field   // 👈 추가: all | title | type
    ) {
        Specification<Notice> spec = Specification.where(null);

        if (q != null && !q.isBlank()) {
            String like = "%" + q.toLowerCase() + "%";
            String f = (field == null || field.isBlank()) ? "all" : field;

            switch (f) {
                case "title" -> spec = spec.and((root, cq, cb) ->
                        cb.like(cb.lower(root.get("title")), like));
                case "type" -> spec = spec.and((root, cq, cb) ->
                        cb.like(cb.lower(root.get("noticeType")), like));
                default -> spec = spec.and((root, cq, cb) ->
                        cb.or(
                                cb.like(cb.lower(root.get("title")), like),
                                cb.like(cb.lower(root.get("noticeType")), like)
                        ));
            }
        }

        if (noticeType != null && !noticeType.isBlank() && !"전체".equals(noticeType)) {
            spec = spec.and((root, cq, cb) -> cb.equal(root.get("noticeType"), noticeType));
        }

        if (startDate != null || endDate != null) {
            LocalDateTime start = (startDate != null) ? startDate.atStartOfDay() : LocalDate.MIN.atStartOfDay();
            LocalDateTime end = (endDate != null) ? endDate.atTime(LocalTime.MAX) : LocalDate.MAX.atTime(LocalTime.MAX);
            spec = spec.and((root, cq, cb) -> cb.between(root.get("createdDate"), start, end));
        }

        return noticeRepository.findAll(spec, Sort.by(Sort.Direction.DESC, "createdDate"))
                .stream()
                .map(n -> AdminNoticeListItemDto.builder()
                        .id(n.getId())
                        .title(n.getTitle())
                        .createdDate(n.getCreatedDate())
                        .noticeType(n.getNoticeType())
                        .build())
                .toList();
    }
}
