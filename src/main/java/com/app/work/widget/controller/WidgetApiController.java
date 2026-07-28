package com.app.work.widget.controller;

import com.app.work.domain.vo.ScheduleVO;
import com.app.work.schedule.service.ScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/api/widget/*")
@RequiredArgsConstructor
public class WidgetApiController {

    private final ScheduleService scheduleService;

//    오늘 일정 요약 조회 (READ 스코프로 접근 가능)
    @GetMapping("/today")
    public List<ScheduleVO> today(HttpServletRequest req) {
        Integer memberNumber = (Integer) req.getSession().getAttribute("sessionNumber");
        return scheduleService.findScheduleList(memberNumber);
    }

//    업무 시작/완료 처리 (READ_WRITE 스코프 필요 — WidgetTokenInterceptor가 검증)
   /* @PostMapping("/status")
    public String status(HttpServletRequest req, Integer workNumber, String workStatus, String workRealStartTime, String workSpentTime) {
        Integer memberNumber = (Integer) req.getSession().getAttribute("sessionNumber");
        scheduleService.completeSchedule(workNumber, memberNumber, workStatus, workRealStartTime, workSpentTime);
        return "업데이트 성공";
    }*/
}
