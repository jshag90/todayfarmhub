package com.dodamsoft.todayfarmhub.service;

import com.dodamsoft.todayfarmhub.dto.AuctionAPIDto;
import com.dodamsoft.todayfarmhub.entity.Prices;
import com.dodamsoft.todayfarmhub.repository.*;
import com.dodamsoft.todayfarmhub.util.HttpCallUtil;
import com.dodamsoft.todayfarmhub.util.OriginAPIUrlEnum;
import com.dodamsoft.todayfarmhub.vo.AuctionAPIVO;
import com.dodamsoft.todayfarmhub.vo.AuctionPriceVO;
import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;


@Service
@RequiredArgsConstructor
@Slf4j
public class AuctionService {

    private final PricesRepository pricesRepository;
    private final LClassCodeRepository lClassCodeRepository;
    private final MClassCodeRepository mClassCodeRepository;
    private final SClassCodeRepository sClassCodeRepository;
    private final MarketCodeRepository marketCodeRepository;
    private final Gson gson;

    public AuctionAPIDto getAuctionPricesByOriginOpenAPIURL(AuctionPriceVO auctionPriceVO) throws IOException {

        String responseData = "";

        AuctionAPIVO auctionAPIVO = AuctionAPIVO.builder()
                                                .lClassCode(auctionPriceVO.getLClassCode())
                                                .mClassCode(auctionPriceVO.getMClassCode())
                                                .sClassCode_arr(auctionPriceVO.getSClassCode())
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
                                                .pageIndex(1)
                                                .limit("10000")
                                                .build();

        responseData = HttpCallUtil.getHttpPost(OriginAPIUrlEnum.GET_PRICES_URL.getUrl(), gson.toJson(auctionAPIVO));
        log.info(responseData);


        AuctionAPIDto auctionAPIDto = gson.fromJson(responseData, AuctionAPIDto.class);
        //TODO 총 개수의 마지막 페이지까지 데이터 요청해서 저장하기
        auctionAPIDto.getTotCnt();
        for(AuctionAPIDto.ResultList resultList : auctionAPIDto.getResultList()){

            pricesRepository.save(Prices.builder()
                            .bidtime(resultList.getBidtime())
                            .coco(resultList.getCoco())
                            .cocode(resultList.getCocode())
                            .coname(resultList.getConame())
                            .dates(resultList.getDates())
                            .price(resultList.getPrice())
                            .sanco(resultList.getSanco())
                            .sanji(resultList.getSanji())
                            .lClassCode(lClassCodeRepository.findOneBylclasscode(resultList.getLclasscode()))
                            .mClassCode(mClassCodeRepository.findOneBymclasscode(resultList.getMclasscode()))
                            .sClassCode(sClassCodeRepository.findOneBysclasscode(resultList.getSclasscode()))
                            .marketCode(marketCodeRepository.findOneByMarketCode(resultList.getMarketcode()))
                    .build());

        }


        return auctionAPIDto;
    }


}
