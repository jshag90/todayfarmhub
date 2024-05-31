package com.dodamsoft.todayfarmhub.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

/**
 * 도매시장 정보 테이블
 */
@Entity
public class MarketCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    String marketCode;
    String marketName;

}
