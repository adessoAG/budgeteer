package org.wickedsource.budgeteer.persistence.record;

import org.joda.money.Money;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.wickedsource.budgeteer.persistence.budget.BudgetEntity;
import org.wickedsource.budgeteer.persistence.person.PersonEntity;
import org.wickedsource.budgeteer.persistence.project.ProjectEntity;
import org.wickedsource.budgeteer.service.record.WorkRecordFilter;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.Date;
import java.util.List;

public interface WorkRecordRepository extends CrudRepository<WorkRecordEntity, Long>, RecordRepository, JpaSpecificationExecutor<WorkRecordEntity> {

    /**
     * Aggregates the monetary value of all work records in the given budget.
     *
     * @param budgetId ID of the budget whose spending to aggregate.
     * @return aggregated monetary value of the spent budget in cents.
     */
    @Query("select sum(record.minutes * record.dailyRate) / 60 / 8 from WorkRecordEntity record where record.budget.id = :budgetId")
    Double getSpentBudget(@Param("budgetId") long budgetId);

    /**
     * Calculates the monetary value of the average daily rate in the given budget.
     *
     * @param budgetId ID of the budget whose average daily rate to calculate
     * @return monetary value of the average daily rate in cents.
     */
    @Query("select case when (sum(record.minutes) = 0) then 0 else (sum(record.dailyRate * record.minutes) / sum(record.minutes)) end from WorkRecordEntity record where record.budget.id=:budgetId")
    Double getAverageDailyRate(@Param("budgetId") long budgetId);

    @Query("select sum(record.minutes * record.dailyRate) / 60 / 8 from WorkRecordEntity record where record.budget.id = :budgetId and record.date <= :untilDate")
    Double getSpentBudgetUntilDate(@Param("budgetId") long budgetId, @Param("untilDate") Date untilDate);

    @Query("select sum(record.minutes * record.dailyRate) / 60 / 8 from WorkRecordEntity record where record.budget.id = :budgetId and :fromDate <= record.date and record.date <= :untilDate")
    Double getSpentBudgetInTimeRange(@Param("budgetId") long budgetId, @Param("fromDate") Date fromDate, @Param("untilDate") Date untilDate);

    @Query("select max(record.date) from WorkRecordEntity record where record.budget.id=:budgetId")
    Date getLatestWorkRecordDate(@Param("budgetId") long budgetId);

    @Query("select min(record.date) from WorkRecordEntity record where record.budget.id=:budgetId")
    Date getFirstWorkRecordDate(@Param("budgetId") long budgetId);

    @Query("select min(record.date) from WorkRecordEntity record where record.budget.id in (:budgetIds)")
    Date getFirstWorkRecordDateByBudgetIds(@Param("budgetIds") List<Long> budgetIds);

    @Query("select cast(sum(record.minutes) AS double) / 60.0 from WorkRecordEntity record where record.budget.id=:budgetId")
    Double getTotalHoursByBudgetId(@Param("budgetId") long budgetId);

    @Query("select cast(sum(record.minutes) AS double) / 60.0 from WorkRecordEntity record where record.budget.id=:budgetId and record.date <= :untilDate")
    Double getTotalHoursByBudgetIdAndUntilDate(@Param("budgetId") long budgetId, @Param("untilDate") Date until);

    @Query("select case when (count(*) = 0) then 0.0 else (cast(sum(record.minutes) AS double) / 60.0) end from WorkRecordEntity record where record.budget.id=:budgetId and :fromDate <= record.date and record.date <= :untilDate")
    Double getTotalHoursInTimeRange(@Param("budgetId") long budgetId, @Param("fromDate") Date fromDate, @Param("untilDate") Date untilDate);

    @Override
    @Modifying
    @Query("delete from WorkRecordEntity r where r.importRecord.id = :importId")
    void deleteByImport(@Param("importId") long importId);

    @Override
    @Modifying
    @Query("delete from WorkRecordEntity r where r.id in (select p.id from WorkRecordEntity p where p.importRecord.project.id = :projectId )")
    void deleteByImportAndProjectId(@Param("projectId") long projectId);


    @Override
    @Modifying
    @Query("delete from WorkRecordEntity r where r.budget.id in ( select b.id from BudgetEntity b where b.project.id = :projectId)")
    void deleteByProjectId(@Param("projectId") long projectId);

    @Query("select new org.wickedsource.budgeteer.persistence.record.MissingDailyRateBean(r.person.id, r.person.name, min(r.date), max(r.date)) from WorkRecordEntity r where r.dailyRate = 0 and r.person.project.id = :projectId group by r.person.id, r.person.name")
    List<MissingDailyRateBean> getMissingDailyRatesForProject(@Param("projectId") long projectId);

    @Query("select new org.wickedsource.budgeteer.persistence.record.MissingDailyRateForBudgetBean(r.person.id, r.person.name, min(r.date), max(r.date), b.name, b.id) from WorkRecordEntity r join r.budget b where r.dailyRate = 0 and r.person.id = :personId group by r.person.id, r.person.name, b.name, b.id")
    List<MissingDailyRateForBudgetBean> getMissingDailyRatesForPerson(@Param("personId") long personId);

