package com.dodamsoft.todayfarmhub.vo;

import lombok.*;

@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuctionPriceVO {

    String startDate;
    String endDate;
    String speciesName;

}
