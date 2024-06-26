package com.dodamsoft.todayfarmhub.vo;

import lombok.*;

@Builder
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class AuctionAPIVO {
    String ccName;
    String cc_arr;
    String sDate;
    String eDate;
    String flag;
    String lClassCode;
    String lcate;
    String limit;
    String mClassCode;
    int pageIndex;
    String sClassCode_arr;
    String sClassName;
    String sort;
    String sortGbn;
    String wcName;
    String wc_arr;


    @Builder
    @Setter
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    public static class Market {
        String lClassCode;
        String mClassCode;
        String sClassCode_arr;
        String sClassName;
        String wcName;
        String wc_arr;
        String ccName;
        String cc_arr;
        String lcate;
        String sDate;
        String eDate;
        String sort;
        String sortGbn;
    }
}
