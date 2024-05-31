package com.dodamsoft.todayfarmhub.service;

import com.dodamsoft.todayfarmhub.dto.AuctionAPIDto;
import com.dodamsoft.todayfarmhub.util.OriginAPIUrlEnum;
import com.dodamsoft.todayfarmhub.vo.AuctionAPIVO;
import com.dodamsoft.todayfarmhub.vo.AuctionPriceVO;
import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.springframework.stereotype.Service;

import java.io.IOException;


@Service
@RequiredArgsConstructor
@Slf4j
public class AuctionService {


    private final Gson gson;

    public AuctionAPIDto getAuctionPricesByOrginOpenAPIURL(AuctionPriceVO auctionPriceVO) throws IOException {

        String responseData = "";

        AuctionAPIVO auctionAPIVO = AuctionAPIVO.builder()
                                                .lClassCode("12")
                                                .mClassCode("1208")
                                                .sClassCode_arr("120801")
                                                .sClassName(" 홍고추(일반)")
                                                .wc_arr("")
                                                .wcName("")
                                                .cc_arr("")
                                                .ccName("")
                                                .lcate("prd")
                                                .sDate(auctionPriceVO.getStartDate())
                                                .eDate(auctionPriceVO.getEndDate())
                                                .sort("desc")
                                                .sortGbn("")
                                                .pageIndex(1)
                                                .limit("10")
                                                .build();

        HttpPost postRequest = new HttpPost(OriginAPIUrlEnum.GET_PRICES_URL.getUrl()); //POST 메소드 URL 생성
        postRequest.setHeader("Accept", "application/json");
        postRequest.setHeader("Connection", "keep-alive");
        postRequest.setHeader("Content-Type", "application/json");
        //postRequest.addHeader("x-api-key", RestTestCommon.API_KEY); //KEY 입력
        //postRequest.addHeader("Authorization", token); // token 이용시

        postRequest.setEntity(new StringEntity(gson.toJson(auctionAPIVO))); //json 메시지 입력

        try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
            try (CloseableHttpResponse response = httpclient.execute(postRequest)) {
                HttpEntity entity = response.getEntity();
                responseData = EntityUtils.toString(entity);
                EntityUtils.consume(entity);
            }
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
        log.info(responseData);
        return gson.fromJson(responseData, AuctionAPIDto.class);
    }

    public void initCategoryInfo(AuctionPriceVO auctionPriceVO){
        String responseData = "";

        AuctionAPIVO auctionAPIVO = AuctionAPIVO.builder()
                .lClassCode("")
                .mClassCode("")
                .sClassCode_arr("")
                .sClassName("")
                .flag("lClassCode")
                .wc_arr("")
                .wcName("")
                .cc_arr("")
                .ccName("")
                .lcate("prd")
                .sDate(auctionPriceVO.getStartDate())
                .eDate(auctionPriceVO.getEndDate())
                .sort("desc")
                .sortGbn("")
                .build();

        HttpPost postRequest = new HttpPost(OriginAPIUrlEnum.GET_LCLASS_URL.getUrl()); //POST 메소드 URL 생성
        postRequest.setHeader("Accept", "application/json");
        postRequest.setHeader("Connection", "keep-alive");
        postRequest.setHeader("Content-Type", "application/json");
        //postRequest.addHeader("x-api-key", RestTestCommon.API_KEY); //KEY 입력
        //postRequest.addHeader("Authorization", token); // token 이용시

        postRequest.setEntity(new StringEntity(gson.toJson(auctionAPIVO))); //json 메시지 입력

        try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
            try (CloseableHttpResponse response = httpclient.execute(postRequest)) {
                HttpEntity entity = response.getEntity();
                responseData = EntityUtils.toString(entity);
                EntityUtils.consume(entity);
            }
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
        log.info(responseData);


    }

}
