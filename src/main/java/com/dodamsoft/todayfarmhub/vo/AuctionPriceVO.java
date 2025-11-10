package com.dodamsoft.todayfarmhub.vo;

import lombok.*;

@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuctionPriceVO {

    private String startDate;
    private String endDate;
    private String speciesName;
    private String lClassCode;
    private String mClassCode;
    private String sClassCode;
    private String marketCode;

    // 클라이언트 요청용 페이지 번호
    private int pageNumber;

    // API 호출용 페이지 인덱스
    private int pageIndex = 1;

    // API 호출 시 limit (한 페이지에 가져올 데이터 수)
    private String limit = "50";

}
