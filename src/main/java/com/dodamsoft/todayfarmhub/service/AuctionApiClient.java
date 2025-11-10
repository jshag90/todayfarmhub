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
import org.springframework.web.util.UriComponentsBuilder;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuctionApiClient {

    private final RestTemplate restTemplate;
    private final Gson gson;

    @Value("${api.kat.service-key}")
    private String serviceKey;

    /**
     * Auction API 호출 (GET 방식)
     *
     * @param auctionAPIVO 요청 VO
     * @param pageIndex    조회할 페이지
     * @param pageSize     한 페이지 조회 건수
     * @return AuctionAPIDto 응답 DTO
     */
    public AuctionAPIDto fetchAuctionData(Object auctionAPIVO, int pageIndex, int pageSize) {
        try {
            // GET 방식은 VO 필드들을 쿼리 파라미터로 변환
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(OriginAPIUrlEnum.GET_PRICES_URL.getUrl())
                    .queryParam("serviceKey", serviceKey)
                    .queryParam("pageNo", pageIndex)
                    .queryParam("numOfRows", pageSize)
                    .queryParam("returnType", "json");

            // auctionAPIVO 필드들을 쿼리로 추가
            if (auctionAPIVO != null) {
                var fields = auctionAPIVO.getClass().getDeclaredFields();
                for (var field : fields) {
                    field.setAccessible(true);
                    Object value = field.get(auctionAPIVO);
                    if (value != null) {
                        // 예: cond[gds_lclsf_cd::EQ]=12 형태로 변환 필요
                        String paramName = "cond[" + field.getName() + "::EQ]";
                        builder.queryParam(paramName, value.toString());
                    }
                }
            }

            String url = builder.toUriString();
            log.info("Auction API GET URL: {}", url);

            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

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
