package com.app.work.mapper.main;

import com.app.work.domain.vo.ListVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface MainMapper {
//    당일 날짜
    public String toDay(String toDay);

//    화이트보드(메인페이지)
    public List<ListVO> mainList(ListVO listVO);

    //    휴가자 리스트
    public List<ListVO> vacationMember(ListVO listVO);
}
