package com.dodamsoft.todayfarmhub.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class ApiUrlBuilder {

    @Value("${api.kat.service-key}")
    private String serviceKey;

    public String buildUrl(String baseUrl,
                                  int pageNo,
                                  int numOfRows,
                                  String returnType,
                                  Map<String, String> condParams,
                                  List<String> selectable) {

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%s?serviceKey=%s&pageNo=%d&numOfRows=%d&returnType=%s",
                baseUrl, serviceKey, pageNo, numOfRows, returnType));

        // 조건절 cond[...] 처리
        if (condParams != null) {
            condParams.forEach((key, value) -> {
                sb.append("&cond[").append(key).append("::EQ]=").append(value);
            });
        }

        // selectable 처리
        if (selectable != null && !selectable.isEmpty()) {
            sb.append("&selectable=");
            sb.append(String.join(",", selectable));
        }

        return sb.toString();
    }
}
