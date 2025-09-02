package com.sinabro.backend.inquiry.app.service;

import com.sinabro.backend.inquiry.app.dto.*;
import com.sinabro.backend.inquiry.entity.*;
import com.sinabro.backend.inquiry.repository.*;
import com.sinabro.backend.user.entity.User;
import com.sinabro.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.springframework.http.HttpStatus.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class InquiryAppService {

    private static final int MAX_PAGE_SIZE = 100;

    private final InquiryRepository inquiryRepository;
    private final InquiryReplyRepository replyRepository;
    private final UserRepository userRepository;
    private static final String REPLY_AUTHOR = "팀 시나브로"; // 문의사항 답변자 이름 고정

    /**
     * [목록]
     * - 특정 부모가 작성한 문의만 페이지로 조회
     * - 최신순(createdAt desc)
     */
    public InquiryListResponseDto getList(String parentUserId, int page, int size) {
        int pageSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE); // 1..100 사이로 클램프
        log.info("[Inquiry] list parent={} page={} size={}", parentUserId, page, pageSize);

        Pageable pageable = PageRequest.of(page, pageSize, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Inquiry> p = inquiryRepository.findByParent_UserId(parentUserId, pageable);

        var items = p.getContent().stream()
                .map(this::toListItemDto) // ← 엔티티 → 리스트용 DTO로 변환
                .toList();

        return new InquiryListResponseDto(
                items,
                p.getNumber(),
                p.getSize(),
                p.getTotalElements(),
                p.getTotalPages(),
                p.hasNext()
        );
    }

    /**
     * [상세]
     * - 소유권까지 확인(부모 본인이 쓴 글만 접근 가능)
     * - 최신 답변 1건을 함께 내려줌(없으면 null)
     */
    public InquiryDetailDto getDetail(String parentUserId, Long inquiryId) {
        log.info("[Inquiry] detail parent={} inquiryId={}", parentUserId, inquiryId);

        Inquiry inq = inquiryRepository.findByIdAndParent_UserId(inquiryId, parentUserId)
                .orElseThrow(() -> {
                    log.warn("[Inquiry] not-found or not-owned. parent={}, inquiryId={}", parentUserId, inquiryId);
                    return new ResponseStatusException(NOT_FOUND, "문의를 찾을 수 없습니다.");
                });

        Optional<InquiryReply> latest = replyRepository.findTopByInquiry_IdOrderByCreatedDateDesc(inquiryId);
        latest.ifPresent(r -> log.debug("[Inquiry] latestReplyId={}", r.getId()));

        return toDetailDto(inq, latest);
    }

    /**
     * [등록]
     * - 부모 계정 존재 검증 후 저장
     * - createdAt/status는 @PrePersist로 자동 세팅
     */
    @Transactional
    public Long create(String parentUserId, InquiryCreateRequestDto req) {
        log.info("[Inquiry] create parent={}, title(len={})", parentUserId,
                req.getTitle() != null ? req.getTitle().length() : 0);

        User parent = userRepository.findById(parentUserId)
                .orElseThrow(() -> {
                    log.warn("[Inquiry] parent-not-found: {}", parentUserId);
                    return new ResponseStatusException(NOT_FOUND, "부모 계정을 찾을 수 없습니다.");
                });

        Inquiry entity = Inquiry.builder()
                .title(req.getTitle())
                .content(req.getContent())
                .parent(parent)
                .build();

        Long id = inquiryRepository.save(entity).getId();
        log.info("[Inquiry] created id={}", id);
        return id;
    }

    /* ===========================
     *        MAPPERS
     *  - 엔티티 → 전송용 DTO 변환
     *  - 지연로딩(Lazy)된 연관객체는 여기서만 접근
     * =========================== */

    /** 목록 카드에 필요한 최소 필드만 담는 매퍼 */
    private InquiryListItemDto toListItemDto(Inquiry i) {
        return new InquiryListItemDto(
                i.getId(),
                i.getTitle(),
                i.getParent().getUserName(),   // User 엔티티의 필드명에 맞춰 필요시 수정
                i.getCreatedAt(),
                i.getStatus()
        );
    }

    /** 상세화면용: 본문 + 최신 답변 1건까지 포함하는 매퍼 */
    private InquiryDetailDto toDetailDto(Inquiry i, Optional<InquiryReply> latest) {
        InquiryDetailDto.ReplyDto replyDto = latest
                .map(r -> new InquiryDetailDto.ReplyDto(
                        r.getId(),
                        REPLY_AUTHOR, // 관리자/작성자 표시명. 원래 코드인 r.getUser().getUserName() 에서 고정값으로 바꿈.
                        r.getContent(),
                        r.getCreatedDate()
                ))
                .orElse(null);

        return new InquiryDetailDto(
                i.getId(),
                i.getTitle(),
                i.getParent().getUserName(),
                i.getCreatedAt(),
                i.getStatus(),
                i.getContent(),
                replyDto
        );
    }
}