    @Override
    @Modifying
    @Query("update WorkRecordEntity r set r.dailyRate = :dailyRate where r.editedManually = false AND r.budget.id=:budgetId and r.person.id=:personId and r.date between :fromDate and :toDate")
    void updateDailyRates(@Param("budgetId") long budgetId, @Param("personId") long personId, @Param("fromDate") Date fromDate, @Param("toDate") Date toDate, @Param("dailyRate") Money dailyRate);

    @Override
    @Query("select new org.wickedsource.budgeteer.persistence.record.WeeklyAggregatedRecordBean(r.year, r.week, sum(r.minutes) / 60.0, sum(r.minutes * r.dailyRate) / 60 / 8 ) from WorkRecordEntity r where r.person.id=:personId group by r.year, r.week order by r.year, r.week")
    List<WeeklyAggregatedRecordBean> aggregateByWeekAndPerson(@Param("personId") long personId);

    @Override
    @Query("select new org.wickedsource.budgeteer.persistence.record.MonthlyAggregatedRecordBean(r.year, r.month, sum(r.minutes) / 60.0, sum(r.minutes * r.dailyRate) / 60 / 8 ) from WorkRecordEntity r where r.person.id=:personId group by r.year, r.month order by r.year, r.month")
    List<MonthlyAggregatedRecordBean> aggregateByMonthAndPerson(@Param("personId") long personId);

    @Override
    @Query("select new org.wickedsource.budgeteer.persistence.record.WeeklyAggregatedRecordBean(r.year, r.week, sum(r.minutes) / 60.0, sum(r.minutes * r.dailyRate) / 60 / 8 ) from WorkRecordEntity r where r.budget.id=:budgetId group by r.year, r.week order by r.year, r.week")
    List<WeeklyAggregatedRecordBean> aggregateByWeekAndBudget(@Param("budgetId") long budgetId);

    @Override
    @Query("select new org.wickedsource.budgeteer.persistence.record.WeeklyAggregatedRecordWithTaxBean(r.year, r.month, r.week, sum(r.minutes), r.dailyRate, r.budget.contract.taxRate) from WorkRecordEntity r where r.budget.id=:budgetId group by r.year, r.month, r.week, r.dailyRate, r.budget.contract.taxRate order by r.year, r.month, r.week")
    List<WeeklyAggregatedRecordWithTaxBean> aggregateByWeekAndBudgetWithTax(@Param("budgetId") long budgetId);

    @Override
    @Query("select new org.wickedsource.budgeteer.persistence.record.MonthlyAggregatedRecordBean(r.year, r.month, sum(r.minutes) / 60.0, sum(r.minutes * r.dailyRate) / 60 / 8 ) from WorkRecordEntity r where r.budget.id=:budgetId group by r.year, r.month order by r.year, r.month")
    List<MonthlyAggregatedRecordBean> aggregateByMonthAndBudget(@Param("budgetId") long budgetId);

    @Override
    @Query("select new org.wickedsource.budgeteer.persistence.record.MonthlyAggregatedRecordWithTaxBean(r.year, r.month, sum(r.minutes), r.dailyRate, r.budget.contract.taxRate ) from WorkRecordEntity r where r.budget.id=:budgetId group by r.year, r.month, r.dailyRate, r.budget.contract.taxRate order by r.year, r.month")
    List<MonthlyAggregatedRecordWithTaxBean> aggregateByMonthAndBudgetWithTax(@Param("budgetId") long budgetId);

    @Override
    @Query("select new org.wickedsource.budgeteer.persistence.record.WeeklyAggregatedRecordBean(r.year, r.week, sum(r.minutes) / 60.0, sum(r.minutes * r.dailyRate) / 60 / 8 ) from WorkRecordEntity r join r.budget b join b.tags t where b.project.id=:projectId and t.tag in (:tags) group by r.year, r.week order by r.year, r.week")
    List<WeeklyAggregatedRecordBean> aggregateByWeekAndBudgetTags(@Param("projectId") long projectId, @Param("tags") List<String> tags);

    @Override
    @Query("select new org.wickedsource.budgeteer.persistence.record.WeeklyAggregatedRecordWithTaxBean(r.year, r.month, r.week, sum(r.minutes), r.dailyRate, r.budget.contract.taxRate) from WorkRecordEntity r join r.budget b join b.tags t where b.project.id=:projectId and t.tag in (:tags) group by r.year, r.month, r.week, r.dailyRate, r.budget.contract.taxRate order by r.year, r.month, r.week")
    List<WeeklyAggregatedRecordWithTaxBean> aggregateByWeekAndBudgetTagsWithTax(@Param("projectId") long projectId, @Param("tags") List<String> tags);


    @Override
    @Query("select new org.wickedsource.budgeteer.persistence.record.MonthlyAggregatedRecordBean(r.year, r.month, sum(r.minutes) / 60.0, sum(r.minutes * r.dailyRate) / 60 / 8 ) from WorkRecordEntity r join r.budget b join b.tags t where b.project.id=:projectId and t.tag in (:tags) group by r.year, r.month order by r.year, r.month")
    List<MonthlyAggregatedRecordBean> aggregateByMonthAndBudgetTags(@Param("projectId") long projectId, @Param("tags") List<String> tags);

