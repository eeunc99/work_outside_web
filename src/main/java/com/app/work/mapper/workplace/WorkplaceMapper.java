package com.app.work.mapper.workplace;

import com.app.work.domain.Criteria;
import com.app.work.domain.dto.WorkplaceDTO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface WorkplaceMapper {

//    근무지 조회
    public List<WorkplaceDTO> selectWorkplaceList(String workplaceUse, Criteria criteria);

//    근무지 조회 조건에 맞는 전체 개수
    public int selectWorkplaceCount(String workplaceUse);

//    근무지 확인(중복 이름)
    public int selectWorkplace(String workplaceName);

//    이름으로 단건 조회
    public WorkplaceDTO selectWorkplaceByName(String workplaceName);

//    근무지 등록
    public void saveWorkplace(String workplaceName);

//    근무지 수정
    public void updateWorkplace(String workplaceName, String originName);

//    근무지 삭제 및 사용
    public void updateWorkplaceUse(String workplaceName, String workplaceUse);
}
