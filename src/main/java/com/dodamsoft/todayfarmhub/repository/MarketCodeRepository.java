package com.dodamsoft.todayfarmhub.repository;

import com.dodamsoft.todayfarmhub.entity.MarketCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface MarketCodeRepository extends JpaRepository<MarketCode, Long> {
    boolean existsByMarketCode(String marketCode);

    MarketCode findOneByMarketCode(String marketCode);

    @Query("SELECT m FROM MarketCode m ORDER BY CASE WHEN m.marketName LIKE %:keyword% THEN 0 ELSE 1 END, m.marketName ASC")
    List<MarketCode> findAllOrderedByNameWithKeywordFirst(@Param("keyword") String keyword);

}
