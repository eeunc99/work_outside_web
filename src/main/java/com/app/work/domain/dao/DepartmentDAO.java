package com.app.work.domain.dao;

import com.app.work.domain.dto.DeptDTO;
import com.app.work.domain.dto.TeamDTO;
import com.app.work.mapper.department.DepartmentMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class DepartmentDAO {

    private final DepartmentMapper departmentMapper;
    public List<DeptDTO> selectDepartmentList() {
        return departmentMapper.selectDepartmentList();
    }

    public void saveDepartment(String deptName) {
        departmentMapper.saveDepartment(deptName);
    }

    public void updateDepartment(Integer deptNumber, String deptName) {
        departmentMapper.updateDepartment(deptNumber, deptName);
    }

    public void deleteDepartment(Integer deptNumber) {
        departmentMapper.deleteDepartment(deptNumber);
    }

    public List<TeamDTO> selectTeamList(Integer deptNumber) {
        return departmentMapper.selectTeamList(deptNumber);
    }

    public void saveTeam(String teamName, Integer deptNumber) {
        departmentMapper.saveTeam(teamName, deptNumber);
    }

    public void updateTeam(Integer teamNumber, String teamName) {
        departmentMapper.updateTeam(teamNumber, teamName);
    }

    public void deleteTeam(Integer teamNumber, Integer deptNumber) {
        departmentMapper.deleteTeam(teamNumber, deptNumber);
    }

}
