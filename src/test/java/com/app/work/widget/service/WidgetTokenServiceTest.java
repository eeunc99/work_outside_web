package com.app.work.widget.service;

import com.app.work.domain.vo.WidgetTokenVO;
import com.app.work.mapper.widget.WidgetTokenMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class WidgetTokenServiceTest {

    @Test
    void issueToken_generatesUniqueUrlSafeTokenAndPersists() {
        WidgetTokenMapper mapper = mock(WidgetTokenMapper.class);
        WidgetTokenService service = new WidgetTokenService(mapper);

        String token = service.issueToken(5, "READ_WRITE", "홍길동 아이폰");

        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(token.matches("[A-Za-z0-9_-]+"));

        ArgumentCaptor<WidgetTokenVO> captor = ArgumentCaptor.forClass(WidgetTokenVO.class);
        verify(mapper).insertToken(captor.capture());
        WidgetTokenVO saved = captor.getValue();
        assertEquals(5, saved.getMemberNumber());
        assertEquals("READ_WRITE", saved.getScope());
        assertEquals("홍길동 아이폰", saved.getLabel());
        assertEquals(token, saved.getToken());
    }

    @Test
    void validate_returnsNullForUnknownToken() {
        WidgetTokenMapper mapper = mock(WidgetTokenMapper.class);
        when(mapper.selectByToken("missing")).thenReturn(null);
        WidgetTokenService service = new WidgetTokenService(mapper);

        assertNull(service.validate("missing"));
    }

    @Test
    void validate_returnsNullForRevokedToken() {
        WidgetTokenMapper mapper = mock(WidgetTokenMapper.class);
        WidgetTokenVO revoked = new WidgetTokenVO();
        revoked.setRevokedAt(java.time.LocalDateTime.now());
        when(mapper.selectByToken("revoked-token")).thenReturn(revoked);
        WidgetTokenService service = new WidgetTokenService(mapper);

        assertNull(service.validate("revoked-token"));
    }

    @Test
    void validate_returnsTokenWhenActive() {
        WidgetTokenMapper mapper = mock(WidgetTokenMapper.class);
        WidgetTokenVO active = new WidgetTokenVO();
        active.setTokenId(7);
        active.setMemberNumber(5);
        when(mapper.selectByToken("active-token")).thenReturn(active);
        WidgetTokenService service = new WidgetTokenService(mapper);

        WidgetTokenVO result = service.validate("active-token");
        assertNotNull(result);
        assertEquals(5, result.getMemberNumber());
    }

    @Test
    void issueToken_rejectsInvalidScope() {
        WidgetTokenMapper mapper = mock(WidgetTokenMapper.class);
        WidgetTokenService service = new WidgetTokenService(mapper);

        assertThrows(IllegalArgumentException.class, () -> service.issueToken(5, "ADMIN", "label"));
        verify(mapper, never()).insertToken(any());
    }
}
