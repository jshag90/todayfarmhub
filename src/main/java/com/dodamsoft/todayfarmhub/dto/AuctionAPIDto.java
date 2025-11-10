package com.dodamsoft.todayfarmhub.dto;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class AuctionAPIDto {

    /**
     * 총 데이터 건수
     */
    @SerializedName("totCnt")
    private int totCnt;

    /**
     * 결과 리스트
     */
    @SerializedName("resultList")
    private List<ResultList> resultList = new ArrayList<>();

    /**
     * 총 페이지 수 계산 (pageSize 단위)
     */
    public int getTotalPage(int pageSize) {
        if (pageSize <= 0) return 1;
        return (int) Math.ceil((double) totCnt / pageSize);
    }

    /**
     * API 결과 리스트 내부 클래스
     */
    @Data
    public static class ResultList {

        private String bidtime;       // 입찰 시간
        private String coco;          // 도매시장 코드?
        private String cocode;        // 업체 코드?
        private String coname;        // 업체 이름
        private String dates;         // 거래일
        private Integer price;        // 가격
        private String sanco;         // 산지
        private String sanji;         // 산지 상세
        private String unitname;      // 단위
        private Integer tradeamt;     // 거래량
        private String lclasscode;    // 대분류 코드
        private String mclasscode;    // 중분류 코드
        private String sclasscode;    // 소분류 코드
        private String marketcode;    // 도매시장 코드

        // 필요한 경우 다른 필드 추가 가능
    }
}
