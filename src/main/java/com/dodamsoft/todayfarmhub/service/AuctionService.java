package com.dodamsoft.todayfarmhub.service;

import com.dodamsoft.todayfarmhub.dto.AuctionAPIDto;
import com.dodamsoft.todayfarmhub.dto.PriceStatisticsDto;
import com.dodamsoft.todayfarmhub.dto.PriceTradeCountDto;
import com.dodamsoft.todayfarmhub.dto.PricesDto;
import com.dodamsoft.todayfarmhub.entity.FavoriteCategory;
import com.dodamsoft.todayfarmhub.entity.Prices;
import com.dodamsoft.todayfarmhub.repository.*;
import com.dodamsoft.todayfarmhub.vo.AuctionConvertClassCode;
import com.dodamsoft.todayfarmhub.vo.AuctionPriceVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import static com.dodamsoft.todayfarmhub.util.CategoryType.*;
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
    private final FavoriteCategoryRepository favoriteCategoryRepository;
    private final AuctionApiClient auctionApiClient;
    private final AuctionConvertClassCode auctionConvertClassCode;

    public Page<PricesDto> getAuctionPricesByOrigin(AuctionPriceVO auctionPriceVO) {

        updateFavoriteCategory(auctionPriceVO);

        String[] codes = new String[] { auctionPriceVO.getMarketCode(), auctionPriceVO.getLClassCode(),
                auctionPriceVO.getMClassCode(), auctionPriceVO.getSClassCode() };

        boolean exists = pricesRepository.existsByDatesAndLClassCodeIdAndMClassCodeIdAndSClassCodeId(
                formatDateForApi(auctionPriceVO.getEndDate()),
                auctionConvertClassCode.getClassId(LCLASS, codes),
                auctionConvertClassCode.getClassId(MCLASS, codes),
                auctionConvertClassCode.getClassId(SCLASS, codes),
                auctionConvertClassCode.getClassId(MARKET, codes));

        if (!exists) {
            fetchAndSaveAllPages(auctionPriceVO);
        }

        PageRequest pageRequest = PageRequest.of(auctionPriceVO.getPageNumber() - 1, PAGE_SIZE, Sort.Direction.DESC,
                "bidtime");

        return pricesRepository.findByDatesAndLClassCodeAndMClassCodeAndSClassCode(
                formatDateForApi(auctionPriceVO.getEndDate()),
                auctionConvertClassCode.getClassId(LCLASS, codes),
                auctionConvertClassCode.getClassId(MCLASS, codes),
                auctionConvertClassCode.getClassId(SCLASS, codes),
                auctionConvertClassCode.getClassId(MARKET, codes),
                pageRequest);

    }

    private void fetchAndSaveAllPages(AuctionPriceVO auctionPriceVO) {
        int totalPage = 1;

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
                    marketCodeRepository);
            pricesRepository.save(entity);
        }
        pricesRepository.flush();
    }

    public Page<PriceStatisticsDto> findPriceStatisticsByConditions(AuctionPriceVO auctionPriceVO) {

        String[] codes = new String[] { auctionPriceVO.getMarketCode(), auctionPriceVO.getLClassCode(),
                auctionPriceVO.getMClassCode(), auctionPriceVO.getSClassCode() };

        PageRequest pageRequest = PageRequest.of(0, 5000);
        String dates = formatDateForApi(auctionPriceVO.getEndDate());
        Long lclass = auctionConvertClassCode.getClassId(LCLASS, codes);
        Long mclass = auctionConvertClassCode.getClassId(MCLASS, codes);
        Long sclass = auctionConvertClassCode.getClassId(SCLASS, codes);
        Long market = auctionConvertClassCode.getClassId(MARKET, codes);
        Page<PriceStatisticsDto> statsPage = pricesRepository.findPriceStatisticsByConditions(
                dates,
                lclass,
                mclass,
                sclass,
                market,
                pageRequest);

        if (statsPage.getContent().isEmpty() || statsPage.getContent().get(0).getUnitname() == null) {
            return Page.empty(statsPage.getPageable());
        }

        return statsPage;
    }

    public PriceTradeCountDto findPriceTradeCountStatisticsByConditions(AuctionPriceVO auctionPriceVO) {

        String[] codes = new String[] { auctionPriceVO.getMarketCode(), auctionPriceVO.getLClassCode(),
                auctionPriceVO.getMClassCode(), auctionPriceVO.getSClassCode() };

        return pricesRepository.findPriceTradeCountStatisticsByConditions(
                formatDateForApi(auctionPriceVO.getEndDate()),
                auctionConvertClassCode.getClassId(LCLASS, codes),
                auctionConvertClassCode.getClassId(MCLASS, codes),
                auctionConvertClassCode.getClassId(SCLASS, codes),
                auctionConvertClassCode.getClassId(MARKET, codes));
    }

    private void updateFavoriteCategory(AuctionPriceVO vo) {
        String sCode = vo.getSClassCode() == null ? "" : vo.getSClassCode();
        FavoriteCategory favoriteCategory = favoriteCategoryRepository
                .findByLclassCodeAndMclassCodeAndSclassCodeAndMarketCode(
                        vo.getLClassCode(), vo.getMClassCode(), sCode, vo.getMarketCode())
                .orElse(FavoriteCategory.builder()
                        .lclassCode(vo.getLClassCode())
                        .mclassCode(vo.getMClassCode())
                        .sclassCode(sCode)
                        .marketCode(vo.getMarketCode())
                        .viewCount(0L)
                        .build());

        favoriteCategory.incrementViewCount();
        favoriteCategoryRepository.save(favoriteCategory);
    }

    public void fetchPricesForScheduler(AuctionPriceVO auctionPriceVO) {
        // Check if data already exists to avoid duplicate fetching if run multiple
        // times or manually
        String[] codes = new String[] { auctionPriceVO.getMarketCode(), auctionPriceVO.getLClassCode(),
                auctionPriceVO.getMClassCode(), auctionPriceVO.getSClassCode() };
        boolean exists = pricesRepository.existsByDatesAndLClassCodeIdAndMClassCodeIdAndSClassCodeId(
                formatDateForApi(auctionPriceVO.getEndDate()),
                auctionConvertClassCode.getClassId(LCLASS, codes),
                auctionConvertClassCode.getClassId(MCLASS, codes),
                auctionConvertClassCode.getClassId(SCLASS, codes),
                auctionConvertClassCode.getClassId(MARKET, codes));

        if (!exists) {
            fetchAndSaveAllPages(auctionPriceVO);
        }
    }

}
