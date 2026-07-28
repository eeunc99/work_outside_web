package com.app.work.schedule.service;

import com.app.work.domain.dto.WorkplaceDTO;
import com.app.work.domain.vo.ScheduleVO;
import com.app.work.mapper.member.MemberMapper;
import com.app.work.mapper.member.ScheduleMapper;
import com.app.work.mapper.workplace.WorkplaceMapper;
import com.app.work.schedule.util.VacationUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ScheduleService {

    private final ScheduleMapper scheduleMapper;
    private final WorkplaceMapper workplaceMapper;
    private final MemberMapper memberMapper;

//  날짜
    public String findToDay(String toDay){
        return scheduleMapper.toDay(toDay);
    }

//  시간
    public String findToTime(String toTime) {return scheduleMapper.toTime(toTime);}
    public String findEndTime(String endTime) {return scheduleMapper.endTime(endTime);}

//    등록된 일정 리스트
    public List<ScheduleVO> findScheduleList(Integer memberNumber){
        List<ScheduleVO> list = scheduleMapper.scheduleList(memberNumber);

        return list;
    }

//    일정 완료하기
    public void modifySchedule(Integer workNumber, Integer memberNumber, String workStatus, String updateEndTime, String workRealStartTime){
        scheduleMapper.scheduleStatus(workNumber, memberNumber, workStatus, updateEndTime, workRealStartTime);
    }

//    근무지 리스트
    public List<WorkplaceDTO> findWorkName(){
        List<WorkplaceDTO> workList = scheduleMapper.workList();

        for(int i=0; i<workList.size(); i++){
            workList.get(i).getWorkplaceNumber();
            workList.get(i).getWorkplaceName();
        }

        return workList;
    }

    public ScheduleVO findData(Integer workNumber){
        if (workNumber == null) {
            throw new IllegalArgumentException("일정 번호 누락");
        }
        return Optional.ofNullable(scheduleMapper.getData(workNumber)).orElseThrow(() -> {
                    throw new IllegalArgumentException("존재하지 않는 외근 번호");
        });
    }

//    일정 등록하기
    public void workRegister(ScheduleVO scheduleVO){
        if (scheduleVO.getMemberNumber() == null) {
            throw new IllegalArgumentException("회원 번호 누락");
        }
        scheduleMapper.workInsert(scheduleVO);
    }

    public void scheduleModify(ScheduleVO scheduleVO){
        if (scheduleVO == null) {
            throw new IllegalArgumentException("번호 누락");
        }
        scheduleMapper.scheduleUpdate(scheduleVO);
    }

    public void scheduleModify2(ScheduleVO scheduleVO){
        if (scheduleVO == null) {
            throw new IllegalArgumentException("번호 누락");
        }
        scheduleMapper.scheduleUpdate2(scheduleVO);
    }

    public void SpentTimeModify(Integer workNumber, String workSpentTime){
        if (workNumber == null) {
            throw new IllegalArgumentException("번호 누락");
        }
        scheduleMapper.spentTime(workNumber, workSpentTime);
    }

    public ScheduleVO findScheduleTime(Integer workNumber) {
        if (workNumber == null) {
            throw new IllegalArgumentException("번호 누락");
        }
        return Optional.ofNullable(scheduleMapper.scheduleTime(workNumber)).orElseThrow(() -> {
            throw new IllegalArgumentException("존재하지 않는 외근 번호");
        });
    }

    public void scheduleRemove(Integer workNumber) {
        if (workNumber == null) {
            throw new IllegalArgumentException("번호 누락");
        }

        scheduleMapper.scheduleDelete(workNumber);
    }

    public void vacationModify(Integer memberNumber, String memberUse){
        if (memberNumber == null) {
            throw new IllegalArgumentException("멤버 번호 누락");
        }

        scheduleMapper.vacationUpdate(memberNumber, memberUse);
    }

    //스케줄 처리
    public List<ScheduleVO> findSchedule(){
        List<ScheduleVO> list = scheduleMapper.schedule();
        return list;
    }

    //스케줄 - 상태만 변경
    public void status(Integer workNumber, Integer memberNumber, String workStatus) {
        scheduleMapper.status(workNumber, memberNumber, workStatus);

    }

    // 휴가 등록 — 이월일수 먼저 차감 후 vacation_count 차감 (야간휴식은 차감 없음)
    public void vacationRegister(Integer memberNumber, String vacationStDate, String vacationEndDate, String vacationCheck) {
        scheduleMapper.vacationInsert(memberNumber, vacationStDate, vacationEndDate, vacationCheck);
        if (!"N".equals(vacationCheck)) {
            double usedDays = VacationUtil.calculateUsedDays(vacationStDate, vacationEndDate, vacationCheck);
            deductVacation(memberNumber, usedDays);
            double remaining = memberMapper.selectVacationCount(memberNumber);
            scheduleMapper.vacationHistoryInsert(memberNumber, "등록", vacationStDate, vacationEndDate, vacationCheck, usedDays, remaining);
        }
    }

    // 야간 근무 후 휴식 수정
    public void nightRestEdit(Integer memberNumber, String vacationStDate, String vacationEndDate) {
        if (memberNumber == null) {
            throw new IllegalArgumentException("멤버 번호 누락");
        }
        scheduleMapper.nightRestEdit(memberNumber, vacationStDate, vacationEndDate);
    }

    // 야간 근무 후 휴식 삭제
    public void nightRestRemove(Integer memberNumber) {
        if (memberNumber == null) {
            throw new IllegalArgumentException("멤버 번호 누락");
        }
        scheduleMapper.nightRestDelete(memberNumber);
    }

    /**
     * 이월일수(carryover) 먼저 소진하고, 남은 차감분은 vacation_count에서 차감
     * 이월일수가 없으면 vacation_count에서만 차감
     */
    private void deductVacation(Integer memberNumber, double usedDays) {
        double carryover = memberMapper.selectCarryoverCount(memberNumber);
        if (carryover > 0) {
            double carryoverDeduct = Math.min(carryover, usedDays);
            memberMapper.updateCarryoverCountByDelta(memberNumber, -carryoverDeduct);
            double remainder = usedDays - carryoverDeduct;
            if (remainder > 0) {
                memberMapper.updateVacationCountByDelta(memberNumber, -remainder);
            }
        } else {
            memberMapper.updateVacationCountByDelta(memberNumber, -usedDays);
        }
    }

    // 휴가 삭제(취소) — vacation_number로 특정 휴가 삭제, 차감일수 복구
    public void vacationRemove(Integer vacationNumber) {
        ScheduleVO existing = scheduleMapper.selectVacation(vacationNumber);
        if (existing != null) {
            Integer memberNumber = existing.getMemberNumber();
            double usedDays = VacationUtil.calculateUsedDays(
                    existing.getVacationStDate2(), existing.getVacationEndDate2(), existing.getVacationCheck());
            scheduleMapper.vacationDelete(vacationNumber);
            restoreVacation(memberNumber, usedDays);
            double remaining = memberMapper.selectVacationCount(memberNumber);
            scheduleMapper.vacationHistoryInsert(memberNumber, "삭제",
                    existing.getVacationStDate2(), existing.getVacationEndDate2(),
                    existing.getVacationCheck(), -usedDays, remaining);
        }
    }

    /**
     * 휴가 취소 시 복구 — carryover가 원래 한도(최대 이월가능 수치)보다 적으면 carryover 먼저 채우고,
     * 나머지는 vacation_count로 복구
     * 간단하게: 현재 carryover_expire_date가 살아있으면 carryover로 복구, 아니면 vacation_count로 복구
     */
    private void restoreVacation(Integer memberNumber, double usedDays) {
        // carryover_expire_date 유효 여부와 무관하게 단순히 vacation_count 복구
        // (차감 당시 어느 pool에서 차감했는지 기록이 없으므로 vacation_count 복구가 안전)
        memberMapper.updateVacationCountByDelta(memberNumber, usedDays);
    }

    // 휴가 만료 처리 (스케줄러 전용) — vacation_number로 특정 휴가 만료
    public void vacationExpire(Integer vacationNumber) {
        ScheduleVO existing = scheduleMapper.selectVacation(vacationNumber);
        if (existing != null) {
            Integer memberNumber = existing.getMemberNumber();
            double usedDays = VacationUtil.calculateUsedDays(
                    existing.getVacationStDate2(), existing.getVacationEndDate2(), existing.getVacationCheck());
            scheduleMapper.vacationDelete(vacationNumber);
            double remaining = memberMapper.selectVacationCount(memberNumber);
            scheduleMapper.vacationHistoryInsert(memberNumber, "만료",
                    existing.getVacationStDate2(), existing.getVacationEndDate2(),
                    existing.getVacationCheck(), usedDays, remaining);
        }
    }

    // 기한 초과 일정 자동 완료
    public void autoCompleteOverdue() {
        scheduleMapper.autoCompleteOverdue();
    }

    //휴가 스케줄 처리
    public List<ScheduleVO> findVacationList(){
        List<ScheduleVO> list = scheduleMapper.selectVacationList();
        return list;
    }

    //회원의 전체 휴가 목록 조회 (최대 3개)
    public List<ScheduleVO> findVacation(Integer memberNumber){
        if (memberNumber == null) {
            throw new IllegalArgumentException("멤버 번호 누락");
        }
        return scheduleMapper.selectVacationsByMember(memberNumber);
    }

    public Integer findVacationCnt(Integer memberNumber) {
        if (memberNumber == null) {
            throw new IllegalArgumentException("멤버 번호 누락");
        }

        return Optional.ofNullable(scheduleMapper.vacationCnt(memberNumber)).orElseThrow(() -> {
            throw new IllegalArgumentException("존재하지 않는 회원");
        });
    }

    // 휴가 수정 — 기존 일수 복구 후 새 일수 차감 (vacation_number로 특정 휴가 수정)
    public void editVacation(Integer memberNumber, Integer vacationNumber, String vacationStDate, String vacationEndDate, String vacationCheck){
        if (memberNumber == null || vacationNumber == null) {
            throw new IllegalArgumentException("번호 누락");
        }

        double newDays = VacationUtil.calculateUsedDays(vacationStDate, vacationEndDate, vacationCheck);
        ScheduleVO existing = scheduleMapper.selectVacation(vacationNumber);
        if (existing != null) {
            double oldDays = VacationUtil.calculateUsedDays(
                    existing.getVacationStDate2(), existing.getVacationEndDate2(), existing.getVacationCheck());
            restoreVacation(memberNumber, oldDays);
            deductVacation(memberNumber, newDays);
        }

        scheduleMapper.vacationEdit(vacationNumber, vacationStDate, vacationEndDate, vacationCheck);
        double remaining = memberMapper.selectVacationCount(memberNumber);
        scheduleMapper.vacationHistoryInsert(memberNumber, "수정", vacationStDate, vacationEndDate, vacationCheck, newDays, remaining);
    }
}
