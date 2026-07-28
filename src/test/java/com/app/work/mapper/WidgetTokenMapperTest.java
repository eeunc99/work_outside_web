package com.app.work.mapper;

import com.app.work.domain.vo.WidgetTokenVO;
import com.app.work.mapper.widget.WidgetTokenMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test for WidgetTokenMapper against the real (local) Postgres DB.
 * member_number = 4 is used because it is a real row in tbl_member on the
 * local DB (YOUR_DB_HOST/work_outside_local) — see .superpowers/sdd/task-1-2-note.md.
 * Table tbl_widget_token must already exist (docs/sql/widget_token.sql applied manually).
 */
@SpringBootTest
@Transactional
public class WidgetTokenMapperTest {

    private static final Integer TEST_MEMBER_NUMBER = 4;

    @Autowired
    WidgetTokenMapper widgetTokenMapper;

    private String uniqueToken(String prefix) {
        return prefix + "-" + UUID.randomUUID();
    }

    @Test
    public void insertAndSelectByToken_READ_scope_roundTripsAllFields() {
        String token = uniqueToken("wtok-read");

        WidgetTokenVO vo = new WidgetTokenVO();
        vo.setMemberNumber(TEST_MEMBER_NUMBER);
        vo.setToken(token);
        vo.setScope("READ");
        vo.setLabel("테스트 기기 A");

        widgetTokenMapper.insertToken(vo);
        assertNotNull(vo.getTokenId(), "insertToken must populate the generated tokenId");

        WidgetTokenVO found = widgetTokenMapper.selectByToken(token);
        assertNotNull(found, "selectByToken must find the row just inserted");
        assertEquals(vo.getTokenId(), found.getTokenId());
        assertEquals(TEST_MEMBER_NUMBER, found.getMemberNumber());
        assertEquals(token, found.getToken());
        assertEquals("READ", found.getScope());
        assertEquals("테스트 기기 A", found.getLabel());
        assertNotNull(found.getCreatedAt(), "created_at should default to now() on insert");
        assertNull(found.getLastUsedAt());
        assertNull(found.getRevokedAt());
    }

    @Test
    public void insertAndSelectByToken_READ_WRITE_scope_persistsCorrectScope() {
        String token = uniqueToken("wtok-readwrite");

        WidgetTokenVO vo = new WidgetTokenVO();
        vo.setMemberNumber(TEST_MEMBER_NUMBER);
        vo.setToken(token);
        vo.setScope("READ_WRITE");
        vo.setLabel("테스트 기기 B");

        widgetTokenMapper.insertToken(vo);

        WidgetTokenVO found = widgetTokenMapper.selectByToken(token);
        assertNotNull(found);
        assertEquals("READ_WRITE", found.getScope());
    }

    @Test
    public void selectByToken_unknownToken_returnsNull() {
        WidgetTokenVO found = widgetTokenMapper.selectByToken(uniqueToken("wtok-does-not-exist"));
        assertNull(found);
    }

    @Test
    public void selectByMember_returnsOnlyThatMembersTokens() {
        String tokenA = uniqueToken("wtok-member-a1");
        String tokenB = uniqueToken("wtok-member-a2");

        WidgetTokenVO voA = new WidgetTokenVO();
        voA.setMemberNumber(TEST_MEMBER_NUMBER);
        voA.setToken(tokenA);
        voA.setScope("READ");
        widgetTokenMapper.insertToken(voA);

        WidgetTokenVO voB = new WidgetTokenVO();
        voB.setMemberNumber(TEST_MEMBER_NUMBER);
        voB.setToken(tokenB);
        voB.setScope("READ_WRITE");
        widgetTokenMapper.insertToken(voB);

        List<WidgetTokenVO> list = widgetTokenMapper.selectByMember(TEST_MEMBER_NUMBER);

        assertTrue(list.stream().anyMatch(t -> t.getTokenId().equals(voA.getTokenId())
                && "READ".equals(t.getScope()) && tokenA.equals(t.getToken())));
        assertTrue(list.stream().anyMatch(t -> t.getTokenId().equals(voB.getTokenId())
                && "READ_WRITE".equals(t.getScope()) && tokenB.equals(t.getToken())));
        assertTrue(list.stream().allMatch(t -> TEST_MEMBER_NUMBER.equals(t.getMemberNumber())));
    }

