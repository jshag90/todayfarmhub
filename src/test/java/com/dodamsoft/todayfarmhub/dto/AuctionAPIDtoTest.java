package com.dodamsoft.todayfarmhub.dto;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class AuctionAPIDtoTest {

    @Test
    void mapToLegacyFieldsFallsBackToQtyWhenUnitTotalQuantityIsMissing() {
        AuctionAPIDto.ResultList item = new AuctionAPIDto.ResultList();
        ReflectionTestUtils.setField(item, "scsbd_dt", "2026-03-18 06:51:32");
        ReflectionTestUtils.setField(item, "trd_clcln_ymd", "2026-03-17");
        ReflectionTestUtils.setField(item, "whsl_mrkt_cd", "320101");
        ReflectionTestUtils.setField(item, "corp_cd", "32010101");
        ReflectionTestUtils.setField(item, "plor_cd", "680010");
        ReflectionTestUtils.setField(item, "unit_nm", "kg");
        ReflectionTestUtils.setField(item, "gds_lclsf_cd", "08");
        ReflectionTestUtils.setField(item, "gds_mclsf_cd", "04");
        ReflectionTestUtils.setField(item, "gds_sclsf_cd", "99");
        ReflectionTestUtils.setField(item, "scsbd_prc", "7000.000");
        ReflectionTestUtils.setField(item, "qty", "20.000");
        ReflectionTestUtils.setField(item, "unit_qty", "1.000");

        item.mapToLegacyFields();

        assertThat(item.getBidtime()).isEqualTo("2026-03-18 06:51:32");
        assertThat(item.getDates()).isEqualTo("2026-03-17");
        assertThat(item.getMarketcode()).isEqualTo("320101");
        assertThat(item.getPrice()).isEqualTo(7000);
        assertThat(item.getTradeamt()).isEqualTo(20);
    }

    @Test
    void mapToLegacyFieldsUsesBidtimeFallbackWhenAuctionTimeIsMissing() {
        AuctionAPIDto.ResultList item = new AuctionAPIDto.ResultList();
        ReflectionTestUtils.setField(item, "trd_clcln_ymd", "2026-03-17");

        item.mapToLegacyFields();

        assertThat(item.getBidtime()).isEqualTo("2026-03-17 23:00:00");
    }
}
