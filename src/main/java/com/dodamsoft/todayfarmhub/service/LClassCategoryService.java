package com.dodamsoft.todayfarmhub.service;

import com.dodamsoft.todayfarmhub.dto.LClassAPIDto;
import com.dodamsoft.todayfarmhub.entity.LClassCode;
import com.dodamsoft.todayfarmhub.entity.MClassCode;
import com.dodamsoft.todayfarmhub.repository.LClassCodeRepository;
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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.dodamsoft.todayfarmhub.util.StringUtil.containsAlphabet;

@RequiredArgsConstructor
@Slf4j
@Service("lClassCategoryService")
public class LClassCategoryService implements GetAuctionCategoryService {

    private final Gson gson;
    private final LClassCodeRepository lClassCodeRepository;
    private final ApiUrlBuilder apiUrlBuilder;

    @Override
    public boolean isType(CategoryType categoryType) {
        return CategoryType.LCLASS.equals(categoryType);
    }

    @Override
    public <T> T getCategory(AuctionPriceVO auctionPriceVO) throws InterruptedException {

        // 1. DB에 대분류 코드가 없으면 API로 가져와 저장
        if (lClassCodeRepository.count() == 0) {
            log.info("대분류 코드가 DB에 없으므로 API로 수집 시작");
            saveInfoByResponseDataUsingAPI( null, null); // T는 더미, null 사용
        } else {
            log.info("DB에 대분류 코드 존재 ({}건)", lClassCodeRepository.count());
        }

        // 2. DB에서 정렬된 대분류 코드 조회
        List<LClassCode> lClassCodes = lClassCodeRepository.findAll(
                Sort.by(Sort.Direction.ASC, "lclassname")
        );

        // 3. API 응답 형식에 맞는 Item 리스트 생성
        List<LClassAPIDto.Item> itemList = lClassCodes.stream()
                .map(code -> LClassAPIDto.Item.builder()
                        .gds_lclsf_cd(code.getLclasscode())
                        .gds_lclsf_nm(code.getLclassname())
                        .build())
                .collect(Collectors.toList());

        // 4. 전체 응답 구조 생성 (클라이언트가 기대하는 형식)
        LClassAPIDto responseDto = LClassAPIDto.builder()
                .response(LClassAPIDto.Response.builder()
                        .body(LClassAPIDto.Body.builder()
                                .items(LClassAPIDto.Items.builder()
                                        .item(itemList)
                                        .build())
                                .totalCount(itemList.size())
                                .numOfRows(itemList.size())
                                .pageNo(1)
                                .build())
                        .header(LClassAPIDto.Header.builder()
                                .resultCode("0")
                                .resultMsg("정상")
                                .build())
                        .build())
                .build();

        return (T) responseDto;
    }

    @Override
    public <T> void saveInfoByResponseDataUsingAPI(LClassCode lClassCode, MClassCode mClassCode) throws InterruptedException {
        int numOfRows = 4000; // API 최대 허용치 확인 후 조정 (보통 1000)
        int pageNo = 1;
        int totalCount = 0;
        boolean firstPage = true;

        Set<String> seenCodes = new HashSet<>(); // 중복 방지 (같은 코드 반복 저장 방지)

        while (true) {

            String url = apiUrlBuilder.buildUrl(
                    OriginAPIUrlEnum.GET_CATEGORY_INFO_URL.getUrl(),
                    pageNo,
                    numOfRows,
                    "json",
                    null,   // 조건 없음
                    List.of("gds_lclsf_cd", "gds_lclsf_nm")
            );

            log.info("요청 url : {}", url);
            Thread.sleep(500);
            String responseData = HttpCallUtil.getHttpGet(url);
            log.info("Page {} 응답: {}", pageNo, responseData);

            if (responseData == null || responseData.trim().isEmpty()) {
                log.warn("Page {} 응답이 비어있습니다.", pageNo);
                break;
            }

            LClassAPIDto dto;
            try {
                dto = gson.fromJson(responseData, LClassAPIDto.class);
            } catch (Exception e) {
                log.error("JSON 파싱 실패 (page {}): {}", pageNo, e.getMessage());
                break;
            }

            // 첫 페이지에서 totalCount 설정
            if (firstPage && dto.getResponse() != null && dto.getResponse().getBody() != null) {
                totalCount = dto.getResponse().getBody().getTotalCount();
                firstPage = false;
                log.info("총 데이터 수: {}", totalCount);
            }

            List<LClassAPIDto.Item> items = dto.getResponse().getBody().getItems().getItem();
            if (items == null || items.isEmpty()) {
                log.info("Page {}: 더 이상 데이터 없음", pageNo);
                break;
            }

            for (LClassAPIDto.Item item : items) {
                String code = item.getGds_lclsf_cd();
                String name = item.getGds_lclsf_nm();

                // 2. 알파벳 포함 → 저장 제외
                if (containsAlphabet(code)) {
                    continue;
                }

                if (seenCodes.contains(code)) {
                    continue; // 중복 방지
                }
                seenCodes.add(code);

                lClassCodeRepository.save(LClassCode.builder()
                        .lclasscode(code)
                        .lclassname(name)
                        .build());
            }

            // 마지막 페이지 체크
            if (pageNo * numOfRows >= totalCount) {
                break;
            }
            pageNo++;
        }

        log.info("대분류 코드 저장 완료. 총 {}건", seenCodes.size());
    }



}

