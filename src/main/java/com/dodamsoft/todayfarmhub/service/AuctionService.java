package com.dodamsoft.todayfarmhub.service;

import com.dodamsoft.todayfarmhub.dto.AuctionAPIDto;
import com.dodamsoft.todayfarmhub.dto.PriceStatisticsDto;
import com.dodamsoft.todayfarmhub.dto.PriceTradeCountDto;
import com.dodamsoft.todayfarmhub.dto.PricesDto;
import com.dodamsoft.todayfarmhub.entity.LClassCode;
import com.dodamsoft.todayfarmhub.entity.MClassCode;
import com.dodamsoft.todayfarmhub.entity.Prices;
import com.dodamsoft.todayfarmhub.repository.*;
import com.dodamsoft.todayfarmhub.vo.AuctionConvertClassCode;
import com.dodamsoft.todayfarmhub.vo.AuctionPriceVO;
import jakarta.transaction.Transactional;
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
    private final AuctionConvertClassCode auctionConvertClassCode;

    public Page<PricesDto> getAuctionPricesByOrigin(AuctionPriceVO auctionPriceVO) {

        log.info(" 검색 날짜 : {}", auctionPriceVO.getEndDate());

        String[] codes = new String[]{auctionPriceVO.getMarketCode(), auctionPriceVO.getLClassCode(), auctionPriceVO.getMClassCode(), auctionPriceVO.getSClassCode()};

        boolean exists = pricesRepository.existsByDatesAndLClassCodeIdAndMClassCodeIdAndSClassCodeId(
                formatDateForApi(auctionPriceVO.getEndDate()),
                auctionConvertClassCode.getClassId("lclass", codes),
                auctionConvertClassCode.getClassId("mclass", codes),
                auctionConvertClassCode.getClassId("sclass", codes),
                auctionConvertClassCode.getClassId("market", codes)
        );

        if (!exists) {
            log.info("경매가격이 db 존재하지 않음");
            fetchAndSaveAllPages(auctionPriceVO);
        }

        PageRequest pageRequest = PageRequest.of(auctionPriceVO.getPageNumber() - 1, PAGE_SIZE, Sort.Direction.DESC, "bidtime");

        return pricesRepository.findByDatesAndLClassCodeAndMClassCodeAndSClassCode(
                formatDateForApi(auctionPriceVO.getEndDate()),
                auctionConvertClassCode.getClassId("lclass", codes),
                auctionConvertClassCode.getClassId("mclass", codes),
                auctionConvertClassCode.getClassId("sclass", codes),
                auctionConvertClassCode.getClassId("market", codes),
                pageRequest
        );

    }

    /**
     * 모든 페이지를 순회하며 API 호출 후 DB 저장
     */
    @Transactional
    private void fetchAndSaveAllPages(AuctionPriceVO auctionPriceVO) {
        int totalPage = 1; // 첫 루프에서 pageIndex=1 실행 보장

        for (int pageIndex = 1; pageIndex <= totalPage; pageIndex++) {
            AuctionAPIDto apiResponse = auctionApiClient.fetchAuctionData(auctionPriceVO, pageIndex, PAGE_SIZE);
            saveAuctionPrices(apiResponse);
            totalPage = apiResponse.getTotalPage(PAGE_SIZE);
        }
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

        String[] codes = new String[]{auctionPriceVO.getMarketCode(), auctionPriceVO.getLClassCode()
                                    , auctionPriceVO.getMClassCode(), auctionPriceVO.getSClassCode()};

        log.info("codes : {}", codes);

        PageRequest pageRequest = PageRequest.of(0, 5000);
        String dates = formatDateForApi(auctionPriceVO.getEndDate());
        log.info(dates);
        Long lclass = auctionConvertClassCode.getClassId("lclass", codes);
        log.info(String.valueOf(lclass));
        Long mclass = auctionConvertClassCode.getClassId("mclass", codes);
        log.info(String.valueOf(mclass));
        Long sclass = auctionConvertClassCode.getClassId("sclass", codes);
        log.info(String.valueOf(sclass));
        Long market = auctionConvertClassCode.getClassId("market", codes);
        log.info(String.valueOf(market));
        Page<PriceStatisticsDto> statsPage = pricesRepository.findPriceStatisticsByConditions(
                dates,
                lclass,
                mclass,
                sclass,
                market,
                pageRequest // 기존 statisticsPageSize
        );

        if (statsPage.getContent().isEmpty() || statsPage.getContent().get(0).getUnitname() == null) {
            return Page.empty(statsPage.getPageable());
        }

        return statsPage;
    }

    public PriceTradeCountDto findPriceTradeCountStatisticsByConditions(AuctionPriceVO auctionPriceVO) {

        String[] codes = new String[]{auctionPriceVO.getMarketCode(), auctionPriceVO.getLClassCode(),
                                    auctionPriceVO.getMClassCode(), auctionPriceVO.getSClassCode()};

        return pricesRepository.findPriceTradeCountStatisticsByConditions(
                formatDateForApi(auctionPriceVO.getEndDate()),
                auctionConvertClassCode.getClassId("lclass", codes),
                auctionConvertClassCode.getClassId("mclass", codes),
                auctionConvertClassCode.getClassId("sclass", codes),
                auctionConvertClassCode.getClassId("market", codes)
        );
    }

}
