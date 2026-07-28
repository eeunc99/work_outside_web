package com.app.work.mapper.member;

import com.app.work.domain.Criteria;
import com.app.work.domain.dto.MemberDTO;
import com.app.work.domain.vo.MemberVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MemberMapper {
	//	로그인
	public Integer login(@Param("memberId")String memberId, @Param("memberPassword")String memberPassword);

//	세션 처리
	public Integer sessionMember(@Param("memberId")String memberId, @Param("memberPassword")String memberPassword);

//	세션으로 이름 처리
	public MemberVO selectMember(Integer sessionNumber);

//	등급 기본값 외에 추가로 부여된 권한 코드 목록
	public List<String> selectMemberPermissionCodes(@Param("memberNumber") Integer memberNumber);

//	권한 토글 저장 (기존 부여 내역 삭제 후 재기록)
	public void deleteMemberPermissions(@Param("memberNumber") Integer memberNumber);

	public void insertMemberPermission(@Param("memberNumber") Integer memberNumber, @Param("permissionCode") String permissionCode);

//	사원 리스트 조회(관리자 제외)
	public List<MemberVO> selectMemberListWithoutAdmin(Integer teamNumber);

//	사원 조회
	public List<MemberVO> selectMemberListWithPaging(Integer deptNumber, Integer teamNumber, String memberName, String memberUse, Criteria criteria);

//	사원 조회 시 전체 명수
	public int countMemberForPaging(Integer deptNumber, Integer teamNumber, String memberName, String memberUse, Criteria criteria);

//	사원 등록
	public void insertMember(MemberDTO memberDTO);

//	사원 수정
	public void updateMember(MemberDTO memberDTO);

//	사원 삭제(상태 수정)
	public void deleteMemberUse(Integer memberNumber, String memberUse);

//	부서 삭제 시 해당 부서 사원들 해당없음으로 이동
	public void updateDeptAndTeamForDeleteDept(Integer deptNumber);

//	팀 삭제 시 해당 팀 사원들 빈칸으로 수정
	public void updateTeamForDeleteTeam(Integer teamNumber);

//	휴가 일수 절댓값 설정 (초기화 시)
	public void setVacationCount(@Param("memberNumber") Integer memberNumber, @Param("vacationCount") String vacationCount);

//	휴가 일수 증감 (차감: 음수, 복구: 양수)
	public void updateVacationCountByDelta(@Param("memberNumber") Integer memberNumber, @Param("delta") double delta);

//	스케줄러용 전체 재직 사원 조회
	public List<MemberVO> selectAllMembersForReset();

//	휴가 잔여 일수 조회
	public double selectVacationCount(@Param("memberNumber") Integer memberNumber);

//	이월 잔여 일수 조회
	public double selectCarryoverCount(@Param("memberNumber") Integer memberNumber);

//	이월 일수 설정 (리셋 시 잔여분 이전)
	public void setCarryoverCount(@Param("memberNumber") Integer memberNumber, @Param("carryoverCount") String carryoverCount);

//	이월 일수 증감 (차감: 음수)
	public void updateCarryoverCountByDelta(@Param("memberNumber") Integer memberNumber, @Param("delta") double delta);

//	이월 만료일 지난 사원 일괄 이월 초기화
	public void expireCarryoverIfDue(@Param("today") String today);

//	이월 만료일 설정
	public void setCarryoverExpireDate(@Param("memberNumber") Integer memberNumber, @Param("carryoverExpireDate") String carryoverExpireDate);
}
