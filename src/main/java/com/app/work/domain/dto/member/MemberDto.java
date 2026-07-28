package com.app.work.domain.dto.member;

import org.springframework.stereotype.Component;

import lombok.Data;
import lombok.NoArgsConstructor;

@Component
@Data
@NoArgsConstructor
public class MemberDto {
	private Integer memberNumber;
	private String memberId;
	private String memberPassword;
	private String memberName;
	private String memberRank;
	private String memberUse;
	private Integer deptNumber;
	private Integer teamNumber;
	private String vacationCount;
	private String carryoverCount;
	private String carryoverExpireDate;
	private String employmentDate;
	private Integer resetDate;
}
