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
    public <T> T getCategory(AuctionPriceVO auctionPriceVO) {

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

        log.info(auctionAPIVO.toString());

        saveInfoByResponseDataUsingAPI(auctionAPIVO, null, null);

        List<MarketInfoAPIDto.ResultList> resultList = new ArrayList<>();
        for (MarketCode marketCode : marketCodeRepository.findAll(Sort.by(Sort.Direction.ASC, "marketName"))) {
            resultList.add(MarketInfoAPIDto.ResultList.builder()
                    .marketCode(marketCode.getMarketCode())
                    .marketName(marketCode.getMarketName())
                    .build());
        }

        return (T) MarketInfoAPIDto.builder().resultList(resultList).build();
    }

    @Override
    public <T> void saveInfoByResponseDataUsingAPI(T t, LClassCode lClassCode, MClassCode mClassCode) {
        try {

            String responseData = HttpCallUtil.getHttpPost(OriginAPIUrlEnum.GET_MARKET_INFO_URL.getUrl(), gson.toJson(t));
            log.info(responseData);

            MarketInfoAPIDto marketInfoAPIDto = gson.fromJson(responseData, MarketInfoAPIDto.class);
            for (MarketInfoAPIDto.ResultList resultList : marketInfoAPIDto.getResultList()) {

                if (marketCodeRepository.existsByMarketCode(resultList.getMarketCode()))
                    continue;

                marketCodeRepository.save(MarketCode.builder()
                        .marketName(resultList.getMarketName())
                        .marketCode(resultList.getMarketCode())
                        .build());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
