package com.dodamsoft.todayfarmhub.controller;

import com.dodamsoft.todayfarmhub.dto.*;
import com.dodamsoft.todayfarmhub.service.AuctionService;
import com.dodamsoft.todayfarmhub.service.GetAuctionCategoryService;
import com.dodamsoft.todayfarmhub.util.DateUtils;
import com.dodamsoft.todayfarmhub.vo.AuctionPriceVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;


@RestController
@RequiredArgsConstructor
@RequestMapping("/auction")
public class AuctionController {

    private final AuctionService auctionService;

    @Qualifier("mClassCategoryService")
    private final GetAuctionCategoryService mClassCategoryService;

    @Qualifier("lClassCategoryService")
    private final GetAuctionCategoryService lClassCategoryService;

    @Qualifier("sClassCategoryService")
    private final GetAuctionCategoryService sClassCategoryService;

    @Qualifier("marketCategoryService")
    private final GetAuctionCategoryService marketCategoryService;

    @GetMapping("/prices")
    public ResponseEntity getAuctionPrices(
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

        return new ResponseEntity(auctionService.getAuctionPricesByOrigin(auctionPriceVO), HttpStatus.OK);
    }

    @GetMapping("/price/statistics")
    public ResponseEntity findPriceStatisticsByConditions(
            @RequestParam("date") String date
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

        return new ResponseEntity(auctionService.findPriceStatisticsByConditions(auctionPriceVO), HttpStatus.OK);

    }

    @GetMapping("/price/counts")
    public ResponseEntity findPriceTradeCountStatisticsByConditions(
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
    public ResponseEntity getCategory(@PathVariable("type") String type,
                                      @RequestParam(value = "lclasscode", required = false) String lClassCode,
                                      @RequestParam(value = "mclasscode", required = false) String mClassCode
    ) throws IOException {

        // 매주 금요일 날짜
        LocalDate friday = DateUtils.getPreviousFriday(LocalDate.now());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        String formattedNow = friday.format(formatter);

        LocalDate startDate = friday.minusDays(3);
        String formattedStartDate = startDate.format(formatter);

        AuctionPriceVO auctionPriceVO = AuctionPriceVO.builder().startDate(formattedStartDate).endDate(formattedNow).build();

        switch (type) {
            case "lclass":
                return new ResponseEntity((LClassAPIDto) lClassCategoryService.getCategory(auctionPriceVO), HttpStatus.OK);
            case "mclass":
                auctionPriceVO.setLClassCode(lClassCode);
                return new ResponseEntity((MClassAPIDto) mClassCategoryService.getCategory(auctionPriceVO), HttpStatus.OK);
            case "sclass":
                auctionPriceVO.setLClassCode(lClassCode);
                auctionPriceVO.setMClassCode(mClassCode);
                return new ResponseEntity((SClassAPIDto) sClassCategoryService.getCategory(auctionPriceVO), HttpStatus.OK);
            case "market":
                return new ResponseEntity((MarketInfoAPIDto) marketCategoryService.getCategory(auctionPriceVO), HttpStatus.OK);
        }

        return new ResponseEntity("", HttpStatus.OK);
    }


}
