package com.app.work.login.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import com.app.work.login.service.LoginService;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/*")
@RequiredArgsConstructor
public class LoginController {

	private final LoginService loginService;

//	로그인 페이지 이동
	@GetMapping("")
	public String login(HttpServletRequest request) {
		//if(request.getRemoteAddr().startsWith("192.168.0.") || request.getRemoteAddr().startsWith("127.0.0.1")) {
		if(request.getRemoteAddr().startsWith("192.168.0.")){
			return "redirect:/list/list";
		}

		return "login";
	}

	@GetMapping("login")
	public String goLogin() {
		return "login";
	}

//	로그인 처리 
	@PostMapping("login")
	public RedirectView login(@CookieValue(name = "memberId", required = false) String memberId, String memberPassword, HttpServletRequest req) {
		try {
			int memberNumber = loginService.findMemberNumber(memberId, memberPassword);

			HttpSession session = req.getSession();
			session.setAttribute("memberNumber", memberNumber);

			int sessionNumber = loginService.findSessionMember(memberId, memberPassword);
			session.setAttribute("sessionNumber" , sessionNumber);

		} catch (IllegalArgumentException e) {
			e.printStackTrace();
			return new RedirectView("login");
		}
		return new RedirectView("/list/list");

	}

//	로그아웃 처리
	@GetMapping("logout")
	public RedirectView logout(HttpServletRequest req){
		req.getSession().invalidate();
		return new RedirectView("login");
	}

}