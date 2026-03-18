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
        return buildUrl(baseUrl, pageNo, numOfRows, returnType, condParams, selectable, null);
    }

    public String buildUrl(String baseUrl,
                           int pageNo,
                           int numOfRows,
                           String returnType,
                           Map<String, String> condParams,
                           List<String> selectable,
                           String serviceKeyOverride) {
        String resolvedServiceKey = hasText(serviceKeyOverride) ? serviceKeyOverride : serviceKey;

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%s?serviceKey=%s&pageNo=%d&numOfRows=%d&returnType=%s",
                baseUrl, resolvedServiceKey, pageNo, numOfRows, returnType));

        if (condParams != null) {
            condParams.forEach((key, value) -> {
                if (hasText(value)) {
                    sb.append("&cond[").append(key).append("::EQ]=").append(value);
                }
            });
        }

        if (selectable != null && !selectable.isEmpty()) {
            sb.append("&selectable=");
            sb.append(String.join(",", selectable));
        }

        return sb.toString();
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
