package com.dodamsoft.todayfarmhub.service;

import com.dodamsoft.todayfarmhub.dto.AuctionAPIDto;
import com.dodamsoft.todayfarmhub.dto.LClassAPIDto;
import com.dodamsoft.todayfarmhub.entity.LClassCode;
import com.dodamsoft.todayfarmhub.repository.LClassCodeRepository;
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


    private final Gson gson;
    private final LClassCodeRepository lClassCodeRepository;

    public AuctionAPIDto getAuctionPricesByOrginOpenAPIURL(AuctionPriceVO auctionPriceVO) throws IOException {

        String responseData = "";

        AuctionAPIVO auctionAPIVO = AuctionAPIVO.builder()
                                                .lClassCode("12")
                                                .mClassCode("1208")
                                                .sClassCode_arr("120801")
                                                .sClassName(" 홍고추(일반)")
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
                                                .limit("10")
                                                .build();

        responseData = HttpCallUtil.getHttpPost(OriginAPIUrlEnum.GET_PRICES_URL.getUrl(), gson.toJson(auctionAPIVO));

        return gson.fromJson(responseData, AuctionAPIDto.class);
    }

    public void initCategoryInfo(AuctionPriceVO auctionPriceVO) {

        AuctionAPIVO auctionAPIVO = AuctionAPIVO.builder()
                                                .lClassCode("")
                                                .mClassCode("")
                                                .sClassCode_arr("")
                                                .sClassName("")
                                                .flag("lClassCode")
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

        String responseData = HttpCallUtil.getHttpPost(OriginAPIUrlEnum.GET_LCLASS_URL.getUrl(), gson.toJson(auctionAPIVO));

        log.info(responseData);

        LClassAPIDto lClassAPIDto = gson.fromJson(responseData, LClassAPIDto.class);
        if (lClassCodeRepository.count() < 1) {
            for (LClassAPIDto.ResultList resultList : lClassAPIDto.getResultList()) {
                lClassCodeRepository.save(LClassCode.builder().lclassname(resultList.getLclassname()).lclasscode(resultList.getLclasscode()).build());
            }
        }
    }

}
