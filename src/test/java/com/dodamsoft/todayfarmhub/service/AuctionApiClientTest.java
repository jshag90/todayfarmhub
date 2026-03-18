package com.dodamsoft.todayfarmhub.service;

import com.dodamsoft.todayfarmhub.dto.AuctionAPIDto;
import com.dodamsoft.todayfarmhub.util.ApiUrlBuilder;
import com.dodamsoft.todayfarmhub.vo.AuctionPriceVO;
import com.google.gson.Gson;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class AuctionApiClientTest {

    private AuctionApiClient auctionApiClient;
    private MockRestServiceServer server;

    @BeforeEach
    void setUp() {
        RestTemplate restTemplate = new RestTemplate();
        ApiUrlBuilder apiUrlBuilder = new ApiUrlBuilder();
        ReflectionTestUtils.setField(apiUrlBuilder, "serviceKey", "default-key");

        auctionApiClient = new AuctionApiClient(restTemplate, new Gson(), apiUrlBuilder);
        ReflectionTestUtils.setField(auctionApiClient, "liveServiceKey", "live-key");
        server = MockRestServiceServer.createServer(restTemplate);
    }

    @Test
    void fetchAuctionDataUsesLiveEndpointAndLiveServiceKey() {
        server.expect(requestTo(containsString("/katRealTime2/trades2?serviceKey=live-key")))
                .andExpect(requestTo(containsString("cond%5Btrd_clcln_ymd::EQ%5D=2026-03-17")))
                .andExpect(requestTo(containsString("cond%5Bwhsl_mrkt_cd::EQ%5D=320101")))
                .andExpect(method(GET))
                .andRespond(withSuccess("""
                        {
                          "response": {
                            "body": {
                              "dataType": "JSON",
                              "items": {
                                "item": [
                                  {
                                    "scsbd_dt": "2026-03-18 06:51:32",
                                    "trd_clcln_ymd": "2026-03-17",
                                    "whsl_mrkt_cd": "320101",
                                    "gds_lclsf_cd": "08",
                                    "gds_mclsf_cd": "04",
                                    "gds_sclsf_cd": "99",
                                    "scsbd_prc": "7000.000",
                                    "qty": "20.000",
                                    "unit_qty": "1.000"
                                  }
                                ]
                              },
                              "numOfRows": 1,
                              "pageNo": 1,
                              "totalCount": 1
                            },
                            "header": {
                              "resultCode": "0",
                              "resultMsg": "OK"
                            }
                          }
                        }
                        """, MediaType.APPLICATION_JSON));

        AuctionPriceVO vo = AuctionPriceVO.builder()
                .endDate("2026-03-17")
                .marketCode("320101")
                .lClassCode("08")
                .mClassCode("04")
                .sClassCode("99")
                .build();

        AuctionAPIDto response = auctionApiClient.fetchAuctionData(vo, 1, 50);

        assertThat(response.getTotCnt()).isEqualTo(1);
        assertThat(response.getResultList()).hasSize(1);
        server.verify();
    }
}
