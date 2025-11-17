package com.dodamsoft.todayfarmhub.service;

import com.dodamsoft.todayfarmhub.dto.MClassAPIDto;
import com.dodamsoft.todayfarmhub.entity.LClassCode;
import com.dodamsoft.todayfarmhub.entity.MClassCode;
import com.dodamsoft.todayfarmhub.repository.LClassCodeRepository;
import com.dodamsoft.todayfarmhub.repository.MClassCodeRepository;
import com.dodamsoft.todayfarmhub.util.HttpCallUtil;
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

    @Value("${api.kat.service-key}")
    private String serviceKey;

    private final int PAGE_SIZE = 1000;

    // ===================================================================
    // 인터페이스 필수 구현 1: getCategory (제네릭 유지)
    // ===================================================================
    @Override
    @SuppressWarnings("unchecked")
    public <T> T getCategory(AuctionPriceVO auctionPriceVO) {
        log.debug("getMClassCategory() 호출 - lClassCode: {}", auctionPriceVO.getLClassCode());

        // MClassAPIDto 반환 (클라이언트 기대 형식)
        Map<String, Object>  result = getMClassCategoryInternal(auctionPriceVO);
        return (T) result;
    }

    // ===================================================================
    // 인터페이스 필수 구현 2: saveInfoByResponseDataUsingAPI (제네릭 유지)
    // ===================================================================
    @Override
    @Transactional
    public <T> void saveInfoByResponseDataUsingAPI(T t, LClassCode lClassCode, MClassCode mClassCode) {
        if (!(t instanceof AuctionAPIVO)) {
            log.warn("예상치 못한 타입: {}", t != null ? t.getClass() : "null");
            return;
        }

        AuctionAPIVO auctionAPIVO = (AuctionAPIVO) t;
        String lClassCodeValue = auctionAPIVO.getLClassCode();

        log.info("중분류 데이터 동기화 시작 (lClassCode: {})", lClassCodeValue);

        // 내부 최적화된 메서드 호출
        syncMClassCodesFromAPI(lClassCodeValue);
    }

    // ===================================================================
    // 내부 최적화된 getCategory 로직
    // ===================================================================
    private Map<String, Object> getMClassCategoryInternal(AuctionPriceVO auctionPriceVO) {
        String lClassCode = auctionPriceVO.getLClassCode();

        LClassCode lClass = lClassCodeRepository.findOneBylclasscode(lClassCode);
        if (lClass == null) {
            log.warn("존재하지 않는 대분류 코드: {}", lClassCode);
            return new HashMap<>();
        }

        // DB에 중분류 없으면 동기화 (인터페이스 메서드 호출)
        if (!mClassCodeRepository.existsBylClassCode(lClass)) {
            log.info("중분류 데이터 없음 → 동기화 시작 (lClassCode: {})", lClassCode);

            // 인터페이스 메서드 호출 (필수 구현)
            AuctionAPIVO dummyVO = AuctionAPIVO.builder()
                    .lClassCode(lClassCode)
                    .flag("mClassCode")  // 플래그 설정
                    .build();
            saveInfoByResponseDataUsingAPI(dummyVO, lClass, null);
        }

        return buildMClassApiResponseForMobile(lClass);
    }

    // ===================================================================
    // 내부 최적화된 동기화 로직 (페이징 + 중복 방지)
    // ===================================================================
    private void syncMClassCodesFromAPI(String lClassCodeValue) {
        LClassCode lClass = lClassCodeRepository.findOneBylclasscode(lClassCodeValue);
        if (lClass == null) {
            log.error("동기화 실패: 존재하지 않는 대분류 코드 {}", lClassCodeValue);
            return;
        }

        int pageNo = 1;
        int totalCount = 0;
        boolean firstPage = true;
        Set<String> seenCodes = new HashSet<>();

        while (true) {
            String encodedLClass = URLEncoder.encode(lClassCodeValue, StandardCharsets.UTF_8);

            String url = String.format(
                    "%s?serviceKey=%s&pageNo=%d&numOfRows=%d&returnType=json" +
                            "&cond%%5Bgds_lclsf_cd%%3A%%3AEQ%%5D=%s" +
                            "&selectable=gds_mclsf_cd%%2Cgds_mclsf_nm",
                    GET_CATEGORY_INFO_URL.getUrl(), serviceKey, pageNo, PAGE_SIZE, encodedLClass
            );

            String responseData = HttpCallUtil.getHttpGet(url);
            if (responseData == null || responseData.trim().isEmpty()) {
                log.warn("Page {}: 응답 없음 (lClassCode: {})", pageNo, lClassCodeValue);
                break;
            }

            MClassAPIDto dto = parseResponse(responseData, pageNo);
            if (dto == null || dto.getResponse() == null || dto.getResponse().getBody() == null) {
                log.error("Page {}: 파싱 실패 (lClassCode: {})", pageNo, lClassCodeValue);
                break;
            }

            if (firstPage) {
                totalCount = dto.getResponse().getBody().getTotalCount();
                firstPage = false;
                log.info("총 중분류 수: {} (lClassCode: {})", totalCount, lClassCodeValue);
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
                    pageNo, savedThisPage, seenCodes.size(), lClassCodeValue);

            if (pageNo * PAGE_SIZE >= totalCount || items.size() < PAGE_SIZE) {
                break;
            }
            pageNo++;
        }

        log.info("중분류 동기화 완료 (lClassCode: {}): 총 {}건 저장", lClassCodeValue, seenCodes.size());
    }

    // ===================================================================
    // 헬퍼 메서드들 (동일)
    // ===================================================================
    private MClassAPIDto parseResponse(String json, int pageNo) {
        try {
            return gson.fromJson(json, MClassAPIDto.class);
        } catch (Exception e) {
            log.error("Page {} JSON 파싱 실패: {}", pageNo, e.getMessage(), e);
            return null;
        }
    }

    private MClassAPIDto buildMClassApiResponse(LClassCode lClass) {
        List<MClassCode> mClasses = mClassCodeRepository.findAllBylClassCode(
                lClass, Sort.by(Sort.Direction.ASC, "mclassname")
        );

        List<MClassAPIDto.Item> items = mClasses.stream()
                .map(m -> MClassAPIDto.Item.builder()
                        .gds_mclsf_cd(m.getMclasscode())
                        .gds_mclsf_nm(m.getMclassname())
                        .build())
                .collect(Collectors.toList());

        return MClassAPIDto.builder()
                .response(MClassAPIDto.Response.builder()
                        .header(MClassAPIDto.Header.builder()
                                .resultCode("0")
                                .resultMsg("정상")
                                .build())
                        .body(MClassAPIDto.Body.builder()
                                .items(MClassAPIDto.Items.builder().item(items).build())
                                .totalCount(items.size())
                                .numOfRows(items.size())
                                .pageNo(1)
                                .build())
                        .build())
                .build();
    }

    private Map<String, Object> buildMClassApiResponseForMobile(LClassCode lClass) {
        List<MClassCode> mClasses = mClassCodeRepository.findAllBylClassCode(
                lClass, Sort.by(Sort.Direction.ASC, "mclassname")
        );

        List<Map<String, String>> resultList = mClasses.stream()
                .map(m -> {
                    Map<String, String> map = new HashMap<>();
                    map.put("mclasscode", m.getMclasscode());
                    map.put("mclassname", m.getMclassname());
                    map.put("lclasscode", m.getLClassCode().getLclasscode());
                    return map;
                })
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("resultList", resultList);

        return response;
    }


    private MClassAPIDto buildEmptyResponse() {
        return MClassAPIDto.builder()
                .response(MClassAPIDto.Response.builder()
                        .header(MClassAPIDto.Header.builder()
                                .resultCode("99")
                                .resultMsg("대분류 코드 없음")
                                .build())
                        .body(MClassAPIDto.Body.builder()
                                .items(MClassAPIDto.Items.builder().item(List.of()).build())
                                .totalCount(0)
                                .numOfRows(0)
                                .pageNo(1)
                                .build())
                        .build())
                .build();
    }
}