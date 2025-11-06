package com.dodamsoft.todayfarmhub.service;

import com.dodamsoft.todayfarmhub.dto.MarketInfoAPIDto;
import com.dodamsoft.todayfarmhub.dto.SClassAPIDto;
import com.dodamsoft.todayfarmhub.entity.LClassCode;
import com.dodamsoft.todayfarmhub.entity.MClassCode;
import com.dodamsoft.todayfarmhub.entity.MarketCode;
import com.dodamsoft.todayfarmhub.entity.SClassCode;
import com.dodamsoft.todayfarmhub.repository.LClassCodeRepository;
import com.dodamsoft.todayfarmhub.repository.MClassCodeRepository;
import com.dodamsoft.todayfarmhub.repository.MarketCodeRepository;
import com.dodamsoft.todayfarmhub.repository.SClassCodeRepository;
import com.dodamsoft.todayfarmhub.util.HttpCallUtil;
import com.dodamsoft.todayfarmhub.util.OriginAPIUrlEnum;
import com.dodamsoft.todayfarmhub.vo.AuctionAPIVO;
import com.dodamsoft.todayfarmhub.vo.AuctionPriceVO;
import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service("marketCategoryService")
@Slf4j
@RequiredArgsConstructor
public class MarketCategoryService implements GetAuctionCategoryService {

    private final MarketCodeRepository marketCodeRepository;
    private final Gson gson;

    @Override
    public MarketInfoAPIDto getCategory(AuctionPriceVO auctionPriceVO) {

        AuctionAPIVO.Market auctionAPIVO = AuctionAPIVO.Market.builder()
                .lClassCode("")
                .mClassCode("")
                .sClassCode_arr("")
                .sClassName("")
                .wc_arr("")
                .wcName("")
                .cc_arr("")
                .ccName("")
                .lcate("prd")
                .sDate(auctionPriceVO.getStartDate())
                .eDate(auctionPriceVO.getEndDate())
                .sort("desc")
                .sortGbn("")
                .build();

        log.info("AuctionAPIVO: {}", auctionAPIVO);

        // DB에 데이터 없으면 API로 가져와서 저장
        if (marketCodeRepository.count() < 1) {
            saveInfoByResponseDataUsingAPI(auctionAPIVO, null, null);
        }

        // "서울" 키워드로 정렬된 시장 목록 조회
        List<MarketCode> marketNameList = marketCodeRepository.findAllOrderedByNameWithKeywordFirst("서울");

        // ResultList로 변환
        List<MarketInfoAPIDto.ResultList> itemList = marketNameList.stream()
                .map(marketCode -> MarketInfoAPIDto.ResultList.builder()
                        .whsl_mrkt_cd(marketCode.getMarketCode())
                        .whsl_mrkt_nm(marketCode.getMarketName())
                        .build())
                .toList();

        // 전체 응답 구조 생성
        MarketInfoAPIDto.Items items = MarketInfoAPIDto.Items.builder()
                .item(itemList)
                .build();

        MarketInfoAPIDto.Body body = MarketInfoAPIDto.Body.builder()
                .dataType("JSON")
                .numOfRows(itemList.size())
                .pageNo(1)
                .totalCount(itemList.size())
                .items(items)
                .build();

        MarketInfoAPIDto.Header header = MarketInfoAPIDto.Header.builder()
                .resultCode("0")
                .resultMsg("정상")
                .build();

        MarketInfoAPIDto.Response response = MarketInfoAPIDto.Response.builder()
                .header(header)
                .body(body)
                .build();

        return MarketInfoAPIDto.builder()
                .response(response)
                .build();
    }

    @Override
    public <T> void saveInfoByResponseDataUsingAPI(T t, LClassCode lClassCode, MClassCode mClassCode) {
        try {


            // 사용 예시 (완성된 URL 생성)
            String getMarketInfoUrl = OriginAPIUrlEnum.GET_MARKET_INFO_URL.getUrl() + "?"
                    + "serviceKey=7661d3c8bad3519c927fa736cc3214fed973dad9520645c34a1a1f1f20344d46"
                    + "&pageNo=1"
                    + "&numOfRows=33"
                    + "&returnType=json"
                    + "&selectable=whsl_mrkt_cd,whsl_mrkt_nm";

            String responseData = HttpCallUtil.getHttpGet(OriginAPIUrlEnum.GET_MARKET_INFO_URL.getUrl());
            log.info(responseData);

            MarketInfoAPIDto marketInfoAPIDto = gson.fromJson(responseData, MarketInfoAPIDto.class);
            for (MarketInfoAPIDto.ResultList resultList : marketInfoAPIDto.getResponse().getBody().getItems().getItem()) {

                if (marketCodeRepository.existsByMarketCode(resultList.getWhsl_mrkt_cd()))
                    continue;

                marketCodeRepository.save(MarketCode.builder()
                        .marketName(resultList.getWhsl_mrkt_nm())
                        .marketCode(resultList.getWhsl_mrkt_cd())
                        .build());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
