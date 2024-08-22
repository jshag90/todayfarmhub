package com.dodamsoft.todayfarmhub.dto;

import lombok.*;

@Getter
@Builder
public class PriceStatisticsDto {
    Integer avg;
    Integer max;
    Integer min;
    String unitname;
}
