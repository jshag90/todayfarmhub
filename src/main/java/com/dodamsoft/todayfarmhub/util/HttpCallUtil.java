package com.dodamsoft.todayfarmhub.util;

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
        HttpPost postRequest = new HttpPost(OriginAPIUrlEnum.GET_CATEGORY_INFO_URL.getUrl()); //POST 메소드 URL 생성
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
}
