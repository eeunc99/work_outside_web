package com.app.work.main.service;

import com.app.work.domain.vo.ListVO;
import com.app.work.mapper.main.MainMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MainService {

    private final MainMapper mainMapper;

    public String findToDay(String toDay){
        return mainMapper.toDay(toDay);
    }

    public List<ListVO> findMainList(ListVO listVO){
        List<ListVO> list =  mainMapper.mainList(listVO);

        return list;
    }

    public List<ListVO> findVacationMember(ListVO listVO){
        List<ListVO> vacationList = mainMapper.vacationMember(listVO);

        return vacationList;
    }
}
