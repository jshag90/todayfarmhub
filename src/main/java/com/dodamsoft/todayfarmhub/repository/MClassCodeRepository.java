package com.dodamsoft.todayfarmhub.repository;

import com.dodamsoft.todayfarmhub.entity.MClassCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MClassCodeRepository extends JpaRepository<MClassCode, Long> {
}
