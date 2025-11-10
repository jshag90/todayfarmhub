package com.dodamsoft.todayfarmhub.service;

import com.dodamsoft.todayfarmhub.dto.AuctionAPIDto;
import com.dodamsoft.todayfarmhub.util.OriginAPIUrlEnum;
import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuctionApiClient {

    private final RestTemplate restTemplate;
    private final Gson gson;

    @Value("${api.kat.service-key}")
    private String serviceKey;

    /**
     * Auction API 호출
     *
     * @param auctionAPIVO 요청 VO
     * @param pageIndex    조회할 페이지
     * @param pageSize     한 페이지 조회 건수
     * @return AuctionAPIDto 응답 DTO
     */
    public AuctionAPIDto fetchAuctionData(Object auctionAPIVO, int pageIndex, int pageSize) {

        try {
            // 페이지, limit 세팅
            auctionAPIVO.getClass().getMethod("setPageIndex", int.class).invoke(auctionAPIVO, pageIndex);
            auctionAPIVO.getClass().getMethod("setLimit", String.class).invoke(auctionAPIVO, String.valueOf(pageSize));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            String requestBody = gson.toJson(auctionAPIVO);

            HttpEntity<String> request = new HttpEntity<>(requestBody, headers);

            String url = OriginAPIUrlEnum.GET_PRICES_URL.getUrl() + "?serviceKey=" + serviceKey;

            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                String body = response.getBody();
                log.info("Auction API Response: {}", body);
                return gson.fromJson(body, AuctionAPIDto.class);
            } else {
                log.error("Auction API 호출 실패: statusCode={}", response.getStatusCode());
            }

        } catch (Exception e) {
            log.error("Auction API 호출 중 예외 발생", e);
        }

        return new AuctionAPIDto(); // 실패 시 빈 객체 반환
    }
}
