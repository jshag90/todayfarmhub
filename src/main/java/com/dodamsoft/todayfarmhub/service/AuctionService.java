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
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.io.IOException;


@Service
@RequiredArgsConstructor
@Slf4j
public class AuctionService {

    private static final String pageSize = "100";
    private final PricesRepository pricesRepository;
    private final LClassCodeRepository lClassCodeRepository;
    private final MClassCodeRepository mClassCodeRepository;
    private final SClassCodeRepository sClassCodeRepository;
    private final MarketCodeRepository marketCodeRepository;
    private final Gson gson;

    public Page<Prices> getAuctionPricesByOriginOpenAPIURL(AuctionPriceVO auctionPriceVO) throws IOException {

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
                .limit(pageSize)
                .build();

        if (!pricesRepository.existsByDatesAndLClassCodeIdAndMClassCodeIdAndSClassCodeId(auctionPriceVO.getEndDate()
                , lClassCodeRepository.findOneBylclasscode(auctionPriceVO.getLClassCode()).getId()
                , mClassCodeRepository.findOneBymclasscode(auctionAPIVO.getMClassCode()).getId()
                , sClassCodeRepository.findOneBysclasscode(auctionAPIVO.getSClassCode_arr()).getId()
        )) {

            String firstResponseData = HttpCallUtil.getHttpPost(OriginAPIUrlEnum.GET_PRICES_URL.getUrl(), gson.toJson(auctionAPIVO));
            log.info(firstResponseData);

            AuctionAPIDto firstAuctionAPIDto = gson.fromJson(firstResponseData, AuctionAPIDto.class);
            log.info("총 데이터 갯수 : " + firstAuctionAPIDto.getTotCnt());

            saveAuctionPrices(firstAuctionAPIDto);

            int totalPage = firstAuctionAPIDto.getTotCnt() == 0?0:firstAuctionAPIDto.getTotCnt() / Integer.parseInt(pageSize) + 1;
            log.info("총 페이지 갯수 : " + totalPage);
            for (int index = 2; index <= totalPage; index++) {

                AuctionAPIVO nextPageAuctionAPIVO = new AuctionAPIVO();
                BeanUtils.copyProperties(auctionAPIVO, nextPageAuctionAPIVO);
                nextPageAuctionAPIVO.setPageIndex(index);

                String responseData = HttpCallUtil.getHttpPost(OriginAPIUrlEnum.GET_PRICES_URL.getUrl(), gson.toJson(nextPageAuctionAPIVO));

                log.info(responseData);

                AuctionAPIDto auctionAPIDto = gson.fromJson(responseData, AuctionAPIDto.class);
                saveAuctionPrices(auctionAPIDto);
            }

        }


        Pageable pageable = PageRequest.of(auctionPriceVO.getPageNumber() - 1, Integer.parseInt(pageSize), Sort.by(Sort.Order.desc("bidtime")));
        return pricesRepository.findByDatesAndLClassCodeAndMClassCodeAndSClassCode(auctionPriceVO.getStartDate(),
                lClassCodeRepository.findOneBylclasscode(auctionPriceVO.getLClassCode()),
                mClassCodeRepository.findOneBymclasscode(auctionAPIVO.getMClassCode()),
                sClassCodeRepository.findOneBysclasscode(auctionAPIVO.getSClassCode_arr()),
                pageable
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
                    .lClassCode(lClassCodeRepository.findOneBylclasscode(resultList.getLclasscode()))
                    .mClassCode(mClassCodeRepository.findOneBymclasscode(resultList.getMclasscode()))
                    .sClassCode(sClassCodeRepository.findOneBysclasscode(resultList.getSclasscode()))
                    .marketCode(marketCodeRepository.findOneByMarketCode(resultList.getMarketcode()))
                    .build());
        }
    }


}
