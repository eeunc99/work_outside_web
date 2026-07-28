package com.app.work.login.service;

import java.util.List;
import java.util.Optional;

import com.app.work.domain.vo.MemberVO;
import org.springframework.stereotype.Service;

import com.app.work.domain.dto.member.MemberDto;
import com.app.work.mapper.member.MemberMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LoginService {

	private final MemberMapper memberMapper;

	//	로그인
	public Integer findMemberNumber(String memberId, String memberPassword) {
		if (memberId == null || memberPassword == null) {
			throw new IllegalArgumentException("회원 정보 누락");
		}

		return Optional.ofNullable(memberMapper.login(memberId, memberPassword)).orElseThrow(() -> {
			throw new IllegalArgumentException("존재하지 않는 회원");
		});
	}

//	세션으로 정보 불러오기
	public MemberVO findMemberName(Integer sessionNumber) {
		if (sessionNumber == null) {
			throw new IllegalArgumentException("정보 누락");
		}
		return memberMapper.selectMember(sessionNumber);
	}

//	등급 기본값 외에 추가로 부여된 권한 코드 목록 (이름 하드코딩 예외 대체)
	public List<String> findPermissionCodes(Integer memberNumber) {
		return memberMapper.selectMemberPermissionCodes(memberNumber);
	}

//	세션으로 번호 불러오기
	public Integer findSessionMember(String memberId, String memberPassword) {
		if (memberId == null || memberPassword == null) {
			throw new IllegalArgumentException("회원 정보 누락");
		}

		return Optional.ofNullable(memberMapper.sessionMember(memberId, memberPassword)).orElseThrow(() -> {
			throw new IllegalArgumentException("존재하지 않는 회원");
		});
	}

}
