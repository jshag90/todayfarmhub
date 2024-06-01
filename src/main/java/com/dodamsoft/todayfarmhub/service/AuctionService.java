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
import java.util.ArrayList;
import java.util.List;


@Service
@RequiredArgsConstructor
@Slf4j
public class AuctionService {

    private final Gson gson;

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


}