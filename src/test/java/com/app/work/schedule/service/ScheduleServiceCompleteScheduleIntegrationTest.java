package com.app.work.schedule.service;

import com.app.work.domain.dto.WorkplaceDTO;
import com.app.work.domain.vo.ScheduleVO;
import com.app.work.mapper.workplace.WorkplaceMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Integration test for ScheduleService.completeSchedule() (Task 4).
 *
 * Real Spring context + real Postgres DB (application-local.properties), no mocks.
 * Registers real rows via ScheduleService.workRegister(...) so the test does not
 * depend on any pre-existing schedule data, then drives completeSchedule(...) and
 * reads the persisted row back straight from the DB to prove behavior is preserved
 * across the extraction from ScheduleRestController.scheduleStatus.
 *
 * member_number=4 is a real row in tbl_member (see .superpowers/sdd/task-1-2-note.md).
 */
@SpringBootTest
@Transactional
@Slf4j
class ScheduleServiceCompleteScheduleIntegrationTest {

    private static final Integer MEMBER_NUMBER = 4;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    @Autowired
    ScheduleService scheduleService;

    @Autowired
    WorkplaceMapper workplaceMapper;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Test
    void completeSchedule_start_setsInProgressWithNoEndTimeInRealDb() {
        Integer workNumber = registerSchedule();
        String workRealStartTime = LocalDateTime.now().format(FORMATTER);

        scheduleService.completeSchedule(workNumber, MEMBER_NUMBER, "업무시작", workRealStartTime, "NaN");

        Map<String, Object> row = readRow(workNumber);
        assertEquals("진행중", row.get("work_status"));
        assertNull(row.get("work_end_time"), "업무시작 상태에서는 work_end_time이 설정되면 안 된다");
        assertEquals(workRealStartTime, row.get("real_start"));
        // Original assertion (assertNull) tested a DB state that is unreachable: tbl_work.work_spent_time
        // is NOT NULL, so registerSchedule() must supply an initial value ("0") for any row to be
        // inserted at all. Corrected to assert the reachable form of the same invariant: 업무시작
        // does not recompute/overwrite spent time, so it stays exactly what was inserted. Confirmed
        // via readRow()/JdbcTemplate.queryForMap that this column comes back as a String (see the
        // (String) cast on work_spent_time elsewhere in this file, e.g. completeSchedule_complete_computesSpentTimeWhenNaN).
        assertEquals("0", row.get("work_spent_time"), "업무시작 상태에서는 소요시간이 재계산되면 안 된다");
    }

    @Test
    void completeSchedule_complete_computesSpentTimeWhenNaN() {
        Integer workNumber = registerSchedule();
        String start = LocalDateTime.now().minusMinutes(5).format(FORMATTER);

        scheduleService.completeSchedule(workNumber, MEMBER_NUMBER, "완료", start, "NaN");

        Map<String, Object> row = readRow(workNumber);
        assertEquals("종료", row.get("work_status"));
        assertTrue((Boolean) row.get("has_end_time"), "완료 처리 시 work_end_time이 설정되어야 한다");
        assertEquals(start, row.get("real_start"));

        long minutes = Long.parseLong((String) row.get("work_spent_time"));
        assertTrue(minutes >= 4 && minutes <= 6,
                "5분 전 시작 기준 계산된 소요시간이 범위를 벗어남: " + minutes);
    }

    @Test
    void completeSchedule_complete_usesExplicitSpentTimeInsteadOfComputing() {
        Integer workNumber = registerSchedule();
        // 시작 시각을 충분히 과거로 두어, 만약 구현이 잘못되어 계산 경로를 타면
        // 계산값(수십 분)과 명시값("42")이 뚜렷이 구분되게 한다.
        String start = LocalDateTime.now().minusMinutes(999).format(FORMATTER);

        scheduleService.completeSchedule(workNumber, MEMBER_NUMBER, "완료", start, "42");

        Map<String, Object> row = readRow(workNumber);
        assertEquals("종료", row.get("work_status"));
        assertEquals("42", row.get("work_spent_time"),
                "workSpentTime이 명시적으로 주어지면 재계산하지 않고 그대로 저장해야 한다");
    }

    /** ScheduleService.workRegister(...)로 실제 행을 등록하고 새로 생성된 work_number를 반환한다. */
    private Integer registerSchedule() {
        // tbl_work.work_title is varchar(30); "IT-" + full UUID (36 chars) overflows it.
        // tbl_workplace.workplace_name is varchar(15); "IT-WORKPLACE-" + full UUID overflows it too.
        // Both overflow the same way and fail every test with a PSQLException unrelated to
        // completeSchedule's own logic, so both are shortened to fit (deviation reported in
        // task-4-report.md).
        String marker = "IT-" + UUID.randomUUID().toString().substring(0, 8);
        Integer workplaceNumber = ensureWorkplace("W-" + UUID.randomUUID().toString().substring(0, 8));

        ScheduleVO scheduleVO = new ScheduleVO();
        scheduleVO.setMemberNumber(MEMBER_NUMBER);
        scheduleVO.setWorkPlaceNumber(workplaceNumber);
        scheduleVO.setWorkTitle(marker);
        scheduleVO.setWorkDetail("integration test row");
        scheduleVO.setWorkStatus("대기");
        scheduleVO.setWorkStartTime(LocalDateTime.now().format(FORMATTER));
        // tbl_work.work_spent_time is NOT NULL; the normal UI flow always sends an initial
        // value (unset here in the original test) — defaulted to "0" to satisfy the
        // constraint (deviation reported in task-4-report.md).
        scheduleVO.setWorkSpentTime("0");

        scheduleService.workRegister(scheduleVO);

        return jdbcTemplate.queryForObject(
                "select work_number from tbl_work where work_title = ? and member_number = ?",
                Integer.class, marker, MEMBER_NUMBER);
    }

    private Integer ensureWorkplace(String name) {
        if (workplaceMapper.selectWorkplace(name) == 0) {
            workplaceMapper.saveWorkplace(name);
        }
        WorkplaceDTO dto = workplaceMapper.selectWorkplaceByName(name);
        return dto.getWorkplaceNumber();
    }

    private Map<String, Object> readRow(Integer workNumber) {
        return jdbcTemplate.queryForMap(
                "select work_status, (work_end_time is not null) as has_end_time, " +
                        "to_char(work_real_start_time, 'YYYY-MM-DD HH24:MI:SS.MS') as real_start, " +
                        "work_spent_time " +
                        "from tbl_work where work_number = ?",
                workNumber);
    }
}
