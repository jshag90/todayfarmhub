package com.dodamsoft.todayfarmhub.service;

import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;

@Slf4j
public class AuctionPricesSaveScheduler {

    public static void saveRemotePriceToDB(){

        LocalDate collectEndDate = LocalDate.now();
        LocalDate collectStartDate = collectEndDate.minus(Period.ofDays(365));

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyMMdd");

        LocalDate cursorDate = collectStartDate;
        while (!cursorDate.isAfter(collectEndDate)||cursorDate == collectEndDate) {

            log.info(String.valueOf(cursorDate.format(formatter)));


            if(cursorDate == collectEndDate)
                break;
            cursorDate = cursorDate.plusDays(1);
        }

    }

    public static void main(String[] args) {
        saveRemotePriceToDB();
    }


}