    @Override
    @Query("select new org.wickedsource.budgeteer.persistence.record.MonthlyAggregatedRecordWithTaxBean(r.year, r.month, sum(r.minutes), r.dailyRate, r.budget.contract.taxRate) from WorkRecordEntity r join r.budget b join b.tags t where b.project.id=:projectId and t.tag in (:tags) group by r.year, r.month, r.dailyRate, r.budget.contract.taxRate order by r.year, r.month")
    List<MonthlyAggregatedRecordWithTaxBean> aggregateByMonthAndBudgetTagsWithTax(@Param("projectId") long projectId, @Param("tags") List<String> tags);

    @Override
    @Query("select new org.wickedsource.budgeteer.persistence.record.WeeklyAggregatedRecordBean(r.year, r.week, sum(r.minutes) / 60.0, sum(r.minutes * r.dailyRate) / 60 / 8 ) from WorkRecordEntity r where r.budget.project.id=:projectId and r.date >= :startDate group by r.year, r.week order by r.year, r.week")
    List<WeeklyAggregatedRecordBean> aggregateByWeekForProject(@Param("projectId") long projectId, @Param("startDate") Date start);

    @Query("select new org.wickedsource.budgeteer.persistence.record.DailyAverageRateBean(r.year, r.month, r.day, avg(r.dailyRate)) from WorkRecordEntity r where r.budget.project.id = :projectId and r.date >= :startDate group by r.year, r.month, r.day order by r.year, r.month, r.day")
    List<DailyAverageRateBean> getAverageDailyRatesPerDay(@Param("projectId") long projectId, @Param("startDate") Date startDate);

    @Query("select new org.wickedsource.budgeteer.persistence.record.ShareBean(b.name, sum(r.minutes * r.dailyRate) / 60 / 8) from WorkRecordEntity r join r.budget b where r.person.id = :personId group by b.name")
    List<ShareBean> getBudgetShareForPerson(@Param("personId") long personId);

    @Query("select new org.wickedsource.budgeteer.persistence.record.ShareBean(p.name, sum(r.minutes * r.dailyRate) / 60 / 8) from WorkRecordEntity r join r.person p where r.budget.id = :budgetId group by p.name")
    List<ShareBean> getPersonShareForBudget(@Param("budgetId") long budgetId);

    @Query("select new org.wickedsource.budgeteer.persistence.record.WeeklyAggregatedRecordBean(r.year, r.week, sum(r.minutes) / 60.0, sum(r.minutes * r.dailyRate) / 60 / 8 ) from WorkRecordEntity r where r.person.id=:personId and r.date >= :startDate group by r.year, r.week order by r.year, r.week")
    List<WeeklyAggregatedRecordBean> aggregateByWeekForPerson(@Param("personId") long personId, @Param("startDate") Date start);

    @Override
    @Query("select new org.wickedsource.budgeteer.persistence.record.WeeklyAggregatedRecordWithTitleBean(r.year, r.week, sum(r.minutes) / 60.0, sum(r.minutes * r.dailyRate) / 60 / 8, b.name ) from WorkRecordEntity r join r.budget b where r.person.id=:personId and r.date >= :startDate group by r.year, r.week, b.name order by b.name, r.year, r.week")
    List<WeeklyAggregatedRecordWithTitleBean> aggregateByWeekAndBudgetForPerson(@Param("personId") long personId, @Param("startDate") Date start);

    @Override
    @Query("select new org.wickedsource.budgeteer.persistence.record.WeeklyAggregatedRecordWithTitleBean(r.year, r.week, sum(r.minutes) / 60.0, sum(r.minutes * r.dailyRate) / 60 / 8, p.name ) from WorkRecordEntity r join r.person p where r.budget.id=:budgetId and r.date >= :startDate group by r.year, r.week, p.name order by p.name, r.year, r.week")
    List<WeeklyAggregatedRecordWithTitleBean> aggregateByWeekAndPersonForBudget(@Param("budgetId") long budgetId, @Param("startDate") Date startDate);

    @Override
    @Query("select new org.wickedsource.budgeteer.persistence.record.WeeklyAggregatedRecordWithTitleAndTaxBean(r.year, r.month, r.week, sum(r.minutes), r.dailyRate, r.budget.contract.taxRate, p.name ) from WorkRecordEntity r join r.person p where r.budget.id=:budgetId and r.date >= :startDate group by r.year, r.month, r.week, r.dailyRate, r.budget.contract.taxRate, p.name order by p.name, r.year, r.week")
    List<WeeklyAggregatedRecordWithTitleAndTaxBean> aggregateByWeekAndPersonForBudgetWithTax(@Param("budgetId") long budgetId, @Param("startDate") Date startDate);

    @Override
    @Query("select new org.wickedsource.budgeteer.persistence.record.WeeklyAggregatedRecordWithTitleAndTaxBean(r.year, r.month, r.week, sum(r.minutes), r.dailyRate, r.budget.contract.taxRate, p.name ) from WorkRecordEntity r join r.person p where r.budget.id=:budgetId group by r.year, r.month, r.week, r.dailyRate, r.budget.contract.taxRate, p.name order by p.name, r.year, r.week")
    List<WeeklyAggregatedRecordWithTitleAndTaxBean> aggregateByWeekAndPersonForBudgetWithTax(@Param("budgetId") long budgetId);

