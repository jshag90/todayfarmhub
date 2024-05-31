package com.dodamsoft.todayfarmhub.repository;

import com.dodamsoft.todayfarmhub.entity.SClassCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SClassCodeRepository extends JpaRepository<SClassCode, Long> {
}
