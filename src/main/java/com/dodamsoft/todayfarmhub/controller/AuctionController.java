package com.dodamsoft.todayfarmhub.controller;

import com.dodamsoft.todayfarmhub.dto.*;
import com.dodamsoft.todayfarmhub.service.AuctionService;
import com.dodamsoft.todayfarmhub.service.GetAuctionCategoryService;
import com.dodamsoft.todayfarmhub.util.CategoryType;
import com.dodamsoft.todayfarmhub.util.DateUtils;
import com.dodamsoft.todayfarmhub.vo.AuctionPriceVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;


@RestController
@RequiredArgsConstructor
@RequestMapping("/auction")
@Slf4j
public class AuctionController {

    private final AuctionService auctionService;
    private final List<GetAuctionCategoryService> getAuctionCategoryService;

    @GetMapping("/prices")
    public ResponseEntity<?> getAuctionPrices(
              @RequestParam("date") String date
            , @RequestParam(value = "lclass") String lClassCode
            , @RequestParam(value = "mclass") String mClassCode
            , @RequestParam(value = "sclass") String sClassCode
            , @RequestParam(value = "pageNumber") int pageNumber
            , @RequestParam(value = "marketCode") String marketCode
    )  {

        AuctionPriceVO auctionPriceVO = AuctionPriceVO.builder()
                                                        .startDate(date)
                                                        .endDate(date)
                                                        .lClassCode(lClassCode)
                                                        .mClassCode(mClassCode)
                                                        .sClassCode(sClassCode == null ? "" : sClassCode)
                                                        .pageNumber(pageNumber)
                                                        .marketCode(marketCode)
                                                        .build();

        log.info("/auction/prices");
        log.info("요청 parma url : {}", auctionPriceVO);
        return new ResponseEntity<>(auctionService.getAuctionPricesByOrigin(auctionPriceVO), HttpStatus.OK);
    }

    @GetMapping("/price/statistics")
    public ResponseEntity<?> findPriceStatisticsByConditions(
            @RequestParam("date") String date
            , @RequestParam(value = "lclass") String lClassCode
            , @RequestParam(value = "mclass") String mClassCode
            , @RequestParam(value = "sclass") String sClassCode
            , @RequestParam(value = "marketCode") String marketCode
    )  {

        AuctionPriceVO auctionPriceVO = AuctionPriceVO.builder()
                                                      .startDate(date)
                                                      .endDate(date)
                                                      .lClassCode(lClassCode)
                                                      .mClassCode(mClassCode)
                                                      .sClassCode(sClassCode)
                                                      .marketCode(marketCode)
                                                      .build();

        return new ResponseEntity<>(auctionService.findPriceStatisticsByConditions(auctionPriceVO), HttpStatus.OK);

    }

    @GetMapping("/price/counts")
    public ResponseEntity<?> findPriceTradeCountStatisticsByConditions(
            @RequestParam("startDate") String date
            , @RequestParam(value = "lclass") String lClassCode
            , @RequestParam(value = "mclass") String mClassCode
            , @RequestParam(value = "sclass") String sClassCode
            , @RequestParam(value = "marketCode") String marketCode
    )  {

        AuctionPriceVO auctionPriceVO = AuctionPriceVO.builder()
                .startDate(date)
                .lClassCode(lClassCode)
                .mClassCode(mClassCode)
                .sClassCode(sClassCode)
                .marketCode(marketCode)
                .build();

        PriceTradeCountDto priceTradeCountStatisticsByConditions = auctionService.findPriceTradeCountStatisticsByConditions(auctionPriceVO);
        return new ResponseEntity<>(priceTradeCountStatisticsByConditions, HttpStatus.OK);

    }

    /**
     * lcass, mclass, sclass, market
     *
     * @param type
     * @return
     * @throws IOException
     */
    @GetMapping("/category/{type}")
    public ResponseEntity<?> getCategory(@PathVariable("type") String type,
                                         @RequestParam(value = "lclasscode", required = false) String lClassCode,
                                         @RequestParam(value = "mclasscode", required = false) String mClassCode
    ) throws InterruptedException {
        CategoryType categoryType = CategoryType.from(type);

        AuctionPriceVO auctionPriceVO = AuctionPriceVO.builder().lClassCode(lClassCode).mClassCode(mClassCode).build();

        GetAuctionCategoryService categoryService = getAuctionCategoryService.stream().filter(categoryServiceType -> categoryServiceType.isType(categoryType)).findFirst().get();
        if (categoryType.equals(CategoryType.LCLASS)) {
            return new ResponseEntity<>((List<LClassAPIDto.Item>)categoryService.getCategory(auctionPriceVO), HttpStatus.OK);
        }

        return new ResponseEntity<>((CategoryListResponse<?>)categoryService.getCategory(auctionPriceVO), HttpStatus.OK);

    }

}
