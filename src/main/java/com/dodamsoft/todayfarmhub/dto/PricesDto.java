package com.dodamsoft.todayfarmhub.dto;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class PricesDto {
    Long id;
    String dates;
    String bidtime;
    String sanco;
    String sanji;
    String coco;
    Integer price;
    String lclasscode;
    String mclasscode;
    String sclasscode;
    String marketCode;
    String cocode;
    String coname;
    String unitname;
    String tradeamt;
}
