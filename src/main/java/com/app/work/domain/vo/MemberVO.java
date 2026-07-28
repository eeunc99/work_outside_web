package com.app.work.domain.vo;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class MemberVO {
    private Integer memberNumber;
    private String memberId;
    private String memberPassword;
    private String memberName;
    private String memberRank;
    private String memberUse;
    private Integer deptNumber;
    private Integer teamNumber;
    private String deptName;
    private String teamName;
    private String vacationCount;
    private String carryoverCount;       // 이월 잔여 일수
    private String carryoverExpireDate;  // 이월 만료일 (YYYY-MM-DD)
    private String employmentDate;
    private Integer resetDate;
    private Integer sessionNumber;
    private List<String> permissionCodes = new ArrayList<>();  // 등급 기본값 외에 추가로 부여된 권한 (관리자 화면 표시용)
}