    @Override
    @Query("select new org.wickedsource.budgeteer.persistence.record.WeeklyAggregatedRecordBean(r.year, r.week, sum(r.minutes) / 60.0, sum(r.minutes * r.dailyRate) / 60 / 8 ) from WorkRecordEntity r where r.budget.id=:budgetId and r.date >= :startDate group by r.year, r.week order by r.year, r.week")
    List<WeeklyAggregatedRecordBean> aggregateByWeekForBudget(@Param("budgetId") long budgetId, @Param("startDate") Date start);

    @Override
    @Query("select new org.wickedsource.budgeteer.persistence.record.WeeklyAggregatedRecordWithTaxBean(r.year, r.week, sum(r.minutes) / 60.0, sum(r.minutes * r.dailyRate) / 60 / 8, r.budget.contract.taxRate ) from WorkRecordEntity r where r.budget.id=:budgetId and r.date >= :startDate group by r.year, r.week, r.budget.contract.taxRate order by r.year, r.week")
    List<WeeklyAggregatedRecordWithTaxBean> aggregateByWeekForBudgetWithTax(@Param("budgetId") long budgetId, @Param("startDate") Date start);

    @Override
    @Query("select new org.wickedsource.budgeteer.persistence.record.WeeklyAggregatedRecordBean(r.year, r.week, sum(r.minutes) / 60.0, sum(r.minutes * r.dailyRate) / 60 / 8 ) from WorkRecordEntity r where r.budget.project.id=:projectId and r.date >= :startDate group by r.year, r.week order by r.year, r.week")
    List<WeeklyAggregatedRecordBean> aggregateByWeekForBudgets(@Param("projectId") long projectId, @Param("startDate") Date start);

    @Override
    @Query("select new org.wickedsource.budgeteer.persistence.record.WeeklyAggregatedRecordBean(r.year, r.week, sum(r.minutes) / 60.0, sum(r.minutes * r.dailyRate) / 60 / 8 ) from WorkRecordEntity r where r.budget.project.id=:projectId group by r.year, r.week order by r.year, r.week")
    List<WeeklyAggregatedRecordBean> aggregateByWeek(@Param("projectId") long projectId);

    @Override
    @Query("select new org.wickedsource.budgeteer.persistence.record.WeeklyAggregatedRecordWithTaxBean(r.year, r.month, r.week, sum(r.minutes), r.dailyRate, r.budget.contract.taxRate ) from WorkRecordEntity r where r.budget.project.id=:projectId group by r.year, r.month, r.week, r.dailyRate, r.budget.contract.taxRate order by r.year, r.month, r.week, r.budget.contract.taxRate")
    List<WeeklyAggregatedRecordWithTaxBean> aggregateByWeekWithTax(@Param("projectId") long projectId);

    @Override
    @Query("select new org.wickedsource.budgeteer.persistence.record.WeeklyAggregatedRecordWithTitleBean(r.year, r.week, sum(r.minutes) / 60.0, sum(r.minutes * r.dailyRate) / 60 / 8, p.name ) from WorkRecordEntity r join r.person p join r.budget b join b.tags t where b.project.id=:projectId and t.tag in (:tags) and r.date >= :startDate group by r.year, r.week, p.name order by p.name, r.year, r.week")
    List<WeeklyAggregatedRecordWithTitleBean> aggregateByWeekAndPersonForBudgets(@Param("projectId") long projectId, @Param("tags") List<String> tags, @Param("startDate") Date startDate);

    @Override
    @Query("select new org.wickedsource.budgeteer.persistence.record.WeeklyAggregatedRecordWithTitleBean(r.year, r.week, sum(r.minutes) / 60.0, sum(r.minutes * r.dailyRate) / 60 / 8, p.name ) from WorkRecordEntity r join r.person p join r.budget b where b.project.id=:projectId and r.date >= :startDate group by r.year, r.week, p.name order by p.name, r.year, r.week")
    List<WeeklyAggregatedRecordWithTitleBean> aggregateByWeekAndPersonForBudgets(@Param("projectId") long projectId, @Param("startDate") Date startDate);

    @Override
    @Query("select new org.wickedsource.budgeteer.persistence.record.WeeklyAggregatedRecordWithTitleAndTaxBean(r.year, r.month, r.week, sum(r.minutes), r.dailyRate, r.budget.contract.taxRate, p.name ) from WorkRecordEntity r join r.person p join r.budget b join b.tags t where b.project.id=:projectId and t.tag in (:tags) and r.date >= :startDate group by r.year, r.month, r.week, r.dailyRate, r.budget.contract.taxRate, p.name order by p.name, r.year, r.week")
    List<WeeklyAggregatedRecordWithTitleAndTaxBean> aggregateByWeekAndPersonForBudgetsWithTax(@Param("projectId") long projectId, @Param("tags") List<String> tags, @Param("startDate") Date startDate);

