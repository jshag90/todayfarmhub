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
}
