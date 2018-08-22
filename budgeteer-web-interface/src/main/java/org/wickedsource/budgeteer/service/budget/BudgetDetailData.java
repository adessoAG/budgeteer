package org.wickedsource.budgeteer.service.budget;

import lombok.Data;
import org.joda.money.Money;

import java.util.Date;
import java.util.List;

@Data
public class BudgetDetailData {

    private long id;
    private String name;
    private String description;
    private String note;
    private List<String> tags;
    private Money total;
    private Money totalGross;
    private Money spent;
    private Money spentGross;
    private Date lastUpdated;
    private Money avgDailyRate;
    private Money avgDailyRateGross;
    private Money unplanned;
    private Money unplannedGross;
    private String contractName;
    private long contractId;


    public Money getRemaining() {
        return this.total.minus(this.spent);
    }

    public Money getRemainingGross() {
        return this.totalGross.minus(this.spentGross);
    }

    public Double getProgress() {
        return this.getSpent().getAmount().doubleValue() / this.total.getAmount().doubleValue();
    }

    /**
     * Gets the progress in percent rounded to two decimal places.
     *
     * @return the progress in percent.
     */
    public Double getProgressInPercent() {
        return getProgress() * 100;
    }
}
