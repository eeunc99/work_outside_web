package com.app.work.statistics.controller;

import com.app.work.admin.service.AdminService;
import com.app.work.config.ExcelXlsView;
import com.app.work.domain.Criteria;
import com.app.work.domain.dto.*;
import com.app.work.domain.vo.GraphVO;
import com.app.work.domain.vo.MemberVO;
import com.app.work.domain.vo.StatisticsVO;
import com.app.work.schedule.service.ScheduleService;
import com.app.work.statistics.service.StatisticsService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpSession;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Controller
@RequestMapping("/statistics/*")
@RequiredArgsConstructor
public class StatisticsController {

    private final StatisticsService statisticsService;
    private final AdminService adminService;
    private final ScheduleService scheduleService;

    @GetMapping("statistics")
    public String statistics(String workStartTime, String workEndTime, Integer workplaceNumber, Integer memberNumber, Integer page, Model model, HttpSession session) {
        try {
            Integer sessionNumber = (Integer) session.getAttribute("sessionNumber");

            if(page == null) page = 1;

            Criteria criteria = new Criteria(page, 10);

            Integer teamNumber = adminService.getMember(sessionNumber).getTeamNumber();

//        teamNumber는 session에 담긴 것 이용하기
            List<StatisticsVO> statisticsList = statisticsService.getStatisticsList(workStartTime, workEndTime, teamNumber, workplaceNumber, memberNumber, criteria);
            int total = statisticsService.getWorkListCount(workStartTime, workEndTime, teamNumber, workplaceNumber, memberNumber);

            List<WorkplaceDTO> workplaceList = adminService.getWorkplaceList("Y", null);
            List<MemberVO> memberList = adminService.getMemberListWithPaging(null, teamNumber, null, "Y", null);

            PageDTO pageDTO = new PageDTO(criteria, total);

            model.addAttribute("statisticsVOS", statisticsList);
            model.addAttribute("workplaceDTOS", workplaceList);
            model.addAttribute("memberVOS", memberList);
            model.addAttribute("pageDTO", pageDTO);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return "statistics/statistics";
    }

    @GetMapping("statistics-admin")
    public String statisticsAdmin(String workStartTime, String workEndTime, Integer teamNumber, Integer workplaceNumber, Integer memberNumber, Integer page, Model model) {
        try {
            if (page == null) page = 1;

            Criteria criteria = new Criteria(page, 10);

            List<StatisticsVO> statisticsList = statisticsService.getStatisticsList(workStartTime, workEndTime, teamNumber, workplaceNumber, memberNumber, criteria);
            int total = statisticsService.getWorkListCount(workStartTime, workEndTime, teamNumber, workplaceNumber, memberNumber);

            List<WorkplaceDTO> workplaceList = adminService.getWorkplaceList("Y", null);
            List<TeamDTO> teamList = adminService.getTeamList(null);

            List<WorkplaceDTO> workList = scheduleService.findWorkName();
            model.addAttribute("workList", workList);

            PageDTO pageDTO = new PageDTO(criteria, total);

            model.addAttribute("statisticsVOS", statisticsList);
            model.addAttribute("workplaceDTOS", workplaceList);
            model.addAttribute("teamDTOS", teamList);
            model.addAttribute("pageDTO", pageDTO);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return "statistics/statistics-admin";
    }

    @PostMapping("get-member-list")
    @ResponseBody
    public List<MemberVO> getMemberList(Integer teamNumber) {
        return adminService.getMemberListWithoutAdmin(teamNumber);
    }

    @PostMapping("get-graph-data")
    @ResponseBody
    public List<GraphVO> getGraphData(String workStartTime, String workEndTime, Integer teamNumber, Integer workplaceNumber, Integer memberNumber) {
        return statisticsService.getStatisticsListForGraph(workStartTime, workEndTime, teamNumber, workplaceNumber, memberNumber);
    }

    @PostMapping("get-graph-data-normal")
    @ResponseBody
    public List<GraphVO> getGraphData(String workStartTime, String workEndTime, HttpSession session, Integer workplaceNumber, Integer memberNumber) {
        Integer sessionNumber = (Integer) session.getAttribute("sessionNumber");

//        동일, null을 세션에 담긴걸로 처리해야함
        return statisticsService.getStatisticsListForGraph(workStartTime, workEndTime, adminService.getMember(sessionNumber).getTeamNumber(), workplaceNumber, memberNumber);
    }

    @GetMapping("graph")
    public String graph() {
        return "statistics/graph";
    }

    @GetMapping("/excel")
    public ModelAndView excelStatistics(String workStartTime, String workEndTime, Integer teamNumber, Integer workplaceNumber, Integer memberNumber, String type, HttpSession session, Locale locale) {
        Map<String, Object> excelData = new HashMap<>();
        try {
            if(teamNumber == null && type.equals("normal")) {
                Integer sessionNumber = (Integer) session.getAttribute("sessionNumber");
                teamNumber = adminService.getMember(sessionNumber).getTeamNumber();
            }
            excelData = statisticsService.excelStatisticsDownload(workStartTime, workEndTime, teamNumber, workplaceNumber, memberNumber, session, locale);
            return new ModelAndView(new ExcelXlsView(), excelData);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ModelAndView(new ExcelXlsView(), excelData);
    }
}
