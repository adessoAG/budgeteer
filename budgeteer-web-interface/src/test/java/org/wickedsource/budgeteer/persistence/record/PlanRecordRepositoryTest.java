package org.wickedsource.budgeteer.persistence.record;

import com.github.springtestdbunit.annotation.DatabaseOperation;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.github.springtestdbunit.annotation.DatabaseTearDown;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.wickedsource.budgeteer.IntegrationTestTemplate;
import org.wickedsource.budgeteer.MoneyUtil;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;

class PlanRecordRepositoryTest extends IntegrationTestTemplate {

    private DateFormat format = new SimpleDateFormat("dd.MM.yyyy");

    @Autowired
    private PlanRecordRepository repository;

    @Test
    @DatabaseSetup("getPlannedBudget.xml")
    @DatabaseTearDown(value = "getPlannedBudget.xml", type = DatabaseOperation.DELETE_ALL)
    void testGetPlannedBudget() throws Exception {
        double value = repository.getPlannedBudget(1L);
        Assertions.assertEquals(170000d, value, 1d);
    }

    @Test
    @DatabaseSetup("updateDailyRates.xml")
    @DatabaseTearDown(value = "updateDailyRates.xml", type = DatabaseOperation.DELETE_ALL)
    void testUpdatePlanRecordDailyRates() throws Exception {
        repository.updateDailyRates(1L, 1L, format.parse("01.01.2015"), format.parse("15.08.2015"), MoneyUtil.createMoneyFromCents(50000L));
        PlanRecordEntity record = repository.findById(1L).orElseThrow(RuntimeException::new);
        Assertions.assertEquals(MoneyUtil.createMoneyFromCents(50000L), record.getDailyRate());
        record = repository.findById(3L).orElseThrow(RuntimeException::new);
        Assertions.assertEquals(MoneyUtil.createMoneyFromCents(50000L), record.getDailyRate());
    }

    @Test
    @DatabaseSetup("aggregateByWeekAndPerson.xml")
    @DatabaseTearDown(value = "aggregateByWeekAndPerson.xml", type = DatabaseOperation.DELETE_ALL)
    void testAggregateByWeekAndPerson() {
        List<WeeklyAggregatedRecordBean> records = repository.aggregateByWeekAndPerson(1L);
        Assertions.assertEquals(3, records.size());

        Assertions.assertEquals(2014, records.get(0).getYear());
        Assertions.assertEquals(1, records.get(0).getWeek());
        Assertions.assertEquals(8d, records.get(0).getHours(), 0.1d);
        Assertions.assertEquals(10000L, records.get(0).getValueInCents());

        Assertions.assertEquals(2015, records.get(1).getYear());
        Assertions.assertEquals(1, records.get(1).getWeek());
        Assertions.assertEquals(16d, records.get(1).getHours(), 0.1d);
        Assertions.assertEquals(30000L, records.get(1).getValueInCents());

        Assertions.assertEquals(2015, records.get(2).getYear());
        Assertions.assertEquals(33, records.get(2).getWeek());
        Assertions.assertEquals(16d, records.get(2).getHours(), 0.1d);
        Assertions.assertEquals(0L, records.get(2).getValueInCents());
    }

    @Test
    @DatabaseSetup("aggregateByMonthAndPerson.xml")
    @DatabaseTearDown(value = "aggregateByMonthAndPerson.xml", type = DatabaseOperation.DELETE_ALL)
    void testAggregateByMonthAndPerson() {
        List<MonthlyAggregatedRecordBean> records = repository.aggregateByMonthAndPerson(1L);
        Assertions.assertEquals(3, records.size());

        Assertions.assertEquals(2014, records.get(0).getYear());
        Assertions.assertEquals(0, records.get(0).getMonth());
        Assertions.assertEquals(8d, records.get(0).getHours(), 0.1d);
        Assertions.assertEquals(10000L, records.get(0).getValueInCents());

        Assertions.assertEquals(2015, records.get(1).getYear());
        Assertions.assertEquals(0, records.get(1).getMonth());
        Assertions.assertEquals(16d, records.get(1).getHours(), 0.1d);
        Assertions.assertEquals(30000L, records.get(1).getValueInCents());

        Assertions.assertEquals(2015, records.get(2).getYear());
        Assertions.assertEquals(7, records.get(2).getMonth());
        Assertions.assertEquals(16d, records.get(2).getHours(), 0.1d);
        Assertions.assertEquals(0L, records.get(2).getValueInCents());
    }

