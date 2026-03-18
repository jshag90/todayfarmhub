package com.dodamsoft.todayfarmhub.util;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ApiUrlBuilderTest {

    @Test
    void buildUrlUsesOverrideKeyAndSkipsBlankConditions() {
        ApiUrlBuilder builder = new ApiUrlBuilder();
        ReflectionTestUtils.setField(builder, "serviceKey", "default-key");

        String url = builder.buildUrl(
                "https://example.com/live",
                2,
                50,
                "json",
                Map.of(
                        "trd_clcln_ymd", "2026-03-17",
                        "whsl_mrkt_cd", "320101"),
                null,
                "live-key");

        String urlWithoutBlankCondition = builder.buildUrl(
                "https://example.com/live",
                2,
                50,
                "json",
                Map.of("gds_sclsf_cd", " "),
                null,
                "live-key");

        assertThat(url).contains("serviceKey=live-key");
        assertThat(url).contains("pageNo=2");
        assertThat(url).contains("numOfRows=50");
        assertThat(url).contains("cond[trd_clcln_ymd::EQ]=2026-03-17");
        assertThat(url).contains("cond[whsl_mrkt_cd::EQ]=320101");
        assertThat(urlWithoutBlankCondition).doesNotContain("cond[gds_sclsf_cd::EQ]");
    }
}