    @Override
    @Query("select new org.wickedsource.budgeteer.persistence.record.WeeklyAggregatedRecordWithTitleAndTaxBean(r.year, r.month, r.week, sum(r.minutes), r.dailyRate, r.budget.contract.taxRate, p.name ) from WorkRecordEntity r join r.person p join r.budget b join b.tags t where b.project.id=:projectId and t.tag in (:tags) group by r.year, r.month, r.week, r.dailyRate, r.budget.contract.taxRate, p.name order by p.name, r.year, r.week")
    List<WeeklyAggregatedRecordWithTitleAndTaxBean> aggregateByWeekAndPersonForBudgetsWithTax(@Param("projectId") long projectId, @Param("tags") List<String> tags);

    @Override
    @Query("select new org.wickedsource.budgeteer.persistence.record.WeeklyAggregatedRecordWithTitleAndTaxBean(r.year, r.month, r.week, sum(r.minutes), r.dailyRate, r.budget.contract.taxRate, p.name ) from WorkRecordEntity r join r.person p join r.budget b where b.project.id=:projectId and r.date >= :startDate group by r.year, r.month, r.week, r.dailyRate, r.budget.contract.taxRate, p.name order by p.name, r.year, r.week")
    List<WeeklyAggregatedRecordWithTitleAndTaxBean> aggregateByWeekAndPersonForBudgetsWithTax(@Param("projectId") long projectId, @Param("startDate") Date startDate);

    @Override
    @Query("select new org.wickedsource.budgeteer.persistence.record.WeeklyAggregatedRecordWithTitleAndTaxBean(r.year, r.month, r.week, sum(r.minutes), r.dailyRate, r.budget.contract.taxRate, p.name ) from WorkRecordEntity r join r.person p join r.budget b where b.project.id=:projectId group by r.year, r.month, r.week, r.dailyRate, r.budget.contract.taxRate, p.name order by p.name, r.year, r.week")
    List<WeeklyAggregatedRecordWithTitleAndTaxBean> aggregateByWeekAndPersonForBudgetsWithTax(@Param("projectId") long projectId);

    @Override
    @Query("select new org.wickedsource.budgeteer.persistence.record.WeeklyAggregatedRecordBean(r.year, r.week, sum(r.minutes) / 60.0, sum(r.minutes * r.dailyRate) / 60 / 8 ) from WorkRecordEntity r join r.budget b join b.tags t where b.project.id=:projectId and t.tag in (:tags) and r.date >= :startDate group by r.year, r.week order by r.year, r.week")
    List<WeeklyAggregatedRecordBean> aggregateByWeekForBudgets(@Param("projectId") long projectId, @Param("tags") List<String> tags, @Param("startDate") Date start);

    @Override
    @Query("select new org.wickedsource.budgeteer.persistence.record.WeeklyAggregatedRecordWithTaxBean(r.year, r.week, sum(r.minutes) / 60.0, sum(r.minutes * r.dailyRate) / 60 / 8, r.budget.contract.taxRate ) from WorkRecordEntity r join r.budget b join b.tags t where b.project.id=:projectId and t.tag in (:tags) and r.date >= :startDate group by r.year, r.week, r.budget.contract.taxRate order by r.year, r.week")
    List<WeeklyAggregatedRecordWithTaxBean> aggregateByWeekForBudgetsWithTax(@Param("projectId") long projectId, @Param("tags") List<String> tags, @Param("startDate") Date start);

    @Override
    @Query("select new org.wickedsource.budgeteer.persistence.record.WeeklyAggregatedRecordWithTaxBean(r.year, r.week, sum(r.minutes) / 60.0, sum(r.minutes * r.dailyRate) / 60 / 8, r.budget.contract.taxRate ) from WorkRecordEntity r join r.budget b where b.project.id=:projectId and r.date >= :startDate group by r.year, r.week, r.budget.contract.taxRate order by r.year, r.week")
    List<WeeklyAggregatedRecordWithTaxBean> aggregateByWeekForBudgetsWithTax(@Param("projectId") long projectId, @Param("startDate") Date start);

    @Override
    @Query("select new org.wickedsource.budgeteer.persistence.record.MonthlyAggregatedRecordBean(r.year, r.month, sum(r.minutes) / 60.0, sum(r.minutes * r.dailyRate) / 60 / 8 ) from WorkRecordEntity r where r.person.id=:personId and r.date >= :startDate group by r.year, r.month order by r.year, r.month")
    List<MonthlyAggregatedRecordBean> aggregateByMonthForPerson(@Param("personId") long personId, @Param("startDate") Date startDate);

    @Override
    @Query("select new org.wickedsource.budgeteer.persistence.record.MonthlyAggregatedRecordWithTitleBean(r.year, r.month, sum(r.minutes) / 60.0, sum(r.minutes * r.dailyRate) / 60 / 8, b.name ) from WorkRecordEntity r join r.budget b where r.person.id=:personId and r.date >= :startDate group by r.year, r.month, b.name order by b.name, r.year, r.month")
    List<MonthlyAggregatedRecordWithTitleBean> aggregateByMonthAndBudgetForPerson(@Param("personId") long personId, @Param("startDate") Date startDate);

