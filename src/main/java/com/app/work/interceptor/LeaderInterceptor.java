package com.app.work.interceptor;

import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class LeaderInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        HttpSession session = request.getSession(false);

        if(!session.getAttribute("memberRank").equals("팀장") && !session.getAttribute("memberRank").equals("수석 매니저") && !session.getAttribute("memberRank").equals("매니저")) {
            response.sendRedirect("/login?interceptor=Y");
            session.invalidate();
            return false;
        }

        return true;
    }
}
