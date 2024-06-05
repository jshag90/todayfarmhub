package com.dodamsoft.todayfarmhub.repository;

import com.dodamsoft.todayfarmhub.entity.Prices;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PricesRepository extends JpaRepository<Prices, Long> {


}
