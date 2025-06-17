package com.dodamsoft.todayfarmhub.service;

import com.dodamsoft.todayfarmhub.dto.AuctionAPIDto;
import com.dodamsoft.todayfarmhub.dto.PriceStatisticsDto;
import com.dodamsoft.todayfarmhub.dto.PriceTradeCountDto;
import com.dodamsoft.todayfarmhub.dto.PricesDto;
import com.dodamsoft.todayfarmhub.entity.Prices;
import com.dodamsoft.todayfarmhub.repository.*;
import com.dodamsoft.todayfarmhub.util.HttpCallUtil;
import com.dodamsoft.todayfarmhub.util.OriginAPIUrlEnum;
import com.dodamsoft.todayfarmhub.vo.AuctionAPIVO;
import com.dodamsoft.todayfarmhub.vo.AuctionPriceVO;
import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuctionService {

    private static final String pageSize = "50";
    private static final String orderByField = "bidtime";
    private static final Integer statisticsPageSize = 5000;
    private final PricesRepository pricesRepository;
    private final LClassCodeRepository lClassCodeRepository;
    private final MClassCodeRepository mClassCodeRepository;
    private final SClassCodeRepository sClassCodeRepository;
    private final MarketCodeRepository marketCodeRepository;
    private final Gson gson;

    public Page<PricesDto> getAuctionPricesByOriginOpenAPIURL(AuctionPriceVO auctionPriceVO) {

        if (!pricesRepository.existsByDatesAndLClassCodeIdAndMClassCodeIdAndSClassCodeId(auctionPriceVO.getEndDate()
                , lClassCodeRepository.findOneBylclasscode(auctionPriceVO.getLClassCode()).getId()
                , mClassCodeRepository.findOneBymclasscode(auctionPriceVO.getMClassCode()).getId()
                , sClassCodeRepository.findOneBysclasscode(auctionPriceVO.getSClassCode()).getId()
                , marketCodeRepository.findOneByMarketCode(auctionPriceVO.getMarketCode()).getId()
        )) {

            AuctionAPIVO auctionAPIVO = AuctionAPIVO.builder()
                    .lClassCode(auctionPriceVO.getLClassCode())
                    .mClassCode(auctionPriceVO.getMClassCode())
                    .sClassCode_arr(auctionPriceVO.getSClassCode())
                    .sClassName("")
                    .wc_arr(auctionPriceVO.getMarketCode())
                    .wcName("")
                    .cc_arr("")
                    .ccName("")
                    .lcate("prd")
                    .sDate(auctionPriceVO.getStartDate())
                    .eDate(auctionPriceVO.getEndDate())
                    .sort("desc")
                    .sortGbn("")
                    .pageIndex(1)
                    .limit(pageSize)
                    .build();

            String firstResponseData = HttpCallUtil.getHttpPost(OriginAPIUrlEnum.GET_PRICES_URL.getUrl(), gson.toJson(auctionAPIVO));
            log.info(firstResponseData);

            AuctionAPIDto firstAuctionAPIDto = gson.fromJson(firstResponseData, AuctionAPIDto.class);
            log.info("총 데이터 갯수 : " + firstAuctionAPIDto.getTotCnt());

            if (firstAuctionAPIDto.getTotCnt() != 0) {

                saveAuctionPrices(firstAuctionAPIDto);

                for (int index = 2; index <= firstAuctionAPIDto.getTotalPage(pageSize); index++) {

                    AuctionAPIVO nextPageAuctionAPIVO = new AuctionAPIVO();
                    BeanUtils.copyProperties(auctionAPIVO, nextPageAuctionAPIVO);
                    nextPageAuctionAPIVO.setPageIndex(index);

                    String responseData = HttpCallUtil.getHttpPost(OriginAPIUrlEnum.GET_PRICES_URL.getUrl(), gson.toJson(nextPageAuctionAPIVO));

                    log.info(responseData);

                    AuctionAPIDto auctionAPIDto = gson.fromJson(responseData, AuctionAPIDto.class);
                    saveAuctionPrices(auctionAPIDto);
                }

            }

        }

        return pricesRepository.findByDatesAndLClassCodeAndMClassCodeAndSClassCode(auctionPriceVO.getStartDate(),
                lClassCodeRepository.findOneBylclasscode(auctionPriceVO.getLClassCode()).getId(),
                mClassCodeRepository.findOneBymclasscode(auctionPriceVO.getMClassCode()).getId(),
                sClassCodeRepository.findOneBysclasscode(auctionPriceVO.getSClassCode()).getId(),
                marketCodeRepository.findOneByMarketCode(auctionPriceVO.getMarketCode()).getId(),
                PageRequest.of(auctionPriceVO.getPageNumber() - 1, Integer.parseInt(pageSize), Sort.Direction.DESC, orderByField)
        );

    }

    private void saveAuctionPrices(AuctionAPIDto auctionAPIDto) {
        for (AuctionAPIDto.ResultList resultList : auctionAPIDto.getResultList()) {
            pricesRepository.save(Prices.builder()
                    .bidtime(resultList.getBidtime())
                    .coco(resultList.getCoco())
                    .cocode(resultList.getCocode())
                    .coname(resultList.getConame())
                    .dates(resultList.getDates())
                    .price(resultList.getPrice())
                    .sanco(resultList.getSanco())
                    .sanji(resultList.getSanji())
                    .unitname(resultList.getUnitname())
                    .tradeamt(resultList.getTradeamt())
                    .lClassCode(lClassCodeRepository.findOneBylclasscode(resultList.getLclasscode()))
                    .mClassCode(mClassCodeRepository.findOneBymclasscode(resultList.getMclasscode()))
                    .sClassCode(sClassCodeRepository.findOneBysclasscode(resultList.getSclasscode()))
                    .marketCode(marketCodeRepository.findOneByMarketCode(resultList.getMarketcode()))
                    .build());
        }
    }

    public Page<PriceStatisticsDto> findPriceStatisticsByConditions(AuctionPriceVO auctionPriceVO) {
        Page<PriceStatisticsDto> priceStatisticsByConditions = pricesRepository.findPriceStatisticsByConditions(auctionPriceVO.getStartDate(),
                lClassCodeRepository.findOneBylclasscode(auctionPriceVO.getLClassCode()).getId(),
                mClassCodeRepository.findOneBymclasscode(auctionPriceVO.getMClassCode()).getId(),
                sClassCodeRepository.findOneBysclasscode(auctionPriceVO.getSClassCode()).getId(),
                marketCodeRepository.findOneByMarketCode(auctionPriceVO.getMarketCode()).getId(),
                PageRequest.of(0, statisticsPageSize));

        if(priceStatisticsByConditions.getContent().size() < 1){
            return Page.empty(priceStatisticsByConditions.getPageable());
        }

        if (priceStatisticsByConditions.getContent().get(0).getUnitname() == null) {
            return Page.empty(priceStatisticsByConditions.getPageable());
        }

        return priceStatisticsByConditions;
    }

    public PriceTradeCountDto findPriceTradeCountStatisticsByConditions(AuctionPriceVO auctionPriceVO) {
        return pricesRepository.findPriceTradeCountStatisticsByConditions(auctionPriceVO.getStartDate(),
                lClassCodeRepository.findOneBylclasscode(auctionPriceVO.getLClassCode()).getId(),
                mClassCodeRepository.findOneBymclasscode(auctionPriceVO.getMClassCode()).getId(),
                sClassCodeRepository.findOneBysclasscode(auctionPriceVO.getSClassCode()).getId(),
                marketCodeRepository.findOneByMarketCode(auctionPriceVO.getMarketCode()).getId());
    }

}
