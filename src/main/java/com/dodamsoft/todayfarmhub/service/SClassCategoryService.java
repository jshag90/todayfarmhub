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
import com.dodamsoft.todayfarmhub.util.CategoryType;
import com.dodamsoft.todayfarmhub.util.HttpCallUtil;
import com.dodamsoft.todayfarmhub.vo.AuctionAPIVO;
import com.dodamsoft.todayfarmhub.vo.AuctionPriceVO;
import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.dodamsoft.todayfarmhub.util.OriginAPIUrlEnum.GET_CATEGORY_INFO_URL;

@RequiredArgsConstructor
@Slf4j
@Service("sClassCategoryService")
public class SClassCategoryService implements GetAuctionCategoryService {

    private final SClassCodeRepository sClassCodeRepository;
    private final MClassCodeRepository mClassCodeRepository;
    private final LClassCodeRepository lClassCodeRepository;
    private final Gson gson;

    @Value("${api.kat.service-key}")
    private String serviceKey;

    private final int PAGE_SIZE = 1000;

    @Override
    public boolean isType(CategoryType categoryType) {
        return CategoryType.SCLASS.equals(categoryType);
    }

    // ===================================================================
    // 1. getCategory (읽기 전용 - 트랜잭션 제거)
    // ===================================================================
    @Override
    @SuppressWarnings("unchecked")
    public <T> T getCategory(AuctionPriceVO auctionPriceVO) {
        log.debug("getSClassCategory() 호출 - lClassCode: {}, mClassCode: {}",
                auctionPriceVO.getLClassCode(), auctionPriceVO.getMClassCode());

        CategoryListResponse<SClassDto> result = getSClassCategoryInternal(auctionPriceVO);
        return (T) result;
    }

    // ===================================================================
    // 2. saveInfoByResponseDataUsingAPI (쓰기 전용)
    // ===================================================================
    @Override
    @Transactional
    public <T> void saveInfoByResponseDataUsingAPI(LClassCode lClassCode, MClassCode mClassCode) {
        syncSClassCodesFromAPI(lClassCode.getLclasscode(), mClassCode.getMclasscode());
    }

    // ===================================================================
    // 3. 내부: 실제 소분류 조회 로직 (트랜잭션 없음)
    // ===================================================================
    private CategoryListResponse<SClassDto> getSClassCategoryInternal(AuctionPriceVO auctionPriceVO) {
        String lClassCode = auctionPriceVO.getLClassCode();
        String mClassCode = auctionPriceVO.getMClassCode();

        LClassCode lClass = lClassCodeRepository.findOneBylclasscode(lClassCode);
        MClassCode mClass = mClassCodeRepository.findOneBylClassCodeAndMclasscode(lClass,mClassCode);

        if (lClass == null || mClass == null) {
            log.warn("존재하지 않는 코드: lClassCode={}, mClassCode={}", lClassCode, mClassCode);
            return buildEmptyResponse();
        }

        Long lClassId = lClass.getId();
        Long mClassId = mClass.getId();

        // DB에 데이터 있는지 확인
        Integer count = sClassCodeRepository.countByLClassCodeAndMClassCode(lClassId, mClassId);
        log.info("🔍 countByLClassCodeAndMClassCode 결과: {} (lClassId: {}, mClassId: {})", count, lClassId, mClassId);

        // 실제 리스트로 다시 확인
        List<SClassCode> existingList = sClassCodeRepository.findAllByLClassCodeAndMClassCode(lClassId, mClassId);
        log.info("🔍 findAllByLClassCodeAndMClassCode 결과: {}건", existingList != null ? existingList.size() : 0);

        if (existingList == null || existingList.isEmpty()) {
            log.info("DB에 소분류 데이터 없음 → API 호출하여 저장 시작 (lClassCode: {}, mClassCode: {})", lClassCode, mClassCode);

            saveInfoByResponseDataUsingAPI(lClass, mClass);

            log.info("API 호출 및 저장 완료 → DB에서 재조회");
        } else {
            log.info("DB에 소분류 데이터 존재 ({}건) → DB에서 조회", existingList.size());
        }

        // DB에서 조회하여 반환
        return buildSClassApiResponse(lClassId, mClassId);
    }

    // ===================================================================
    // 4. API → DB 동기화 (중복 체크 추가!)
    // ===================================================================
    @Transactional
    public void syncSClassCodesFromAPI(String lClassCodeValue, String mClassCodeValue) {
        log.info("=== syncSClassCodesFromAPI 시작 ===");
        log.info("입력값 - lClassCode: {}, mClassCode: {}", lClassCodeValue, mClassCodeValue);

        LClassCode lClass = lClassCodeRepository.findOneBylclasscode(lClassCodeValue);
        MClassCode mClass = mClassCodeRepository.findOneBylClassCodeAndMclasscode(lClass, mClassCodeValue);

        log.info("조회 결과 - lClass: {}, mClass: {}",
                lClass != null ? lClass.getId() : "null",
                mClass != null ? mClass.getId() : "null");

        if (lClass == null || mClass == null) {
            log.error("❌ 동기화 실패: 존재하지 않는 코드 (lClassCode: {}, mClassCode: {})",
                    lClassCodeValue, mClassCodeValue);
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

            String url = String.format(
                    "%s?serviceKey=%s&pageNo=%d&numOfRows=%d&returnType=json" +
                            "&cond[gds_lclsf_cd::EQ]=%s" +
                            "&cond[gds_mclsf_cd::EQ]=%s" +
                            "&selectable=gds_sclsf_cd,gds_sclsf_nm",
                    GET_CATEGORY_INFO_URL.getUrl(), serviceKey, pageNo, PAGE_SIZE, lClassCodeValue, mClassCodeValue
            );


            log.info("API 호출 URL: {}", url);

            String responseData = HttpCallUtil.getHttpGet(url);
            log.info("API 응답 길이: {}", responseData != null ? responseData.length() : 0);

            if (responseData == null || responseData.trim().isEmpty()) {
                log.warn("❌ Page {}: 응답 없음", pageNo);
                break;
            }

            SClassAPIDto dto = parseResponse(responseData, pageNo);
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

    // ===================================================================
    // 5. DB → API 응답 형식 변환 (읽기 전용 트랜잭션 추가)
    // ===================================================================
    @Transactional(readOnly = true)
    private CategoryListResponse<SClassDto> buildSClassApiResponse(Long lClassId, Long mClassId) {
        // DB에서 조회
        List<SClassCode> sClasses = sClassCodeRepository.findAllByLClassCodeAndMClassCode(lClassId, mClassId);

        // resultList로 변환
        List<SClassDto> resultList = sClasses.stream()
                .map(s -> new SClassDto(
                        s.getSclassname(),                  // mclassname
                        s.getSclasscode(), // lclasscode
                        s.getMClassCode().getMclasscode() // mclasscode
                ))
                .collect(Collectors.toList());

        return new CategoryListResponse(resultList);
    }


    // ===================================================================
    // 6. JSON 파싱 헬퍼
    // ===================================================================
    private SClassAPIDto parseResponse(String json, int pageNo) {
        try {
            return gson.fromJson(json, SClassAPIDto.class);
        } catch (Exception e) {
            log.error("Page {} JSON 파싱 실패: {}", pageNo, e.getMessage(), e);
            return null;
        }
    }

    // ===================================================================
    // 7. 빈 응답
    // ===================================================================
    private CategoryListResponse<SClassDto> buildEmptyResponse() {
        return new CategoryListResponse<>();
    }
}