    @Test
    @DatabaseSetup("aggregateByWeekAndBudget.xml")
    @DatabaseTearDown(value = "aggregateByWeekAndBudget.xml", type = DatabaseOperation.DELETE_ALL)
    void testAggregateByWeekAndBudget() {
        List<WeeklyAggregatedRecordBean> records = repository.aggregateByWeekAndBudget(1L);
        Assertions.assertEquals(3, records.size());

        Assertions.assertEquals(2014, records.get(0).getYear());
        Assertions.assertEquals(1, records.get(0).getWeek());
        Assertions.assertEquals(8d, records.get(0).getHours(), 0.1d);
        Assertions.assertEquals(10000L, records.get(0).getValueInCents());

        Assertions.assertEquals(2015, records.get(1).getYear());
        Assertions.assertEquals(1, records.get(1).getWeek());
        Assertions.assertEquals(16d, records.get(1).getHours(), 0.1d);
        Assertions.assertEquals(30000L, records.get(1).getValueInCents());

        Assertions.assertEquals(2015, records.get(2).getYear());
        Assertions.assertEquals(33, records.get(2).getWeek());
        Assertions.assertEquals(16d, records.get(2).getHours(), 0.1d);
        Assertions.assertEquals(0L, records.get(2).getValueInCents());
    }

    @Test
    @DatabaseSetup("aggregateByMonthAndBudget.xml")
    @DatabaseTearDown(value = "aggregateByMonthAndBudget.xml", type = DatabaseOperation.DELETE_ALL)
    void testAggregateByMonthAndBudget() {
        List<MonthlyAggregatedRecordBean> records = repository.aggregateByMonthAndBudget(1L);
        Assertions.assertEquals(3, records.size());

        Assertions.assertEquals(2014, records.get(0).getYear());
        Assertions.assertEquals(0, records.get(0).getMonth());
        Assertions.assertEquals(8d, records.get(0).getHours(), 0.1d);
        Assertions.assertEquals(10000L, records.get(0).getValueInCents());

        Assertions.assertEquals(2015, records.get(1).getYear());
        Assertions.assertEquals(0, records.get(1).getMonth());
        Assertions.assertEquals(16d, records.get(1).getHours(), 0.1d);
        Assertions.assertEquals(30000L, records.get(1).getValueInCents());

        Assertions.assertEquals(2015, records.get(2).getYear());
        Assertions.assertEquals(7, records.get(2).getMonth());
        Assertions.assertEquals(16d, records.get(2).getHours(), 0.1d);
        Assertions.assertEquals(0L, records.get(2).getValueInCents());
    }

    @Test
    @DatabaseSetup("aggregateByBudgetTags.xml")
    @DatabaseTearDown(value = "aggregateByBudgetTags.xml", type = DatabaseOperation.DELETE_ALL)
    void testAggregatedByWeekAndBudgetTags() {
        List<WeeklyAggregatedRecordBean> records = repository.aggregateByWeekAndBudgetTags(1L, Arrays.asList("tag1"));
        Assertions.assertEquals(2, records.size());

        Assertions.assertEquals(2014, records.get(0).getYear());
        Assertions.assertEquals(1, records.get(0).getWeek());
        Assertions.assertEquals(8d, records.get(0).getHours(), 0.1d);
        Assertions.assertEquals(10000L, records.get(0).getValueInCents());

        Assertions.assertEquals(2015, records.get(1).getYear());
        Assertions.assertEquals(1, records.get(1).getWeek());
        Assertions.assertEquals(16d, records.get(1).getHours(), 0.1d);
        Assertions.assertEquals(30000L, records.get(1).getValueInCents());

        records = repository.aggregateByWeekAndBudgetTags(1L, Arrays.asList("tag2"));
        Assertions.assertEquals(2, records.size());

        Assertions.assertEquals(2015, records.get(0).getYear());
        Assertions.assertEquals(1, records.get(0).getWeek());
        Assertions.assertEquals(16d, records.get(0).getHours(), 0.1d);
        Assertions.assertEquals(30000L, records.get(0).getValueInCents());

        Assertions.assertEquals(2015, records.get(1).getYear());
        Assertions.assertEquals(33, records.get(1).getWeek());
        Assertions.assertEquals(16d, records.get(1).getHours(), 0.1d);
        Assertions.assertEquals(0L, records.get(1).getValueInCents());
    }

