package org.wickedsource.budgeteer.persistence.person;

import lombok.Getter;
import org.joda.money.Money;

import java.util.Date;

@Getter
public class PersonDetailDataBean {

    private Long id;
    private String name;
    private Long averageDailyRateInCents;
    private Money defaultDailyRate;
    private Date firstBookedDate;
    private Date lastBookedDate;
    private Double hoursBooked;
    private Long budgetBurnedInCents;

    public PersonDetailDataBean(Long id, String name, Long valuedMinutes, Long valuedRate, Date firstBookedDate, Date lastBookedDate, Double hoursBooked, Long budgetBurnedInCents, Money defaultDailyRate) {
        this.id = id;
        this.name = name;
        if (valuedRate == null || valuedRate == 0L)
            this.averageDailyRateInCents = 0L;
        else
            this.averageDailyRateInCents = valuedMinutes / valuedRate;
        this.firstBookedDate = firstBookedDate;
        this.lastBookedDate = lastBookedDate;
        this.hoursBooked = hoursBooked;
        this.budgetBurnedInCents = budgetBurnedInCents;
        this.defaultDailyRate = defaultDailyRate;
    }
}
