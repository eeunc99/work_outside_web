package com.app.work.schedule.controller;

import com.app.work.domain.dto.WorkplaceDTO;
import com.app.work.domain.vo.MemberVO;
import com.app.work.domain.vo.ScheduleVO;
import com.app.work.mapper.member.MemberMapper;
import com.app.work.mapper.workplace.WorkplaceMapper;
import com.app.work.schedule.service.ScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@RestController
@RequestMapping("/schedules/*")
@RequiredArgsConstructor
public class ScheduleRestController {

    private final ScheduleService scheduleService;
    private final MemberMapper memberMapper;
    private final WorkplaceMapper workplaceMapper;

    @PostMapping("/status")
    public String scheduleStatus(HttpServletRequest req, ScheduleVO scheduleVO, Integer workNumber, String workSpentTime, String workRealStartTime){
        Integer memberNumber  = (Integer) req.getSession().getAttribute("sessionNumber");
        scheduleVO.setMemberNumber(memberNumber);

        String workStatus = scheduleVO.getWorkStatus().equals("업무시작") ? "진행중" : "종료";
        String updateEndTime = scheduleVO.getWorkStatus().equals("업무시작") ? null : "Y";

        scheduleService.modifySchedule(workNumber, memberNumber, workStatus, updateEndTime, workRealStartTime);
        if (scheduleVO.getWorkStatus().equals("완료")) {
            String workRealStartTimeStr = scheduleVO.getWorkRealStartTime().trim(); // 공백 제거
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"); // 밀리초 포함
            System.out.println("소요시간! : " + scheduleVO.getWorkSpentTime());

            LocalDateTime workRealStartTime2 = LocalDateTime.parse(workRealStartTimeStr, formatter);
            LocalDateTime workEndTime2 = LocalDateTime.now();

            Duration duration = Duration.between(workRealStartTime2, workEndTime2);
            long minutesSpent = duration.toMinutes();


            if (scheduleVO.getWorkSpentTime().equals("NaN")) {
                scheduleVO.setWorkSpentTime(String.valueOf(minutesSpent));
                scheduleService.SpentTimeModify(workNumber, scheduleVO.getWorkSpentTime());
            } else {
                scheduleService.SpentTimeModify(workNumber, workSpentTime);
            }

        }
        return "진행완료 업데이트 성공";
    }

    @PostMapping("/sdAdd")
    public String scheduleAdd(ScheduleVO scheduleVO, HttpServletRequest req){
        int memberNumber = (Integer) req.getSession().getAttribute("sessionNumber");
        scheduleVO.setMemberNumber(memberNumber);

        if (scheduleVO.getWorkRealStartTime() == null || scheduleVO.getWorkRealStartTime().equals("")) {
            scheduleVO.setWorkRealStartTime(null);
        } else {
            scheduleVO.setWorkRealStartTime(scheduleVO.getWorkRealStartTime());
        }

        scheduleService.workRegister(scheduleVO);

        return  "등록 성공";
    }

    @PatchMapping("/update")
    public String scheduleUpdate(ScheduleVO scheduleVO , Integer workNumber , HttpServletRequest req){
        System.out.println(scheduleVO.getWorkPlaceNumber());
        scheduleVO.setWorkNumber(workNumber);
        scheduleService.scheduleModify(scheduleVO);

        return "업데이트 성공";
    }

    @PatchMapping("/update2")
    public String scheduleUpdate2(ScheduleVO scheduleVO , Integer workNumber , HttpServletRequest req){
        scheduleVO.setWorkNumber(workNumber);
        scheduleService.scheduleModify2(scheduleVO);

        return "업데이트 성공";
    }

    @GetMapping("/time")
    public String scheduleTime(Integer workNumber, ScheduleVO scheduleVO,HttpServletRequest req){
        int memberNumber = (Integer) req.getSession().getAttribute("sessionNumber");
        scheduleVO.setMemberNumber(memberNumber);
        scheduleService.findScheduleTime(workNumber);

        return "불러오기 성공";
    }

    @DeleteMapping("/delete")
    public String scheduleDelete(Integer workNumber, HttpServletRequest req){
        scheduleService.scheduleRemove(workNumber);
        return "삭제 성공!";
    }

    @PatchMapping("/spentTime")
    public String SpentTime(Integer workNumber, String workSpentTime){
        scheduleService.SpentTimeModify(workNumber, workSpentTime);
        return "소요 시간 계산 성공";
    }

    @GetMapping("/data")
    public ScheduleVO getData(Integer workNumber){
        return scheduleService.findData(workNumber);
    }

    @PatchMapping("/vacation")
    public String vacationUpdate(Integer memberNumber, String memberUse, HttpSession session){
        session.setAttribute("memberUse", String.valueOf((memberUse.equals('Y') ? 'V' : memberUse)));
        scheduleService.vacationModify(memberNumber, memberUse);
        return "변경 성공";
    }

    @PostMapping("/vacationAdd")
    public String vacationAdd(Integer memberNumber, String vacationStDate, String vacationEndDate, String vacationCheck, HttpServletRequest req){
        memberNumber = (Integer) req.getSession().getAttribute("sessionNumber");
        scheduleService.vacationRegister(memberNumber, vacationStDate, vacationEndDate, vacationCheck);
        refreshVacationCountInSession(memberNumber, req);
        return  "등록 성공";
    }

    @PostMapping("/vacationEdit")
    public String vacationEdit(Integer vacationNumber, ScheduleVO scheduleVO, HttpServletRequest req) {
        Integer memberNumber = (Integer) req.getSession().getAttribute("sessionNumber");
        scheduleService.editVacation(memberNumber, vacationNumber, scheduleVO.getVacationStDate(), scheduleVO.getVacationEndDate(), scheduleVO.getVacationCheck());
        refreshVacationCountInSession(memberNumber, req);
        return "수정 성공";
    }

    @DeleteMapping("/vacationDelete")
    public String vacationDelete(Integer vacationNumber, HttpServletRequest req){
        Integer memberNumber = (Integer) req.getSession().getAttribute("sessionNumber");
        scheduleService.vacationRemove(vacationNumber);
        refreshVacationCountInSession(memberNumber, req);
        return "삭제 성공!";
    }

    @PatchMapping("/nightRestEdit")
    public String nightRestEdit(String vacationStDate, String vacationEndDate, HttpServletRequest req) {
        Integer memberNumber = (Integer) req.getSession().getAttribute("sessionNumber");
        scheduleService.nightRestEdit(memberNumber, vacationStDate, vacationEndDate);
        return "야간 후 휴식 수정 성공";
    }

    @DeleteMapping("/nightRestDelete")
    public String nightRestDelete(HttpServletRequest req) {
        Integer memberNumber = (Integer) req.getSession().getAttribute("sessionNumber");
        scheduleService.nightRestRemove(memberNumber);
        return "야간 후 휴식 삭제 성공";
    }

    @PostMapping("/workplaceAdd")
    public WorkplaceDTO workplaceAdd(String workplaceName) {
        if (workplaceName == null || workplaceName.trim().isEmpty()) {
            throw new IllegalArgumentException("근무지 이름을 입력해주세요.");
        }
        String name = workplaceName.trim();
        if (workplaceMapper.selectWorkplace(name) == 0) {
            workplaceMapper.saveWorkplace(name);
        }
        return workplaceMapper.selectWorkplaceByName(name);
    }

    private void refreshVacationCountInSession(Integer memberNumber, HttpServletRequest req) {
        MemberVO member = memberMapper.selectMember(memberNumber);
        if (member != null) {
            req.getSession().setAttribute("vacationCount", member.getVacationCount());
        }
    }
}
