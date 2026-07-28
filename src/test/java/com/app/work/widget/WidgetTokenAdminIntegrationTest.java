package com.app.work.widget;

import com.app.work.domain.vo.WidgetTokenVO;
import com.app.work.mapper.widget.WidgetTokenMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Adversarial integration test for combined Task 7 (AdminController widget-token
 * issue/list/revoke endpoints) and Task 8 (widget_token_mgmt.html template + sub-nav
 * links in the 5 existing admin pages).
 *
 * Task 7's GET /admin/widget-token-mgmt cannot render at all without Task 8's
 * Thymeleaf template, so both are exercised together here through the real MVC
 * stack: full @SpringBootTest + @AutoConfigureMockMvc (NOT a @WebMvcTest slice),
 * so the real WebMvcConfig, LoginCheckInterceptor, and AdminInterceptor all run
 * for real against a MockHttpSession built to satisfy their preconditions
 * (sessionNumber present, memberRank == "관리자", memberName not one of the
 * hardcoded super-admin names AdminInterceptor special-cases). This is ordinary
 * "an admin is already logged in" test setup — the login mechanism itself is
 * out of scope and not touched. Every widget-token-specific behavior (issuance,
 * scope validation, revocation, template rendering) runs for real through the
 * real AdminController / AdminService / WidgetTokenService / DB (no mocking).
 *
 * member_number=4 is a real row in tbl_member (see .superpowers/sdd/task-1-2-note.md).
 */
@SpringBootTest
@AutoConfigureMockMvc
class WidgetTokenAdminIntegrationTest {

    private static final Integer MEMBER_NUMBER = 4;

    @Autowired
    MockMvc mockMvc;

    @Autowired
    WidgetTokenMapper widgetTokenMapper;

    private final List<Integer> tokenIdsToClean = new java.util.ArrayList<>();

    @AfterEach
    void tearDown() {
        // best-effort direct cleanup via mapper; revoke is idempotent-ish so this
        // just marks rows revoked/it's fine if the underlying schema lacks a hard
        // delete mapper method — we only rely on selectByMember/selectAll for
        // assertions, and stray READ tokens for member 4 do not affect other tests
        // since every test looks up its own token by unique label.
        for (Integer tokenId : tokenIdsToClean) {
            try {
                widgetTokenMapper.updateRevoke(tokenId);
            } catch (Exception ignored) {
            }
        }
    }

    private MockHttpSession adminSession() {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("sessionNumber", MEMBER_NUMBER);
        session.setAttribute("memberRank", "관리자");
        session.setAttribute("memberName", "테스트관리자");
        return session;
    }

    private WidgetTokenVO findByLabel(String label) {
        List<WidgetTokenVO> tokens = widgetTokenMapper.selectByMember(MEMBER_NUMBER);
        for (WidgetTokenVO vo : tokens) {
            if (label.equals(vo.getLabel())) {
                return vo;
            }
        }
        return null;
    }

    // ---------- GET /admin/widget-token-mgmt ----------

