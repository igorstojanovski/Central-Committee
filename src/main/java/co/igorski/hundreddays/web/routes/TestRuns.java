package co.igorski.hundreddays.web.routes;

import co.igorski.hundreddays.model.Run;
import co.igorski.hundreddays.repositories.RunRepository;
import co.igorski.hundreddays.services.RunService;
import co.igorski.hundreddays.web.dataproviders.PageableRunProvider;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.LocalDateTimeRenderer;
import com.vaadin.flow.function.ValueProvider;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;

@Route("runs")
public class TestRuns extends VerticalLayout {

    public TestRuns(@Autowired RunService runService, @Autowired RunRepository runRepository) {
        Grid<Run> grid = new Grid<>();
        grid.addColumn(Run::getId).setHeader("ID");
        grid.addColumn((ValueProvider<Run, Integer>) run -> run.getResults().size()).setHeader("CcTest Count");
        grid.addColumn(
                new LocalDateTimeRenderer<>(
                        run -> run.getStart().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime(),
                        DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT, FormatStyle.MEDIUM)
                )
        ).setHeader("Start");

        grid.setDataProvider(new PageableRunProvider<Run, Void>());

        add(grid);
    }
}
