package com.app.work.widget;

import com.app.work.domain.vo.ScheduleVO;
import com.app.work.domain.vo.WidgetTokenVO;
import com.app.work.mapper.widget.WidgetTokenMapper;
import com.app.work.mapper.workplace.WorkplaceMapper;
import com.app.work.domain.dto.WorkplaceDTO;
import com.app.work.schedule.service.ScheduleService;
import com.app.work.widget.service.WidgetTokenService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Adversarial integration test for combined Task 5 (WidgetTokenInterceptor + registration)
 * and Task 6 (WidgetApiController).
 *
 * Real Spring Boot HTTP server (RANDOM_PORT) + real TestRestTemplate + real Postgres DB
 * (application-local.properties) + real WidgetTokenService.issueToken(...) production path.
 * No mocking of the interceptor, service, or DB.
 *
 * NOTE: because requests go over real HTTP, they execute on a separate Tomcat worker
 * thread from the test thread, so there is no @Transactional rollback safety net for
 * data written via HTTP calls. All rows created by this test (tokens, schedules,
 * workplaces) are cleaned up explicitly in tearDown() via JdbcTemplate, keyed off
 * UUID-based markers, matching the convention already used by
 * ScheduleServiceCompleteScheduleIntegrationTest.
 *
 * member_number=4 is a real row in tbl_member (see .superpowers/sdd/task-1-2-note.md).
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class WidgetApiIntegrationTest {

    private static final Integer MEMBER_NUMBER = 4;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    @LocalServerPort
    int port;

    @Autowired
    TestRestTemplate restTemplate;

    @Autowired
    WidgetTokenService widgetTokenService;

    @Autowired
    WidgetTokenMapper widgetTokenMapper;

    @Autowired
    ScheduleService scheduleService;

    @Autowired
    WorkplaceMapper workplaceMapper;

    @Autowired
    JdbcTemplate jdbcTemplate;

    private final List<Integer> tokenIdsToClean = new ArrayList<>();
    private final List<Integer> workNumbersToClean = new ArrayList<>();

    @AfterEach
    void tearDown() {
        for (Integer workNumber : workNumbersToClean) {
            jdbcTemplate.update("delete from tbl_work where work_number = ?", workNumber);
        }
        for (Integer tokenId : tokenIdsToClean) {
            jdbcTemplate.update("delete from tbl_widget_token where token_id = ?", tokenId);
        }
    }

    private String baseUrl() {
        return "http://localhost:" + port;
    }

    private String issueAndTrackToken(String scope) {
        String label = "IT-" + UUID.randomUUID().toString().substring(0, 8);
        String token = widgetTokenService.issueToken(MEMBER_NUMBER, scope, label);
        WidgetTokenVO vo = widgetTokenMapper.selectByToken(token);
        assertNotNull(vo, "issueToken must have persisted a row findable by selectByToken");
        tokenIdsToClean.add(vo.getTokenId());
        return token;
    }

    private Integer issueAndTrackTokenId(String scope) {
        String label = "IT-" + UUID.randomUUID().toString().substring(0, 8);
        String token = widgetTokenService.issueToken(MEMBER_NUMBER, scope, label);
        WidgetTokenVO vo = widgetTokenMapper.selectByToken(token);
        assertNotNull(vo);
        tokenIdsToClean.add(vo.getTokenId());
        return vo.getTokenId();
    }

    private Integer registerSchedule() {
        String marker = "WT-" + UUID.randomUUID().toString().substring(0, 8);
        Integer workplaceNumber = ensureWorkplace("WP-" + UUID.randomUUID().toString().substring(0, 8));

        ScheduleVO scheduleVO = new ScheduleVO();
        scheduleVO.setMemberNumber(MEMBER_NUMBER);
        scheduleVO.setWorkPlaceNumber(workplaceNumber);
        scheduleVO.setWorkTitle(marker);
        scheduleVO.setWorkDetail("widget api integration test row");
        scheduleVO.setWorkStatus("대기");
        scheduleVO.setWorkStartTime(LocalDateTime.now().format(FORMATTER));
        scheduleVO.setWorkSpentTime("0");

        scheduleService.workRegister(scheduleVO);

        Integer workNumber = jdbcTemplate.queryForObject(
                "select work_number from tbl_work where work_title = ? and member_number = ?",
                Integer.class, marker, MEMBER_NUMBER);
        workNumbersToClean.add(workNumber);
        return workNumber;
    }

    private Integer ensureWorkplace(String name) {
        if (workplaceMapper.selectWorkplace(name) == 0) {
            workplaceMapper.saveWorkplace(name);
        }
        WorkplaceDTO dto = workplaceMapper.selectWorkplaceByName(name);
        return dto.getWorkplaceNumber();
    }

    private HttpHeaders bearerHeaders(String token) {
        HttpHeaders headers = new HttpHeaders();
        if (token != null) {
            headers.set("Authorization", "Bearer " + token);
        }
        return headers;
    }

    // ---------- GET /api/widget/today ----------

    @Test
    void today_noAuthorizationHeader_returns401() {
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl() + "/api/widget/today", HttpMethod.GET,
                new HttpEntity<>(new HttpHeaders()), String.class);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode(),
                "missing Authorization header must be rejected with 401, not silently proceed with no session");
    }

    @Test
    void today_malformedAuthorizationHeader_returns401() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "not-a-bearer-token");

        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl() + "/api/widget/today", HttpMethod.GET,
                new HttpEntity<>(headers), String.class);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode(),
                "a header without the 'Bearer ' prefix must be rejected with 401");
    }

    @Test
    void today_unknownGarbageToken_returns401() {
        HttpHeaders headers = bearerHeaders("this-token-does-not-exist-" + UUID.randomUUID());

        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl() + "/api/widget/today", HttpMethod.GET,
                new HttpEntity<>(headers), String.class);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode(),
                "an unrecognized token must never be treated as valid");
    }

    @Test
    void today_validReadScopeToken_returns200AndContainsRegisteredSchedule() {
        String token = issueAndTrackToken("READ");
        Integer workNumber = registerSchedule();

        ResponseEntity<ScheduleVO[]> response = restTemplate.exchange(
                baseUrl() + "/api/widget/today", HttpMethod.GET,
                new HttpEntity<>(bearerHeaders(token)), ScheduleVO[].class);

        assertEquals(HttpStatus.OK, response.getStatusCode(), "a valid READ token must be granted read access");
        assertNotNull(response.getBody());
        boolean found = false;
        for (ScheduleVO vo : response.getBody()) {
            if (workNumber.equals(vo.getWorkNumber())) {
                found = true;
                assertEquals(MEMBER_NUMBER, vo.getMemberNumber(),
                        "returned schedule must belong to the token's own member, not another member");
            }
        }
        assertTrue(found, "the schedule just registered for this member must appear in /api/widget/today's response");
    }

    @Test
    void today_validReadWriteScopeToken_returns200_readIsNotRestrictedToReadOnlyTokens() {
        String token = issueAndTrackToken("READ_WRITE");

        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl() + "/api/widget/today", HttpMethod.GET,
                new HttpEntity<>(bearerHeaders(token)), String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode(),
                "a READ_WRITE token must also be able to read /api/widget/today (read is not READ-only)");
    }

    @Test
    void today_revokedToken_isRejectedWith401OnSubsequentRequest() {
        Integer tokenIdHolder = null;
        String label = "IT-" + UUID.randomUUID().toString().substring(0, 8);
        String token = widgetTokenService.issueToken(MEMBER_NUMBER, "READ", label);
        WidgetTokenVO vo = widgetTokenMapper.selectByToken(token);
        assertNotNull(vo);
        tokenIdsToClean.add(vo.getTokenId());

        // sanity check: token works before revocation
        ResponseEntity<String> beforeRevoke = restTemplate.exchange(
                baseUrl() + "/api/widget/today", HttpMethod.GET,
                new HttpEntity<>(bearerHeaders(token)), String.class);
        assertEquals(HttpStatus.OK, beforeRevoke.getStatusCode(), "token must work before revocation");

        widgetTokenService.revoke(vo.getTokenId());

        ResponseEntity<String> afterRevoke = restTemplate.exchange(
                baseUrl() + "/api/widget/today", HttpMethod.GET,
                new HttpEntity<>(bearerHeaders(token)), String.class);
        assertEquals(HttpStatus.UNAUTHORIZED, afterRevoke.getStatusCode(),
                "a revoked token must be rejected with 401 on every subsequent request, even though the token string itself is unchanged");
    }

    // ---------- POST /api/widget/status ----------

    @Test
    void status_readScopeToken_returns403() {
        String token = issueAndTrackToken("READ");
        Integer workNumber = registerSchedule();

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("workNumber", String.valueOf(workNumber));
        form.add("workStatus", "완료");
        form.add("workRealStartTime", LocalDateTime.now().minusMinutes(5).format(FORMATTER));
        form.add("workSpentTime", "NaN");

        HttpHeaders headers = bearerHeaders(token);
        headers.setContentType(org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED);

        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl() + "/api/widget/status", HttpMethod.POST,
                new HttpEntity<>(form, headers), String.class);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode(),
                "a READ-scope token must never be allowed to perform a write (POST) request");

        Map<String, Object> row = jdbcTemplate.queryForMap(
                "select work_status from tbl_work where work_number = ?", workNumber);
        assertEquals("대기", row.get("work_status"),
                "a rejected write must not have mutated the underlying schedule row");
    }

    @Test
    void status_readWriteScopeToken_returns200AndActuallyUpdatesTheScheduleRow() {
        String token = issueAndTrackToken("READ_WRITE");
        Integer workNumber = registerSchedule();
        String start = LocalDateTime.now().minusMinutes(5).format(FORMATTER);

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("workNumber", String.valueOf(workNumber));
        form.add("workStatus", "완료");
        form.add("workRealStartTime", start);
        form.add("workSpentTime", "NaN");

        HttpHeaders headers = bearerHeaders(token);
        headers.setContentType(org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED);

        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl() + "/api/widget/status", HttpMethod.POST,
                new HttpEntity<>(form, headers), String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode(),
                "a READ_WRITE-scope token must be allowed to perform the write");

        Map<String, Object> row = jdbcTemplate.queryForMap(
                "select work_status, (work_end_time is not null) as has_end_time from tbl_work where work_number = ?",
                workNumber);
        assertEquals("종료", row.get("work_status"),
                "the underlying schedule row's status must actually change in the DB, not just return HTTP 200");
        assertTrue((Boolean) row.get("has_end_time"),
                "completing the schedule must set work_end_time");
    }

    @Test
    void status_noAuthorizationHeader_returns401_andDoesNotMutateAnything() {
        Integer workNumber = registerSchedule();

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("workNumber", String.valueOf(workNumber));
        form.add("workStatus", "완료");
        form.add("workRealStartTime", LocalDateTime.now().format(FORMATTER));
        form.add("workSpentTime", "NaN");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED);

        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl() + "/api/widget/status", HttpMethod.POST,
                new HttpEntity<>(form, headers), String.class);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode(),
                "a write request with no Authorization header at all must be rejected with 401 before reaching the controller");

        Map<String, Object> row = jdbcTemplate.queryForMap(
                "select work_status from tbl_work where work_number = ?", workNumber);
        assertEquals("대기", row.get("work_status"), "an unauthenticated write must never mutate data");
    }
}
