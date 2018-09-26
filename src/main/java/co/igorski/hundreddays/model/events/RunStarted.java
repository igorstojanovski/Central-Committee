package co.igorski.hundreddays.model.events;

import co.igorski.hundreddays.model.CcTest;
import co.igorski.hundreddays.model.Organization;
import co.igorski.hundreddays.model.User;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class RunStarted extends Event {
    private Organization organization;
    private User user;
    private List<CcTest> tests;
}
