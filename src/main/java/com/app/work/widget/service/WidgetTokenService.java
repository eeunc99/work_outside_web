package com.app.work.widget.service;

import com.app.work.domain.vo.WidgetTokenVO;
import com.app.work.mapper.widget.WidgetTokenMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WidgetTokenService {

    private static final SecureRandom RANDOM = new SecureRandom();

    private final WidgetTokenMapper widgetTokenMapper;

    public String issueToken(Integer memberNumber, String scope, String label) {
        if (!"READ".equals(scope) && !"READ_WRITE".equals(scope)) {
            throw new IllegalArgumentException("scope must be either 'READ' or 'READ_WRITE', got: " + scope);
        }
        if (label != null && label.length() > 50) {
            throw new IllegalArgumentException("label must be 50 characters or fewer");
        }

        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);

        WidgetTokenVO vo = new WidgetTokenVO();
        vo.setMemberNumber(memberNumber);
        vo.setToken(token);
        vo.setScope(scope);
        vo.setLabel(label);
        widgetTokenMapper.insertToken(vo);

        return token;
    }

    public List<WidgetTokenVO> findByMember(Integer memberNumber) {
        return widgetTokenMapper.selectByMember(memberNumber);
    }

    public List<WidgetTokenVO> findAll() {
        return widgetTokenMapper.selectAll();
    }

    public WidgetTokenVO validate(String token) {
        WidgetTokenVO vo = widgetTokenMapper.selectByToken(token);
        if (vo == null || vo.getRevokedAt() != null) {
            return null;
        }
        return vo;
    }

    public void touchLastUsed(Integer tokenId) {
        widgetTokenMapper.updateLastUsed(tokenId);
    }

    public void revoke(Integer tokenId) {
        widgetTokenMapper.updateRevoke(tokenId);
    }
}