    @Test
    @DatabaseSetup("aggregateByBudgetTags.xml")
    @DatabaseTearDown(value = "aggregateByBudgetTags.xml", type = DatabaseOperation.DELETE_ALL)
    void testAggregatedByMonthAndBudgetTags() {
        List<MonthlyAggregatedRecordBean> records = repository.aggregateByMonthAndBudgetTags(1L, Arrays.asList("tag1"));
        Assertions.assertEquals(2, records.size());

        Assertions.assertEquals(2014, records.get(0).getYear());
        Assertions.assertEquals(0, records.get(0).getMonth());
        Assertions.assertEquals(8d, records.get(0).getHours(), 0.1d);
        Assertions.assertEquals(10000L, records.get(0).getValueInCents());

        Assertions.assertEquals(2015, records.get(1).getYear());
        Assertions.assertEquals(0, records.get(1).getMonth());
        Assertions.assertEquals(16d, records.get(1).getHours(), 0.1d);
        Assertions.assertEquals(30000L, records.get(1).getValueInCents());

        records = repository.aggregateByMonthAndBudgetTags(1L, Arrays.asList("tag2"));
        Assertions.assertEquals(2, records.size());

        Assertions.assertEquals(2015, records.get(0).getYear());
        Assertions.assertEquals(0, records.get(0).getMonth());
        Assertions.assertEquals(16d, records.get(0).getHours(), 0.1d);
        Assertions.assertEquals(30000L, records.get(0).getValueInCents());

        Assertions.assertEquals(2015, records.get(1).getYear());
        Assertions.assertEquals(7, records.get(1).getMonth());
        Assertions.assertEquals(16d, records.get(1).getHours(), 0.1d);
        Assertions.assertEquals(0L, records.get(1).getValueInCents());
    }

    @Test
    @DatabaseSetup("aggregate.xml")
    @DatabaseTearDown(value = "aggregate.xml", type = DatabaseOperation.DELETE_ALL)
    void testAggregateByWeekForProject() throws ParseException {
        List<WeeklyAggregatedRecordBean> records = repository.aggregateByWeekForProject(1L, format.parse("02.01.2015"));
        Assertions.assertEquals(2, records.size());

        Assertions.assertEquals(2015, records.get(0).getYear());
        Assertions.assertEquals(1, records.get(0).getWeek());
        Assertions.assertEquals(8d, records.get(0).getHours(), 0.1d);
        Assertions.assertEquals(20000L, records.get(0).getValueInCents());

        Assertions.assertEquals(2015, records.get(1).getYear());
        Assertions.assertEquals(33, records.get(1).getWeek());
        Assertions.assertEquals(16d, records.get(1).getHours(), 0.1d);
        Assertions.assertEquals(0L, records.get(1).getValueInCents());
    }

    @Test
    @DatabaseSetup("aggregate.xml")
    @DatabaseTearDown(value = "aggregate.xml", type = DatabaseOperation.DELETE_ALL)
    void testAggregateByWeekForPerson() throws ParseException {
        List<WeeklyAggregatedRecordBean> records = repository.aggregateByWeekForPerson(1L, format.parse("02.01.2015"));
        Assertions.assertEquals(1, records.size());

        Assertions.assertEquals(2015, records.get(0).getYear());
        Assertions.assertEquals(1, records.get(0).getWeek());
        Assertions.assertEquals(8d, records.get(0).getHours(), 0.1d);
        Assertions.assertEquals(20000L, records.get(0).getValueInCents());
    }

