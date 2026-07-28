package com.app.work.schedule.util;

import java.time.DayOfWeek;
import java.time.LocalDate;

public class VacationUtil {

    /**
     * 휴가 사용 일수 계산 (주말 제외, 반차 0.5일)
     * stDateStr / endDateStr : "YYYY-MM-DD HH:MI:SS" 또는 "YYYY-MM-DD" 형식
     */
    public static double calculateUsedDays(String stDateStr, String endDateStr, String vacationCheck) {
        if ("H".equals(vacationCheck)) {
            return 0.5;
        }
        LocalDate start = LocalDate.parse(stDateStr.substring(0, 10));
        LocalDate end   = LocalDate.parse(endDateStr.substring(0, 10));

        double count = 0;
        LocalDate cur = start;
        while (!cur.isAfter(end)) {
            DayOfWeek dow = cur.getDayOfWeek();
            if (dow != DayOfWeek.SATURDAY && dow != DayOfWeek.SUNDAY) {
                count++;
            }
            cur = cur.plusDays(1);
        }
        return count;
    }

    /**
     * 근속 연수에 따른 연간 휴가 부여일수 (근로기준법 제60조)
     * 1년  → 15일 / 3년 → 16일 / 2년마다 +1, 최대 25일
     */
    public static double calculateAnnualEntitlement(long years) {
        if (years < 1) return 0;
        return Math.min(15 + (years - 1) / 2, 25);
    }

    /**
     * double → String 변환 (정수면 "15", 소수면 "14.5")
     */
    public static String format(double count) {
        return (count % 1 == 0) ? String.valueOf((int) count) : String.valueOf(count);
    }

    public static double parse(String vacationCount) {
        if (vacationCount == null || vacationCount.isBlank()) return 0.0;
        try {
            return Double.parseDouble(vacationCount);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }
}
