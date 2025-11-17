package com.dodamsoft.todayfarmhub.util;

import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;

import java.io.IOException;

public class HttpCallUtil {

    public static String getHttpPost(String url, String jsonMessage) {

        String responseData = "";
        HttpPost postRequest = new HttpPost(url); //POST 메소드 URL 생성
        postRequest.setHeader("Accept", "application/json");
        postRequest.setHeader("Connection", "keep-alive");
        postRequest.setHeader("Content-Type", "application/json");
        //postRequest.addHeader("x-api-key", RestTestCommon.API_KEY); //KEY 입력
        //postRequest.addHeader("Authorization", token); // token 이용시

        postRequest.setEntity(new StringEntity(jsonMessage)); //json 메시지 입력

        try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
            try (CloseableHttpResponse response = httpclient.execute(postRequest)) {
                HttpEntity entity = response.getEntity();
                responseData = EntityUtils.toString(entity);
                EntityUtils.consume(entity);
            }
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }

        return responseData;


    }

    // === GET 요청 (신규 추가) ===
    public static String getHttpGet(String url) {
        String responseData = "";
        HttpGet getRequest = new HttpGet(url);
        getRequest.setHeader("Accept", "application/json, text/javascript, */*; q=0.01");
        getRequest.setHeader("Accept-Language", "ko-KR,ko;q=0.9,en-US;q=0.8,en;q=0.7");
        getRequest.setHeader("Cache-Control", "no-cache");
        getRequest.setHeader("Pragma", "no-cache");
        getRequest.setHeader("Connection", "keep-alive");
        getRequest.setHeader("User-Agent",
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) "
                        + "AppleWebKit/537.36 (KHTML, like Gecko) "
                        + "Chrome/120.0.0.0 Safari/537.36");
        getRequest.setHeader("Content-Type", "application/json; charset=UTF-8");

        try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
            try (CloseableHttpResponse response = httpclient.execute(getRequest)) {
                HttpEntity entity = response.getEntity();
                if (entity != null) {
                    responseData = EntityUtils.toString(entity);
                    EntityUtils.consume(entity);
                }
            }
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }

        return responseData;
    }
}
