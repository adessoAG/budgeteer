package org.wickedsource.budgeteer.persistence.manualRecord;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.joda.money.Money;
import org.wickedsource.budgeteer.persistence.budget.BudgetEntity;

import javax.persistence.*;
import java.util.Calendar;
import java.util.Date;


@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ManualRecordEntity {
    @Id
    @GeneratedValue
    private long id;

    @Column(nullable = false, length = 255)
    private String description;

    @Column(nullable = false)
    private Money moneyAmount;

    @ManyToOne(optional = false)
    @JoinColumn(name = "BUDGET_ID")
    private BudgetEntity budget;

    @Temporal(TemporalType.DATE)
    @Column(name="CREATION_DATE", nullable = false)
    private Date creationDate;

    @Temporal(TemporalType.DATE)
    @Column(name="BILLING_DATE", nullable = false)
    private Date billingDate;

    @Column(name="RECORD_YEAR", nullable = false)
    private int year;

    @Column(name="RECORD_MONTH", nullable = false)
    private int month;

    @Column(name="RECORD_DAY", nullable = false)
    private int day;

    @Column(name="RECORD_WEEK", nullable = false)
    private int week;

    public void setBillingDate(Date date) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        this.billingDate = date;
        this.year = c.get(Calendar.YEAR);
        this.month = c.get(Calendar.MONTH);
        this.day = c.get(Calendar.DAY_OF_MONTH);
        this.week = c.get(Calendar.WEEK_OF_YEAR);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ManualRecordEntity)) return false;

        ManualRecordEntity that = (ManualRecordEntity) o;

        if (day != that.day) return false;
        if (id != that.id) return false;
        if (month != that.month) return false;
        if (week != that.week) return false;
        if (year != that.year) return false;
        if (budget != null ? !budget.equals(that.budget) : that.budget != null) return false;
        if (creationDate != null ? !creationDate.equals(that.creationDate) : that.creationDate != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = (int) (id ^ (id >>> 32));
        result = 31 * result + (budget != null ? budget.hashCode() : 0);
        result = 31 * result + (creationDate != null ? creationDate.hashCode() : 0);
        result = 31 * result + year;
        result = 31 * result + month;
        result = 31 * result + day;
        result = 31 * result + week;
        return result;
    }
}
