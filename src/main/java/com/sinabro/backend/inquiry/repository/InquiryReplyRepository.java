package com.sinabro.backend.inquiry.repository;

import com.sinabro.backend.inquiry.entity.InquiryReply;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface InquiryReplyRepository extends JpaRepository<InquiryReply, Long> {

    // 해당 문의에 대한 가장 최근 답변 1건
    Optional<InquiryReply> findTopByInquiry_IdOrderByCreatedDateDesc(Long inquiryId);

    // 문의의 모든 답변 삭제(단일 답변 정책이면 이걸로 한 번에 처리)
    @Modifying
    void deleteByInquiry_Id(Long inquiryId);

    // ✅ 여러 문의의 답변 한 번에 삭제 (권장)
    @Modifying
    void deleteByInquiry_IdIn(Collection<Long> inquiryIds);

    // 부모가 직접 쓴 답글 삭제
    @Modifying
    void deleteByUser_UserId(String userId);

    // (선택) 해당 문의의 답변 전체 조회 - 상세 화면에서 히스토리 보여줄 필요가 생길 수 있어 추가
    List<InquiryReply> findByInquiry_IdOrderByCreatedDateDesc(Long inquiryId);
}
