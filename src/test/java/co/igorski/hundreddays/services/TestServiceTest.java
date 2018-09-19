package co.igorski.hundreddays.services;

import co.igorski.hundreddays.model.Outcome;
import co.igorski.hundreddays.model.Result;
import co.igorski.hundreddays.model.Run;
import co.igorski.hundreddays.model.Status;
import co.igorski.hundreddays.model.events.TestFinished;
import co.igorski.hundreddays.model.events.TestStarted;
import co.igorski.hundreddays.repositories.TestRepository;
import co.igorski.hundreddays.stores.RunStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith({SpringExtension.class, MockitoExtension.class})
class TestServiceTest {

    @Mock
    private TestRepository testRepository;
    @Mock
    private RunStore runStore;
    private co.igorski.hundreddays.model.Test theTest;
    private Run run;

    @BeforeEach
    public void beforeEach() {
        theTest = new co.igorski.hundreddays.model.Test();
        theTest.setTestName("shouldMarkRunAsStarted");
        theTest.setTestPath("org.igorski");

        List<Result> results = new ArrayList<>();
        Result result = new Result();
        result.setTest(theTest);
        result.setStatus(Status.RUNNING);
        results.add(result);

        run = new Run();
        run.setId("runId");
        run.setStart(new Date());
        run.setOrganizationId("orgId");
        run.setResults(results);
    }

    @Test
    public void shouldUpdateStartedTestInRunObject() {
        TestService testService = new TestService(testRepository, runStore);
        TestStarted testStarted = new TestStarted();
        testStarted.setTest(theTest);
        testStarted.setTimestamp(new Date());
        testStarted.setRunId("runId");

        when(runStore.getRun("runId")).thenReturn(run);

        testService.testStarted(testStarted);

        assertThat(run.getResults().get(0).getStatus()).isEqualTo(Status.RUNNING);
        assertThat(run.getResults().get(0).getStart()).isNotNull();
    }

    @Test
    public void shouldUpdateFinishedTestInRunObject() {
        TestService testService = new TestService(testRepository, runStore);
        TestFinished testFinished = new TestFinished();
        testFinished.setTest(theTest);
        testFinished.setTimestamp(new Date());
        testFinished.setRunId("runId");
        testFinished.setOutcome(Outcome.PASSED);

        when(runStore.getRun("runId")).thenReturn(run);

        testService.testFinished(testFinished);

        assertThat(run.getResults().get(0).getStatus()).isEqualTo(Status.FINISHED);
        assertThat(run.getResults().get(0).getOutcome()).isEqualTo(Outcome.PASSED);
        assertThat(run.getResults().get(0).getEnd()).isNotNull();
    }

    @Test
    public void shouldNotCreateTestWhenOneExists() {
        when(testRepository.findByTestName("shouldMarkRunAsStarted")).thenReturn(theTest);
        TestService testService = new TestService(testRepository, runStore);
        testService.getOrCreate(theTest);

        verify(testRepository, times(0)).save(any());
    }

    @Test
    public void shouldCreateTestWhenNoneExists() {
        when(testRepository.findByTestName("shouldMarkRunAsStarted")).thenReturn(null);
        TestService testService = new TestService(testRepository, runStore);
        testService.getOrCreate(theTest);

        verify(testRepository, times(1)).save(theTest);
    }
}