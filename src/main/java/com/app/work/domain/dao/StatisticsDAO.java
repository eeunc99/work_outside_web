package com.app.work.domain.dao;

import com.app.work.domain.Criteria;
import com.app.work.domain.vo.GraphVO;
import com.app.work.domain.vo.StatisticsVO;
import com.app.work.mapper.statistics.StatisticsMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class StatisticsDAO {

    private final StatisticsMapper statisticsMapper;

    public List<StatisticsVO> selectWorkList(String workStartTime, String workEndTime, Integer teamNumber, Integer workplaceNumber, Integer memberNumber, Criteria criteria) {
        return statisticsMapper.selectWorkList(workStartTime, workEndTime, teamNumber, workplaceNumber, memberNumber, criteria);
    }

    public List<StatisticsVO> adminSelectWorkList(String workStartTime, String workEndTime, Integer teamNumber, Integer workplaceNumber, Integer memberNumber, Criteria criteria) {
        return statisticsMapper.selectWorkList(workStartTime, workEndTime, teamNumber, workplaceNumber, memberNumber, criteria);
    }

    public int countWorkListForPaging(String workStartTime, String workEndTime, Integer teamNumber, Integer workplaceNumber, Integer memberNumber) {
        return statisticsMapper.countWorkListForPaging(workStartTime, workEndTime, teamNumber, workplaceNumber, memberNumber);
    }

    public List<GraphVO> selectWorkListForGraph(String workStartTime, String workEndTime, Integer teamNumber, Integer workplaceNumber, Integer memberNumber) {
        return statisticsMapper.selectWorkListForGraph(workStartTime, workEndTime, teamNumber, workplaceNumber, memberNumber);
    }
}
