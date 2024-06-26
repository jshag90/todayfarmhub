package com.dodamsoft.todayfarmhub.repository;

import com.dodamsoft.todayfarmhub.entity.LClassCode;
import com.dodamsoft.todayfarmhub.entity.MClassCode;
import com.dodamsoft.todayfarmhub.entity.Prices;
import com.dodamsoft.todayfarmhub.entity.SClassCode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface PricesRepository extends JpaRepository<Prices, Long> {

    /**
     * 금액 테이블에 해당 날짜, lclass, mclass, sclass 카테고리에 해당하는 데이터가 있는지 조회
     *
     * @param dates
     * @param lClassCodeId
     * @param mClassCodeId
     * @param sClassCodeId
     * @return
     */
    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END " +
            "FROM Prices p " +
            "WHERE p.dates = ?1 AND p.lClassCode.id = ?2 AND p.mClassCode.id = ?3 AND p.sClassCode.id = ?4")
    boolean existsByDatesAndLClassCodeIdAndMClassCodeIdAndSClassCodeId(String dates, Long lClassCodeId, Long mClassCodeId, Long sClassCodeId);

    //TODO 다음 메서드 조회 되도록 수정 !!!
    Page<Prices> findByDatesAndLClassCodeAndMClassCodeAndSClassCode(String dates, LClassCode lClassCode, MClassCode mClassCode, SClassCode sClassCode, Pageable pageable);

}
