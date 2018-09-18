package co.igorski.hundreddays;

import co.igorski.hundreddays.model.Organization;
import co.igorski.hundreddays.model.Outcome;
import co.igorski.hundreddays.model.Result;
import co.igorski.hundreddays.model.Run;
import co.igorski.hundreddays.model.Status;
import co.igorski.hundreddays.model.User;
import co.igorski.hundreddays.model.events.RunFinished;
import co.igorski.hundreddays.model.events.RunStarted;
import co.igorski.hundreddays.model.events.TestFinished;
import co.igorski.hundreddays.model.events.TestStarted;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Java6Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class SmokeTestIT {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    public void shouldSimulateWholeRun() {
        Organization organization = new Organization();
        organization.setId("5b943fbd6e644024f4cab9e2");

        User user = new User();
        user.setName("Igor");
        user.setUsername("igorski");
        user.setId("5b94464d6e6440221810064c");
        user.setOrganizationId("5b943fbd6e644024f4cab9e2");

        co.igorski.hundreddays.model.Test testOne = new co.igorski.hundreddays.model.Test();
        testOne.setTestName("shouldMarkRunAsStarted");
        testOne.setTestPath("org.igorski");

        co.igorski.hundreddays.model.Test testTwo = new co.igorski.hundreddays.model.Test();
        testTwo.setTestName("shouldMarkRunAsFinished");
        testTwo.setTestPath("org.igorski");

        List<co.igorski.hundreddays.model.Test> tests = new ArrayList<>();
        tests.add(testOne);
        tests.add(testTwo);

        RunStarted runStarted = new RunStarted();
        runStarted.setOrganization(organization);
        runStarted.setUser(user);
        runStarted.setTests(tests);

        ResponseEntity<Run> runResponseEntity = restTemplate.postForEntity("/event/run/started", runStarted, Run.class);
        assertThat(runResponseEntity.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        String runId = runResponseEntity.getBody().getId();

        TestStarted testOneStarted = new TestStarted();
        testOneStarted.setRunId(runId);
        testOneStarted.setTimestamp(new Date());
        testOneStarted.setTest(testOne);

        TestStarted testTwoStarted = new TestStarted();
        testTwoStarted.setRunId(runId);
        testTwoStarted.setTimestamp(new Date());
        testTwoStarted.setTest(testTwo);

        restTemplate.postForEntity("/event/test/started", testOneStarted, co.igorski.hundreddays.model.Test.class);
        restTemplate.postForEntity("/event/test/started", testTwoStarted, co.igorski.hundreddays.model.Test.class);

        ResponseEntity<Run[]> activeRunsResponse = restTemplate.getForEntity("/run", Run[].class);
        assertThat(activeRunsResponse.getBody()).hasSize(1);
        Run activeRun = activeRunsResponse.getBody()[0];
        List<Result> currentResults = activeRun.getResults();
        assertThat(currentResults).hasSize(2);

        assertThat(currentResults.get(0).getStatus()).isEqualTo(Status.RUNNING);
        assertThat(currentResults.get(1).getStatus()).isEqualTo(Status.RUNNING);

        TestFinished testOneFinished = new TestFinished();
        testOneFinished.setRunId(runId);
        testOneFinished.setTimestamp(new Date());
        testOneFinished.setTest(testOne);
        testOneFinished.setOutcome(Outcome.FAILED);

        TestFinished testTwoFinished = new TestFinished();
        testTwoFinished.setRunId(runId);
        testTwoFinished.setTimestamp(new Date());
        testTwoFinished.setTest(testTwo);
        testTwoFinished.setOutcome(Outcome.PASSED);

        restTemplate.postForEntity("/event/test/finished", testTwoFinished, co.igorski.hundreddays.model.Test.class);
        restTemplate.postForEntity("/event/test/finished", testOneFinished, co.igorski.hundreddays.model.Test.class);

        activeRunsResponse = restTemplate.getForEntity("/run", Run[].class);
        activeRun = activeRunsResponse.getBody()[0];
        currentResults = activeRun.getResults();
        assertThat(currentResults).hasSize(2);

        assertThat(currentResults.get(0).getStatus()).isEqualTo(Status.FINISHED);
        assertThat(currentResults.get(1).getStatus()).isEqualTo(Status.FINISHED);

        RunFinished runFinished = new RunFinished();
        runFinished.setRunId(runId);
        restTemplate.postForEntity("/event/run/finished", runFinished, Run.class);

        activeRunsResponse = restTemplate.getForEntity("/run", Run[].class);
        assertThat(activeRunsResponse.getBody()).hasSize(0);
    }

    @TestConfiguration
    static class Config {

        @Bean
        public RestTemplateBuilder restTemplateBuilder() {
            return new RestTemplateBuilder().setConnectTimeout(1000).setReadTimeout(1000);
        }

    }

}