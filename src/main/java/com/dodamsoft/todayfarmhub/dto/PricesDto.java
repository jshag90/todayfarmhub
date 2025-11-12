package com.dodamsoft.todayfarmhub.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Builder
@Getter
public class PricesDto {
    private Long id;
    private String dates;
    private String bidtime; // 최종 안드로이드용
    private String sanco;
    private String sanji;
    private String coco;
    private Integer price;
    private String lclasscode;
    private String mclasscode;
    private String sclasscode;
    private String sclassname;
    private String marketCode;
    private String cocode;
    private String coname;
    private String unitname;
    private String tradeamt;

    public PricesDto(Long id, String dates, String bidtime, String sanco, String sanji,
                     String coco, Integer price, String lclasscode, String mclasscode,
                     String sclasscode, String sclassname, String marketCode, String cocode,
                     String coname, String unitname, String tradeamt) {
        this.id = id;
        this.dates = dates;
        this.bidtime = formatBidTimeForAndroid(bidtime);
        this.sanco = sanco;
        this.sanji = sanji;
        this.coco = coco;
        this.price = price;
        this.lclasscode = lclasscode;
        this.mclasscode = mclasscode;
        this.sclasscode = sclasscode;
        this.sclassname = sclassname;
        this.marketCode = marketCode;
        this.cocode = cocode;
        this.coname = coname;
        this.unitname = unitname;
        this.tradeamt = tradeamt;
    }

    private String formatBidTimeForAndroid(String bidtime) {
        // DB에는 yyyy-MM-dd HH:mm:ss
        DateTimeFormatter dbFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        DateTimeFormatter androidFormatter = DateTimeFormatter.ofPattern("MM/dd HH:mm:ss");

        LocalDateTime dateTime = LocalDateTime.parse(bidtime, dbFormatter);
        return dateTime.format(androidFormatter); // 연도 없이 MM-dd HH:mm:ss
    }

    // getter/setter 생략
}
