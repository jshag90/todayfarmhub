package com.dodamsoft.todayfarmhub.controller;

import com.dodamsoft.todayfarmhub.dto.AuctionAPIDto;
import com.dodamsoft.todayfarmhub.dto.LClassAPIDto;
import com.dodamsoft.todayfarmhub.service.AuctionService;
import com.dodamsoft.todayfarmhub.service.GetAuctionCategoryService;
import com.dodamsoft.todayfarmhub.vo.AuctionPriceVO;
import lombok.RequiredArgsConstructor;
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

    private final GetAuctionCategoryService getAuctionCategory;

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
    public ResponseEntity getCategory(@PathVariable("type") String type) throws IOException {

        // 현재 날짜 구하기
        LocalDate now = LocalDate.now();         // 포맷 정의
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        String formattedNow = now.format(formatter);

        AuctionPriceVO auctionPriceVO = AuctionPriceVO.builder().startDate(formattedNow).endDate(formattedNow).build();

        switch (type) {
            case "lclass":
                return new ResponseEntity((LClassAPIDto) getAuctionCategory.getCategory(auctionPriceVO), HttpStatus.OK);
            case "mclass":
                return new ResponseEntity<String>("", HttpStatus.OK);
            case "sclass":
                return new ResponseEntity<String>("", HttpStatus.OK);
            case "market":
                return new ResponseEntity<String>("", HttpStatus.OK);
        }

        return new ResponseEntity<String>("", HttpStatus.OK);
    }


}
