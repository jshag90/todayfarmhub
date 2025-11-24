package com.dodamsoft.todayfarmhub.service;

import com.dodamsoft.todayfarmhub.dto.CategoryListResponse;
import com.dodamsoft.todayfarmhub.dto.SClassAPIDto;
import com.dodamsoft.todayfarmhub.dto.SClassDto;
import com.dodamsoft.todayfarmhub.entity.LClassCode;
import com.dodamsoft.todayfarmhub.entity.MClassCode;
import com.dodamsoft.todayfarmhub.entity.SClassCode;
import com.dodamsoft.todayfarmhub.repository.LClassCodeRepository;
import com.dodamsoft.todayfarmhub.repository.MClassCodeRepository;
import com.dodamsoft.todayfarmhub.repository.SClassCodeRepository;
import com.dodamsoft.todayfarmhub.util.ApiUrlBuilder;
import com.dodamsoft.todayfarmhub.util.CategoryType;
import com.dodamsoft.todayfarmhub.util.HttpCallUtil;
import com.dodamsoft.todayfarmhub.util.OriginAPIUrlEnum;
import com.dodamsoft.todayfarmhub.vo.AuctionPriceVO;
import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;


@RequiredArgsConstructor
@Slf4j
@Service("sClassCategoryService")
public class SClassCategoryService implements GetAuctionCategoryService {

    private final SClassCodeRepository sClassCodeRepository;
    private final MClassCodeRepository mClassCodeRepository;
    private final LClassCodeRepository lClassCodeRepository;
    private final Gson gson;
    private final ApiUrlBuilder apiUrlBuilder;

    private final static int PAGE_SIZE = 1000;
    private final static String EXCEPTION_KEYWORD = "사용불가";

    @Override
    public boolean isType(CategoryType categoryType) {
        return CategoryType.SCLASS.equals(categoryType);
    }

    @Override
    @SuppressWarnings("unchecked")
    @Cacheable(
            value = "auctionCategoryCache",
            key = "#auctionPriceVO.lClassCode + '_' + #auctionPriceVO.mClassCode + '_' + #type",
            unless = "#result == null"
    )
    public CategoryListResponse<?> getCategory(AuctionPriceVO auctionPriceVO) {
        log.debug("getSClassCategory() 호출 - lClassCode: {}, mClassCode: {}",
                auctionPriceVO.getLClassCode(), auctionPriceVO.getMClassCode());

        return getSClassCategoryInternal(auctionPriceVO);
    }

    private CategoryListResponse<SClassDto> getSClassCategoryInternal(AuctionPriceVO auctionPriceVO) {

        LClassCode lClass = lClassCodeRepository.findOneBylclasscode(auctionPriceVO.getLClassCode());
        MClassCode mClass = mClassCodeRepository.findOneBylClassCodeAndMclasscode(lClass, auctionPriceVO.getMClassCode());
        if (lClass == null || mClass == null) {
            log.warn("존재하지 않는 코드: lClassCode={}, mClassCode={}", auctionPriceVO.getLClassCode(), auctionPriceVO.getMClassCode());
            return new CategoryListResponse<>();
        }

        if (sClassCodeRepository.countByLClassCodeAndMClassCode(lClass.getId(), mClass.getId()) == 0) {
            log.info("DB에 소분류 데이터 없음 → API 호출하여 저장 시작 (lClassCode: {}, mClassCode: {})", auctionPriceVO.getLClassCode(), auctionPriceVO.getMClassCode());

            saveInfoByResponseDataUsingAPI(lClass, mClass);

            log.info("API 호출 및 저장 완료 → DB에서 재조회");
        }

        List<SClassDto> resultList = sClassCodeRepository.findAllByLClassCodeAndMClassCode(lClass.getId(), mClass.getId()).stream()
                .filter(s -> !EXCEPTION_KEYWORD.equals(s.getSclassname()))
                .map(s -> new SClassDto(
                        s.getSclassname(),
                        s.getSclasscode(),
                        s.getMClassCode().getMclasscode()
                ))
                .collect(Collectors.toList());

        return new CategoryListResponse<>(resultList);
    }

