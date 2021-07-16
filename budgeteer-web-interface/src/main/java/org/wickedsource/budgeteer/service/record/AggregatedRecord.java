package org.wickedsource.budgeteer.service.record;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.joda.money.Money;
import org.wickedsource.budgeteer.MoneyUtil;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

@Data
@NoArgsConstructor
public class AggregatedRecord {
    private String aggregationPeriodTitle;
    private Date aggregationPeriodStart;
    private Date aggregationPeriodEnd;
    private Double hours;
    private Money budgetPlanned_net;
    private Money budgetBurned_net;
    private Money budgetPlanned_gross;
    private Money budgetBurned_gross;

    public AggregatedRecord(PlanAndWorkRecord planAndWorkRecord) {
        Calendar c = Calendar.getInstance(Locale.GERMANY);
        c.clear();
        c.set(Calendar.YEAR, planAndWorkRecord.getYear());

        // If the planAndWorkRecord is monthly, the week property is -1.
        // So the aggregationPeriod must be calculated monthly.
        if (planAndWorkRecord.getWeek() == -1) {
            c.set(Calendar.MONTH, planAndWorkRecord.getMonth());
            aggregationPeriodStart = c.getTime();
            c.add(Calendar.MONTH, 1);
            c.add(Calendar.DAY_OF_YEAR, -1);
            aggregationPeriodTitle = String.format("Month %d-%02d", planAndWorkRecord.getYear(), planAndWorkRecord.getMonth() + 1);
        } else {
            c.set(Calendar.WEEK_OF_YEAR, planAndWorkRecord.getWeek());
            aggregationPeriodStart = c.getTime();
            c.add(Calendar.DAY_OF_YEAR, 6);
            aggregationPeriodTitle = String.format("Week %d-%d", planAndWorkRecord.getYear(), planAndWorkRecord.getWeek());
        }

        aggregationPeriodEnd = c.getTime();
        hours = planAndWorkRecord.getHoursWorked();
        budgetPlanned_net = MoneyUtil.createMoneyFromCents(planAndWorkRecord.getValueInCentsPlanned());
        budgetPlanned_gross = MoneyUtil.createMoneyFromCents(planAndWorkRecord.getValueInCentsPlanned_gross());
        budgetBurned_net = MoneyUtil.createMoneyFromCents(planAndWorkRecord.getValueInCentsBurned());
        budgetBurned_gross = MoneyUtil.createMoneyFromCents(planAndWorkRecord.getValueInCentsBurned_gross());
    }

    public Money getDifference() {
        Money first = budgetPlanned_net != null ? budgetPlanned_net : MoneyUtil.createMoney(0);
        Money second = budgetBurned_net != null ? budgetBurned_net : MoneyUtil.createMoney(0);
        return first.minus(second);
    }

    public Money getDifference_gross() {
        Money first = budgetPlanned_gross != null ? budgetPlanned_gross : MoneyUtil.createMoney(0);
        Money second = budgetBurned_gross != null ? budgetBurned_gross : MoneyUtil.createMoney(0);
        return first.minus(second);
    }

    /***
     * Ensure that all properties for calculating are 0, not null.
     */
    public void removeNullValues() {
        if (hours == null) {
            hours = 0.0;
        }
        if (budgetBurned_net == null) {
            budgetBurned_net = MoneyUtil.createMoney(0.0);
            budgetBurned_gross = MoneyUtil.createMoney(0.0);
        }
        if (budgetPlanned_net == null) {
            budgetPlanned_net = MoneyUtil.createMoney(0.0);
            budgetPlanned_gross = MoneyUtil.createMoney(0.0);
        }
    }
}
