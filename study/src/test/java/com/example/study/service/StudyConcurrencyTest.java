package com.example.study.service;

import static com.example.study.service.ConcurrencyTestSupport.runTogether;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.example.study.DatabaseCleaner;
import com.example.study.IntegrationTest;
import com.example.study.common.exception.ErrorCode;
import com.example.study.domain.RecruitStatus;
import com.example.study.dto.CreateStudyRequest;
import com.example.study.dto.SignupRequest;
import com.example.study.repository.StudyRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@IntegrationTest
class StudyConcurrencyTest {

    @Autowired
    MemberService memberService;
    @Autowired
    StudyService studyService;
    @Autowired
    StudyDeletionService studyDeletionService;
    @Autowired
    StudyRepository studyRepository;
    @Autowired
    PlatformTransactionManager transactionManager;
    @Autowired
    JdbcTemplate jdbcTemplate;
    @Autowired
    DatabaseCleaner databaseCleaner;

    @BeforeEach
    void clean() {
        databaseCleaner.clean();
    }

    @RepeatedTest(5)
    @DisplayName("한 회원이 겹치는 시간의 스터디를 동시에 개설하면 하나만 성공한다")
    void concurrentOpen() throws Exception {
        Long owner = member("owner");
        List<Callable<?>> tasks = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            tasks.add(() -> studyService.open(owner, request()));
        }

        var outcomes = runTogether(tasks);

        assertThat(outcomes).filteredOn(o -> o.success()).hasSize(1);
        assertThat(outcomes).filteredOn(o -> o.error() == ErrorCode.TIME_OVERLAP).hasSize(5);
        assertThat(count("study")).isEqualTo(1);
        assertThat(count("study_member")).isEqualTo(1);
    }

    @RepeatedTest(5)
    @DisplayName("같은 스터디를 동시에 삭제하면 한 번만 성공하고 이력도 한 건이다")
    void concurrentDelete() throws Exception {
        Long owner = member("owner");
        Long studyId = studyService.open(owner, request()).id();
        Callable<?> delete = () -> {
            studyDeletionService.delete(owner, studyId);
            return null;
        };

        var outcomes = runTogether(List.of(delete, delete, delete));

        assertThat(outcomes).filteredOn(o -> o.success()).hasSize(1);
        assertThat(outcomes).filteredOn(o -> o.error() == ErrorCode.STUDY_NOT_FOUND).hasSize(2);
        assertThat(count("study_history")).isEqualTo(1);
    }

    @Test
    @DisplayName("삭제가 스터디 잠금을 기다리는 동안 참여자가 늘면, 다시 시도해 그 참여자에게도 이력을 남긴다")
    void deletionSeesMemberAddedWhileWaiting() throws Exception {
        Long owner = member("owner");
        Long joiner = member("joiner");
        Long studyId = studyService.open(owner, request()).id();

        try (ExecutorService executor = Executors.newSingleThreadExecutor()) {
            Future<?> deletion = new TransactionTemplate(transactionManager).execute(status -> {
                studyRepository.findByIdForUpdate(studyId).orElseThrow();
                Future<?> future = executor.submit(() -> studyDeletionService.delete(owner, studyId));
                // 삭제는 참여자 목록을 읽은 뒤 스터디 잠금에서 대기한다.
                assertThatThrownBy(() -> future.get(1, TimeUnit.SECONDS)).isInstanceOf(TimeoutException.class);
                // 대기하는 동안 참여자를 추가하고 커밋한다.
                jdbcTemplate.update("insert into study_member (study_id, member_id) values (?, ?)",
                        studyId, joiner);
                return future;
            });
            deletion.get(10, TimeUnit.SECONDS);
        }

        assertThat(count("study")).isZero();
        assertThat(count("study_member")).isZero();
        assertThat(jdbcTemplate.queryForList(
                "select concat(member_id, ':', reason) from study_history order by member_id", String.class))
                .containsExactly(owner + ":OWNER_DELETED", joiner + ":STUDY_DELETED");
    }

    private Long member(String name) {
        return memberService.signUp(new SignupRequest(name + "@example.com", "password123", name, "")).id();
    }

    private CreateStudyRequest request() {
        return new CreateStudyRequest("스터디", "내용", 4, RecruitStatus.RECRUITING,
                DayOfWeek.MONDAY, LocalTime.of(19, 0), LocalTime.of(21, 0));
    }

    private long count(String table) {
        return jdbcTemplate.queryForObject("select count(*) from " + table, Long.class);
    }
}
