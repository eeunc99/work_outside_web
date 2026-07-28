package com.app.work.admin.service;

import com.app.work.domain.Criteria;
import com.app.work.domain.dto.DeptDTO;
import com.app.work.domain.dto.MemberDTO;
import com.app.work.domain.dto.TeamDTO;
import com.app.work.domain.dto.WorkplaceDTO;
import com.app.work.domain.vo.MemberVO;

import java.util.List;

public interface AdminService {
//    사원 정보 조회
    public MemberVO getMember(Integer sessionNumber);

//	사원 리스트 조회(관리자 제외)
    public List<MemberVO> getMemberListWithoutAdmin(Integer teamNumber);

//    사원 조회
    public List<MemberVO> getMemberListWithPaging(Integer deptNumber, Integer teamNumber, String memberName, String memberUse, Criteria criteria);

//    사원 조회 명수
    public int getMemberListCount(Integer deptNumber, Integer teamNumber, String memberName, String memberUse, Criteria criteria);

//    사원 등록
    public void saveMember(MemberDTO memberDTO);

//    사원 수정
    public void setMember(MemberDTO memberDTO);

//    사원 상태 변경
    public void changeMemberUse(Integer memberNumber, String memberUse);

//    사원 추가 권한(관리자 메뉴, 외근현황 전체팀 열람) 부여/회수
    public void setMemberPermissions(Integer memberNumber, boolean adminAccess, boolean listAllTeams);

//    부서 조회
    public List<DeptDTO> getDepartmentList();

//    부서 등록
    public void saveDepartment(String deptName);

//    부서 수정
    public void setDepartmentName(Integer deptNumber, String deptName);

//    부서 삭제(상태 변경)
    public void removeDepartment(Integer deptNumber);

//    팀 조회
    public List<TeamDTO> getTeamList(Integer deptNumber);

//    팀 등록
    public void saveTeam(String teamName, Integer deptNumber);

//    팀 수정
    public void setTeamName(Integer teamNumber, String teamName);

//    팀 삭제(상태 변경)
    public void removeTeam(Integer teamNumber, Integer deptNumber);

//    근무지 조회
    public List<WorkplaceDTO> getWorkplaceList(String workplaceUse, Criteria criteria);

//    근무지 검색 조건에 따른 전체 개수
    public int getWorkplaceCount(String workplaceUse);

//    근무지 확인(중복 이름)
    public int checkWorkplaceName(String workplaceName);

//    근무지 등록
    public void saveWorkplace(String workplaceName);

//    근무지 수정
    public void setWorkplace(String workplaceName, String originName);

//    근무지 사용 및 삭제
    public void setWorkplaceUse(String workplaceName, String workplaceUse);

}
