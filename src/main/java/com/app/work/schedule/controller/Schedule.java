package com.app.work.schedule.controller;

import com.app.work.domain.vo.ScheduleVO;
import com.app.work.schedule.service.ScheduleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class Schedule {
    private final ScheduleService scheduleService;

    @Scheduled(cron = "0 0/1 * * * *")
    public void scheduledCheck(){
        List<ScheduleVO> scheduleList = scheduleService.findSchedule();
        LocalDateTime now = LocalDateTime.now();

        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        now.format(dateTimeFormatter);

        for (ScheduleVO scheduleVO : scheduleList) {
            if (scheduleVO.getWorkRealStartTime() == null) {
                log.info("workRealStartTime이 null이므로 업데이트를 수행하지 않습니다.");
            } else if(scheduleVO.getWorkStatus().equals("이동중") && scheduleVO.getWorkRealStartTime2().equals(now.format(dateTimeFormatter)) && scheduleVO.getWorkRealStartTime() != null) {
                scheduleVO.setWorkStatus("진행중"); // workStatus를 '진행중'으로 변경합니다.
                scheduleService.status(scheduleVO.getWorkNumber(), scheduleVO.getMemberNumber(),scheduleVO.getWorkStatus());
                log.info("workRealStartTime이 null이 아니므로 workStatus를 '진행중'으로 변경하고 업데이트를 수행했습니다.");
            }
        }
    }

    // 매일 00:01 — 전날 종료 예정이었는데 완료 처리 안된 '진행중' 일정을 자동 완료 (이동중 제외)
    @Scheduled(cron = "0 1 0 * * *")
    public void autoCompleteOverdueSchedules() {
        log.info("자동 완료 처리 시작: 기한 초과 일정을 종료 예정 시간으로 완료 처리합니다.");
        scheduleService.autoCompleteOverdue();
        log.info("자동 완료 처리 완료");
    }

    @Scheduled(cron = "0 0/30 * * * *")
    public void vacationCheck(){
        List<ScheduleVO> vacationList = scheduleService.findVacationList();
        LocalDateTime now = LocalDateTime.now();

        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        now.format(dateTimeFormatter);

        DateTimeFormatter dateTimeFormatter2 = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        now.format(dateTimeFormatter2);

        for (ScheduleVO scheduleVO : vacationList) {
            log.info(scheduleVO.getVacationCheck());
            if(scheduleVO.getVacationCheck().equals("O")) {
                if (scheduleVO.getVacationStDate2().equals(now.format(dateTimeFormatter2))) {
                    scheduleService.vacationModify(scheduleVO.getMemberNumber(), "V");
                    log.info("시작날짜가 동일하여 업데이트 합니다.");
                } else if (scheduleVO.getVacationEndDate2().equals(now.format(dateTimeFormatter2))) {
                    scheduleService.vacationModify(scheduleVO.getMemberNumber(), "Y");
                    scheduleService.vacationExpire(scheduleVO.getVacationNumber());
                    log.info("종료날짜가 동일하여 해당 데이터를 삭제 합니다.");
                }  else if (!scheduleVO.getVacationEndDate2().equals(now.format(dateTimeFormatter2))) {
                    log.info("해당 날짜에 포함되는 휴가가 없어 수정 및 삭제하지 않았습니다.");
                }
            } else if(scheduleVO.getVacationCheck().equals("H")) {
                if (scheduleVO.getVacationStDate2().equals(now.format(dateTimeFormatter2))) {
                    scheduleService.vacationModify(scheduleVO.getMemberNumber(), "V");
                } else if (scheduleVO.getVacationEndDate2().equals(now.format(dateTimeFormatter2))) {
                    scheduleService.vacationModify(scheduleVO.getMemberNumber(), "Y");
                    scheduleService.vacationExpire(scheduleVO.getVacationNumber());
                }  else if (!scheduleVO.getVacationEndDate2().equals(now.format(dateTimeFormatter2))) {
                    log.info("해당 날짜에 포함되는 휴가가 없어 수정 및 삭제하지 않았습니다.");
                }
            } else {
                log.info("등록된 휴가가 없습니다.");
            }
        }
    }

    // 매 1분마다 — 야간 후 휴식 종료 시각 도달 시 자동 삭제
    @Scheduled(cron = "0 0/1 * * * *")
    public void nightRestAutoExpire() {
        List<ScheduleVO> vacationList = scheduleService.findVacationList();
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        for (ScheduleVO scheduleVO : vacationList) {
            if (scheduleVO.getVacationCheck().equals("N")) {
                if (scheduleVO.getVacationEndDate2().compareTo(now.format(formatter)) <= 0) {
                    scheduleService.nightRestRemove(scheduleVO.getMemberNumber());
                    log.info("야간 근무 후 휴식 종료 — 자동 삭제 완료 (memberNumber: {})", scheduleVO.getMemberNumber());
                }
            }
        }
    }
}
