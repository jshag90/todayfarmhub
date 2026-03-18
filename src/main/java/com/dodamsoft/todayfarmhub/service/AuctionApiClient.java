package com.dodamsoft.todayfarmhub.service;

import com.dodamsoft.todayfarmhub.dto.AuctionAPIDto;
import com.dodamsoft.todayfarmhub.util.ApiUrlBuilder;
import com.dodamsoft.todayfarmhub.util.OriginAPIUrlEnum;
import com.dodamsoft.todayfarmhub.vo.AuctionPriceVO;
import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.LinkedHashMap;
import java.util.Map;

import static com.dodamsoft.todayfarmhub.util.DateUtils.formatDateForApi;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuctionApiClient {

    private static final String RETURN_TYPE = "json";
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36";

    private final RestTemplate restTemplate;
    private final Gson gson;
    private final ApiUrlBuilder apiUrlBuilder;

    @Value("${api.kat.service-key-live}")
    private String liveServiceKey;

    public AuctionAPIDto fetchAuctionData(AuctionPriceVO vo, int pageIndex, int pageSize) {
        try {
            String url = buildUrl(vo, pageIndex, pageSize);
            log.info("Auction API URL: {}", url);

            ResponseEntity<String> response = executeRequest(url);
            return parseResponse(response);
        } catch (RestClientException e) {
            log.error("Auction API network error: pageIndex={}, error={}", pageIndex, e.getMessage());
            return new AuctionAPIDto();
        } catch (Exception e) {
            log.error("Unexpected error while calling auction API: pageIndex={}", pageIndex, e);
            return new AuctionAPIDto();
        }
    }

    private String buildUrl(AuctionPriceVO vo, int pageIndex, int pageSize) {
        Map<String, String> cond = new LinkedHashMap<>();
        cond.put("trd_clcln_ymd", formatDateForApi(vo.getEndDate()));
        cond.put("whsl_mrkt_cd", vo.getMarketCode());
        cond.put("gds_lclsf_cd", vo.getLClassCode());
        cond.put("gds_mclsf_cd", vo.getMClassCode());
        cond.put("gds_sclsf_cd", vo.getSClassCode());

        return apiUrlBuilder.buildUrl(
                OriginAPIUrlEnum.GET_LIVE_PRICES_URL.getUrl(),
                pageIndex,
                pageSize,
                RETURN_TYPE,
                cond,
                null,
                liveServiceKey
        );
    }

    private ResponseEntity<String> executeRequest(String url) {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.USER_AGENT, USER_AGENT);
        headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);

        HttpEntity<String> entity = new HttpEntity<>(headers);
        return restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
    }

    private AuctionAPIDto parseResponse(ResponseEntity<String> response) {
        if (response.getStatusCode() != HttpStatus.OK) {
            log.error("Auction API call failed: statusCode={}", response.getStatusCode());
            return new AuctionAPIDto();
        }

        String body = response.getBody();
        if (body == null || body.isEmpty()) {
            log.warn("Auction API response body is empty");
            return new AuctionAPIDto();
        }

        try {
            AuctionAPIDto auctionAPIDto = gson.fromJson(body, AuctionAPIDto.class);
            if (auctionAPIDto == null) {
                log.warn("Gson parsed a null auction response");
                return new AuctionAPIDto();
            }

            log.debug("Auction API response parsed successfully: totalCount={}", auctionAPIDto.getTotCnt());
            return auctionAPIDto;
        } catch (Exception e) {
            log.error("Failed to parse auction API response", e);
            return new AuctionAPIDto();
        }
    }
}
