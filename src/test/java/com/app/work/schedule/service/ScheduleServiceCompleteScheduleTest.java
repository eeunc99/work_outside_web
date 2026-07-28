package com.app.work.schedule.service;

import com.app.work.mapper.member.MemberMapper;
import com.app.work.mapper.member.ScheduleMapper;
import com.app.work.mapper.workplace.WorkplaceMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class ScheduleServiceCompleteScheduleTest {

    @Test
    void completeSchedule_start_setsInProgressWithNoEndTimeAndSkipsSpentTime() {
        ScheduleMapper scheduleMapper = mock(ScheduleMapper.class);
        ScheduleService service = new ScheduleService(scheduleMapper, mock(WorkplaceMapper.class), mock(MemberMapper.class));

        service.completeSchedule(1, 2, "업무시작", "2026-07-13 09:00:00.000", "NaN");

        verify(scheduleMapper).scheduleStatus(1, 2, "진행중", null, "2026-07-13 09:00:00.000");
        verify(scheduleMapper, never()).spentTime(anyInt(), anyString());
    }

    @Test
    void completeSchedule_complete_computesSpentTimeWhenNaN() {
        ScheduleMapper scheduleMapper = mock(ScheduleMapper.class);
        ScheduleService service = new ScheduleService(scheduleMapper, mock(WorkplaceMapper.class), mock(MemberMapper.class));

        String start = LocalDateTime.now().minusMinutes(10)
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"));

        service.completeSchedule(1, 2, "완료", start, "NaN");

        verify(scheduleMapper).scheduleStatus(1, 2, "종료", "Y", start);
        ArgumentCaptor<String> spentCaptor = ArgumentCaptor.forClass(String.class);
        verify(scheduleMapper).spentTime(eq(1), spentCaptor.capture());
        long minutes = Long.parseLong(spentCaptor.getValue());
        assertTrue(minutes >= 9 && minutes <= 11);
    }

    @Test
    void completeSchedule_complete_usesGivenSpentTimeWhenNotNaN() {
        ScheduleMapper scheduleMapper = mock(ScheduleMapper.class);
        ScheduleService service = new ScheduleService(scheduleMapper, mock(WorkplaceMapper.class), mock(MemberMapper.class));

        service.completeSchedule(1, 2, "완료", "2026-07-13 09:00:00.000", "42");

        verify(scheduleMapper).spentTime(1, "42");
    }
}
