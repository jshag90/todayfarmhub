package com.dodamsoft.todayfarmhub.repository;

import com.dodamsoft.todayfarmhub.entity.LClassCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LClassCodeRepository extends JpaRepository<LClassCode, Long> {
    LClassCode findOneBylclasscode(String lClassCode);
}
