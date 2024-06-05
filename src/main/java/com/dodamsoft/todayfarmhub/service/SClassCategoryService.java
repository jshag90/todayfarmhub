package com.dodamsoft.todayfarmhub.service;

import com.dodamsoft.todayfarmhub.dto.SClassAPIDto;
import com.dodamsoft.todayfarmhub.entity.LClassCode;
import com.dodamsoft.todayfarmhub.entity.MClassCode;
import com.dodamsoft.todayfarmhub.entity.SClassCode;
import com.dodamsoft.todayfarmhub.repository.LClassCodeRepository;
import com.dodamsoft.todayfarmhub.repository.MClassCodeRepository;
import com.dodamsoft.todayfarmhub.repository.SClassCodeRepository;
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

@Service("sClassCategoryService")
@Slf4j
@RequiredArgsConstructor
public class SClassCategoryService implements GetAuctionCategoryService {

    private final MClassCodeRepository mClassCodeRepository;
    private final LClassCodeRepository lClassCodeRepository;
    private final SClassCodeRepository sClassCodeRepository;
    private final Gson gson;

    @Override
    public <T> T getCategory(AuctionPriceVO auctionPriceVO) {

        AuctionAPIVO auctionAPIVO = AuctionAPIVO.builder()
                .lClassCode(auctionPriceVO.getLClassCode())
                .mClassCode(auctionPriceVO.getMClassCode())
                .sClassCode_arr("")
                .sClassName("")
                .flag("sClassCode")
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

        LClassCode findOneLClassCode = lClassCodeRepository.findOneBylclasscode(auctionAPIVO.getLClassCode());
        MClassCode findOneMClassCode = mClassCodeRepository.findOneBymclasscode(auctionAPIVO.getMClassCode());

        if (!sClassCodeRepository.existsByMClassCodeIdAndLClassCodeId(findOneLClassCode.getId(), findOneMClassCode.getId())) {
            saveInfoByResponseDataUsingAPI(auctionAPIVO, findOneLClassCode, findOneMClassCode);
        }

        List<SClassAPIDto.ResultList> resultList = new ArrayList<>();
        for (SClassCode sClassCode : sClassCodeRepository.findAllByMClassCodeIdAndLClassCodeIdOrderBySclassnameDesc(findOneLClassCode.getId(), findOneMClassCode.getId())) {
            resultList.add(SClassAPIDto.ResultList.builder().sclasscode(sClassCode.getSclasscode())
                    .sclassname(sClassCode.getSclassname())
                    .build());
        }

        return (T) SClassAPIDto.builder().resultList(resultList).build();
    }

    @Override
    public <T> void saveInfoByResponseDataUsingAPI(T t, LClassCode lClassCode, MClassCode mClassCode) {
        String responseData = HttpCallUtil.getHttpPost(OriginAPIUrlEnum.GET_CATEGORY_INFO_URL.getUrl(), gson.toJson(t));
        log.info(responseData);
        SClassAPIDto mClassResponseDataDto = gson.fromJson(responseData, SClassAPIDto.class);
        for (SClassAPIDto.ResultList resultList : mClassResponseDataDto.getResultList()) {
            sClassCodeRepository.save(SClassCode.builder()
                    .sclassname(resultList.getSclassname())
                    .sclasscode(resultList.getSclasscode())
                    .lClassCode(lClassCode)
                    .mClassCode(mClassCode)
                    .build());
        }
    }

}