    @Test
    void widgetTokenMgmtPage_asAuthenticatedAdmin_returns200WithMemberDropdownAndSubNavLink() throws Exception {
        mockMvc.perform(get("/admin/widget-token-mgmt").session(adminSession()))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("위젯 토큰")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("memberNumber")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("/admin/widget-token-mgmt")));
    }

    // ---------- POST /admin/widget-token-issue ----------

    @Test
    void issueToken_withRealMemberAndReadWriteScope_redirectsAndPersistsCorrectRow() throws Exception {
        String label = "IT-" + UUID.randomUUID().toString().substring(0, 8);

        MvcResult result = mockMvc.perform(post("/admin/widget-token-issue")
                        .session(adminSession())
                        .param("memberNumber", String.valueOf(MEMBER_NUMBER))
                        .param("scope", "READ_WRITE")
                        .param("label", label))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/widget-token-mgmt"))
                .andReturn();

        WidgetTokenVO persisted = findByLabel(label);
        assertNotNull(persisted, "issueWidgetToken must persist a row findable via WidgetTokenMapper.selectByMember");
        tokenIdsToClean.add(persisted.getTokenId());
        assertEquals(MEMBER_NUMBER, persisted.getMemberNumber());
        assertEquals("READ_WRITE", persisted.getScope());
        assertEquals(label, persisted.getLabel());
        assertNull(persisted.getRevokedAt(), "freshly issued token must not be revoked");

        String issuedToken = (String) result.getFlashMap().get("issuedToken");
        assertNotNull(issuedToken, "the issued token value must be carried as a flash attribute named 'issuedToken'");
        assertEquals(persisted.getToken(), issuedToken,
                "the flash-attribute token value must match the token actually persisted to the DB");
    }

    @Test
    void issueToken_thenFollowRedirect_showsIssuedTokenExactlyOnceOnThePage() throws Exception {
        String label = "IT-" + UUID.randomUUID().toString().substring(0, 8);
        MockHttpSession session = adminSession();

        MvcResult issueResult = mockMvc.perform(post("/admin/widget-token-issue")
                        .session(session)
                        .param("memberNumber", String.valueOf(MEMBER_NUMBER))
                        .param("scope", "READ")
                        .param("label", label))
                .andExpect(status().is3xxRedirection())
                .andReturn();

        WidgetTokenVO persisted = findByLabel(label);
        assertNotNull(persisted);
        tokenIdsToClean.add(persisted.getTokenId());

        // Simulate following the redirect, carrying the flash map into the next request,
        // matching Spring's RedirectAttributes flash-scope semantics.
        MvcResult mgmtPageResult = mockMvc.perform(get("/admin/widget-token-mgmt")
                        .session(session)
                        .sessionAttrs(issueResult.getFlashMap()))
                .andExpect(status().isOk())
                .andReturn();

        String body = mgmtPageResult.getResponse().getContentAsString();
        String issuedToken = (String) issueResult.getFlashMap().get("issuedToken");
        assertNotNull(issuedToken);

        int firstIndex = body.indexOf(issuedToken);
        int lastIndex = body.lastIndexOf(issuedToken);
        assertTrue(firstIndex >= 0, "the issued token value must be displayed on the re-rendered mgmt page (one-time reveal)");
        assertEquals(firstIndex, lastIndex, "the issued token value must appear exactly once on the page");
    }

    // ---------- POST /admin/widget-token-revoke ----------

    @Test
    void revokeToken_setsRevokedAtInDbAndHidesRevokeButtonOnRerender() throws Exception {
        String label = "IT-" + UUID.randomUUID().toString().substring(0, 8);
        MockHttpSession session = adminSession();

        mockMvc.perform(post("/admin/widget-token-issue")
                        .session(session)
                        .param("memberNumber", String.valueOf(MEMBER_NUMBER))
                        .param("scope", "READ")
                        .param("label", label))
                .andExpect(status().is3xxRedirection());

        WidgetTokenVO persisted = findByLabel(label);
        assertNotNull(persisted);
        Integer tokenId = persisted.getTokenId();
        tokenIdsToClean.add(tokenId);
        assertNull(persisted.getRevokedAt(), "sanity check: token must start un-revoked");

        mockMvc.perform(post("/admin/widget-token-revoke")
                        .session(session)
                        .param("tokenId", String.valueOf(tokenId)))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/widget-token-mgmt"));

        WidgetTokenVO afterRevoke = widgetTokenMapper.selectByMember(MEMBER_NUMBER).stream()
                .filter(t -> tokenId.equals(t.getTokenId()))
                .findFirst().orElse(null);
        assertNotNull(afterRevoke);
        assertNotNull(afterRevoke.getRevokedAt(), "revokeWidgetToken must set revokedAt in the DB");

        MvcResult mgmtPageResult = mockMvc.perform(get("/admin/widget-token-mgmt").session(session))
                .andExpect(status().isOk())
                .andReturn();
        String body = mgmtPageResult.getResponse().getContentAsString();

        // The row for this token must show revoked status and must not contain a
        // still-active revoke form/button referencing this specific tokenId.
        assertTrue(body.contains("폐기됨"),
                "a revoked token's row must show '폐기됨' status text on re-render");
        assertFalse(body.contains("name=\"tokenId\" value=\"" + tokenId + "\""),
                "a revoked token must no longer expose an active revoke button (hidden tokenId input) for its own row");
    }

    // ---------- invalid scope ----------

    @Test
    void issueToken_withInvalidScope_doesNotSilentlyReturn200() throws Exception {
        String label = "IT-" + UUID.randomUUID().toString().substring(0, 8);

        MvcResult result = mockMvc.perform(post("/admin/widget-token-issue")
                        .session(adminSession())
                        .param("memberNumber", String.valueOf(MEMBER_NUMBER))
                        .param("scope", "SUPER_ADMIN_MODE")
                        .param("label", label))
                .andReturn();

        int status = result.getResponse().getStatus();
        assertNotEquals(200, status,
                "WidgetTokenService.issueToken throws IllegalArgumentException for an invalid scope; the "
                        + "controller must not swallow this into a silent 200 OK. If this assertion fails, "
                        + "AdminController.widgetTokenIssue lacks its own error handling for invalid scope "
                        + "(a real gap: it should return 400/500, not proceed as if the token was issued).");

        WidgetTokenVO shouldNotExist = findByLabel(label);
        assertNull(shouldNotExist, "an invalid-scope issue attempt must not persist a token row");
        if (shouldNotExist != null) {
            tokenIdsToClean.add(shouldNotExist.getTokenId());
        }
    }

    // ---------- nonexistent memberNumber (FK violation) ----------

    @Test
    void issueToken_withNonExistentMemberNumber_doesNotCrashAndDoesNotPersist() throws Exception {
        // tbl_widget_token.member_number has a NOT NULL FK to tbl_member(member_number)
        // (docs/sql/widget_token.sql). A memberNumber that doesn't exist in tbl_member
        // must be rejected gracefully by the controller (like the invalid-scope case),
        // not allowed to blow up as an unhandled DataIntegrityViolationException that
        // MockMvc rethrows out of .perform() as a raw stack trace.
        String label = "IT-" + UUID.randomUUID().toString().substring(0, 8);
        Integer nonExistentMemberNumber = 999999;

        MvcResult result;
        try {
            result = mockMvc.perform(post("/admin/widget-token-issue")
                            .session(adminSession())
                            .param("memberNumber", String.valueOf(nonExistentMemberNumber))
                            .param("scope", "READ")
                            .param("label", label))
                    .andReturn();
        } catch (Exception e) {
            fail("POST /admin/widget-token-issue with a nonexistent memberNumber must not propagate an "
                    + "unhandled exception out of the controller (e.g. a raw DataIntegrityViolationException "
                    + "from the FK constraint on tbl_widget_token.member_number); it must be caught and turned "
                    + "into a real HTTP error response, the same way the invalid-scope case is handled. Got: " + e);
            return;
        }

        int status = result.getResponse().getStatus();
        assertNotEquals(200, status,
                "issuing a token for a memberNumber that does not exist in tbl_member must not silently "
                        + "succeed with a 200 OK");

        List<WidgetTokenVO> allTokensForBogusLabel = widgetTokenMapper.selectAll().stream()
                .filter(t -> label.equals(t.getLabel()))
                .collect(java.util.stream.Collectors.toList());
        assertTrue(allTokensForBogusLabel.isEmpty(),
                "no token row should be persisted for a nonexistent memberNumber");
        for (WidgetTokenVO vo : allTokensForBogusLabel) {
            tokenIdsToClean.add(vo.getTokenId());
        }
    }

    // ---------- Task 8 sub-nav link on an existing admin page ----------

    @Test
    void departmentMgmtPage_containsSubNavLinkToWidgetTokenMgmt() throws Exception {
        mockMvc.perform(get("/admin/department-mgmt").session(adminSession()))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("/admin/widget-token-mgmt")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("위젯 토큰")));
    }
}
