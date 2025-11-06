package com.dodamsoft.todayfarmhub.util;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum OriginAPIUrlEnum {

      GET_PRICES_URL("https://www.agrion.kr/portal/fdp/fpi/selectRltmAucBrkNewsTobeList.do")
    , GET_CATEGORY_INFO_URL("https://www.agrion.kr/portal/fdp/fpi/selectFrmprdPcInfoRealClasscodeList.do")
    , GET_MARKET_INFO_URL("https://apis.data.go.kr/B552845/katCode/wholesaleMarkets")
    ;

    private String url;
}
