package co.igorski.hundreddays.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@Document
public class Result {
    @Id
    private String id;
    @DBRef
    private CcTest test;
    private Outcome outcome;
    private Status status;
    private Date start;
    private Date end;
}
