package com.dodamsoft.todayfarmhub.repository;

import com.dodamsoft.todayfarmhub.entity.MarketCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MarketCodeRepository extends JpaRepository<MarketCode, Long> {
    boolean existsByMarketCode(String marketCode);


}
