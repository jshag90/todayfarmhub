package com.dodamsoft.todayfarmhub.service;

import com.dodamsoft.todayfarmhub.entity.LClassCode;
import com.dodamsoft.todayfarmhub.entity.MClassCode;
import com.dodamsoft.todayfarmhub.entity.SClassCode;
import com.dodamsoft.todayfarmhub.repository.LClassCodeRepository;
import com.dodamsoft.todayfarmhub.repository.MClassCodeRepository;
import com.dodamsoft.todayfarmhub.repository.SClassCodeRepository;
import com.dodamsoft.todayfarmhub.vo.AuctionAPIVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuctionPricesSaveScheduler {

    private final LClassCodeRepository lClassCodeRepository;
    private final MClassCodeRepository mClassCodeRepository;
    private final SClassCodeRepository sClassCodeRepository;

    public void saveRemotePriceToDB() {

        LocalDate collectEndDate = LocalDate.now();
        LocalDate collectStartDate = collectEndDate.minus(Period.ofDays(365));

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyMMdd");

        LocalDate cursorDate = collectStartDate;
        List<AuctionAPIVO> result = new ArrayList<>();
        while (!cursorDate.isAfter(collectEndDate) || cursorDate == collectEndDate) {

            log.info(cursorDate.format(formatter));

            result = initAuctionAPIVO(cursorDate.format(formatter));

            if (cursorDate == collectEndDate)
                break;
            cursorDate = cursorDate.plusDays(1);
        }

        System.out.println(result);


    }

    public List<AuctionAPIVO> initAuctionAPIVO(String dates) {

        List<AuctionAPIVO> auctionAPIVOList = new ArrayList<>();

        for (LClassCode lClassCode : lClassCodeRepository.findAll()) {

            for (MClassCode mclassCode : mClassCodeRepository.findAllBylClassCode(lClassCode, Sort.by(Sort.Direction.ASC, "mclassname"))) {

                for (SClassCode sClassCode : sClassCodeRepository.findAllByMClassCodeIdAndLClassCodeIdOrderBySclassnameDesc(lClassCode.getId(), mclassCode.getId())) {

                    AuctionAPIVO auctionAPIVO = AuctionAPIVO.builder()
                            .lClassCode(lClassCode.getLclasscode())
                            .mClassCode(mclassCode.getMclasscode())
                            .sClassCode_arr(sClassCode.getSclasscode())
                            .sClassName("")
                            .wc_arr("")
                            .wcName("")
                            .cc_arr("")
                            .ccName("")
                            .lcate("prd")
                            .sDate(dates)
                            .eDate(dates)
                            .sort("desc")
                            .sortGbn("")
                            .pageIndex(1)
                            .limit("")
                            .build();

                    auctionAPIVOList.add(auctionAPIVO);
                }

            }
        }

        log.info(String.valueOf(auctionAPIVOList.size()));

        return auctionAPIVOList;

    }


}
