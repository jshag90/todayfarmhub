package com.dodamsoft.todayfarmhub.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * 도매시장 정보 테이블
 */
@Entity
@Table(indexes = {@Index(name = "i_dates", columnList = "dates")})
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Prices {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    String dates;
    String bidtime;
    String sanco;
    String sanji;
    String coco;
    Integer price;
    @ManyToOne(targetEntity = MClassCode.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "m_class_code_id")
    MClassCode mClassCode;
    @ManyToOne(targetEntity = LClassCode.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "l_class_code_id")
    LClassCode lClassCode;
    @ManyToOne(targetEntity = SClassCode.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "s_class_code_id")
    SClassCode sClassCode;
    @ManyToOne(targetEntity = MarketCode.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "market_code_id")
    MarketCode marketCode;
    String cocode;
    String coname;
    String unitname;
    String tradeamt;
}
