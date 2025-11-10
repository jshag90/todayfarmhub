package com.dodamsoft.todayfarmhub.service;

import com.dodamsoft.todayfarmhub.dto.AuctionAPIDto;
import com.dodamsoft.todayfarmhub.dto.PriceStatisticsDto;
import com.dodamsoft.todayfarmhub.dto.PriceTradeCountDto;
import com.dodamsoft.todayfarmhub.dto.PricesDto;
import com.dodamsoft.todayfarmhub.entity.Prices;
import com.dodamsoft.todayfarmhub.repository.*;
import com.dodamsoft.todayfarmhub.vo.AuctionPriceVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

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

    public Page<PricesDto> getAuctionPricesByOrigin(AuctionPriceVO vo) {

        // DB 존재 여부 체크
        boolean exists = pricesRepository.existsByDatesAndLClassCodeIdAndMClassCodeIdAndSClassCodeId(
                vo.getEndDate(),
                getLClassId(vo.getLClassCode()),
                getMClassId(vo.getMClassCode()),
                getSClassId(vo.getSClassCode()),
                getMarketId(vo.getMarketCode())
        );

        if (!exists) {
            // API 호출 후 DB 저장
            int pageIndex = 1;
            AuctionAPIDto apiResponse;
            do {
                apiResponse = auctionApiClient.fetchAuctionData(vo, pageIndex, PAGE_SIZE);
                saveAuctionPrices(apiResponse);
                pageIndex++;
            } while (pageIndex <= apiResponse.getTotalPage(PAGE_SIZE));
        }

        return pricesRepository.findByDatesAndLClassCodeAndMClassCodeAndSClassCode(
                vo.getStartDate(),
                getLClassId(vo.getLClassCode()),
                getMClassId(vo.getMClassCode()),
                getSClassId(vo.getSClassCode()),
                getMarketId(vo.getMarketCode()),
                PageRequest.of(vo.getPageNumber() - 1, PAGE_SIZE, Sort.Direction.DESC, "bidtime")
        );
    }

    private Long getLClassId(String code) {
        return lClassCodeRepository.findOneBylclasscode(code).getId();
    }

    private Long getMClassId(String code) {
        return mClassCodeRepository.findOneBymclasscode(code).getId();
    }

    private Long getSClassId(String code) {
        return sClassCodeRepository.findOneBysclasscode(code).getId();
    }

    private Long getMarketId(String code) {
        return marketCodeRepository.findOneByMarketCode(code).getId();
    }

    private void saveAuctionPrices(AuctionAPIDto apiDto) {
        apiDto.getResultList().stream()
                .map(this::toEntity)
                .forEach(pricesRepository::save);
    }

    private Prices toEntity(AuctionAPIDto.ResultList r) {
        return Prices.builder()
                .bidtime(r.getBidtime())
                .coco(r.getCoco())
                .cocode(r.getCocode())
                .coname(r.getConame())
                .dates(r.getDates())
                .price(r.getPrice())
                .sanco(r.getSanco())
                .sanji(r.getSanji())
                .unitname(r.getUnitname())
                .tradeamt(String.valueOf(r.getTradeamt()))
                .lClassCode(lClassCodeRepository.findOneBylclasscode(r.getLclasscode()))
                .mClassCode(mClassCodeRepository.findOneBymclasscode(r.getMclasscode()))
                .sClassCode(sClassCodeRepository.findOneBysclasscode(r.getSclasscode()))
                .marketCode(marketCodeRepository.findOneByMarketCode(r.getMarketcode()))
                .build();
    }

    public Page<PriceStatisticsDto> findPriceStatisticsByConditions(AuctionPriceVO vo) {
        Page<PriceStatisticsDto> statsPage = pricesRepository.findPriceStatisticsByConditions(
                vo.getStartDate(),
                getLClassId(vo.getLClassCode()),
                getMClassId(vo.getMClassCode()),
                getSClassId(vo.getSClassCode()),
                getMarketId(vo.getMarketCode()),
                PageRequest.of(0, 5000) // 기존 statisticsPageSize
        );

        if (statsPage.getContent().isEmpty() || statsPage.getContent().get(0).getUnitname() == null) {
            return Page.empty(statsPage.getPageable());
        }

        return statsPage;
    }

    public PriceTradeCountDto findPriceTradeCountStatisticsByConditions(AuctionPriceVO vo) {
        return pricesRepository.findPriceTradeCountStatisticsByConditions(
                vo.getStartDate(),
                getLClassId(vo.getLClassCode()),
                getMClassId(vo.getMClassCode()),
                getSClassId(vo.getSClassCode()),
                getMarketId(vo.getMarketCode())
        );
    }

}

