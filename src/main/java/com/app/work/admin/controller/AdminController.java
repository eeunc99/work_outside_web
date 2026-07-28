package com.app.work.admin.controller;

import com.app.work.admin.service.AdminService;
import com.app.work.domain.Criteria;
import com.app.work.domain.dto.*;
import com.app.work.domain.vo.MemberVO;
import com.app.work.domain.vo.ScheduleVO;
import com.app.work.domain.vo.StatisticsVO;
import com.app.work.schedule.service.ScheduleService;
import com.app.work.statistics.service.StatisticsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpSession;
import java.util.List;

@Controller
@RequestMapping("/admin/*")
@RequiredArgsConstructor
@Slf4j
public class AdminController {

	private final AdminService adminService;
	private final StatisticsService statisticsService;
	private final ScheduleService scheduleService;
	
	@GetMapping("department-mgmt")
	public String deptMgmt(Model model) {
		try {
			List<DeptDTO> departmentList = adminService.getDepartmentList();

			model.addAttribute("departmentDTOS", departmentList);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		return "admin/department_mgmt";
	}

	@PostMapping("department-regist")
	public RedirectView departmentRegist(String deptName) {
		try {
			adminService.saveDepartment(deptName);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		return new RedirectView("/admin/department-mgmt");
	}

	@PostMapping("dept-update")
	@ResponseBody
	public void deptUpdate(Integer deptNumber, String deptName) {
		try {
			adminService.setDepartmentName(deptNumber, deptName);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@PostMapping("dept-delete")
	@ResponseBody
	public void deptDelete(Integer deptNumber) {
		try {
			adminService.removeDepartment(deptNumber);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@PostMapping("team-regist")
	public RedirectView teamRegist(String teamName, Integer deptNumber) {
		try {
			adminService.saveTeam(teamName, deptNumber);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		return new RedirectView("/admin/department-mgmt");
	}

	@PostMapping("team-list")
	@ResponseBody
	public List<TeamDTO> teamList(Integer deptNumber) {
		return adminService.getTeamList(deptNumber);
	}

	@PostMapping("team-update")
	@ResponseBody
	public void teamUpdate(Integer teamNumber, String teamName) {
		try {
			adminService.setTeamName(teamNumber, teamName);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@PostMapping("team-delete")
	@ResponseBody
	public void teamDelete(Integer teamNumber) {
		try {
			adminService.removeTeam(teamNumber, null);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@GetMapping("user-mgmt")
	public String userMgmt(Integer deptNumber, Integer teamNumber, String memberName, String memberUse, Integer page, MemberDTO memberDTO, Model model) {
		try {
			if(memberUse == null) memberUse = "Y";
			if(page == null) page = 1;

			Criteria criteria = new Criteria(page, 20);

			List<MemberVO> memberList = adminService.getMemberListWithPaging(deptNumber, teamNumber, memberName, memberUse, criteria);
			int total = adminService.getMemberListCount(deptNumber, teamNumber, memberName, memberUse, criteria);

			List<DeptDTO> departmentList = adminService.getDepartmentList();

			PageDTO pageDTO = new PageDTO(criteria, total);

			model.addAttribute("memberDTO", memberDTO);
			model.addAttribute("memberVOS", memberList);
			model.addAttribute("departmentDTOS", departmentList);
			model.addAttribute("pageDTO", pageDTO);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		return "admin/user_mgmt";
	}

	@PostMapping("member-join")
	public RedirectView memberJoin(MemberDTO memberDTO) {
		try {
			adminService.saveMember(memberDTO);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		return new RedirectView("/admin/user-mgmt");
	}

	@PostMapping("change-member-info")
	@ResponseBody
	public void changeMemberInfo(@RequestBody MemberDTO memberDTO) {
		try {
			adminService.setMember(memberDTO);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@PostMapping("change-member-use")
	@ResponseBody
	public void changeMemberUse(Integer memberNumber, String memberUse) {
		try {
			adminService.changeMemberUse(memberNumber, memberUse);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@GetMapping("vacation-mgmt")
	public String vacationMgmt(Model model) {
		List<MemberVO> memberList = adminService.getMemberListWithPaging(null, null, null, "Y", new Criteria(1, 1000));
		model.addAttribute("memberVOS", memberList);

		return "admin/vacation_mgmt";
	}

	@PostMapping("vacation-list")
	@ResponseBody
	public List<ScheduleVO> vacationList(Integer memberNumber) {
		return scheduleService.findVacation(memberNumber);
	}

	@PostMapping("vacation-regist")
	@ResponseBody
	public void vacationRegist(Integer memberNumber, String vacationStDate, String vacationEndDate, String vacationCheck) {
		scheduleService.vacationRegister(memberNumber, vacationStDate, vacationEndDate, vacationCheck);
	}

	@PostMapping("vacation-update")
	@ResponseBody
	public void vacationUpdate(Integer vacationNumber, Integer memberNumber, String vacationStDate, String vacationEndDate, String vacationCheck) {
		scheduleService.editVacation(memberNumber, vacationNumber, vacationStDate, vacationEndDate, vacationCheck);
	}

	@PostMapping("vacation-delete")
	@ResponseBody
	public void vacationDelete(Integer vacationNumber) {
		scheduleService.vacationRemove(vacationNumber);
	}

	@PostMapping("set-member-permissions")
	@ResponseBody
	public void setMemberPermissions(Integer memberNumber, Boolean adminAccess, Boolean listAllTeams) {
		try {
			adminService.setMemberPermissions(memberNumber, Boolean.TRUE.equals(adminAccess), Boolean.TRUE.equals(listAllTeams));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@GetMapping("workplace-mgmt")
	public String workplaceMgmt(String workplaceUse, Integer page, Model model) {
		try {
			if(workplaceUse == null) workplaceUse = "Y";
			if(page == null) page = 1;

//		몇개 띄울지 결정하고 싶을 때 뒤에 숫자만 바꿔주면 됨
			Criteria criteria = new Criteria(page, 10);

			List<WorkplaceDTO> workplaceList = adminService.getWorkplaceList(workplaceUse, criteria);
			int total = adminService.getWorkplaceCount(workplaceUse);

			PageDTO pageDTO = new PageDTO(criteria, total);

			model.addAttribute("workplaceDTOS", workplaceList);
			model.addAttribute("pageDTO", pageDTO);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		return "admin/workplace_mgmt";
	}

	@PostMapping("workplace-name-check")
	@ResponseBody
	public int workplaceNameCheck(String workplaceName) {
		return adminService.checkWorkplaceName(workplaceName);
	}

	@PostMapping("workplace-regist")
	public RedirectView workplaceRegist(String workplaceName) {
		try {
			adminService.saveWorkplace(workplaceName);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		return new RedirectView("/admin/workplace-mgmt");
	}

	@PostMapping("workplace-update")
	@ResponseBody
	public void workplaceUpdate(String workplaceName, String originName) {
		try {
			adminService.setWorkplace(workplaceName, originName);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@PostMapping("change-workplace-use")
	@ResponseBody
	public void changeWorkplaceUse(String workplaceName, String workplaceUse) {
		try {
			adminService.setWorkplaceUse(workplaceName, workplaceUse);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@GetMapping("work-mgmt")
	public String workMgmt(String workStartTime, String workEndTime, Integer teamNumber, Integer workplaceNumber, Integer memberNumber, Integer page, Model model){

		if (page == null) page = 1;

		Criteria criteria = new Criteria(page, 10);

		List<StatisticsVO> statisticsList = statisticsService.getAdminStatisticsList(workStartTime, workEndTime, teamNumber, workplaceNumber, memberNumber, criteria);
		int total = statisticsService.getWorkListCount(workStartTime, workEndTime, teamNumber, workplaceNumber, memberNumber);

		List<WorkplaceDTO> workplaceList = adminService.getWorkplaceList("Y", null);
		List<TeamDTO> teamList = adminService.getTeamList(null);

		List<WorkplaceDTO> workList = scheduleService.findWorkName();
		model.addAttribute("workList", workList);

		PageDTO pageDTO = new PageDTO(criteria, total);

		model.addAttribute("statisticsVOS", statisticsList);
		model.addAttribute("workplaceDTOS", workplaceList);
		model.addAttribute("teamDTOS", teamList);
		model.addAttribute("pageDTO", pageDTO);

		return "admin/work_mgmt";
	}

}
