package com.dodamsoft.todayfarmhub.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * 도매시장 정보 테이블
 */
@Entity
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class MarketCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    String marketCode;
    String marketName;


}
