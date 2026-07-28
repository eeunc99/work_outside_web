package com.app.work.statistics.service;

import com.app.work.config.ExcelWriter;
import com.app.work.domain.Criteria;
import com.app.work.domain.dao.StatisticsDAO;
import com.app.work.domain.vo.GraphVO;
import com.app.work.domain.vo.StatisticsVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {

    private final StatisticsDAO statisticsDAO;

    @Override
    public List<StatisticsVO> getStatisticsList(String workStartTime, String workEndTime, Integer teamNumber, Integer workplaceNumber, Integer memberNumber, Criteria criteria) {
        return statisticsDAO.selectWorkList(workStartTime, workEndTime , teamNumber, workplaceNumber, memberNumber, criteria);
    }

    @Override
    public List<StatisticsVO> getAdminStatisticsList(String workStartTime, String workEndTime, Integer teamNumber, Integer workplaceNumber, Integer memberNumber, Criteria criteria) {
        return statisticsDAO.adminSelectWorkList(workStartTime, workEndTime , teamNumber, workplaceNumber, memberNumber, criteria);
    }

    @Override
    public int getWorkListCount(String workStartTime, String workEndTime, Integer teamNumber, Integer workplaceNumber, Integer memberNumber) {
        return statisticsDAO.countWorkListForPaging(workStartTime, workEndTime, teamNumber, workplaceNumber, memberNumber);
    }

    @Override
    public List<GraphVO> getStatisticsListForGraph(String workStartTime, String workEndTime, Integer teamNumber, Integer workplaceNumber, Integer memberNumber) {
        return statisticsDAO.selectWorkListForGraph(workStartTime, workEndTime, teamNumber, workplaceNumber, memberNumber);
    }

    @Override
    public Map<String, Object> excelStatisticsDownload(String workStartTime, String workEndTime, Integer teamNumber, Integer workplaceNumber, Integer memberNumber, HttpSession session, Locale locale) throws Exception {
        List<String> headList = null;				// 엑셀 해더
        List<List<String>> bodyList = null;			// 엑셀 바디
        String fileName = "";						// 엑셀 파일명
        headList = new ArrayList<>();			// 헤더 리스트
        bodyList = new ArrayList<>();	// 바디 리스트

        try {

            /* 파일명(다운로드 이력) */
            fileName = "외근_이력(" + workStartTime + "~" + workEndTime + ")";

            /* 엑셀 헤더 */
            headList.add("이름");
            headList.add("소속");
            headList.add("근무지");
            headList.add("시작시간");
            headList.add("업무시작시간");
            headList.add("종료시간");
            headList.add("소요시간");
            headList.add("업무제목");
//            headList.add("업무내용");

            /* 만들 데이터 추출 */
            List<StatisticsVO> list = statisticsDAO.selectWorkList(workStartTime, workEndTime, teamNumber, workplaceNumber, memberNumber, null);

            if(list.size()>0) {
                for(StatisticsVO item : list) {
                    List<String> tmpBodyList = new ArrayList<>();
                    tmpBodyList.add(item.getMemberName());
                    tmpBodyList.add(item.getTeamName());
                    tmpBodyList.add(item.getWorkplaceName());
                    tmpBodyList.add(item.getWorkStartTime());
                    tmpBodyList.add(item.getWorkRealStartTime());

                    /* 종료 시간이 없으면 진행중 으로 변경 */
                    tmpBodyList.add(item.getWorkEndTime() == null ? "진행중" : item.getWorkEndTime());

                    /* 소요시간 0시간 0분 형식으로 변경 */
                    if(!item.getWorkSpentTime().equals("진행중")) {
                        int hours = Integer.parseInt(item.getWorkSpentTime()) / 60;
                        int minutes = Integer.parseInt(item.getWorkSpentTime()) % 60;

                        String hour = hours > 0 ? hours + "시간 " : "";
                        String minute = minutes > 0 ? minutes + "분" : "";

                        tmpBodyList.add(hour + minute);
                    } else {
                        tmpBodyList.add(item.getWorkSpentTime());
                    }

                    tmpBodyList.add(item.getWorkTitle());
//                    tmpBodyList.add(item.getWorkDetail());

                    /* 엑셀 바디 */
                    bodyList.add(tmpBodyList);
                }
            }

            return ExcelWriter.createExcelData(headList, bodyList, fileName);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return ExcelWriter.createExcelData(headList, bodyList, fileName);
    }
}