    @Override
    @Query("select new org.wickedsource.budgeteer.persistence.record.MonthlyAggregatedRecordBean(r.year, r.month, sum(r.minutes) / 60.0, sum(r.minutes * r.dailyRate) / 60 / 8 ) from WorkRecordEntity r where r.budget.id=:budgetId and r.date >= :startDate group by r.year, r.month order by r.year, r.month")
    List<MonthlyAggregatedRecordBean> aggregateByMonthForBudget(@Param("budgetId") long budgetId, @Param("startDate") Date startDate);

    @Override
    @Query("select new org.wickedsource.budgeteer.persistence.record.MonthlyAggregatedRecordWithTaxBean(r.year, r.month, sum(r.minutes) / 60.0, sum(r.minutes * r.dailyRate) / 60 / 8, r.budget.contract.taxRate) from WorkRecordEntity r where r.budget.id=:budgetId and r.date >= :startDate group by r.year, r.month order by r.year, r.month")
    List<MonthlyAggregatedRecordWithTaxBean> aggregateByMonthForBudgetWithTax(@Param("budgetId") long budgetId, @Param("startDate") Date startDate);

    @Override
    @Query("select new org.wickedsource.budgeteer.persistence.record.MonthlyAggregatedRecordWithTitleBean(r.year, r.month, sum(r.minutes) / 60.0, sum(r.minutes * r.dailyRate) / 60 / 8, p.name ) from WorkRecordEntity r join r.person p where r.budget.id=:budgetId and r.date >= :startDate group by r.year, r.month, p.name order by p.name, r.year, r.month")
    List<MonthlyAggregatedRecordWithTitleBean> aggregateByMonthAndPersonForBudget(@Param("budgetId") long budgetId, @Param("startDate") Date startDate);

    @Override
    @Query("select new org.wickedsource.budgeteer.persistence.record.MonthlyAggregatedRecordWithTitleAndTaxBean(r.year, r.month, sum(r.minutes), r.dailyRate, p.name, r.budget.contract.taxRate ) from WorkRecordEntity r join r.person p where r.budget.id=:budgetId and r.date >= :startDate group by r.year, r.month, r.dailyRate, p.name, r.budget.contract.taxRate order by p.name, r.year, r.month")
    List<MonthlyAggregatedRecordWithTitleAndTaxBean> aggregateByMonthAndPersonForBudgetWithTax(@Param("budgetId") long budgetId, @Param("startDate") Date startDate);

    @Override
    @Query("select new org.wickedsource.budgeteer.persistence.record.MonthlyAggregatedRecordWithTitleAndTaxBean(r.year, r.month, sum(r.minutes), r.dailyRate, p.name, r.budget.contract.taxRate ) from WorkRecordEntity r join r.person p where r.budget.id=:budgetId group by r.year, r.month, r.dailyRate, p.name, r.budget.contract.taxRate order by p.name, r.year, r.month")
    List<MonthlyAggregatedRecordWithTitleAndTaxBean> aggregateByMonthAndPersonForBudgetWithTax(@Param("budgetId") long budgetId);

    @Override
    @Query("select new org.wickedsource.budgeteer.persistence.record.MonthlyAggregatedRecordBean(r.year, r.month, sum(r.minutes) / 60.0, sum(r.minutes * r.dailyRate) / 60 / 8 ) from WorkRecordEntity r join r.budget b join b.tags t where b.project.id=:projectId and t.tag in (:tags) and r.date >= :startDate group by r.year, r.month order by r.year, r.month")
    List<MonthlyAggregatedRecordBean> aggregateByMonthForBudgets(@Param("projectId") long projectId, @Param("tags") List<String> tags, @Param("startDate") Date startDate);

    @Override
    @Query("select new org.wickedsource.budgeteer.persistence.record.MonthlyAggregatedRecordBean(r.year, r.month, sum(r.minutes) / 60.0, sum(r.minutes * r.dailyRate) / 60 / 8 ) from WorkRecordEntity r join r.budget b where b.project.id=:projectId and r.date >= :startDate group by r.year, r.month order by r.year, r.month")
    List<MonthlyAggregatedRecordBean> aggregateByMonthForBudgets(@Param("projectId") long projectId, @Param("startDate") Date startDate);

    @Override
    @Query("select new org.wickedsource.budgeteer.persistence.record.MonthlyAggregatedRecordWithTaxBean(r.year, r.month, sum(r.minutes) / 60.0, sum(r.minutes * r.dailyRate) / 60 / 8, r.budget.contract.taxRate ) from WorkRecordEntity r join r.budget b join b.tags t where b.project.id=:projectId and t.tag in (:tags) and r.date >= :startDate group by r.year, r.month, r.budget.contract.taxRate order by r.year, r.month")
    List<MonthlyAggregatedRecordWithTaxBean> aggregateByMonthForBudgetsWithTax(@Param("projectId") long projectId, @Param("tags") List<String> tags, @Param("startDate") Date startDate);

    @Override
    @Query("select new org.wickedsource.budgeteer.persistence.record.MonthlyAggregatedRecordWithTaxBean(r.year, r.month, sum(r.minutes) / 60.0, sum(r.minutes * r.dailyRate) / 60 / 8, r.budget.contract.taxRate ) from WorkRecordEntity r join r.budget b where b.project.id=:projectId and r.date >= :startDate group by r.year, r.month, r.budget.contract.taxRate order by r.year, r.month")
    List<MonthlyAggregatedRecordWithTaxBean> aggregateByMonthForBudgetsWithTax(@Param("projectId") long projectId, @Param("startDate") Date startDate);

