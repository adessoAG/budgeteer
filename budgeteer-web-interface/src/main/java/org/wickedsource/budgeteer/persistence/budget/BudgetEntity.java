package org.wickedsource.budgeteer.persistence.budget;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.builder.CompareToBuilder;
import org.joda.money.Money;
import org.wickedsource.budgeteer.persistence.contract.ContractEntity;
import org.wickedsource.budgeteer.persistence.person.DailyRateEntity;
import org.wickedsource.budgeteer.persistence.project.ProjectEntity;
import org.wickedsource.budgeteer.persistence.record.PlanRecordEntity;
import org.wickedsource.budgeteer.persistence.record.WorkRecordEntity;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "BUDGET",
        uniqueConstraints = {
                @UniqueConstraint(name = "UNIQUE_IMPORT_KEY_PER_PROJECT", columnNames = {"PROJECT_ID", "IMPORT_KEY"}),
                @UniqueConstraint(name = "UNIQUE_NAME_PER_PROJECT", columnNames = {"PROJECT_ID", "NAME"})
        })
@Getter
@Setter
@NoArgsConstructor
public class BudgetEntity implements Comparable<BudgetEntity> {

    @Id
    @SequenceGenerator(name = "SEQ_BUDGET_ID", sequenceName = "SEQ_BUDGET_ID")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_BUDGET_ID")
    private long id;

    @Column(name="NAME", nullable = false, length = 255)
    private String name;

    @Column(length = 255)
    private String description;

    // Notes have a maximum size of 10KB
    @Lob
    @Column(length = 10 * 1024)
    private String note;

    private Money total;

    @Column(name = "`limit`")
    private Money limit;

    @ManyToOne(optional = false)
    @JoinColumn(name = "PROJECT_ID")
    private ProjectEntity project;

    @OneToMany(orphanRemoval = true, cascade = CascadeType.ALL, fetch = FetchType.EAGER, mappedBy = "budget")
    private List<BudgetTagEntity> tags = new ArrayList<BudgetTagEntity>();

    @OneToMany(mappedBy = "budget", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<WorkRecordEntity> workRecords = new ArrayList<WorkRecordEntity>();

    @OneToMany(mappedBy = "budget", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<PlanRecordEntity> planRecords = new ArrayList<PlanRecordEntity>();

    @OneToMany(mappedBy = "budget", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<DailyRateEntity> dailyRates = new ArrayList<DailyRateEntity>();

    @Column(nullable = false, name = "IMPORT_KEY", length = 255)
    private String importKey;

    @ManyToOne
    @JoinColumn(name = "CONTRACT_ID")
    private ContractEntity contract;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BudgetEntity)) return false;

        BudgetEntity that = (BudgetEntity) o;

        if (id != that.id) return false;
        if (contract != null ? !contract.equals(that.contract) : that.contract != null) return false;
        if (dailyRates != null ? !dailyRates.equals(that.dailyRates) : that.dailyRates != null) return false;
        if (importKey != null ? !importKey.equals(that.importKey) : that.importKey != null) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (description != null ? !description.equals(that.description) : that.description != null) return false;
        if (note != null ? !note.equals(that.note) : that.note != null) return false;
        if (planRecords != null ? !planRecords.equals(that.planRecords) : that.planRecords != null) return false;
        if (project != null ? !project.equals(that.project) : that.project != null) return false;
        if (tags != null ? !tags.equals(that.tags) : that.tags != null) return false;
        if (total != null ? !total.equals(that.total) : that.total != null) return false;
        if (limit != null ? !limit.equals(that.total) : that.limit != null) return false;
        if (workRecords != null ? !workRecords.equals(that.workRecords) : that.workRecords != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = (int) (id ^ (id >>> 32));
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (description != null ? description.hashCode() : 0);
        result = 31 * result + (note != null ? note.hashCode() : 0);
        result = 31 * result + (total != null ? total.hashCode() : 0);
        result = 31 * result + (limit != null ? limit.hashCode() : 0);
        result = 31 * result + (project != null ? project.hashCode() : 0);
        result = 31 * result + (tags != null ? tags.hashCode() : 0);
        result = 31 * result + (workRecords != null ? workRecords.hashCode() : 0);
        result = 31 * result + (planRecords != null ? planRecords.hashCode() : 0);
        result = 31 * result + (dailyRates != null ? dailyRates.hashCode() : 0);
        result = 31 * result + (importKey != null ? importKey.hashCode() : 0);
        result = 31 * result + (contract != null ? contract.hashCode() : 0);
        return result;
    }

    @Override
    public int compareTo(@NotNull BudgetEntity that) {
        return new CompareToBuilder().append(this.name, that.name).append(this.id, that.id).toComparison();
    }
}
