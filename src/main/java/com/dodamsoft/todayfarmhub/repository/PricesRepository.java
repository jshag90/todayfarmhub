package com.dodamsoft.todayfarmhub.repository;

import com.dodamsoft.todayfarmhub.dto.PriceStatisticsDto;
import com.dodamsoft.todayfarmhub.dto.PriceTradeCountDto;
import com.dodamsoft.todayfarmhub.dto.PricesDto;
import com.dodamsoft.todayfarmhub.entity.Prices;
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
    String commonWhereQuery = "WHERE p.dates = ?1 AND p.lClassCode.id = ?2 AND p.mClassCode.id = ?3 AND p.sClassCode.id = ?4";

    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END " +
            "FROM Prices p " + commonWhereQuery)
    boolean existsByDatesAndLClassCodeIdAndMClassCodeIdAndSClassCodeId(String dates, Long lClassCodeId, Long mClassCodeId, Long sClassCodeId, Long marketCodeId);

    @Query(value = "SELECT new com.dodamsoft.todayfarmhub.dto.PricesDto(" +
            "  p.id" +
            ", p.dates" +
            ", p.bidtime" +
            ", p.sanco" +
            ", p.sanji" +
            ", p.coco" +
            ", p.price" +
            ", p.lClassCode.lclasscode" +
            ", p.mClassCode.mclasscode" +
            ", p.sClassCode.sclasscode" +
            ", p.sClassCode.sclassname" +
            ", p.marketCode.marketCode" +
            ", p.cocode" +
            ", p.coname" +
            ", REPLACE(p.unitname, ' ', '')" +
            ", p.tradeamt) " +
            "FROM Prices p " + commonWhereQuery + " AND p.marketCode.id = ?5")
    Page<PricesDto> findByDatesAndLClassCodeAndMClassCodeAndSClassCode(String dates, Long lClassCodeId, Long mClassCodeId, Long sClassCodeId, Long marketCodeId, Pageable pageable);

    @Query(value = "SELECT new com.dodamsoft.todayfarmhub.dto.PriceStatisticsDto(" +
            "  CAST(AVG(p.price) AS integer)" +
            ", CAST(MAX(p.price) AS integer)" +
            ", CAST(MIN(p.price) AS integer)" +
            ", REPLACE(p.unitname, ' ', '')) " +
            "FROM Prices p " + commonWhereQuery + " AND p.marketCode.id = ?5 GROUP BY p.unitname")
    Page<PriceStatisticsDto> findPriceStatisticsByConditions(String dates, Long lClassCodeId, Long mClassCodeId, Long sClassCodeId, Long marketCodeId, Pageable pageable);

    @Query(value = "SELECT new com.dodamsoft.todayfarmhub.dto.PriceTradeCountDto(" +
            "COUNT(p.id), COALESCE(SUM(p.tradeamt), 0.0)) " +
            "FROM Prices p " +
            "WHERE p.dates = ?1 " +
            "AND p.lClassCode.id = ?2 " +
            "AND p.mClassCode.id = ?3 " +
            "AND p.sClassCode.id = ?4 " +
            "AND p.marketCode.id = ?5")
    PriceTradeCountDto findPriceTradeCountStatisticsByConditions(
            String dates,
            Long lClassCodeId,
            Long mClassCodeId,
            Long sClassCodeId,
            Long marketCodeId);


}
