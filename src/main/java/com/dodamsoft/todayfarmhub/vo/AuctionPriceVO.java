package com.dodamsoft.todayfarmhub.vo;

import com.dodamsoft.todayfarmhub.repository.MarketCodeRepository;
import lombok.*;

@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuctionPriceVO {

    // 날짜 범위 (DB 조회용)
    private String startDate;
    private String endDate;

    // 품목 정보
    private String speciesName;
    private String lClassCode;  // 대분류 코드
    private String mClassCode;  // 중분류 코드
    private String sClassCode;  // 소분류 코드
    private String marketCode;  // 시장 코드

    // 페이징 (클라이언트 요청용, 1부터 시작)
    private int pageNumber = 1;


}