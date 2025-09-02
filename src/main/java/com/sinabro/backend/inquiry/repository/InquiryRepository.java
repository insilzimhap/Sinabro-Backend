package com.sinabro.backend.inquiry.repository;

import com.sinabro.backend.inquiry.entity.Inquiry;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface InquiryRepository
        extends JpaRepository<Inquiry, Long>, JpaSpecificationExecutor<Inquiry> { // ✅ JpaSpecificationExecutor 추가

    // App 전용: 특정 부모의 문의를 페이지로 조회
    Page<Inquiry> findByParent_UserId(String parentUserId, org.springframework.data.domain.Pageable pageable);

    // App 전용: 상세 조회(권한 확인 겸용)
    Optional<Inquiry> findByIdAndParent_UserId(Long id, String parentUserId);


    //admin용
    // 전체 최신순
    List<Inquiry> findAllByOrderByCreatedAtDesc();

    // 간단 검색(제목 like) 최신순
    List<Inquiry> findByTitleContainingIgnoreCaseOrderByCreatedAtDesc(String q);

    // 상태 + 제목 like 최신순
    List<Inquiry> findByStatusAndTitleContainingIgnoreCaseOrderByCreatedAtDesc(String status, String q);

    // 상태만 필터 최신순
    List<Inquiry> findByStatusOrderByCreatedAtDesc(String status);

    // ======================
    // 🔽 추가: 작성자 이름 기반 검색(부분일치)
    List<Inquiry> findByParent_UserNameContainingIgnoreCaseOrderByCreatedAtDesc(String userName);
    List<Inquiry> findByStatusAndParent_UserNameContainingIgnoreCaseOrderByCreatedAtDesc(String status, String userName);

    // ======================
    // 🔽 추가: 작성자 ID 기반 검색(정확일치; 필요시 prefix로 바꿔도 됨)
    List<Inquiry> findByParent_UserIdOrderByCreatedAtDesc(String userId);
    List<Inquiry> findByStatusAndParent_UserIdOrderByCreatedAtDesc(String status, String userId);

    // ======================
    // 🔽 추가: '전체' 검색(제목 OR 작성자이름 OR 작성자ID)
    //  - 제목/이름은 부분일치, ID는 정확일치로 처리
    @Query("""
           select i from Inquiry i
           where lower(i.title) like lower(concat('%', :q, '%'))
              or lower(i.parent.userName) like lower(concat('%', :q, '%'))
              or lower(i.parent.userId) = :qExact
           order by i.createdAt desc
           """)
    List<Inquiry> searchAll(@Param("q") String q, @Param("qExact") String qExact);

    @Query("""
           select i from Inquiry i
           where i.status = :status
             and (
               lower(i.title) like lower(concat('%', :q, '%'))
               or lower(i.parent.userName) like lower(concat('%', :q, '%'))
               or lower(i.parent.userId) = :qExact
             )
           order by i.createdAt desc
           """)
    List<Inquiry> searchAllWithStatus(@Param("status") String status,
                                      @Param("q") String q,
                                      @Param("qExact") String qExact);
}
