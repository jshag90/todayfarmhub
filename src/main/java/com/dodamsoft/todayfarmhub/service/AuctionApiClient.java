package com.dodamsoft.todayfarmhub.service;

import com.dodamsoft.todayfarmhub.dto.AuctionAPIDto;
import com.dodamsoft.todayfarmhub.util.OriginAPIUrlEnum;
import com.dodamsoft.todayfarmhub.vo.AuctionPriceVO;
import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import static com.dodamsoft.todayfarmhub.util.DateUtils.formatDateForApi;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuctionApiClient {

    private static final String RETURN_TYPE = "json";
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36";

    private final RestTemplate restTemplate;
    private final Gson gson;

    @Value("${api.kat.service-key}")
    private String serviceKey;

    /**
     * Auction API 호출
     *
     * @param vo        요청 VO
     * @param pageIndex 조회할 페이지 (1부터 시작)
     * @param pageSize  한 페이지 조회 건수
     * @return AuctionAPIDto 응답 DTO (실패 시 빈 객체)
     */
    public AuctionAPIDto fetchAuctionData(AuctionPriceVO vo, int pageIndex, int pageSize) {
        try {
            String url = buildUrl(vo, pageIndex, pageSize);
            log.info("Auction API URL: {}", url);

            ResponseEntity<String> response = executeRequest(url);

            return parseResponse(response);

        } catch (RestClientException e) {
            log.error("Auction API 네트워크 오류: pageIndex={}, error={}", pageIndex, e.getMessage());
            return new AuctionAPIDto();
        } catch (Exception e) {
            log.error("Auction API 호출 중 예외 발생: pageIndex={}", pageIndex, e);
            return new AuctionAPIDto();
        }
    }

    /**
     * API URL 생성
     */
    private String buildUrl(AuctionPriceVO vo, int pageIndex, int pageSize) {
        StringBuilder url = new StringBuilder(OriginAPIUrlEnum.GET_PRICES_URL.getUrl());

        // 기본 파라미터
        url.append("?serviceKey=").append(serviceKey)
                .append("&pageNo=").append(pageIndex)
                .append("&numOfRows=").append(pageSize)
                .append("&returnType=").append(RETURN_TYPE);

        // 조건 파라미터 (인코딩하지 않음)
        appendCondition(url, "trd_clcln_ymd", formatDateForApi(vo.getEndDate()));
        appendCondition(url, "whsl_mrkt_cd", vo.getMarketCode());
        appendCondition(url, "gds_lclsf_cd", vo.getLClassCode());
        appendCondition(url, "gds_mclsf_cd", vo.getMClassCode());
        appendCondition(url, "gds_sclsf_cd", vo.getSClassCode());

        return url.toString();
    }

    /**
     * 조건 파라미터 추가
     */
    private void appendCondition(StringBuilder url, String field, String value) {
        if (value != null && !value.isEmpty()) {
            url.append("&cond[").append(field).append("::EQ]=").append(value);
        }
    }

    /**
     * HTTP 요청 실행
     */
    private ResponseEntity<String> executeRequest(String url) {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.USER_AGENT, USER_AGENT);
        headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        return restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
    }

    /**
     * 응답 파싱
     */
    private AuctionAPIDto parseResponse(ResponseEntity<String> response) {
        if (response.getStatusCode() != HttpStatus.OK) {
            log.error("Auction API 호출 실패: statusCode={}", response.getStatusCode());
            return new AuctionAPIDto();
        }

        String body = response.getBody();
        if (body == null || body.isEmpty()) {
            log.warn("Auction API 응답 본문이 비어있음");
            return new AuctionAPIDto();
        }

        try {
            AuctionAPIDto auctionAPIDto = gson.fromJson(body, AuctionAPIDto.class);

            if (auctionAPIDto == null) {
                log.warn("Gson 파싱 결과가 null");
                return new AuctionAPIDto();
            }

            log.debug("Auction API 응답 파싱 성공: totalCount={}", auctionAPIDto.getTotCnt());
            return auctionAPIDto;

        } catch (Exception e) {
            log.error("Auction API 응답 파싱 실패", e);
            return new AuctionAPIDto();
        }
    }
}