    @Override
    @Query("select new org.wickedsource.budgeteer.persistence.record.MonthlyAggregatedRecordBean(r.year, r.month, sum(r.minutes) / 60.0, sum(r.minutes * r.dailyRate) / 60 / 8 ) from WorkRecordEntity r join r.budget b where b.project.id=:projectId group by r.year, r.month order by r.year, r.month")
    List<MonthlyAggregatedRecordBean> aggregateByMonth(@Param("projectId") long projectId);

    @Override
    @Query("select new org.wickedsource.budgeteer.persistence.record.MonthlyAggregatedRecordWithTaxBean(r.year, r.month, sum(r.minutes), r.dailyRate, r.budget.contract.taxRate) from WorkRecordEntity r join r.budget b where b.project.id=:projectId group by r.year, r.month, r.dailyRate, r.budget.contract.taxRate order by r.year, r.month")
    List<MonthlyAggregatedRecordWithTaxBean> aggregateByMonthWithTax(@Param("projectId") long projectId);

    @Override
    @Query("select new org.wickedsource.budgeteer.persistence.record.MonthlyAggregatedRecordWithTitleBean(r.year, r.month, sum(r.minutes) / 60.0, sum(r.minutes * r.dailyRate) / 60 / 8, p.name ) from WorkRecordEntity r join r.person p join r.budget b join b.tags t where b.project.id=:projectId and t.tag in (:tags) and r.date >= :startDate group by r.year, r.month, p.name order by p.name, r.year, r.month")
    List<MonthlyAggregatedRecordWithTitleBean> aggregateByMonthAndPersonForBudgets(@Param("projectId") long projectId, @Param("tags") List<String> tags, @Param("startDate") Date startDate);

    @Override
    @Query("select new org.wickedsource.budgeteer.persistence.record.MonthlyAggregatedRecordWithTitleBean(r.year, r.month, sum(r.minutes) / 60.0, sum(r.minutes * r.dailyRate) / 60 / 8, p.name ) from WorkRecordEntity r join r.person p join r.budget b where b.project.id=:projectId and r.date >= :startDate group by r.year, r.month, p.name order by p.name, r.year, r.month")
    List<MonthlyAggregatedRecordWithTitleBean> aggregateByMonthAndPersonForBudgets(@Param("projectId") long projectId, @Param("startDate") Date startDate);

    @Override
    @Query("select new org.wickedsource.budgeteer.persistence.record.MonthlyAggregatedRecordWithTitleAndTaxBean(r.year, r.month, sum(r.minutes), r.dailyRate, p.name, r.budget.contract.taxRate ) from WorkRecordEntity r join r.person p join r.budget b join b.tags t where b.project.id=:projectId and t.tag in (:tags) and r.date >= :startDate group by r.year, r.month, p.name, r.dailyRate, r.budget.contract.taxRate order by p.name, r.year, r.month")
    List<MonthlyAggregatedRecordWithTitleAndTaxBean> aggregateByMonthAndPersonForBudgetsWithTax(@Param("projectId") long projectId, @Param("tags") List<String> tags, @Param("startDate") Date startDate);

    @Override
    @Query("select new org.wickedsource.budgeteer.persistence.record.MonthlyAggregatedRecordWithTitleAndTaxBean(r.year, r.month, sum(r.minutes), r.dailyRate, p.name, r.budget.contract.taxRate ) from WorkRecordEntity r join r.person p join r.budget b join b.tags t where b.project.id=:projectId and t.tag in (:tags) group by r.year, r.month, p.name, r.dailyRate, r.budget.contract.taxRate order by p.name, r.year, r.month")
    List<MonthlyAggregatedRecordWithTitleAndTaxBean> aggregateByMonthAndPersonForBudgetsWithTax(@Param("projectId") long projectId, @Param("tags") List<String> tags);

    @Override
    @Query("select new org.wickedsource.budgeteer.persistence.record.MonthlyAggregatedRecordWithTitleAndTaxBean(r.year, r.month, sum(r.minutes), r.dailyRate, p.name, r.budget.contract.taxRate ) from WorkRecordEntity r join r.person p join r.budget b where b.project.id=:projectId and r.date >= :startDate group by r.year, r.month, p.name, r.dailyRate, r.budget.contract.taxRate order by p.name, r.year, r.month")
    List<MonthlyAggregatedRecordWithTitleAndTaxBean> aggregateByMonthAndPersonForBudgetsWithTax(@Param("projectId") long projectId, @Param("startDate") Date startDate);

    @Override
    @Query("select new org.wickedsource.budgeteer.persistence.record.MonthlyAggregatedRecordWithTitleAndTaxBean(r.year, r.month, sum(r.minutes), r.dailyRate, p.name, r.budget.contract.taxRate ) from WorkRecordEntity r join r.person p join r.budget b where b.project.id=:projectId group by r.year, r.month, p.name, r.dailyRate, r.budget.contract.taxRate order by p.name, r.year, r.month")
    List<MonthlyAggregatedRecordWithTitleAndTaxBean> aggregateByMonthAndPersonForBudgetsWithTax(@Param("projectId") long projectId);

