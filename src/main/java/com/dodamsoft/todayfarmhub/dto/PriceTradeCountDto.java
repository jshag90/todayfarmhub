package com.dodamsoft.todayfarmhub.dto;


import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class PriceTradeCountDto {
    private Long count;
    private BigDecimal tradeAmtSum;
}