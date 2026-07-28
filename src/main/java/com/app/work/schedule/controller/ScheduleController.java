package com.app.work.schedule.controller;

import com.app.work.domain.dto.WorkplaceDTO;
import com.app.work.domain.dto.member.MemberDto;
import com.app.work.domain.vo.ListVO;
import com.app.work.domain.vo.ScheduleVO;
import com.app.work.login.service.LoginService;
import com.app.work.main.service.MainService;
import com.app.work.mapper.member.ScheduleMapper;
import com.app.work.schedule.service.ScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Controller
@RequestMapping("/*")
@RequiredArgsConstructor
public class ScheduleController {

    private final ListVO listVO;
    private final ScheduleService scheduleService;
    private final MainService mainService;

    @GetMapping("sd")
    public String schedule(Model model, HttpServletRequest req){
        try {
            Integer memberNumber = (Integer) req.getSession().getAttribute("sessionNumber");

            String toDay = listVO.getToDay();
            String toDay2 = scheduleService.findToDay(toDay);
            model.addAttribute("toDay", toDay2);

            String toTime = listVO.getToTime();
            String toTime2 = scheduleService.findToTime(toTime);
            model.addAttribute("toTime", toTime2);
            String endTime = listVO.getEndTime();
            String endTime2 = scheduleService.findEndTime(endTime);
            model.addAttribute("endTime", endTime2);

            List<ScheduleVO> scheduleList = scheduleService.findScheduleList(memberNumber);
            model.addAttribute("scheduleList", scheduleList);

            List<WorkplaceDTO> workList = scheduleService.findWorkName();
            model.addAttribute("workList", workList);

            List<ListVO> vacationList = mainService.findVacationMember(listVO);
            model.addAttribute("vacationList", vacationList);

            List<ScheduleVO> myVacationList = scheduleService.findVacation(memberNumber);
            model.addAttribute("myVacationList", myVacationList);

            Integer vacationCnt = scheduleService.findVacationCnt(memberNumber);
            model.addAttribute("vacationCnt", vacationCnt);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return "schedule/schedule_management";
    }

}
