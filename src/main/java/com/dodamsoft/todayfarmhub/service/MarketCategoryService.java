package com.dodamsoft.todayfarmhub.service;

import com.dodamsoft.todayfarmhub.dto.Market;
import com.dodamsoft.todayfarmhub.dto.MarketInfoAPIDto;
import com.dodamsoft.todayfarmhub.dto.CategoryListResponse;
import com.dodamsoft.todayfarmhub.entity.LClassCode;
import com.dodamsoft.todayfarmhub.entity.MClassCode;
import com.dodamsoft.todayfarmhub.entity.MarketCode;
import com.dodamsoft.todayfarmhub.repository.MarketCodeRepository;
import com.dodamsoft.todayfarmhub.util.ApiUrlBuilder;
import com.dodamsoft.todayfarmhub.util.CategoryType;
import com.dodamsoft.todayfarmhub.util.HttpCallUtil;
import com.dodamsoft.todayfarmhub.util.OriginAPIUrlEnum;
import com.dodamsoft.todayfarmhub.vo.AuctionPriceVO;
import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("marketCategoryService")
@Slf4j
@RequiredArgsConstructor
public class MarketCategoryService implements GetAuctionCategoryService {

    private final MarketCodeRepository marketCodeRepository;
    private final Gson gson;
    private final ApiUrlBuilder apiUrlBuilder;

    @Override
    public boolean isType(CategoryType categoryType) {
        return CategoryType.MARKET.equals(categoryType);
    }

    @Override
    @SuppressWarnings("unchecked")
    @Cacheable(value = "auctionCategoryCache", key = "'MARKET_LIST'", unless = "#result == null")
    public CategoryListResponse<?> getCategory(AuctionPriceVO auctionPriceVO) {

        // DB에 데이터 없으면 API로 가져와서 저장
        if (marketCodeRepository.count() < 1) {
            saveInfoByResponseDataUsingAPI(null, null);
        }

        // resultList 형태로 변환
        List<Market> resultList = marketCodeRepository.findAllOrderedByNameWithKeywordFirst("서울")
                .stream()
                .map(marketCode -> new Market(
                        marketCode.getMarketCode(),
                        marketCode.getMarketName()))
                .toList();

        return new CategoryListResponse<>(resultList);
    }

    @Override
    public <T> void saveInfoByResponseDataUsingAPI(LClassCode lClassCode, MClassCode mClassCode) {

        String getMarketInfoUrl = apiUrlBuilder.buildUrl(
                OriginAPIUrlEnum.GET_MARKET_INFO_URL.getUrl(),
                1,
                50,
                "json",
                null,
                List.of("whsl_mrkt_cd", "whsl_mrkt_nm"));

        log.info("요청 url : {}", getMarketInfoUrl);
        String responseData = HttpCallUtil.getHttpGet(getMarketInfoUrl);
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

    }

}
