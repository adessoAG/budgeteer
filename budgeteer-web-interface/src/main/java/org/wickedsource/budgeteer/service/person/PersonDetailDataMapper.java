package org.wickedsource.budgeteer.service.person;

import org.springframework.stereotype.Component;
import org.wickedsource.budgeteer.MoneyUtil;
import org.wickedsource.budgeteer.persistence.person.PersonDetailDataBean;
import org.wickedsource.budgeteer.service.AbstractMapper;

@Component
public class PersonDetailDataMapper extends AbstractMapper<PersonDetailDataBean, PersonDetailData> {

    @Override
    public PersonDetailData map(PersonDetailDataBean sourceObject) {
        PersonDetailData data = new PersonDetailData();
        data.setName(sourceObject.getName());
        if (sourceObject.getAverageDailyRateInCents() != null) {
            data.setAverageDailyRate(MoneyUtil.createMoneyFromCents(sourceObject.getAverageDailyRateInCents()));
        }
        data.setDefaultDailyRate(sourceObject.getDefaultDailyRate());
        data.setLastBookedDate(sourceObject.getLastBookedDate());
        data.setFirstBookedDate(sourceObject.getFirstBookedDate());
        data.setHoursBooked(sourceObject.getHoursBooked());
        if (sourceObject.getBudgetBurnedInCents() != null) {
            data.setBudgetBurned(MoneyUtil.createMoneyFromCents(sourceObject.getBudgetBurnedInCents()));
        }
        return data;
    }
}
