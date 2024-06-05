package com.dodamsoft.todayfarmhub.controller;

import com.dodamsoft.todayfarmhub.dto.*;
import com.dodamsoft.todayfarmhub.service.AuctionService;
import com.dodamsoft.todayfarmhub.service.GetAuctionCategoryService;
import com.dodamsoft.todayfarmhub.service.LClassCategoryService;
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
            @RequestParam("speciesName") String speciesName
            , @RequestParam("startDate") String startDate
            , @RequestParam("endDate") String endDate) throws IOException {

        AuctionPriceVO auctionPriceVO = AuctionPriceVO.builder()
                .startDate(startDate)
                .endDate(endDate)
                .build();

        return new ResponseEntity<AuctionAPIDto>(auctionService.getAuctionPricesByOrginOpenAPIURL(auctionPriceVO), HttpStatus.OK);
    }

    /**
     * lcass, mclass, sclass, market
     * @param type
     * @return
     * @throws IOException
     */
    @GetMapping("/category/{type}")
    public ResponseEntity getCategory(@PathVariable("type") String type,
                                      @RequestParam(value = "lclasscode", required = false) String lClassCode,
                                      @RequestParam(value = "mclasscode", required = false) String mClassCode,
                                      @RequestParam(value = "sclasscode", required = false) String sClassCode
                                      ) throws IOException {

        // 매주 금요일 날짜
        LocalDate friday = DateUtils.getPreviousFriday(LocalDate.now());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        String formattedNow = friday.format(formatter);

        AuctionPriceVO auctionPriceVO = AuctionPriceVO.builder().startDate(formattedNow).endDate(formattedNow).build();

        switch (type) {
            case "lclass":
                return new ResponseEntity((LClassAPIDto)lClassCategoryService.getCategory(auctionPriceVO), HttpStatus.OK);
            case "mclass":
                auctionPriceVO.setLClassCode(lClassCode);
                return new ResponseEntity((MClassAPIDto)mClassCategoryService.getCategory(auctionPriceVO), HttpStatus.OK);
            case "sclass":
                auctionPriceVO.setLClassCode(lClassCode);
                auctionPriceVO.setMClassCode(mClassCode);
                return new ResponseEntity((SClassAPIDto)sClassCategoryService.getCategory(auctionPriceVO), HttpStatus.OK);
            case "market":
                return new ResponseEntity((MarketInfoAPIDto)marketCategoryService.getCategory(auctionPriceVO), HttpStatus.OK);
        }

        return new ResponseEntity<String>("", HttpStatus.OK);
    }


}
