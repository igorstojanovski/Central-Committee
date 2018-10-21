package co.igorski.hundreddays.ui.views.run;

import co.igorski.hundreddays.model.Entry;
import co.igorski.hundreddays.services.RunService;
import co.igorski.hundreddays.services.TestService;
import co.igorski.hundreddays.ui.views.layouts.BreadCrumbedView;
import co.igorski.hundreddays.ui.views.test.SingleTest;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationObserver;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;
import org.springframework.beans.factory.annotation.Autowired;

import static co.igorski.hundreddays.model.Outcome.PASSED;

@Route(value = "Run", layout = BreadCrumbedView.class)
public class SingleRun extends VerticalLayout implements HasUrlParameter<String>, AfterNavigationObserver {

    private final RunService runService;
    private final TestService testService;
    private Long runId;
    private final Grid<Entry> grid;

    public SingleRun(@Autowired TestService testService, @Autowired RunService runService) {
        this.runService = runService;
        this.testService  = testService;

        grid = new Grid<>();
        grid.addComponentColumn(entry -> {
            Long testId = entry.getTest().getId();
            return new RouterLink(testService.getTest(testId).getTestName(),
                    SingleTest.class, String.valueOf(testId));
        }).setHeader("Test");
        grid.addComponentColumn(entry -> {
            Label label = new Label(entry.getResult().getOutcome().toString());
            if(PASSED.equals(entry.getResult().getOutcome())) {
                label.getStyle().set("color", "green");
            } else {
                label.getStyle().set("color", "red");
            }

            return label;
        }).setHeader("Outcome");
        grid.addComponentColumn(entry -> new Label(runService.getFormattedTestDuration(entry.getResult()))).setHeader("Duration");

        add(grid);
    }

    @Override
    public void setParameter(BeforeEvent event, String parameter) {
        if(event != null) {
            runId = Long.parseLong(parameter);
        }
    }

    @Override
    public void afterNavigation(AfterNavigationEvent event) {
        if(event != null) {
            ListDataProvider<Entry> entryProvider = DataProvider.ofCollection(runService.getEntries(runId));
            grid.setDataProvider(entryProvider);
        }
    }

}