package com.sinabro.backend.inquiry.admin.service;

import com.sinabro.backend.inquiry.admin.dto.*;
import com.sinabro.backend.inquiry.entity.*;
import com.sinabro.backend.inquiry.repository.*;
import com.sinabro.backend.user.entity.User;
import com.sinabro.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminInquiryService {
    private final InquiryRepository inquiryRepository;
    private final InquiryReplyRepository replyRepository;
    private final UserRepository userRepository; // 답변 작성자 조회용 (role=admin 계정)

    // 목록 (간단 검색/상태 필터)
    public List<AdminInquiryListItemDto> getList(String q, String status) {
        List<Inquiry> list;

        boolean hasQ = q != null && !q.isBlank();
        boolean hasStatus = status != null && !status.isBlank() && !"전체".equals(status);

        if (hasQ && hasStatus) {
            list = inquiryRepository.findByStatusAndTitleContainingIgnoreCaseOrderByCreatedAtDesc(status, q);
        } else if (hasQ) {
            list = inquiryRepository.findByTitleContainingIgnoreCaseOrderByCreatedAtDesc(q);
        } else if (hasStatus) {
            list = inquiryRepository.findByStatusOrderByCreatedAtDesc(status);
        } else {
            list = inquiryRepository.findAllByOrderByCreatedAtDesc();
        }

        return list.stream()
                .map(i -> AdminInquiryListItemDto.builder()
                        .id(i.getId())
                        .title(i.getTitle())
                        .createdAt(i.getCreatedAt())
                        .writerId(i.getParent() != null ? i.getParent().getUserId() : "")     // 🔧 작성자 ID 추가
                        .writerName(i.getParent() != null ? i.getParent().getUserName() : "")
                        .status(i.getStatus())
                        .build())
                .toList();
    }

    // 🔧 [신규] 목록 (필드 선택 + 상태 + 작성일 범위)
    // field: all | title | writerId | writerName
    public List<AdminInquiryListItemDto> getList(
            String q,
            String field,
            String status,
            LocalDate startDate,
            LocalDate endDate
    ) {
        Specification<Inquiry> spec = Specification.where(null);

        // 상태 필터
        if (status != null && !status.isBlank() && !"전체".equals(status)) {
            spec = spec.and((root, cq, cb) -> cb.equal(root.get("status"), status));
        }

        // 검색어 + 필드
        if (q != null && !q.isBlank()) {
            String like = "%" + q.toLowerCase() + "%";
            String f = (field == null || field.isBlank()) ? "all" : field;

            switch (f) {
                case "title" -> spec = spec.and((root, cq, cb) ->
                        cb.like(cb.lower(root.get("title")), like));

                case "writerName" -> spec = spec.and((root, cq, cb) ->
                        cb.like(cb.lower(root.get("parent").get("name")), like));

                case "writerId" -> spec = spec.and((root, cq, cb) ->
                        cb.equal(cb.lower(root.get("parent").get("id")), q.toLowerCase()));

                case "all" -> {
                    spec = spec.and((root, cq, cb) -> cb.or(
                            cb.like(cb.lower(root.get("title")), like),
                            cb.like(cb.lower(root.get("parent").get("name")), like),
                            cb.equal(cb.lower(root.get("parent").get("id")), q.toLowerCase())
                    ));
                }
                default -> { /* no-op */ }
            }
        }

        // 작성일 범위 (createdAt)
        if (startDate != null || endDate != null) {
            LocalDateTime start = (startDate != null)
                    ? startDate.atStartOfDay()
                    : LocalDate.MIN.atStartOfDay(); // 필요 시 프로젝트 기준으로 조정 가능
            LocalDateTime end = (endDate != null)
                    ? endDate.atTime(LocalTime.MAX)
                    : LocalDate.MAX.atTime(LocalTime.MAX);

            spec = spec.and((root, cq, cb) -> cb.between(root.get("createdAt"), start, end));
        }

        return inquiryRepository.findAll(spec, Sort.by(Sort.Direction.DESC, "createdAt"))
                .stream()
                .map(i -> AdminInquiryListItemDto.builder()
                        .id(i.getId())
                        .title(i.getTitle())
                        .createdAt(i.getCreatedAt())
                        .writerId(i.getParent() != null ? i.getParent().getUserId() : "")     // 🔧 작성자 ID 추가
                        .writerName(i.getParent() != null ? i.getParent().getUserName() : "")
                        .status(i.getStatus())
                        .build())
                .toList();
    }

    // 상세 + 최신 답변 1건
    public AdminInquiryDetailDto getDetail(Long id) {
        Inquiry i = inquiryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("문의가 존재하지 않습니다. id=" + id));

        var latestReply = replyRepository.findTopByInquiry_IdOrderByCreatedDateDesc(id);

        return AdminInquiryDetailDto.builder()
                .id(i.getId())
                .title(i.getTitle())
                .content(i.getContent())
                .createdAt(i.getCreatedAt())
                .writerId(i.getParent() != null ? i.getParent().getUserId() : "")           // 🔧 작성자 ID 추가
                .writerName(i.getParent() != null ? i.getParent().getUserName() : "")
                .status(i.getStatus())
                .replyId(latestReply.map(InquiryReply::getId).orElse(null))
                .replyContent(latestReply.map(InquiryReply::getContent).orElse(null))
                .replyCreatedDate(latestReply.map(InquiryReply::getCreatedDate).orElse(null))
                .build();
    }

    // 답변 등록/수정 (단일 답변 정책)
    @Transactional
    public Long upsertReply(Long inquiryId, AdminInquiryReplyUpsertRequestDto req) {
        if (req.getContent() == null || req.getContent().isBlank()) {
            throw new IllegalArgumentException("답변 내용을 입력하세요.");
        }

        Inquiry inquiry = inquiryRepository.findById(inquiryId)
                .orElseThrow(() -> new IllegalArgumentException("문의가 존재하지 않습니다. id=" + inquiryId));

        String adminUserId = (req.getAdminUserId() == null || req.getAdminUserId().isBlank())
                ? "admin"     // 기본 관리자 아이디(프로젝트 정책에 맞춰 수정)
                : req.getAdminUserId();

        User adminUser = userRepository.findById(adminUserId)
                .orElseThrow(() -> new IllegalArgumentException("관리자 계정을 찾을 수 없습니다. id=" + adminUserId));

        var latest = replyRepository.findTopByInquiry_IdOrderByCreatedDateDesc(inquiryId);

        InquiryReply reply = latest.orElseGet(() -> InquiryReply.builder()
                .inquiry(inquiry)
                .user(adminUser)
                .build());

        reply.setContent(req.getContent());
        reply.setUser(adminUser); // (선택) 답변자 변경 가능하도록

        Long replyId = replyRepository.save(reply).getId();

        // 상태 갱신
        inquiry.setStatus("답변 완료");

        return replyId;
    }

    // 답변 삭제 (전체 삭제 후 '답변 전' 상태로)
    @Transactional
    public void deleteReply(Long inquiryId) {
        Inquiry inquiry = inquiryRepository.findById(inquiryId)
                .orElseThrow(() -> new IllegalArgumentException("문의가 존재하지 않습니다. id=" + inquiryId));

        replyRepository.deleteByInquiry_Id(inquiryId);
        inquiry.setStatus("답변 전");
    }
}
