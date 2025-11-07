package com.dodamsoft.todayfarmhub.dto;

import lombok.*;

import java.util.List;

@Builder
@Setter
@Getter
@ToString
public class SClassAPIDto {

    private Response response;

    @Builder
    @Setter
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        private Header header;
        private Body body;
    }

    @Builder
    @Setter
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Header {
        private String resultCode;
        private String resultMsg;
    }

    @Builder
    @Setter
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Body {
        private Items items;
        private int numOfRows;
        private int pageNo;
        private int totalCount;
    }

    @Builder
    @Setter
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Items {
        private List<Item> item;
    }

    @Builder
    @Setter
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Item {
        private String gds_sclsf_cd;   // 소분류 코드
        private String gds_sclsf_nm;   // 소분류 이름
    }
}