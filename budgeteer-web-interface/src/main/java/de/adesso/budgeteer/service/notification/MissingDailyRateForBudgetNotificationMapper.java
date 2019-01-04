package de.adesso.budgeteer.service.notification;

import de.adesso.budgeteer.persistence.record.MissingDailyRateForBudgetBean;
import de.adesso.budgeteer.service.AbstractMapper;
import org.springframework.stereotype.Component;

@Component
public class MissingDailyRateForBudgetNotificationMapper extends AbstractMapper<MissingDailyRateForBudgetBean, Notification> {

    @Override
    public Notification map(MissingDailyRateForBudgetBean rate) {
        MissingDailyRateForBudgetNotification notification = new MissingDailyRateForBudgetNotification();
        notification.setStartDate(rate.getStartDate());
        notification.setEndDate(rate.getEndDate());
        notification.setPersonId(rate.getPersonId());
        notification.setPersonName(rate.getPersonName());
        notification.setBudgetName(rate.getBudgetName());
        return notification;
    }

}
