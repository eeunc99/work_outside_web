package com.app.work.list.service;

import com.app.work.domain.vo.ListVO;
import com.app.work.domain.vo.ScheduleVO;
import com.app.work.mapper.list.ListMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ListService {
    private final ListMapper listMapper;

    public String findToDay(String toDay){
        return listMapper.toDay(toDay);
    }

    public List<ListVO> findWorkList(ListVO listVO, Integer teamNumber){
        List<ListVO> list =  listMapper.workList(listVO, teamNumber);
        return list;
    }

    public List<ListVO> findFinishWorkList(ListVO listVO, Integer teamNumber) {
        return listMapper.selectFinishList(listVO, teamNumber);
    }
}
