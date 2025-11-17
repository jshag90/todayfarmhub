package com.dodamsoft.todayfarmhub.repository;

import com.dodamsoft.todayfarmhub.entity.LClassCode;
import com.dodamsoft.todayfarmhub.entity.MClassCode;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MClassCodeRepository extends JpaRepository<MClassCode, Long> {
    boolean existsBylClassCode(LClassCode lClassCode);

    boolean existsByMclassname(String mclassname);

    List<MClassCode> findAllBylClassCode(LClassCode lClassCode,Sort mclassname);

    MClassCode findOneBylClassCodeAndMclasscode(LClassCode lClassCode, String mclasscode);
}