    @Test
    @DatabaseSetup("aggregate.xml")
    @DatabaseTearDown(value = "aggregate.xml", type = DatabaseOperation.DELETE_ALL)
    void testAggregateByWeekForBudget() throws ParseException {
        List<WeeklyAggregatedRecordBean> records = repository.aggregateByWeekForBudget(1L, format.parse("01.01.2015"));
        Assertions.assertEquals(1, records.size());

        Assertions.assertEquals(2015, records.get(0).getYear());
        Assertions.assertEquals(1, records.get(0).getWeek());
        Assertions.assertEquals(8d, records.get(0).getHours(), 0.1d);
        Assertions.assertEquals(10000L, records.get(0).getValueInCents());
    }

    @Test
    @DatabaseSetup("aggregate.xml")
    @DatabaseTearDown(value = "aggregate.xml", type = DatabaseOperation.DELETE_ALL)
    void testAggregateByWeekAndBudgetForPerson() throws ParseException {
        List<WeeklyAggregatedRecordWithTitleBean> records = repository.aggregateByWeekAndBudgetForPerson(1L, format.parse("01.01.2015"));
        Assertions.assertEquals(2, records.size());

        Assertions.assertEquals(2015, records.get(0).getYear());
        Assertions.assertEquals(1, records.get(0).getWeek());
        Assertions.assertEquals(8d, records.get(0).getHours(), 0.1d);
        Assertions.assertEquals(10000L, records.get(0).getValueInCents());
        Assertions.assertEquals("Budget 1", records.get(0).getTitle());

        Assertions.assertEquals(2015, records.get(1).getYear());
        Assertions.assertEquals(1, records.get(1).getWeek());
        Assertions.assertEquals(8d, records.get(1).getHours(), 0.1d);
        Assertions.assertEquals(20000L, records.get(1).getValueInCents());
        Assertions.assertEquals("Budget 2", records.get(1).getTitle());
    }

    @Test
    @DatabaseSetup("aggregate.xml")
    @DatabaseTearDown(value = "aggregate.xml", type = DatabaseOperation.DELETE_ALL)
    void testAggregateByWeekAndPersonForBudget() throws ParseException {
        List<WeeklyAggregatedRecordWithTitleBean> records = repository.aggregateByWeekAndPersonForBudget(1L, format.parse("01.01.2015"));
        Assertions.assertEquals(1, records.size());

        Assertions.assertEquals(2015, records.get(0).getYear());
        Assertions.assertEquals(1, records.get(0).getWeek());
        Assertions.assertEquals(8d, records.get(0).getHours(), 0.1d);
        Assertions.assertEquals(10000L, records.get(0).getValueInCents());
        Assertions.assertEquals("person1", records.get(0).getTitle());

    }

    @Test
    @DatabaseSetup("aggregate.xml")
    @DatabaseTearDown(value = "aggregate.xml", type = DatabaseOperation.DELETE_ALL)
    void testAggregateByWeekAndPersonForBudgets() throws ParseException {
        List<WeeklyAggregatedRecordWithTitleBean> records = repository.aggregateByWeekAndPersonForBudgets(1L, Arrays.asList("tag1"), format.parse("01.01.2015"));
        Assertions.assertEquals(2, records.size());

        Assertions.assertEquals(2015, records.get(0).getYear());
        Assertions.assertEquals(1, records.get(0).getWeek());
        Assertions.assertEquals(16d, records.get(0).getHours(), 0.1d);
        Assertions.assertEquals(30000L, records.get(0).getValueInCents());
        Assertions.assertEquals("person1", records.get(0).getTitle());

        Assertions.assertEquals(2015, records.get(1).getYear());
        Assertions.assertEquals(33, records.get(1).getWeek());
        Assertions.assertEquals(16d, records.get(1).getHours(), 0.1d);
        Assertions.assertEquals(0L, records.get(1).getValueInCents());
        Assertions.assertEquals("person2", records.get(1).getTitle());

    }

