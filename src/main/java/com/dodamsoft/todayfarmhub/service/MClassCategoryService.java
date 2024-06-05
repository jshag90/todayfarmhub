package com.dodamsoft.todayfarmhub.service;

import com.dodamsoft.todayfarmhub.dto.MClassAPIDto;
import com.dodamsoft.todayfarmhub.entity.LClassCode;
import com.dodamsoft.todayfarmhub.entity.MClassCode;
import com.dodamsoft.todayfarmhub.repository.LClassCodeRepository;
import com.dodamsoft.todayfarmhub.repository.MClassCodeRepository;
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

@Service("mClassCategoryService")
@Slf4j
@RequiredArgsConstructor
public class MClassCategoryService implements GetAuctionCategoryService {

    private final MClassCodeRepository mClassCodeRepository;
    private final LClassCodeRepository lClassCodeRepository;
    private final Gson gson;

    @Override
    public <T> T getCategory(AuctionPriceVO auctionPriceVO) {

        AuctionAPIVO auctionAPIVO = AuctionAPIVO.builder()
                .lClassCode(auctionPriceVO.getLClassCode())
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

        log.info(auctionAPIVO.toString());

        LClassCode findOneLClassCode = lClassCodeRepository.findOneBylclasscode(auctionAPIVO.getLClassCode());
        if (!mClassCodeRepository.existsBylClassCode(findOneLClassCode)) {
            saveInfoByResponseDataUsingAPI(auctionAPIVO, findOneLClassCode, null);
        }

        List<MClassAPIDto.ResultList> resultList = new ArrayList<>();
        for (MClassCode mClassCode : mClassCodeRepository.findAllBylClassCode(findOneLClassCode, Sort.by(Sort.Direction.ASC, "mclassname"))) {
            resultList.add(MClassAPIDto.ResultList.builder().mclasscode(mClassCode.getMclasscode())
                    .mclassname(mClassCode.getMclassname())
                    .build());
        }

        return (T) MClassAPIDto.builder().resultList(resultList).build();
    }

    @Override
    public <T> void saveInfoByResponseDataUsingAPI(T t, LClassCode lClassCode, MClassCode mClassCode) {
        String responseData = HttpCallUtil.getHttpPost(OriginAPIUrlEnum.GET_CATEGORY_INFO_URL.getUrl(), gson.toJson(t));
        log.info(responseData);
        MClassAPIDto mClassResponseDataDto = gson.fromJson(responseData, MClassAPIDto.class);
        for (MClassAPIDto.ResultList resultList : mClassResponseDataDto.getResultList()) {
            System.out.println(resultList.toString());
            mClassCodeRepository.save(MClassCode.builder()
                    .mclassname(resultList.getMclassname())
                    .mclasscode(resultList.getMclasscode())
                    .lClassCode(lClassCode)
                    .build());
        }

    }
}
