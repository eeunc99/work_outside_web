package com.app.work.schedule.controller;

import com.app.work.domain.vo.MemberVO;
import com.app.work.mapper.member.MemberMapper;
import com.app.work.schedule.util.VacationUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Component
@RequiredArgsConstructor
public class VacationResetScheduler {

    private final MemberMapper memberMapper;

    /**
     * 매일 00:01 실행
     *
     * 1년 미만  : 입사 월 기념일마다 1일 적립 (최대 11일)
     * 1년 이상  : reset_date=0 → 1/1 초기화, reset_date=1 → 입사기념일 초기화
     */
    @Scheduled(cron = "0 1 0 * * *")
    public void processVacationReset() {
        LocalDate today = LocalDate.now();
        List<MemberVO> members = memberMapper.selectAllMembersForReset();

        for (MemberVO member : members) {
            if (member.getEmploymentDate() == null) continue;

            LocalDate empDate    = LocalDate.parse(member.getEmploymentDate());
            long      totalMonths = ChronoUnit.MONTHS.between(empDate, today);
            long      years       = totalMonths / 12;

            if (years < 1) {
                accrueMonthly(member, empDate, totalMonths, today);
            } else {
                resetAnnual(member, empDate, years, today);
            }
        }
    }

    /** 1년 미만: 매월 입사일 기준 1일 적립 (최대 11일) */
    private void accrueMonthly(MemberVO member, LocalDate empDate, long totalMonths, LocalDate today) {
        if (totalMonths < 1) return;
        if (today.getDayOfMonth() != empDate.getDayOfMonth()) return;

        double current = VacationUtil.parse(member.getVacationCount());
        if (current < 11) {
            memberMapper.updateVacationCountByDelta(member.getMemberNumber(), 1.0);
        }
    }

    /** 1년 이상: reset_date에 따라 연간 부여일수로 초기화 (이월 처리 포함) */
    private void resetAnnual(MemberVO member, LocalDate empDate, long years, LocalDate today) {
        double entitlement = VacationUtil.calculateAnnualEntitlement(years);
        boolean isResetDay;

        if (Integer.valueOf(0).equals(member.getResetDate())) {
            // 회계연도 기준: 매년 1월 1일
            isResetDay = (today.getMonthValue() == 1 && today.getDayOfMonth() == 1);
        } else {
            // 입사일 기준: 입사 기념일
            isResetDay = (today.getMonthValue() == empDate.getMonthValue()
                    && today.getDayOfMonth() == empDate.getDayOfMonth());
        }

        if (isResetDay) {
            // 이월 만료일이 설정된 경우 → 잔여 일수를 carryover_count로 이전
            if (member.getCarryoverExpireDate() != null && !member.getCarryoverExpireDate().isBlank()) {
                double remaining = VacationUtil.parse(member.getVacationCount());
                if (remaining > 0) {
                    double existingCarryover = VacationUtil.parse(member.getCarryoverCount());
                    memberMapper.setCarryoverCount(
                            member.getMemberNumber(),
                            VacationUtil.format(existingCarryover + remaining)
                    );
                }
            }
            // 신규 연차 부여 (항상 실행)
            memberMapper.setVacationCount(member.getMemberNumber(), VacationUtil.format(entitlement));
        }
    }

    /** 매일 00:01 — 이월 만료일 지난 사원 carryover_count 초기화 */
    @Scheduled(cron = "0 1 0 * * *")
    public void expireCarryover() {
        memberMapper.expireCarryoverIfDue(LocalDate.now().toString());
    }
}
