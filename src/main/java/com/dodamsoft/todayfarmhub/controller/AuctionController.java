package com.dodamsoft.todayfarmhub.controller;

import com.dodamsoft.todayfarmhub.AuctionPriceVO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auction")
public class AuctionController {

    @GetMapping("/prices")
    public ResponseEntity getAuctionPrices(
              @RequestParam("speciesName") String speciesName
            , @RequestParam("startDate") String startDate
            , @RequestParam("endDate") String endDate) {

        AuctionPriceVO auctionPriceVO = AuctionPriceVO.builder()
                .speciesName(speciesName)
                .startDate(startDate)
                .endDate(endDate)
                .build();

        return new ResponseEntity<AuctionPriceVO>(auctionPriceVO, HttpStatus.OK);
    }
}