    @Test
    public void selectAll_includesMemberNameFromJoin() {
        String token = uniqueToken("wtok-selectall");

        WidgetTokenVO vo = new WidgetTokenVO();
        vo.setMemberNumber(TEST_MEMBER_NUMBER);
        vo.setToken(token);
        vo.setScope("READ");
        vo.setLabel("전체조회 테스트");
        widgetTokenMapper.insertToken(vo);

        List<WidgetTokenVO> all = widgetTokenMapper.selectAll();

        WidgetTokenVO match = all.stream()
                .filter(t -> t.getTokenId().equals(vo.getTokenId()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("inserted token not present in selectAll() results"));

        assertEquals(TEST_MEMBER_NUMBER, match.getMemberNumber());
        assertNotNull(match.getMemberName(), "selectAll must join tbl_member and populate memberName");
        assertFalse(match.getMemberName().trim().isEmpty());
    }

    @Test
    public void updateRevoke_setsRevokedAtAndIsReflectedInSelectByMember() {
        String token = uniqueToken("wtok-revoke");

        WidgetTokenVO vo = new WidgetTokenVO();
        vo.setMemberNumber(TEST_MEMBER_NUMBER);
        vo.setToken(token);
        vo.setScope("READ_WRITE");
        widgetTokenMapper.insertToken(vo);

        WidgetTokenVO beforeRevoke = widgetTokenMapper.selectByToken(token);
        assertNull(beforeRevoke.getRevokedAt(), "revoked_at must be null before revocation");

        widgetTokenMapper.updateRevoke(vo.getTokenId());

        WidgetTokenVO afterRevoke = widgetTokenMapper.selectByToken(token);
        assertNotNull(afterRevoke.getRevokedAt(), "revoked_at must be set after updateRevoke");

        List<WidgetTokenVO> list = widgetTokenMapper.selectByMember(TEST_MEMBER_NUMBER);
        WidgetTokenVO revoked = list.stream()
                .filter(t -> t.getTokenId().equals(vo.getTokenId()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("revoked token missing from selectByMember"));
        assertNotNull(revoked.getRevokedAt());
    }

    @Test
    public void updateLastUsed_setsLastUsedAtAndChangesOnSubsequentCall() throws InterruptedException {
        String token = uniqueToken("wtok-lastused");

        WidgetTokenVO vo = new WidgetTokenVO();
        vo.setMemberNumber(TEST_MEMBER_NUMBER);
        vo.setToken(token);
        vo.setScope("READ");
        widgetTokenMapper.insertToken(vo);

        WidgetTokenVO beforeUse = widgetTokenMapper.selectByToken(token);
        assertNull(beforeUse.getLastUsedAt(), "last_used_at must be null before first use");

        widgetTokenMapper.updateLastUsed(vo.getTokenId());
        WidgetTokenVO firstUse = widgetTokenMapper.selectByToken(token);
        assertNotNull(firstUse.getLastUsedAt(), "last_used_at must be set after updateLastUsed");

        // ensure the clock advances so a second update produces a strictly later timestamp
        Thread.sleep(10);

        widgetTokenMapper.updateLastUsed(vo.getTokenId());
        WidgetTokenVO secondUse = widgetTokenMapper.selectByToken(token);
        assertNotNull(secondUse.getLastUsedAt());
        assertTrue(secondUse.getLastUsedAt().isAfter(firstUse.getLastUsedAt()),
                "second updateLastUsed call must move last_used_at strictly forward");
    }

    @Test
    public void insertToken_doesNotSetRevokedAt() {
        String token = uniqueToken("wtok-norevoke");

        WidgetTokenVO vo = new WidgetTokenVO();
        vo.setMemberNumber(TEST_MEMBER_NUMBER);
        vo.setToken(token);
        vo.setScope("READ");
        widgetTokenMapper.insertToken(vo);

        WidgetTokenVO found = widgetTokenMapper.selectByToken(token);
        assertNull(found.getRevokedAt());
        assertNull(found.getLastUsedAt());
    }

    @Test
    public void insertToken_nullLabel_roundTripsAsNull() {
        String token = uniqueToken("wtok-nulllabel");

        WidgetTokenVO vo = new WidgetTokenVO();
        vo.setMemberNumber(TEST_MEMBER_NUMBER);
        vo.setToken(token);
        vo.setScope("READ");
        // label intentionally left unset (null) - it is an optional column

        widgetTokenMapper.insertToken(vo);

        WidgetTokenVO found = widgetTokenMapper.selectByToken(token);
        assertNotNull(found);
        assertNull(found.getLabel(), "label must round-trip as null, not an empty string or default value");
    }

    @Test
    public void insertToken_duplicateToken_violatesUniqueConstraint() {
        String token = uniqueToken("wtok-dup");

        WidgetTokenVO first = new WidgetTokenVO();
        first.setMemberNumber(TEST_MEMBER_NUMBER);
        first.setToken(token);
        first.setScope("READ");
        widgetTokenMapper.insertToken(first);

        WidgetTokenVO second = new WidgetTokenVO();
        second.setMemberNumber(TEST_MEMBER_NUMBER);
        second.setToken(token);
        second.setScope("READ_WRITE");

        assertThrows(DataIntegrityViolationException.class, () -> widgetTokenMapper.insertToken(second),
                "inserting a second row with the same token value must violate the unique constraint on tbl_widget_token.token");
    }
}
