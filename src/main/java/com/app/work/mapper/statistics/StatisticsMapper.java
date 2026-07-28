package com.app.work.mapper.statistics;

import com.app.work.domain.Criteria;
import com.app.work.domain.vo.GraphVO;
import com.app.work.domain.vo.StatisticsVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface StatisticsMapper {

    public List<StatisticsVO> selectWorkList(String workStartTime, String workEndTime, Integer teamNumber, Integer workplaceNumber, Integer memberNumber, Criteria criteria);

    public List<StatisticsVO> adminSelectWorkList(String workStartTime, String workEndTime, Integer teamNumber, Integer workplaceNumber, Integer memberNumber, Criteria criteria);

    public int countWorkListForPaging(String workStartTime, String workEndTime, Integer teamNumber, Integer workplaceNumber, Integer memberNumber);

    public List<GraphVO> selectWorkListForGraph(String workStartTime, String workEndTime, Integer teamNumber, Integer workplaceNumber, Integer memberNumber);
}
