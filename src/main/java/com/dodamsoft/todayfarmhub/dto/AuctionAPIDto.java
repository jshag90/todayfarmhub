package com.dodamsoft.todayfarmhub.dto;

import lombok.*;

import java.util.List;

@Builder
@Setter
@Getter
@ToString
public class AuctionAPIDto {

    int totCnt;
    String pageIndex;
    PaginationInfo paginationInfo;
    List<ResultList> resultList;
    @Builder
    @Setter
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaginationInfo {
        int currentPageNo;
        int recordCountPerPage;
        int pageSize;
        int totalRecordCount;
        int totalPageCount;
        int firstPageNoOnPageList;
        int lastPageNoOnPageList;
        int firstRecordIndex;
        int lastRecordIndex;
        int firstPageNo;
        int lastPageNo;
    }

    @Builder
    @Setter
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ResultList {
        String dates;
        String bidtime;
        String sanco;
        String sanji;
        String coco;
        int price;
        int tradeamt;
        String unitname;
        String lclasscode;
        String lclassname;
        String mclasscode;
        String mclassname;
        String sclasscode;
        String sclassname;
        String cocode;
        String coname;
        String marketcode;
        String marketname;
        int rnum;
        int listRnum;
        int totCount;
    }

}
