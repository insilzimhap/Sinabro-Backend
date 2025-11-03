package com.sinabro.backend.notice.app.service;

import com.sinabro.backend.notice.app.dto.*;
import com.sinabro.backend.notice.app.dto.NoticePageResponse;
import com.sinabro.backend.notice.entity.Notice;
import com.sinabro.backend.notice.repository.NoticeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true) // 기본 읽기 트랜잭션
public class NoticeQueryService {

    private final NoticeRepository noticeRepository;

    // 작성자 표기 (피그마 고정값) - 필요 시 설정값으로 교체
    private static final String DEFAULT_AUTHOR = "팀 시나브로";

    /**
     * ✅ 공지 리스트 조회
     * - 정렬: 긴급 우선 → 최신순
     * - 페이지네이션: page/size
     * - 본문 제외(드롭다운 상세에서 조회)
     *
     * 로그 정책:
     *  - 호출: info
     *  - 분기/중간: debug
     *  - 성공: info
     *  - 오류: error
     */
    public NoticePageResponse<NoticeListItemDto> getNoticeList(int page, int size) {
        log.info("[NoticeList] call page={} size={}", page, size); // 호출

        Page<Notice> result;
        try {
            result = noticeRepository.findAllUrgentFirst(PageRequest.of(page, size));
            log.debug("[NoticeList] fetched page={} size={} totalElements={}",
                    result.getNumber(), result.getSize(), result.getTotalElements()); // 중간
        } catch (Exception e) {
            log.error("[NoticeList] error page={} size={} msg={}", page, size, e.getMessage(), e); // 오류
            throw e;
        }

        var content = result.getContent().stream()
                .map(this::toListDto)
                .toList();

        log.info("[NoticeList] success count={} totalElements={} hasNext={}",
                content.size(), result.getTotalElements(), result.hasNext()); // 성공

        return NoticePageResponse.<NoticeListItemDto>builder()
                .content(content)
                .page(result.getNumber())
                .size(result.getSize())
                .totalElements(result.getTotalElements())
                .totalPages(result.getTotalPages())
                .hasNext(result.hasNext())
                .build();
    }

    /**
     * ✅ 공지 상세 조회 (+ 선택적 조회수 증가)
     * - increaseView=true(기본)면 먼저 view_count +1 후 단건 조회
     */
    @Transactional // 조회수 증가(쓰기) 가능하므로 메서드에 readOnly 해제
    public NoticeDetailDto getNoticeDetail(Long id, boolean increaseView) {
        log.info("[NoticeDetail] call id={} increaseView={}", id, increaseView); // 호출

        try {
            if (increaseView) {
                int updated = noticeRepository.increaseViewCount(id);
                log.debug("[NoticeDetail] increaseView applied id={} updated={}", id, updated); // 중간(분기)
            }

            var notice = noticeRepository.findById(id)
                    .orElseThrow(() -> {
                        log.warn("[NoticeDetail] not-found id={}", id); // 분기(미존재)
                        return new ResponseStatusException(NOT_FOUND, "notice not found: " + id);
                    });

            var dto = toDetailDto(notice);
            log.info("[NoticeDetail] success id={} viewCount={}", id, dto.getViewCount()); // 성공
            return dto;

        } catch (ResponseStatusException e) {
            // 이미 상태 포함된 예외는 그대로 전달
            throw e;
        } catch (Exception e) {
            log.error("[NoticeDetail] error id={} msg={}", id, e.getMessage(), e); // 오류
            throw e;
        }
    }

    // --- 내부 매퍼들 ---

    /** Notice → 리스트 DTO */
    private NoticeListItemDto toListDto(Notice n) {
        boolean urgent = "긴급".equals(n.getNoticeType());
        return NoticeListItemDto.builder()
                .id(n.getId())
                .title(n.getTitle())
                .author(DEFAULT_AUTHOR)
                .createdAt(n.getCreatedDate())
                .viewCount(n.getViewCount() == null ? 0L : n.getViewCount())
                .urgent(urgent)
                .build();
    }

    /** Notice → 상세 DTO */
    private NoticeDetailDto toDetailDto(Notice n) {
        boolean urgent = "긴급".equals(n.getNoticeType());
        return NoticeDetailDto.builder()
                .id(n.getId())
                .title(n.getTitle())
                .content(n.getContent())
                .author(DEFAULT_AUTHOR)
                .createdAt(n.getCreatedDate())
                .viewCount(n.getViewCount() == null ? 0L : n.getViewCount())
                .urgent(urgent)
                .build();
    }
}
