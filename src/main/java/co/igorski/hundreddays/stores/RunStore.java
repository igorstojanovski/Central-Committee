package co.igorski.hundreddays.stores;

import co.igorski.hundreddays.model.Run;
import co.igorski.hundreddays.model.events.Event;
import co.igorski.hundreddays.model.events.RunFinished;
import co.igorski.hundreddays.web.DataListener;
import com.vaadin.flow.spring.annotation.SpringComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SpringComponent
@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
public class RunStore {
    @Autowired
    private KafkaTemplate<String, Event> template;
    private final Map<String, Run> activeRuns = new HashMap<>();
    private final List<DataListener> listeners = new ArrayList<>();

    public void activateRun(Run created) {
        activeRuns.put(created.getId(), created);
        notifyAllListeners();
    }

    public Run deactivateRun(String runId) {
        Run run = activeRuns.remove(runId);
        notifyAllListeners();
        return run;
    }

    public List<Run> getActiveRuns() {
        return new ArrayList<>(activeRuns.values());
    }

    public Collection<Run> getLiveRuns() {
        return activeRuns.values();
    }

    public Run getRun(String runId) {
        return activeRuns.get(runId);
    }

    public void registerListener(DataListener dataListener) {
        listeners.add(dataListener);
    }

    private void notifyAllListeners() {
        for(DataListener dataListener : listeners) {
            dataListener.dataChanged();
        }
    }

    public boolean containsId(String runId) {
        boolean runExists = false;
        for(Run run : activeRuns.values()) {
            if(run.getId().equals(run)) {
                runExists = true;
                break;
            }
        }

        return runExists;
    }

    @Scheduled(fixedRate = 10000)
    public void reportCurrentTime() {
        for(Run run : activeRuns.values()) {
            LocalDateTime runLocalDateTime = run.getStart().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
            if(runLocalDateTime.isBefore(LocalDateTime.now().minusMinutes(3))) {
                RunFinished testFinished = new RunFinished();
                testFinished.setRunId(run.getId());
                testFinished.setTimestamp(new Date());

                template.send("test-events", testFinished);
            }
        }
    }
}
