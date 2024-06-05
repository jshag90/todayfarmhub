package com.dodamsoft.todayfarmhub.service;

import com.dodamsoft.todayfarmhub.dto.LClassAPIDto;
import com.dodamsoft.todayfarmhub.entity.LClassCode;
import com.dodamsoft.todayfarmhub.entity.MClassCode;
import com.dodamsoft.todayfarmhub.repository.LClassCodeRepository;
import com.dodamsoft.todayfarmhub.util.HttpCallUtil;
import com.dodamsoft.todayfarmhub.util.OriginAPIUrlEnum;
import com.dodamsoft.todayfarmhub.vo.AuctionAPIVO;
import com.dodamsoft.todayfarmhub.vo.AuctionPriceVO;
import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Slf4j
@Service("lClassCategoryService")
public class LClassCategoryService implements GetAuctionCategoryService {

    private final Gson gson;
    private final LClassCodeRepository lClassCodeRepository;

    @Override
    public <T> T getCategory(AuctionPriceVO auctionPriceVO) {

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


        if (lClassCodeRepository.count() < 1) {
            saveInfoByResponseDataUsingAPI(auctionAPIVO, null, null);
        }

        List<LClassAPIDto.ResultList> resultList = new ArrayList<>();
        for (LClassCode lClassCode : lClassCodeRepository.findAll(Sort.by(Sort.Direction.ASC, "lclassname"))) {
            resultList.add(LClassAPIDto.ResultList.builder().lclasscode(lClassCode.getLclasscode())
                                                            .lclassname(lClassCode.getLclassname())
                                                            .build());
        }

        return (T) LClassAPIDto.builder().resultList(resultList).build();

    }

    @Override
    public <T> void saveInfoByResponseDataUsingAPI(T t, LClassCode lClassCode, MClassCode mClassCode) {

        String responseData = HttpCallUtil.getHttpPost(OriginAPIUrlEnum.GET_CATEGORY_INFO_URL.getUrl(), gson.toJson(t));
        log.info(responseData);
        LClassAPIDto lClassResponseDataDto = gson.fromJson(responseData, LClassAPIDto.class);
        for (LClassAPIDto.ResultList resultList : lClassResponseDataDto.getResultList()) {
            lClassCodeRepository.save(LClassCode.builder().lclassname(resultList.getLclassname()).lclasscode(resultList.getLclasscode()).build());
        }
    }

}