    @Override
    @Query("select count (wre.id) from WorkRecordEntity wre where wre.budget.project.id = :projectId")
    Long countByProjectId(@Param("projectId") long projectId);

    @Query("select wr from WorkRecordEntity wr where wr.budget.project.id = :projectId AND wr.editedManually = true AND wr.date >= :startDate AND wr.date <= :endDate")
    List<WorkRecordEntity> findManuallyEditedEntries(@Param("projectId") long projectId, @Param("startDate") Date earliestRecordDate, @Param("endDate") Date latestRecordDate);

    @Query("select wr from WorkRecordEntity wr where wr.budget = :budget AND wr.person = :person AND wr.date = :recordDate AND wr.minutes = :workedMinutes AND wr.editedManually = false")
    List<WorkRecordEntity> findDuplicateEntries(@Param("budget") BudgetEntity budget, @Param("person") PersonEntity person, @Param("recordDate") Date recordDate, @Param("workedMinutes") int workedMinutes);

    @Query("select wr from WorkRecordEntity wr where wr.budget.project = :project AND wr.date >= :start and wr.date <= :end")
    List<WorkRecordEntity> findByProjectAndDateRange(@Param("project") ProjectEntity project, @Param("start") Date start, @Param("end") Date end);

    @Override
    @Query("select wr from WorkRecordEntity wr where wr.budget.project.id = :projectId")
    List<WorkRecordEntity> findByProjectId(@Param("projectId") long projectId);

    @Override
    @Query("select new org.wickedsource.budgeteer.persistence.record.WeeklyAggregatedRecordWithTaxBean(r.year, r.month, r.week, sum(r.minutes), r.dailyRate, r.budget.contract.taxRate ) from WorkRecordEntity r join r.budget b where b.project.id=:projectId group by r.year, r.month, r.week, r.dailyRate, r.budget.contract.taxRate order by r.year, r.month, r.week")
    List<WeeklyAggregatedRecordWithTaxBean> aggregateByWeekForBudgetsWithTax(@Param("projectId") long projectId);

    @Override
    @Query("select new org.wickedsource.budgeteer.persistence.record.WeeklyAggregatedRecordWithTaxBean(r.year, r.month, r.week, sum(r.minutes), r.dailyRate, r.budget.contract.taxRate ) from WorkRecordEntity r join r.budget b join b.tags t where b.project.id=:projectId and t.tag in (:tags) group by r.year, r.month, r.week, r.dailyRate, r.budget.contract.taxRate order by r.year, r.month, r.week")
    List<WeeklyAggregatedRecordWithTaxBean> aggregateByWeekForBudgetsWithTax(@Param("projectId") long projectId, @Param("tags") List<String> tags);

    @Override
    @Query("select new org.wickedsource.budgeteer.persistence.record.WeeklyAggregatedRecordWithTaxBean(r.year, r.month, r.week, sum(r.minutes), r.dailyRate, r.budget.contract.taxRate ) from WorkRecordEntity r where r.budget.id=:budgetId group by r.year, r.month, r.week, r.dailyRate, r.budget.contract.taxRate order by r.year, r.week")
    List<WeeklyAggregatedRecordWithTaxBean> aggregateByWeekForBudgetWithTax(@Param("budgetId") long budgetId);

    @Override
    @Query("select new org.wickedsource.budgeteer.persistence.record.MonthlyAggregatedRecordWithTaxBean(r.year, r.month, sum(r.minutes), r.dailyRate, r.budget.contract.taxRate ) from WorkRecordEntity r where r.budget.id=:budgetId group by r.year, r.month, r.dailyRate, r.budget.contract.taxRate order by r.year, r.month")
    List<MonthlyAggregatedRecordWithTaxBean> aggregateByMonthForBudgetWithTax(@Param("budgetId") long budgetId);

    @Override
    @Query("select new org.wickedsource.budgeteer.persistence.record.MonthlyAggregatedRecordWithTaxBean(r.year, r.month, sum(r.minutes), r.dailyRate, r.budget.contract.taxRate ) from WorkRecordEntity r join r.budget b where b.project.id=:projectId group by r.year, r.month, r.dailyRate, r.budget.contract.taxRate order by r.year, r.month")
    List<MonthlyAggregatedRecordWithTaxBean> aggregateByMonthForBudgetsWithTax(@Param("projectId") long projectId);

    @Override
    @Query("select new org.wickedsource.budgeteer.persistence.record.MonthlyAggregatedRecordWithTaxBean(r.year, r.month, sum(r.minutes), r.dailyRate, r.budget.contract.taxRate ) from WorkRecordEntity r join r.budget b join b.tags t where b.project.id=:projectId and t.tag in (:tags) group by r.year, r.month, r.dailyRate, r.budget.contract.taxRate order by r.year, r.month")
    List<MonthlyAggregatedRecordWithTaxBean> aggregateByMonthForBudgetsWithTax(@Param("projectId") long projectId, @Param("tags") List<String> tags);
}
