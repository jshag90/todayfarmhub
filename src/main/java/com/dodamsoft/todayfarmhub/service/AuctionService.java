package com.dodamsoft.todayfarmhub.service;

import com.dodamsoft.todayfarmhub.dto.AuctionAPIDto;
import com.dodamsoft.todayfarmhub.dto.PriceStatisticsDto;
import com.dodamsoft.todayfarmhub.dto.PriceTradeCountDto;
import com.dodamsoft.todayfarmhub.dto.PricesDto;
import com.dodamsoft.todayfarmhub.entity.LClassCode;
import com.dodamsoft.todayfarmhub.entity.MClassCode;
import com.dodamsoft.todayfarmhub.entity.Prices;
import com.dodamsoft.todayfarmhub.repository.*;
import com.dodamsoft.todayfarmhub.vo.AuctionPriceVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import static com.dodamsoft.todayfarmhub.util.DateUtils.formatDateForApi;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuctionService {

    private static final int PAGE_SIZE = 50;

    private final PricesRepository pricesRepository;
    private final LClassCodeRepository lClassCodeRepository;
    private final MClassCodeRepository mClassCodeRepository;
    private final SClassCodeRepository sClassCodeRepository;
    private final MarketCodeRepository marketCodeRepository;
    private final AuctionApiClient auctionApiClient; // Open API 호출 전용 클라이언트

    private Long getLClassId(String code) {
        return lClassCodeRepository.findOneBylclasscode(code).getId();
    }

    private Long getMClassId(String lClassCode, String mClassCode) {
        LClassCode oneBylclasscode = lClassCodeRepository.findOneBylclasscode(lClassCode);
        return mClassCodeRepository.findOneBylClassCodeAndMclasscode(oneBylclasscode, mClassCode).getId();
    }

    private Long getSClassId(MClassCode mClassCode, String sClassCode) {
        return sClassCodeRepository.findOneByMClassCodeAndsclasscode(mClassCode, sClassCode).getId();
    }

    private Long getMarketId(String code) {
        return marketCodeRepository.findOneByMarketCode(code).getId();
    }

    public Page<PricesDto> getAuctionPricesByOrigin(AuctionPriceVO auctionPriceVO) {

        log.info(" 검색 날짜 : {}", auctionPriceVO.getEndDate());
        // DB 존재 여부 체크
        Long mClassId = getMClassId(auctionPriceVO.getLClassCode(), auctionPriceVO.getMClassCode());
        MClassCode mClassCode = mClassCodeRepository.findById(mClassId).get();
        boolean exists = pricesRepository.existsByDatesAndLClassCodeIdAndMClassCodeIdAndSClassCodeId(
                formatDateForApi(auctionPriceVO.getEndDate()),
                getLClassId(auctionPriceVO.getLClassCode()),
                mClassId,
                getSClassId(mClassCode, auctionPriceVO.getSClassCode()),
                getMarketId(auctionPriceVO.getMarketCode())
        );

        if (!exists) {
            // API 호출 후 DB 저장
            int pageIndex = 1;
            AuctionAPIDto apiResponse;
            do {
                apiResponse = auctionApiClient.fetchAuctionData(auctionPriceVO, pageIndex, PAGE_SIZE);
                saveAuctionPrices(apiResponse);
                pageIndex++;
            } while (pageIndex <= apiResponse.getTotalPage(PAGE_SIZE));
        }

        return pricesRepository.findByDatesAndLClassCodeAndMClassCodeAndSClassCode(
                formatDateForApi(auctionPriceVO.getEndDate()),
                getLClassId(auctionPriceVO.getLClassCode()),
                mClassId,
                getSClassId(mClassCode, auctionPriceVO.getSClassCode()),
                getMarketId(auctionPriceVO.getMarketCode()),
                PageRequest.of(auctionPriceVO.getPageNumber() - 1, PAGE_SIZE, Sort.Direction.DESC, "bidtime")
        );
    }

    private void saveAuctionPrices(AuctionAPIDto apiDto) {
        if (apiDto.getResultList() == null || apiDto.getResultList().isEmpty()) {
            return;
        }

        for (var item : apiDto.getResultList()) {
            item.mapToLegacyFields();
            Prices entity = item.toEntity(
                    lClassCodeRepository,
                    mClassCodeRepository,
                    sClassCodeRepository,
                    marketCodeRepository
            );
            pricesRepository.save(entity);
        }
    }

    public Page<PriceStatisticsDto> findPriceStatisticsByConditions(AuctionPriceVO auctionPriceVO) {

        Long mClassId = getMClassId(auctionPriceVO.getLClassCode(), auctionPriceVO.getMClassCode());
        MClassCode mClassCode = mClassCodeRepository.findById(mClassId).get();

        Page<PriceStatisticsDto> statsPage = pricesRepository.findPriceStatisticsByConditions(
                formatDateForApi(auctionPriceVO.getStartDate()),
                getLClassId(auctionPriceVO.getLClassCode()),
                mClassId,
                getSClassId(mClassCode, auctionPriceVO.getSClassCode()),
                getMarketId(auctionPriceVO.getMarketCode()),
                PageRequest.of(0, 5000) // 기존 statisticsPageSize
        );

        if (statsPage.getContent().isEmpty() || statsPage.getContent().get(0).getUnitname() == null) {
            return Page.empty(statsPage.getPageable());
        }

        return statsPage;
    }

    public PriceTradeCountDto findPriceTradeCountStatisticsByConditions(AuctionPriceVO auctionPriceVO) {
        Long mClassId = getMClassId(auctionPriceVO.getLClassCode(), auctionPriceVO.getMClassCode());
        MClassCode mClassCode = mClassCodeRepository.findById(mClassId).get();
        return pricesRepository.findPriceTradeCountStatisticsByConditions(
                formatDateForApi(auctionPriceVO.getStartDate()),
                getLClassId(auctionPriceVO.getLClassCode()),
                mClassId,
                getSClassId(mClassCode, auctionPriceVO.getSClassCode()),
                getMarketId(auctionPriceVO.getMarketCode())
        );
    }

}
