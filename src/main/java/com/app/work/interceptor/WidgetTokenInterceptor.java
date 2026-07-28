package com.app.work.interceptor;

import com.app.work.domain.vo.WidgetTokenVO;
import com.app.work.widget.service.WidgetTokenService;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Set;

public class WidgetTokenInterceptor implements HandlerInterceptor {

    private static final Set<String> WRITE_METHODS = Set.of("POST", "PUT", "PATCH", "DELETE");
    private static final String BEARER_PREFIX = "Bearer ";

    private final WidgetTokenService widgetTokenService;

    public WidgetTokenInterceptor(WidgetTokenService widgetTokenService) {
        this.widgetTokenService = widgetTokenService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "missing bearer token");
            return false;
        }

        String token = authHeader.substring(BEARER_PREFIX.length());
        WidgetTokenVO widgetToken = widgetTokenService.validate(token);
        if (widgetToken == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "invalid or revoked token");
            return false;
        }

        if (WRITE_METHODS.contains(request.getMethod()) && !"READ_WRITE".equals(widgetToken.getScope())) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "token is read-only");
            return false;
        }

        HttpSession session = request.getSession(true);
        session.setAttribute("sessionNumber", widgetToken.getMemberNumber());
        widgetTokenService.touchLastUsed(widgetToken.getTokenId());

        return true;
    }
}
