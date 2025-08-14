package com.sinabro.backend.admin.inquiry.repository;

import com.sinabro.backend.admin.inquiry.entity.AdminInquiryReply;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AdminInquiryReplyRepository extends JpaRepository<AdminInquiryReply, Long> {

    // 해당 문의에 대한 가장 최근 답변 1건
    Optional<AdminInquiryReply> findTopByInquiry_IdOrderByCreatedDateDesc(Long inquiryId);

    // 문의의 모든 답변 삭제(단일 답변 정책이면 이걸로 한 번에 처리)
    void deleteByInquiry_Id(Long inquiryId);

    // (선택) 해당 문의의 답변 전체 조회 - 상세 화면에서 히스토리 보여줄 필요가 생길 수 있어 추가
    List<AdminInquiryReply> findByInquiry_IdOrderByCreatedDateDesc(Long inquiryId);
}
