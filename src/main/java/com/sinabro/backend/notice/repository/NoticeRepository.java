package com.sinabro.backend.notice.repository;

import com.sinabro.backend.notice.entity.Notice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * 공지 도메인 레포지토리 (공용)
 * - 기본 CRUD + 긴급 우선 정렬 페이지 조회
 */
public interface NoticeRepository extends JpaRepository<Notice, Long>, JpaSpecificationExecutor<Notice> {
    // 기본 CRUD 자동 제공됨

    // 리스트: 긴급(notice_type='긴급') 우선 → 최신순(createdDate desc) 페이징
    @Query("""
           select n
             from Notice n
            order by case when n.noticeType = '긴급' then 0 else 1 end,
                     n.createdDate desc
           """)
    Page<Notice> findAllUrgentFirst(Pageable pageable);

    // 상세 열람 시 조회수 +1 (DB에서 원자적으로 증가)
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update Notice n set n.viewCount = n.viewCount + 1 where n.id = :id")
    int increaseViewCount(@Param("id") Long id);
}
