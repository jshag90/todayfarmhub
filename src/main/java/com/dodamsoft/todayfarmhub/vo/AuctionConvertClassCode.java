package com.dodamsoft.todayfarmhub.vo;

import com.dodamsoft.todayfarmhub.entity.LClassCode;
import com.dodamsoft.todayfarmhub.entity.MClassCode;
import com.dodamsoft.todayfarmhub.repository.LClassCodeRepository;
import com.dodamsoft.todayfarmhub.repository.MClassCodeRepository;
import com.dodamsoft.todayfarmhub.repository.MarketCodeRepository;
import com.dodamsoft.todayfarmhub.repository.SClassCodeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuctionConvertClassCode {

    private final LClassCodeRepository lClassCodeRepository;
    private final MClassCodeRepository mClassCodeRepository;
    private final SClassCodeRepository sClassCodeRepository;
    private final MarketCodeRepository marketCodeRepository;

    public Long getClassId(String type, String... code) {
        switch (type) {
            case "market" -> {
                return marketCodeRepository.findOneByMarketCode(code[0]).getId();
            }
            case "lclass" -> {
                return lClassCodeRepository.findOneBylclasscode(code[1]).getId();
            }
            case "mclass" -> {
                LClassCode oneBylclasscode = lClassCodeRepository.findOneBylclasscode(code[1]);
                return mClassCodeRepository.findOneBylClassCodeAndMclasscode(oneBylclasscode, code[2]).getId();
            }
            case "sclass" -> {
                LClassCode oneBylclasscode = lClassCodeRepository.findOneBylclasscode(code[1]);
                MClassCode oneBylClassCodeAndMclasscode = mClassCodeRepository.findOneBylClassCodeAndMclasscode(oneBylclasscode, code[2]);
                return sClassCodeRepository.findByLClassCodeIdAndMClassCodeIdAndSclasscode(oneBylclasscode.getId(), oneBylClassCodeAndMclasscode.getId(), code[3]).getId();
            }
        }
        return 0L;
    }
}

