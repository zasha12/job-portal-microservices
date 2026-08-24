package com.zasha12.reviewms.review;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByCompanyId(Long companyId);
    //SELECT * FROM review WHERE company_id = ?;

    //reviewRepository.findByCompanyId(1L)
    //SELECT * FROM review WHERE company_id = 1;
}
