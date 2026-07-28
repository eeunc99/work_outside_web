package com.app.work.domain.dao;

import com.app.work.domain.Criteria;
import com.app.work.domain.dto.MemberDTO;
import com.app.work.domain.vo.MemberVO;
import com.app.work.mapper.member.MemberMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class MemberDAO {
    private final MemberMapper memberMapper;

//    세션 번호로 정보 가져오기
    public MemberVO selectMember(Integer sessionNumber) {
        return memberMapper.selectMember(sessionNumber);
    }

//	사원 리스트 조회(관리자 제외)
    public List<MemberVO> selectMemberListWithoutAdmin(Integer teamNumber) {
        return memberMapper.selectMemberListWithoutAdmin(teamNumber);
    }

//    사원 조회
    public List<MemberVO> selectMemberListWithPaging(Integer deptNumber, Integer teamNumber, String memberName, String memberUse, Criteria criteria) {
        return memberMapper.selectMemberListWithPaging(deptNumber, teamNumber, memberName, memberUse, criteria);
    }

//    사원 조회 명수
    public int countMemberForPaging(Integer deptNumber, Integer teamNumber, String memberName, String memberUse, Criteria criteria) {
        return memberMapper.countMemberForPaging(deptNumber, teamNumber, memberName, memberUse, criteria);
    }

//    사원 등록
    public void insertMember(MemberDTO memberDTO) {
        memberMapper.insertMember(memberDTO);
    }

//    사원 수정
    public void updateMember(MemberDTO memberDTO) {
        memberMapper.updateMember(memberDTO);
    }

//    사원 삭제(상태 변경)
    public void deleteMemberUse(Integer memberNumber, String memberUse) {
        memberMapper.deleteMemberUse(memberNumber, memberUse);
    }

//	부서 삭제 시 해당 부서 사원들 해당없음으로 이동
    public void updateDeptAndTeamForDeleteDept(Integer deptNumber) {
        memberMapper.updateDeptAndTeamForDeleteDept(deptNumber);
    }

//	팀 삭제 시 해당 팀 사원들 빈칸으로 수정
    public void updateTeamForDeleteTeam(Integer teamNumber) {
        memberMapper.updateTeamForDeleteTeam(teamNumber);
    }

//	등급 기본값 외에 추가로 부여된 권한 코드 목록
    public List<String> getMemberPermissionCodes(Integer memberNumber) {
        return memberMapper.selectMemberPermissionCodes(memberNumber);
    }

//	권한 부여 내역 삭제
    public void deleteMemberPermissions(Integer memberNumber) {
        memberMapper.deleteMemberPermissions(memberNumber);
    }

//	권한 부여
    public void insertMemberPermission(Integer memberNumber, String permissionCode) {
        memberMapper.insertMemberPermission(memberNumber, permissionCode);
    }
}
