package com.dodamsoft.todayfarmhub.util;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum OriginAPIUrlEnum {

    GET_PRICES_URL("https://www.agrion.kr/portal/fdp/fpi/selectRltmAucBrkNewsTobeList.do")
    , GET_LCLASS_URL("https://www.agrion.kr/portal/fdp/fpi/selectFrmprdPcInfoRealClasscodeList.do")
    ;

    private String url;
}
