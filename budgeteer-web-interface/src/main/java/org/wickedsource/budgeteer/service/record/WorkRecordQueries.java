package org.wickedsource.budgeteer.service.record;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.wickedsource.budgeteer.persistence.record.WorkRecordEntity;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class WorkRecordQueries {

    public static Specification<WorkRecordEntity> findByFilter(WorkRecordFilter filter) {
        return (record, query, builder) -> {
            Predicate projectPredicate = builder.equal(record.get("budget").get("project").get("id"), filter.getProjectId());
            return builder.and(projectPredicate,
                    buildStartDatePredicate(filter, record, builder),
                    buildEndDatePredicate(filter, record, builder),
                    buildPersonPredicate(filter, record, builder),
                    buildBudgetPredicate(filter, record, builder));
        };
    }

    private static Predicate buildStartDatePredicate(WorkRecordFilter filter,
                                                     Root<WorkRecordEntity> record,
                                                     CriteriaBuilder builder) {
        if (filter.getDateRange() == null || filter.getDateRange().getStartDate() == null) {
            return builder.and();
        }
        return builder.greaterThanOrEqualTo(record.get("date"), filter.getDateRange().getStartDate());
    }

    private static Predicate buildEndDatePredicate(WorkRecordFilter filter,
                                                   Root<WorkRecordEntity> record,
                                                   CriteriaBuilder builder) {
        if (filter.getDateRange() == null || filter.getDateRange().getEndDate() == null) {
            return builder.and();
        }
        return builder.lessThanOrEqualTo(record.get("date"), filter.getDateRange().getEndDate());
    }

    private static Predicate buildBudgetPredicate(WorkRecordFilter filter,
                                                  Root<WorkRecordEntity> record,
                                                  CriteriaBuilder builder) {
        if (filter.getBudgetList().isEmpty()) {
            return builder.and();
        }
        return builder.or(filter.getBudgetList().stream()
                .map(budget -> builder.equal(record.get("budget").get("id"), budget.getId()))
                .toArray(Predicate[]::new));
    }

    private static Predicate buildPersonPredicate(WorkRecordFilter filter,
                                                  Root<WorkRecordEntity> record,
                                                  CriteriaBuilder builder) {
        if (filter.getPersonList().isEmpty()) {
            return builder.and();
        }
        return builder.or(filter.getPersonList().stream()
                .map(person -> builder.equal(record.get("person").get("id"), person.getId()))
                .toArray(Predicate[]::new));
    }
}
