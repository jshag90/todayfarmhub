package com.dodamsoft.todayfarmhub.service;

import com.dodamsoft.todayfarmhub.vo.AuctionAPIVO;
import com.dodamsoft.todayfarmhub.vo.AuctionPriceVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service("mClassCategoryService")
@Slf4j
public class MClassCategoryService implements GetAuctionCategoryService{
    @Override
    public <T> T getCategory(AuctionPriceVO auctionPriceVO) {

        AuctionAPIVO auctionAPIVO = AuctionAPIVO.builder()
                .lClassCode("")
                .mClassCode("")
                .sClassCode_arr("")
                .sClassName("")
                .flag("mClassCode")
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


        return null;
    }
}
