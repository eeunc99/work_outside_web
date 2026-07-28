package com.app.work.interceptor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class ListInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //if(request.getRemoteAddr().startsWith("192.168.0.") || request.getRemoteAddr().startsWith("127.0.0.1")) {
        if(request.getRemoteAddr().startsWith("192.168.0.")){
            return true;
        }

        HttpSession session = request.getSession(false);

        if(session == null) {
            response.sendRedirect("/login?interceptor=Y");
            return false;
        }
        if(Boolean.TRUE.equals(session.getAttribute("listAllTeams"))) {
            return true;
        }
        if("매니저".equals(session.getAttribute("memberRank"))) {
            response.sendRedirect("/login?interceptor=Y");
            session.invalidate();
            return false;
        }

        return true;
    }
}
