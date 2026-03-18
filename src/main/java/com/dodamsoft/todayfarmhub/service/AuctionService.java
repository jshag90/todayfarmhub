package com.dodamsoft.todayfarmhub.service;

import com.dodamsoft.todayfarmhub.dto.AuctionAPIDto;
import com.dodamsoft.todayfarmhub.dto.PriceStatisticsDto;
import com.dodamsoft.todayfarmhub.dto.PriceTradeCountDto;
import com.dodamsoft.todayfarmhub.dto.PricesDto;
import com.dodamsoft.todayfarmhub.entity.FavoriteCategory;
import com.dodamsoft.todayfarmhub.entity.Prices;
import com.dodamsoft.todayfarmhub.repository.FavoriteCategoryRepository;
import com.dodamsoft.todayfarmhub.repository.LClassCodeRepository;
import com.dodamsoft.todayfarmhub.repository.MClassCodeRepository;
import com.dodamsoft.todayfarmhub.repository.MarketCodeRepository;
import com.dodamsoft.todayfarmhub.repository.PricesRepository;
import com.dodamsoft.todayfarmhub.repository.SClassCodeRepository;
import com.dodamsoft.todayfarmhub.vo.AuctionConvertClassCode;
import com.dodamsoft.todayfarmhub.vo.AuctionPriceVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static com.dodamsoft.todayfarmhub.util.CategoryType.LCLASS;
import static com.dodamsoft.todayfarmhub.util.CategoryType.MARKET;
import static com.dodamsoft.todayfarmhub.util.CategoryType.MCLASS;
import static com.dodamsoft.todayfarmhub.util.CategoryType.SCLASS;
import static com.dodamsoft.todayfarmhub.util.DateUtils.formatDateForApi;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuctionService {

    private static final int PAGE_SIZE = 50;
    private static final String AUCTION_PRICE_CACHE = "auctionPriceCache";
    private static final String AUCTION_STATS_CACHE = "auctionStatsCache";
    private static final String AUCTION_COUNT_CACHE = "auctionCountCache";

    private final PricesRepository pricesRepository;
    private final LClassCodeRepository lClassCodeRepository;
    private final MClassCodeRepository mClassCodeRepository;
    private final SClassCodeRepository sClassCodeRepository;
    private final MarketCodeRepository marketCodeRepository;
    private final FavoriteCategoryRepository favoriteCategoryRepository;
    private final AuctionApiClient auctionApiClient;
    private final AuctionConvertClassCode auctionConvertClassCode;
    private final CacheManager cacheManager;

    public Page<PricesDto> getAuctionPricesByOrigin(AuctionPriceVO auctionPriceVO) {
        updateFavoriteCategory(auctionPriceVO);

        String cacheKey = buildPriceCacheKey(auctionPriceVO);
        Page<PricesDto> cachedPage = getCachedValue(AUCTION_PRICE_CACHE, cacheKey, Page.class);
        if (cachedPage != null) {
            return cachedPage;
        }

        String requestDate = formatDateForApi(auctionPriceVO.getEndDate());
//        String todayDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
//
//        if (todayDate.equals(requestDate)) {
//            PageRequest pageRequest = PageRequest.of(auctionPriceVO.getPageNumber() - 1, PAGE_SIZE, Sort.Direction.DESC, "bidtime");
//            return Page.empty(pageRequest);
//        }

        String[] codes = new String[] {
                auctionPriceVO.getMarketCode(),
                auctionPriceVO.getLClassCode(),
                auctionPriceVO.getMClassCode(),
                auctionPriceVO.getSClassCode()
        };

        boolean exists = pricesRepository.existsByDatesAndLClassCodeIdAndMClassCodeIdAndSClassCodeId(
                requestDate,
                auctionConvertClassCode.getClassId(LCLASS, codes),
                auctionConvertClassCode.getClassId(MCLASS, codes),
                auctionConvertClassCode.getClassId(SCLASS, codes),
                auctionConvertClassCode.getClassId(MARKET, codes));

        if (!exists) {
            fetchAndSaveAllPages(auctionPriceVO);
        }

        PageRequest pageRequest = PageRequest.of(auctionPriceVO.getPageNumber() - 1, PAGE_SIZE, Sort.Direction.DESC, "bidtime");
        Page<PricesDto> result = pricesRepository.findByDatesAndLClassCodeAndMClassCodeAndSClassCode(
                requestDate,
                auctionConvertClassCode.getClassId(LCLASS, codes),
                auctionConvertClassCode.getClassId(MCLASS, codes),
                auctionConvertClassCode.getClassId(SCLASS, codes),
                auctionConvertClassCode.getClassId(MARKET, codes),
                pageRequest);

        if (!result.isEmpty()) {
            putCacheValue(AUCTION_PRICE_CACHE, cacheKey, result);
        }
        return result;
    }

    private void fetchAndSaveAllPages(AuctionPriceVO auctionPriceVO) {
        int totalPage = 1;

        for (int pageIndex = 1; pageIndex <= totalPage; pageIndex++) {
            AuctionAPIDto apiResponse = auctionApiClient.fetchAuctionData(auctionPriceVO, pageIndex, PAGE_SIZE);
            saveAuctionPrices(apiResponse);
            totalPage = apiResponse.getTotalPage(PAGE_SIZE);
        }
    }

    public void saveAuctionPrices(AuctionAPIDto apiDto) {
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
        String cacheKey = buildStatsCacheKey(auctionPriceVO);
        Page<PriceStatisticsDto> cachedStats = getCachedValue(AUCTION_STATS_CACHE, cacheKey, Page.class);
        if (cachedStats != null) {
            return cachedStats;
        }

        String[] codes = new String[] {
                auctionPriceVO.getMarketCode(),
                auctionPriceVO.getLClassCode(),
                auctionPriceVO.getMClassCode(),
                auctionPriceVO.getSClassCode()
        };

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

        putCacheValue(AUCTION_STATS_CACHE, cacheKey, statsPage);
        return statsPage;
    }

    public PriceTradeCountDto findPriceTradeCountStatisticsByConditions(AuctionPriceVO auctionPriceVO) {
        String cacheKey = buildCountCacheKey(auctionPriceVO);
        PriceTradeCountDto cachedCount = getCachedValue(AUCTION_COUNT_CACHE, cacheKey, PriceTradeCountDto.class);
        if (cachedCount != null) {
            return cachedCount;
        }

        String[] codes = new String[] {
                auctionPriceVO.getMarketCode(),
                auctionPriceVO.getLClassCode(),
                auctionPriceVO.getMClassCode(),
                auctionPriceVO.getSClassCode()
        };

        PriceTradeCountDto result = pricesRepository.findPriceTradeCountStatisticsByConditions(
                formatDateForApi(auctionPriceVO.getEndDate()),
                auctionConvertClassCode.getClassId(LCLASS, codes),
                auctionConvertClassCode.getClassId(MCLASS, codes),
                auctionConvertClassCode.getClassId(SCLASS, codes),
                auctionConvertClassCode.getClassId(MARKET, codes));

        if (result != null && (result.getCount() > 0 || result.getTradeAmtSum() != null)) {
            putCacheValue(AUCTION_COUNT_CACHE, cacheKey, result);
        }

        return result;
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
        String[] codes = new String[] {
                auctionPriceVO.getMarketCode(),
                auctionPriceVO.getLClassCode(),
                auctionPriceVO.getMClassCode(),
                auctionPriceVO.getSClassCode()
        };
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

    private String buildPriceCacheKey(AuctionPriceVO auctionPriceVO) {
        return String.join("|",
                formatDateForApi(auctionPriceVO.getEndDate()),
                safe(auctionPriceVO.getMarketCode()),
                safe(auctionPriceVO.getLClassCode()),
                safe(auctionPriceVO.getMClassCode()),
                safe(auctionPriceVO.getSClassCode()),
                String.valueOf(auctionPriceVO.getPageNumber()));
    }

    private String buildStatsCacheKey(AuctionPriceVO auctionPriceVO) {
        return String.join("|",
                formatDateForApi(auctionPriceVO.getEndDate()),
                safe(auctionPriceVO.getMarketCode()),
                safe(auctionPriceVO.getLClassCode()),
                safe(auctionPriceVO.getMClassCode()),
                safe(auctionPriceVO.getSClassCode()));
    }

    private String buildCountCacheKey(AuctionPriceVO auctionPriceVO) {
        return buildStatsCacheKey(auctionPriceVO);
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    @SuppressWarnings("unchecked")
    private <T> T getCachedValue(String cacheName, String key, Class<T> type) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache == null) {
            return null;
        }
        return cache.get(key, type);
    }

    private void putCacheValue(String cacheName, String key, Object value) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            cache.put(key, value);
        }
    }
}
