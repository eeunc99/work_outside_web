package com.app.work.statistics.service;

import com.app.work.domain.Criteria;
import com.app.work.domain.vo.GraphVO;
import com.app.work.domain.vo.StatisticsVO;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public interface StatisticsService {

    public List<StatisticsVO> getStatisticsList(String workStartTime, String workEndTime, Integer teamNumber, Integer workplaceNumber, Integer memberNumber, Criteria criteria);

    public List<StatisticsVO> getAdminStatisticsList(String workStartTime, String workEndTime, Integer teamNumber, Integer workplaceNumber, Integer memberNumber, Criteria criteria);

    public int getWorkListCount(String workStartTime, String workEndTime, Integer teamNumber, Integer workplaceNumber, Integer memberNumber);

    public List<GraphVO> getStatisticsListForGraph(String workStartTime, String workEndTime, Integer teamNumber, Integer workplaceNumber, Integer memberNumber);

    public Map<String, Object> excelStatisticsDownload(String workStartTime, String workEndTime, Integer teamNumber, Integer workplaceNumber, Integer memberNumber, HttpSession session, Locale locale) throws Exception;	// 다운로드 이력 엑셀 다운로드
}
