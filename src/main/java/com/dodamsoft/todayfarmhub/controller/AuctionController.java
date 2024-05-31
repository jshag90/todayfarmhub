package com.dodamsoft.todayfarmhub.controller;

import com.dodamsoft.todayfarmhub.dto.AuctionAPIDto;
import com.dodamsoft.todayfarmhub.service.AuctionService;
import com.dodamsoft.todayfarmhub.vo.AuctionPriceVO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;


@RestController
@RequiredArgsConstructor
@RequestMapping("/auction")
public class AuctionController {

    private final AuctionService auctionService;

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

    @GetMapping("/category")
    public ResponseEntity getCategory(@RequestParam("startDate") String startDate
            , @RequestParam("endDate") String endDate) throws IOException {

        AuctionPriceVO auctionPriceVO = AuctionPriceVO.builder()
                .startDate(startDate)
                .endDate(endDate)
                .build();
        auctionService.initCategoryInfo(auctionPriceVO);
        return new ResponseEntity<String>("test", HttpStatus.OK);
    }


}
