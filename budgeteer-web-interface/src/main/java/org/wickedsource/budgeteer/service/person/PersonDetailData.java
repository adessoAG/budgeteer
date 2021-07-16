package org.wickedsource.budgeteer.service.person;

import lombok.Data;
import org.joda.money.Money;

import java.util.Date;

@Data
public class PersonDetailData {

    private String name;
    private Money averageDailyRate;
    private Money defaultDailyRate;
    private Date firstBookedDate;
    private Date lastBookedDate;
    private Double hoursBooked;
    private Money budgetBurned;

}