    @Test
    @DatabaseSetup("aggregateByMonthAndBudgetForPerson.xml")
    @DatabaseTearDown(value = "aggregateByMonthAndBudgetForPerson.xml", type = DatabaseOperation.DELETE_ALL)
    void testAggregateByMonthAndBudgetForPerson() throws ParseException {
        List<MonthlyAggregatedRecordWithTitleBean> records = repository.aggregateByMonthAndBudgetForPerson(1L, format.parse("01.01.2015"));
        Assertions.assertEquals(2, records.size());

        Assertions.assertEquals(2015, records.get(0).getYear());
        Assertions.assertEquals(0, records.get(0).getMonth());
        Assertions.assertEquals(8d, records.get(0).getHours(), 0.1d);
        Assertions.assertEquals(10000L, records.get(0).getValueInCents());
        Assertions.assertEquals("Budget 1", records.get(0).getTitle());

        Assertions.assertEquals(2015, records.get(1).getYear());
        Assertions.assertEquals(0, records.get(1).getMonth());
        Assertions.assertEquals(8d, records.get(1).getHours(), 0.1d);
        Assertions.assertEquals(20000L, records.get(1).getValueInCents());
        Assertions.assertEquals("Budget 2", records.get(1).getTitle());
    }

    @Test
    @DatabaseSetup("aggregateByMonthForPerson.xml")
    @DatabaseTearDown(value = "aggregateByMonthForPerson.xml", type = DatabaseOperation.DELETE_ALL)
    void testAggregateByMonthForPerson() throws ParseException {
        List<MonthlyAggregatedRecordBean> records = repository.aggregateByMonthForPerson(1L, format.parse("01.01.2015"));
        Assertions.assertEquals(1, records.size());

        Assertions.assertEquals(2015, records.get(0).getYear());
        Assertions.assertEquals(0, records.get(0).getMonth());
        Assertions.assertEquals(16d, records.get(0).getHours(), 0.1d);
        Assertions.assertEquals(30000L, records.get(0).getValueInCents());
    }

    @Test
    @DatabaseSetup("aggregateByMonthAndPersonForBudget.xml")
    @DatabaseTearDown(value = "aggregateByMonthAndPersonForBudget.xml", type = DatabaseOperation.DELETE_ALL)
    void testAggregateByMonthAndPersonForBudget() throws ParseException {
        List<MonthlyAggregatedRecordWithTitleBean> records = repository.aggregateByMonthAndPersonForBudget(1L, format.parse("01.01.2015"));
        Assertions.assertEquals(1, records.size());

        Assertions.assertEquals(2015, records.get(0).getYear());
        Assertions.assertEquals(0, records.get(0).getMonth());
        Assertions.assertEquals(8d, records.get(0).getHours(), 0.1d);
        Assertions.assertEquals(10000L, records.get(0).getValueInCents());
        Assertions.assertEquals("person1", records.get(0).getTitle());

    }

    @Test
    @DatabaseSetup("aggregateByMonthForBudget.xml")
    @DatabaseTearDown(value = "aggregateByMonthForBudget.xml", type = DatabaseOperation.DELETE_ALL)
    void testAggregateByMonthForBudget() throws ParseException {
        List<MonthlyAggregatedRecordBean> records = repository.aggregateByMonthForBudget(1L, format.parse("01.01.2015"));
        Assertions.assertEquals(1, records.size());

        Assertions.assertEquals(2015, records.get(0).getYear());
        Assertions.assertEquals(0, records.get(0).getMonth());
        Assertions.assertEquals(8d, records.get(0).getHours(), 0.1d);
        Assertions.assertEquals(10000L, records.get(0).getValueInCents());
    }

    @Test
    @DatabaseSetup("aggregateByMonthAndPersonForBudgets.xml")
    @DatabaseTearDown(value = "aggregateByMonthAndPersonForBudgets.xml", type = DatabaseOperation.DELETE_ALL)
    void testDeleteByProjectId(){
        repository.deleteByProjectId(1L);
    }

    @Test
    @DatabaseSetup("countByProjectId.xml")
    @DatabaseTearDown(value = "countByProjectId.xml", type = DatabaseOperation.DELETE_ALL)
    void testCountByProjectId(){
        Assertions.assertEquals(2, (long) repository.countByProjectId(1L));
        Assertions.assertEquals(0, (long) repository.countByProjectId(2L));
    }

}
