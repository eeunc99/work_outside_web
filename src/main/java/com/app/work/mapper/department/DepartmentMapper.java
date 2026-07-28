package com.app.work.mapper.department;

import com.app.work.domain.dto.DeptDTO;
import com.app.work.domain.dto.TeamDTO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface DepartmentMapper {

//    부서 조회
    public List<DeptDTO> selectDepartmentList();

//    부서 등록
    public void saveDepartment(String deptName);

//    부서 수정
    public void updateDepartment(Integer deptNumber, String deptName);

//    부서 삭제(상태 변경)
    public void deleteDepartment(Integer deptNumber);

//    팀 조회
    public List<TeamDTO> selectTeamList(Integer deptNumber);

//    팀 등록
    public void saveTeam(String teamName, Integer deptNumber);

//    팀 수정
    public void updateTeam(Integer teamNumber, String teamName);

//    팀 삭제(상태 변경)
    public void deleteTeam(Integer teamNumber, Integer deptNumber);
}
