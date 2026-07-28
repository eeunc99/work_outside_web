package com.app.work.mapper.list;

import com.app.work.domain.vo.ListVO;
import com.app.work.domain.vo.ScheduleVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ListMapper {
//    당일 날짜
    public String toDay(String toDay);
    
//    외근 현황 리스트
    public List<ListVO> workList(ListVO listVO, Integer teamNumber);

//    완료된 리스트
    public List<ListVO> selectFinishList(ListVO listVO, Integer teamNumber);

    // 업무 자동 시작
    public List<ListVO> workAutoCheck(ScheduleVO scheduleVO);
}
