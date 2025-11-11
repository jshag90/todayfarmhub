package com.dodamsoft.todayfarmhub.util;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;

public class DateUtils {

    public static LocalDate getPreviousFriday(LocalDate date) {
        // 현재 날짜의 요일을 가져옴
        DayOfWeek dayOfWeek = date.getDayOfWeek();

        // 오늘이 목요일인지 확인
        if (dayOfWeek == DayOfWeek.THURSDAY) {
            // 오늘이 목요일인 경우 지난 주 금요일로 이동
            return date.minusWeeks(1).with(TemporalAdjusters.previousOrSame(DayOfWeek.FRIDAY));
        } else {
            // 그렇지 않은 경우 현재 주의 금요일로 이동
            return date.with(TemporalAdjusters.previousOrSame(DayOfWeek.FRIDAY));
        }
    }

    /**
     * 날짜 형식 변환: YYMMDD → YYYY-MM-DD
     * 예: "251110" → "2025-11-10"
     */
    public static String formatDateForApi(String date) {
        if (date == null || date.isEmpty()) {
            return date;
        }

        // 이미 YYYY-MM-DD 형식이면 그대로 반환
        if (date.matches("\\d{4}-\\d{2}-\\d{2}")) {
            return date;
        }

        // YYMMDD 형식이면 YYYY-MM-DD로 변환
        if (date.matches("\\d{6}")) {
            String year = "20" + date.substring(0, 2);
            String month = date.substring(2, 4);
            String day = date.substring(4, 6);
            return year + "-" + month + "-" + day;
        }

        // YYYYMMDD 형식이면 YYYY-MM-DD로 변환
        if (date.matches("\\d{8}")) {
            String year = date.substring(0, 4);
            String month = date.substring(4, 6);
            String day = date.substring(6, 8);
            return year + "-" + month + "-" + day;
        }

        return date;
    }
}