    @Override
    @Transactional
    public <T> void saveInfoByResponseDataUsingAPI(LClassCode lClassCode, MClassCode mClassCode) {

        log.info("=== syncSClassCodesFromAPI 시작 ===");
        log.info("입력값 - lClassCode: {}, mClassCode: {}", lClassCode.getLclasscode(), mClassCode.getMclasscode());

        LClassCode lClass = lClassCodeRepository.findOneBylclasscode(lClassCode.getLclasscode());
        MClassCode mClass = mClassCodeRepository.findOneBylClassCodeAndMclasscode(lClass, mClassCode.getMclasscode());

        log.info("조회 결과 - lClass: {}, mClass: {}",
                lClass != null ? lClass.getId() : "null",
                mClass != null ? mClass.getId() : "null");

        if (lClass == null || mClass == null) {
            log.error("❌ 동기화 실패: 존재하지 않는 코드 (lClassCode: {}, mClassCode: {})",
                    lClassCode.getLclasscode(), mClassCode.getMclasscode());
            return;
        }

        Long lClassId = lClass.getId();
        Long mClassId = mClass.getId();

        // ★ 이미 DB에 데이터가 있는지 최종 확인
        List<SClassCode> existingData = sClassCodeRepository.findAllByLClassCodeAndMClassCode(lClassId, mClassId);
        log.info("기존 데이터 리스트 확인: {}건", existingData != null ? existingData.size() : 0);

        if (existingData != null && !existingData.isEmpty()) {
            log.info("✅ 이미 {}건의 소분류 데이터가 존재하여 동기화 스킵", existingData.size());
            return;
        }

        int pageNo = 1;
        int totalCount = 0;
        boolean firstPage = true;
        Set<String> seenCodes = new HashSet<>();
        int savedCount = 0;

        while (true) {

            String url = apiUrlBuilder.buildUrl(
                    OriginAPIUrlEnum.GET_CATEGORY_INFO_URL.getUrl(),
                    pageNo,
                    PAGE_SIZE,
                    "json",
                    Map.of(
                            "gds_lclsf_cd", lClassCode.getLclasscode(),
                            "gds_mclsf_cd", mClassCode.getMclasscode()
                    ),
                    List.of("gds_sclsf_cd", "gds_sclsf_nm")
            );


            log.info("요청 url : {}", url);

            String responseData = HttpCallUtil.getHttpGet(url);
            log.info("API 응답 길이: {}", responseData != null ? responseData.length() : 0);

            if (responseData == null || responseData.trim().isEmpty()) {
                log.warn("❌ Page {}: 응답 없음", pageNo);
                break;
            }

            SClassAPIDto dto = gson.fromJson(responseData, SClassAPIDto.class);
            if (dto == null || dto.getResponse() == null || dto.getResponse().getBody() == null) {
                log.error("❌ Page {}: 파싱 실패 - response: {}", pageNo, responseData.substring(0, Math.min(200, responseData.length())));
                break;
            }

            if (firstPage) {
                totalCount = dto.getResponse().getBody().getTotalCount();
                firstPage = false;
                log.info("총 소분류 수: {}", totalCount);
            }

            List<SClassAPIDto.Item> items = dto.getResponse().getBody().getItems().getItem();
            log.info("Page {}: 조회된 아이템 수 = {}", pageNo, items != null ? items.size() : 0);

            if (items == null || items.isEmpty()) {
                log.info("❌ Page {}: 더 이상 데이터 없음", pageNo);
                break;
            }

            for (SClassAPIDto.Item item : items) {
                String code = item.getGds_sclsf_cd();
                String name = item.getGds_sclsf_nm();

                if (code == null || code.isBlank() || name == null || name.isBlank()) {
                    log.warn("⚠️ 잘못된 데이터 스킵 - code: {}, name: {}", code, name);
                    continue;
                }

                // ★ 이미 처리한 코드는 스킵 (메모리 레벨 중복 체크)
                if (seenCodes.contains(code)) {
                    log.debug("중복 코드 스킵: {}", code);
                    continue;
                }

                // ★ DB에 이미 존재하는지 체크 (DB 레벨 중복 체크)
                Integer existsInDb = sClassCodeRepository.countByLClassCodeAndMClassCodeAndSclasscode(
                        lClassId, mClassId, code
                );

                if (existsInDb != null && existsInDb > 0) {
                    log.debug("DB에 이미 존재하는 코드 스킵: {}", code);
                    seenCodes.add(code);
                    continue;
                }

                // ★ 새로운 데이터만 저장
                seenCodes.add(code);
                SClassCode entity = SClassCode.builder()
                        .sclasscode(code)
                        .sclassname(name)
                        .lClassCode(lClass)
                        .mClassCode(mClass)
                        .build();

                try {
                    sClassCodeRepository.saveAndFlush(entity);
                    savedCount++;
                    log.info("✅ 소분류 저장 성공 [{}/{}]: {} - {}", savedCount, seenCodes.size(), code, name);
                } catch (Exception e) {
                    log.error("❌ 소분류 저장 실패: {} - {} | 에러: {}", code, name, e.getMessage(), e);
                }
            }

            // 페이징 종료 조건
            if (pageNo * PAGE_SIZE >= totalCount || items.size() < PAGE_SIZE) {
                break;
            }
            pageNo++;
        }

        log.info("=== 소분류 동기화 완료 ===");
        log.info("총 {}건 저장 (전체 {}건 확인)", savedCount, seenCodes.size());
        log.info("최종 DB 확인...");

        Integer finalCount = sClassCodeRepository.countByLClassCodeAndMClassCode(lClassId, mClassId);
        log.info("DB에 저장된 최종 건수: {}", finalCount);
    }

}