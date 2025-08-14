package com.sinabro.backend.admin.inquiry.repository;

import com.sinabro.backend.admin.inquiry.entity.AdminInquiry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface AdminInquiryRepository
        extends JpaRepository<AdminInquiry, Long>, JpaSpecificationExecutor<AdminInquiry> { // ✅ JpaSpecificationExecutor 추가

    // 전체 최신순
    List<AdminInquiry> findAllByOrderByCreatedAtDesc();

    // 간단 검색(제목 like) 최신순
    List<AdminInquiry> findByTitleContainingIgnoreCaseOrderByCreatedAtDesc(String q);

    // 상태 + 제목 like 최신순
    List<AdminInquiry> findByStatusAndTitleContainingIgnoreCaseOrderByCreatedAtDesc(String status, String q);

    // 상태만 필터 최신순
    List<AdminInquiry> findByStatusOrderByCreatedAtDesc(String status);

    // ======================
    // 🔽 추가: 작성자 이름 기반 검색(부분일치)
    List<AdminInquiry> findByParent_NameContainingIgnoreCaseOrderByCreatedAtDesc(String writerName);
    List<AdminInquiry> findByStatusAndParent_NameContainingIgnoreCaseOrderByCreatedAtDesc(String status, String writerName);

    // ======================
    // 🔽 추가: 작성자 ID 기반 검색(정확일치; 필요시 prefix로 바꿔도 됨)
    List<AdminInquiry> findByParent_IdOrderByCreatedAtDesc(String writerId);
    List<AdminInquiry> findByStatusAndParent_IdOrderByCreatedAtDesc(String status, String writerId);

    // ======================
    // 🔽 추가: '전체' 검색(제목 OR 작성자이름 OR 작성자ID)
    //  - 제목/이름은 부분일치, ID는 정확일치로 처리
    @Query("""
           select i from AdminInquiry i
           where lower(i.title) like lower(concat('%', :q, '%'))
              or lower(i.parent.name) like lower(concat('%', :q, '%'))
              or lower(i.parent.id) = :qExact
           order by i.createdAt desc
           """)
    List<AdminInquiry> searchAll(@Param("q") String q, @Param("qExact") String qExact);

    @Query("""
           select i from AdminInquiry i
           where i.status = :status
             and (
               lower(i.title) like lower(concat('%', :q, '%'))
               or lower(i.parent.name) like lower(concat('%', :q, '%'))
               or lower(i.parent.id) = :qExact
             )
           order by i.createdAt desc
           """)
    List<AdminInquiry> searchAllWithStatus(@Param("status") String status,
                                           @Param("q") String q,
                                           @Param("qExact") String qExact);
}
