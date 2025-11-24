package com.dodamsoft.todayfarmhub.service;

import com.dodamsoft.todayfarmhub.dto.CategoryListResponse;
import com.dodamsoft.todayfarmhub.dto.MClassAPIDto;
import com.dodamsoft.todayfarmhub.dto.MClassDto;
import com.dodamsoft.todayfarmhub.entity.LClassCode;
import com.dodamsoft.todayfarmhub.entity.MClassCode;
import com.dodamsoft.todayfarmhub.repository.LClassCodeRepository;
import com.dodamsoft.todayfarmhub.repository.MClassCodeRepository;
import com.dodamsoft.todayfarmhub.util.ApiUrlBuilder;
import com.dodamsoft.todayfarmhub.util.CategoryType;
import com.dodamsoft.todayfarmhub.util.HttpCallUtil;
import com.dodamsoft.todayfarmhub.util.OriginAPIUrlEnum;
import com.dodamsoft.todayfarmhub.vo.AuctionAPIVO;
import com.dodamsoft.todayfarmhub.vo.AuctionPriceVO;
import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

import static com.dodamsoft.todayfarmhub.util.OriginAPIUrlEnum.GET_CATEGORY_INFO_URL;

@RequiredArgsConstructor
@Slf4j
@Service("mClassCategoryService")
public class MClassCategoryService implements GetAuctionCategoryService {

    private final MClassCodeRepository mClassCodeRepository;
    private final LClassCodeRepository lClassCodeRepository;
    private final Gson gson;
    private final ApiUrlBuilder apiUrlBuilder;
    private final static int PAGE_SIZE = 1000;

    @Override
    public boolean isType(CategoryType categoryType) {
        return CategoryType.MCLASS.equals(categoryType);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getCategory(AuctionPriceVO auctionPriceVO) throws InterruptedException {

        // LClass 조회
        LClassCode lClass = lClassCodeRepository.findOneBylclasscode(auctionPriceVO.getLClassCode());
        if (lClass == null) {
            log.warn("존재하지 않는 대분류 코드: {}", auctionPriceVO.getLClassCode());
            return (T) new CategoryListResponse<>();
        }

        // MClass 데이터 없으면 동기화
        if (!mClassCodeRepository.existsBylClassCode(lClass)) {
            log.info("중분류 데이터 없음 → 동기화 시작 (lClassCode: {})", auctionPriceVO.getLClassCode());
            saveInfoByResponseDataUsingAPI(lClass, null);
        }

        // 정렬하여 중분류 리스트 조회
        Sort sort = Sort.by(Sort.Direction.ASC, "mclassname");

        CategoryListResponse<MClassDto> response =
                new CategoryListResponse<>(
                        mClassCodeRepository.findAllBylClassCode(lClass, sort)
                                .stream()
                                .map(m -> new MClassDto(
                                        m.getMclasscode(),
                                        m.getMclassname(),
                                        m.getLClassCode().getLclasscode()
                                ))
                                .collect(Collectors.toList())
                );

        return (T) response;
    }

    @Override
    @Transactional
    public <T> void saveInfoByResponseDataUsingAPI(LClassCode lClassCode, MClassCode mClassCode) {
        log.info("중분류 데이터 동기화 시작 (lClassCode: {})", lClassCode);
        LClassCode lClass = lClassCodeRepository.findOneBylclasscode(lClassCode.getLclasscode());
        if (lClass == null) {
            log.error("동기화 실패: 존재하지 않는 대분류 코드 {}", lClassCode.getLclasscode());
            return;
        }

        int pageNo = 1;
        int totalCount = 0;
        boolean firstPage = true;
        Set<String> seenCodes = new HashSet<>();

        while (true) {

            String url = apiUrlBuilder.buildUrl(
                    OriginAPIUrlEnum.GET_CATEGORY_INFO_URL.getUrl(),
                    pageNo,
                    PAGE_SIZE,
                    "json",
                    Map.of("gds_lclsf_cd", lClassCode.getLclasscode()),
                    List.of("gds_mclsf_cd", "gds_mclsf_nm")
            );


            log.info("요청 url : {}", url);

            String responseData = HttpCallUtil.getHttpGet(url);
            if (responseData == null || responseData.trim().isEmpty()) {
                log.warn("Page {}: 응답 없음 (lClassCode: {})", pageNo, lClassCode.getLclasscode());
                break;
            }

            MClassAPIDto dto = gson.fromJson(responseData, MClassAPIDto.class);
            if (dto == null || dto.getResponse() == null || dto.getResponse().getBody() == null) {
                log.error("Page {}: 파싱 실패 (lClassCode: {})", pageNo, lClassCode.getLclasscode());
                break;
            }

            if (firstPage) {
                totalCount = dto.getResponse().getBody().getTotalCount();
                firstPage = false;
                log.info("총 중분류 수: {} (lClassCode: {})", totalCount, lClassCode.getLclasscode());
            }

            List<MClassAPIDto.Item> items = dto.getResponse().getBody().getItems().getItem();
            if (items == null || items.isEmpty()) {
                log.info("Page {}: 더 이상 데이터 없음", pageNo);
                break;
            }

            int savedThisPage = 0;
            for (MClassAPIDto.Item item : items) {
                String code = item.getGds_mclsf_cd();
                String name = item.getGds_mclsf_nm();

                if (code == null || code.isBlank() || name == null || name.isBlank()) {
                    continue;
                }

                if (seenCodes.contains(code)) {
                    continue;
                }

                seenCodes.add(code);

                if(!mClassCodeRepository.existsByMclassname(name)){
                    mClassCodeRepository.save(MClassCode.builder()
                            .mclasscode(code)
                            .mclassname(name)
                            .lClassCode(lClass)
                            .build());
                }

                savedThisPage++;
            }

            log.info("Page {}: 저장 {}건 | 누적 {}건 (lClassCode: {})",
                    pageNo, savedThisPage, seenCodes.size(), lClassCode.getLclasscode());

            if (pageNo * PAGE_SIZE >= totalCount || items.size() < PAGE_SIZE) {
                break;
            }
            pageNo++;
        }

        log.info("중분류 동기화 완료 (lClassCode: {}): 총 {}건 저장", lClassCode.getLclasscode(), seenCodes.size());
    }

}