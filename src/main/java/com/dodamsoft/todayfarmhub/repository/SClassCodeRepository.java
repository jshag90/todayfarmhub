package com.dodamsoft.todayfarmhub.repository;

import com.dodamsoft.todayfarmhub.entity.SClassCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SClassCodeRepository extends JpaRepository<SClassCode, Long> {

    @Query("SELECT CASE WHEN COUNT(sc) > 0 THEN true ELSE false END FROM SClassCode sc WHERE sc.lClassCode.id = ?1 AND sc.mClassCode.id = ?2")
    boolean existsByMClassCodeIdAndLClassCodeId(Long lClassCodeId, Long mClassCodeId);
    @Query("SELECT sc FROM SClassCode sc WHERE sc.lClassCode.id = ?1 AND sc.mClassCode.id = ?2 ORDER BY sc.sclassname DESC")
    List<SClassCode> findAllByMClassCodeIdAndLClassCodeIdOrderBySclassnameDesc(Long lClassCodeId, Long mClassCodeId);

    SClassCode findOneBysclasscode(String sclasscode);
}
