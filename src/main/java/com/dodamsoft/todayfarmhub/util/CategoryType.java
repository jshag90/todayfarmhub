package com.dodamsoft.todayfarmhub.util;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CategoryType {
    LCLASS("lclass"),
    MCLASS("mclass"),
    SCLASS("sclass"),
    MARKET("market");

    private final String code;

    public static CategoryType from(String code) {
        for (CategoryType type : values()) {
            if (type.getCode().equalsIgnoreCase(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown category type: " + code);
    }
}
