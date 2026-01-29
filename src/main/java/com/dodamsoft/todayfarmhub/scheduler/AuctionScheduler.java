package com.dodamsoft.todayfarmhub.scheduler;

import com.dodamsoft.todayfarmhub.entity.FavoriteCategory;
import com.dodamsoft.todayfarmhub.repository.FavoriteCategoryRepository;
import com.dodamsoft.todayfarmhub.service.AuctionService;
import com.dodamsoft.todayfarmhub.vo.AuctionPriceVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuctionScheduler {

    private final AuctionService auctionService;
    private final FavoriteCategoryRepository favoriteCategoryRepository;

    @Scheduled(cron = "0 0 2 * * *")
    public void fetchFavoriteCategoryPrices() {
        log.info("Starting scheduled task: Fetching prices for favorite categories.");

        List<FavoriteCategory> topFavorites = favoriteCategoryRepository.findTop10ByOrderByViewCountDesc();

        if (topFavorites.isEmpty()) {
            log.info("No favorite categories found. Skipping scheduled task.");
            return;
        }

        LocalDate yesterday = LocalDate.now().minusDays(1);
        String dateStr = yesterday.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        for (FavoriteCategory category : topFavorites) {
            try {
                // Ensure sClassCode is empty string if null, though entity might store null.
                // AuctionPriceVO expects String.
                String sClassCode = category.getSclassCode() == null ? "" : category.getSclassCode();

                AuctionPriceVO vo = AuctionPriceVO.builder()
                        .startDate(dateStr)
                        .endDate(dateStr)
                        .lClassCode(category.getLclassCode())
                        .mClassCode(category.getMclassCode())
                        .sClassCode(sClassCode)
                        .marketCode(category.getMarketCode())
                        .pageNumber(1) // Default page 1, fetchPricesForScheduler iterates all pages anyway
                        .build();

                log.info("Fetching prices for category: L={}, M={}, S={}, Market={}",
                        category.getLclassCode(), category.getMclassCode(), sClassCode, category.getMarketCode());

                auctionService.fetchPricesForScheduler(vo);

            } catch (Exception e) {
                log.error("Error fetching prices for category id: {}", category.getId(), e);
            }
        }

        log.info("Scheduled task completed.");
    }
}
