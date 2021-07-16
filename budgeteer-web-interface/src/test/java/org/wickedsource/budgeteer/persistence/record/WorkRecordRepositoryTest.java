package org.wickedsource.budgeteer.persistence.record;

import com.github.springtestdbunit.annotation.DatabaseOperation;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.github.springtestdbunit.annotation.DatabaseTearDown;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.wickedsource.budgeteer.IntegrationTestTemplate;
import org.wickedsource.budgeteer.MoneyUtil;
import org.wickedsource.budgeteer.persistence.budget.BudgetEntity;
import org.wickedsource.budgeteer.persistence.person.PersonEntity;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

class WorkRecordRepositoryTest extends IntegrationTestTemplate {

    private DateFormat format = new SimpleDateFormat("dd.MM.yyyy");

    @Autowired
    private WorkRecordRepository repository;

    @Test
    @DatabaseSetup("getSpentBudget.xml")
    @DatabaseTearDown(value = "getSpentBudget.xml", type = DatabaseOperation.DELETE_ALL)
    void testGetSpentBudget() throws Exception {
        double value = repository.getSpentBudget(1L);
        Assertions.assertEquals(170000d, value, 1d);
    }

    @Test
    @DatabaseSetup("getAverageDailyRate.xml")
    @DatabaseTearDown(value = "getAverageDailyRate.xml", type = DatabaseOperation.DELETE_ALL)
    void testGetAverageDailyRate() throws Exception {
        double value = repository.getAverageDailyRate(1L);
        Assertions.assertEquals(56666d, value, 1d);
    }

    @Test
    @DatabaseSetup("getSpentBudgetUntilDate.xml")
    @DatabaseTearDown(value = "getSpentBudgetUntilDate.xml", type = DatabaseOperation.DELETE_ALL)
    void testGetSpentBudgetUntilDate() throws Exception {
        double value = repository.getSpentBudgetUntilDate(1L, format.parse("15.08.2015"));
        Assertions.assertEquals(170000d, value, 1d);
    }

    @Test
    @DatabaseSetup("getSpentBudgetInTimeRange.xml")
    @DatabaseTearDown(value = "getSpentBudgetInTimeRange.xml", type = DatabaseOperation.DELETE_ALL)
    void testGetSpentBudgetInTimeRange() throws Exception {
        double value = repository.getSpentBudgetInTimeRange(1L, format.parse("15.08.2015"), format.parse("16.08.2015"));
        Assertions.assertEquals(37500d, value, 1d);
    }

    @Test
    @DatabaseSetup("getTotalHoursInTimeRange.xml")
    @DatabaseTearDown(value = "getTotalHoursInTimeRange.xml", type = DatabaseOperation.DELETE_ALL)
    void testGetTotalHoursInTimeRange() throws Exception {
        double value = repository.getTotalHoursInTimeRange(1L, format.parse("15.08.2015"), format.parse("16.08.2015"));
        Assertions.assertEquals(5.5d, value, 10e-8);
    }

    @Test
    @DatabaseSetup("getTotalHoursInTimeRange.xml")
    @DatabaseTearDown(value = "getTotalHoursInTimeRange.xml", type = DatabaseOperation.DELETE_ALL)
    void testGetTotalHoursInTimeRangeAndNoRecordsAreInTimeRange() throws Exception {
        double value = repository.getTotalHoursInTimeRange(1L, format.parse("15.08.2010"), format.parse("16.08.2011"));
        Assertions.assertEquals(0d, value, 10e-8);
    }

    @Test
    @DatabaseSetup("getAverageDailyRateZeroMinutes.xml")
    @DatabaseTearDown(value = "getAverageDailyRateZeroMinutes.xml", type = DatabaseOperation.DELETE_ALL)
    void testGetAverageDailyRateZeroMinutes() throws Exception {
        double value = repository.getAverageDailyRate(1L);
        Assertions.assertEquals(0, value);
    }

    @Test
    @DatabaseSetup("getLastWorkRecordDate.xml")
    @DatabaseTearDown(value = "getLastWorkRecordDate.xml", type = DatabaseOperation.DELETE_ALL)
    void testGetLatestWorkRecordDate() throws Exception {
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Date date = repository.getLatestWorkRecordDate(1L);
        Assertions.assertEquals(format.parse("2015-08-15"), date);
    }

    @Test
    @DatabaseSetup("getFirstWorkRecordDate.xml")
    @DatabaseTearDown(value = "getFirstWorkRecordDate.xml", type = DatabaseOperation.DELETE_ALL)
    void testGetFirstWorkRecordDate() throws Exception {
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Date date = repository.getFirstWorkRecordDate(1L);
        Assertions.assertEquals(format.parse("2015-01-01"), date);
    }

    @Test
    @DatabaseSetup("getTotalHoursByBudgetId.xml")
    @DatabaseTearDown(value = "getTotalHoursByBudgetId.xml", type = DatabaseOperation.DELETE_ALL)
    void testGetTotalHoursByBudgetId() throws Exception {
        double hours = repository.getTotalHoursByBudgetId(1L);
        Assertions.assertEquals(23.5, hours,10e-6);
    }

    @Test
    @DatabaseSetup("getTotalHoursByBudgetIdAndUntilDate.xml")
    @DatabaseTearDown(value = "getTotalHoursByBudgetIdAndUntilDate.xml", type = DatabaseOperation.DELETE_ALL)
    void testGetTotalHoursByBudgetIdAndUntilDate() throws Exception {
        Date date = format.parse("01.01.2015");
        double hours = repository.getTotalHoursByBudgetIdAndUntilDate(1L,date);
        Assertions.assertEquals(15.5, hours,10e-6);
    }

    @Test
    @DatabaseSetup("getFirstWorkRecordDateByBudgetIds.xml")
    @DatabaseTearDown(value = "getFirstWorkRecordDateByBudgetIds.xml", type = DatabaseOperation.DELETE_ALL)
    void testGetFirstWorkRecordDateByBudgetIds() throws Exception {
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Date date = repository.getFirstWorkRecordDateByBudgetIds(Arrays.asList(2L,3L));
        Assertions.assertEquals(format.parse("2015-08-15"), date);
    }

    @Test
    @DatabaseSetup("getMissingDailyRates.xml")
    @DatabaseTearDown(value = "getMissingDailyRates.xml", type = DatabaseOperation.DELETE_ALL)
    void testGetMissingDailyRatesForProject() throws Exception {
        List<MissingDailyRateBean> missingRates = repository.getMissingDailyRatesForProject(1L);
        Assertions.assertEquals(2, missingRates.size());
        Assertions.assertEquals(1L, missingRates.get(0).getPersonId());
        Assertions.assertEquals("person1", missingRates.get(0).getPersonName());
        Assertions.assertEquals(format.parse("01.01.2015"), missingRates.get(0).getStartDate());
        Assertions.assertEquals(format.parse("15.08.2015"), missingRates.get(0).getEndDate());
        Assertions.assertEquals(2L, missingRates.get(1).getPersonId());
        Assertions.assertEquals("person2", missingRates.get(1).getPersonName());
        Assertions.assertEquals(format.parse("01.01.2015"), missingRates.get(1).getStartDate());
        Assertions.assertEquals(format.parse("15.08.2015"), missingRates.get(1).getEndDate());
    }

    @Test
    @DatabaseSetup("getMissingDailyRates.xml")
    @DatabaseTearDown(value = "getMissingDailyRates.xml", type = DatabaseOperation.DELETE_ALL)
    void testGetMissingDailyRatesForPerson() throws Exception {
        List<MissingDailyRateForBudgetBean> missingRates = repository.getMissingDailyRatesForPerson(1L);
        Assertions.assertEquals(2, missingRates.size());

        Assertions.assertEquals(1L, missingRates.get(0).getPersonId());
        Assertions.assertEquals("person1", missingRates.get(0).getPersonName());
        Assertions.assertEquals(format.parse("01.01.2015"), missingRates.get(0).getStartDate());
        Assertions.assertEquals(format.parse("01.01.2015"), missingRates.get(0).getEndDate());
        Assertions.assertEquals("Budget 1", missingRates.get(0).getBudgetName());
    }

    @Test
    @DatabaseSetup("updateDailyRates.xml")
    @DatabaseTearDown(value = "updateDailyRates.xml", type = DatabaseOperation.DELETE_ALL)
    void testUpdateWorkRecordDailyRates() throws Exception {
        repository.updateDailyRates(1L, 1L, format.parse("01.01.2015"), format.parse("15.08.2015"), MoneyUtil.createMoneyFromCents(50000L));
        WorkRecordEntity record = repository.findById(1L).orElseThrow(RuntimeException::new);
        Assertions.assertEquals(MoneyUtil.createMoneyFromCents(50000L), record.getDailyRate());
        record = repository.findById(3L).orElseThrow(RuntimeException::new);
        Assertions.assertEquals(MoneyUtil.createMoneyFromCents(50000L), record.getDailyRate());
    }

    @Test
    @DatabaseSetup("updateDailyRates.xml")
    @DatabaseTearDown(value = "updateDailyRates.xml", type = DatabaseOperation.DELETE_ALL)
    void testUpdateWorkRecordDailyRatesWithEditedRecord() throws Exception {
        repository.updateDailyRates(1L, 1L, format.parse("01.01.2015"), format.parse("15.08.2015"), MoneyUtil.createMoneyFromCents(50000L));
        WorkRecordEntity record = repository.findById(1L).orElseThrow(RuntimeException::new);
        Assertions.assertEquals(MoneyUtil.createMoneyFromCents(50000L), record.getDailyRate());
        record = repository.findById(6L).orElseThrow(RuntimeException::new);
        Assertions.assertEquals(MoneyUtil.createMoneyFromCents(10000L), record.getDailyRate());
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
    @DatabaseSetup("aggregateByWeekAndBudgetForPerson.xml")
    @DatabaseTearDown(value = "aggregateByWeekAndBudgetForPerson.xml", type = DatabaseOperation.DELETE_ALL)
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
    @DatabaseSetup("getAverageDailyRatePerDay.xml")
    @DatabaseTearDown(value = "getAverageDailyRatePerDay.xml", type = DatabaseOperation.DELETE_ALL)
    void testGetAverageDailyRatePerDay() throws ParseException {
        List<DailyAverageRateBean> records = repository.getAverageDailyRatesPerDay(1L, format.parse("01.01.2015"));
        Assertions.assertEquals(2, records.size());

        Assertions.assertEquals(2015, records.get(0).getYear());
        Assertions.assertEquals(0, records.get(0).getMonth());
        Assertions.assertEquals(1, records.get(0).getDay());
        Assertions.assertEquals(MoneyUtil.createMoneyFromCents(15000L), records.get(0).getRate());

        Assertions.assertEquals(2015, records.get(1).getYear());
        Assertions.assertEquals(0, records.get(1).getMonth());
        Assertions.assertEquals(2, records.get(1).getDay());
        Assertions.assertEquals(MoneyUtil.createMoneyFromCents(25000L), records.get(1).getRate());
    }

    @Test
    @DatabaseSetup("getBudgetShareForPerson.xml")
    @DatabaseTearDown(value = "getBudgetShareForPerson.xml", type = DatabaseOperation.DELETE_ALL)
    void testGetBudgetShareForPerson() throws ParseException {
        List<ShareBean> records = repository.getBudgetShareForPerson(1L);
        Assertions.assertEquals(4, records.size());

        Assertions.assertEquals(MoneyUtil.createMoneyFromCents(10000L), records.get(0).getValue());
        Assertions.assertEquals("Budget 1", records.get(0).getName());

        Assertions.assertEquals(MoneyUtil.createMoneyFromCents(20000L), records.get(1).getValue());
        Assertions.assertEquals("Budget 2", records.get(1).getName());

        Assertions.assertEquals(MoneyUtil.createMoneyFromCents(30000L), records.get(2).getValue());
        Assertions.assertEquals("Budget 3", records.get(2).getName());

        Assertions.assertEquals(MoneyUtil.createMoneyFromCents(40000L), records.get(3).getValue());
        Assertions.assertEquals("Budget 4", records.get(3).getName());
    }

    @Test
    @DatabaseSetup("getPersonShareForBudget.xml")
    @DatabaseTearDown(value = "getPersonShareForBudget.xml", type = DatabaseOperation.DELETE_ALL)
    void testGetPersonShareForBudget() throws ParseException {
        List<ShareBean> records = repository.getPersonShareForBudget(1L);
        Assertions.assertEquals(4, records.size());

        Assertions.assertEquals(MoneyUtil.createMoneyFromCents(10000L), records.get(0).getValue());
        Assertions.assertEquals("person1", records.get(0).getName());

        Assertions.assertEquals(MoneyUtil.createMoneyFromCents(20000L), records.get(1).getValue());
        Assertions.assertEquals("person2", records.get(1).getName());

        Assertions.assertEquals(MoneyUtil.createMoneyFromCents(30000L), records.get(2).getValue());
        Assertions.assertEquals("person3", records.get(2).getName());

        Assertions.assertEquals(MoneyUtil.createMoneyFromCents(40000L), records.get(3).getValue());
        Assertions.assertEquals("person4", records.get(3).getName());
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
    @DatabaseSetup("aggregateByWeekForBudget.xml")
    @DatabaseTearDown(value = "aggregateByWeekForBudget.xml", type = DatabaseOperation.DELETE_ALL)
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
    @DatabaseSetup("aggregateByMonthForBudgets.xml")
    @DatabaseTearDown(value = "aggregateByMonthForBudgets.xml", type = DatabaseOperation.DELETE_ALL)
    void testAggregateByMonthForBudgets() throws ParseException {
        List<MonthlyAggregatedRecordBean> records = repository.aggregateByMonthForBudgets(1L, Arrays.asList("tag1"), format.parse("01.01.2015"));
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
    @DatabaseSetup("aggregateByMonthAndPersonForBudgets.xml")
    @DatabaseTearDown(value = "aggregateByMonthAndPersonForBudgets.xml", type = DatabaseOperation.DELETE_ALL)
    void testAggregateByMonthAndPersonForBudgets() throws ParseException {
        List<MonthlyAggregatedRecordWithTitleBean> records = repository.aggregateByMonthAndPersonForBudgets(1L, Arrays.asList("tag1"), format.parse("01.01.2015"));
        Assertions.assertEquals(3, records.size());

        Assertions.assertEquals(2015, records.get(0).getYear());
        Assertions.assertEquals(0, records.get(0).getMonth());
        Assertions.assertEquals(8d, records.get(0).getHours(), 0.1d);
        Assertions.assertEquals(10000L, records.get(0).getValueInCents());
        Assertions.assertEquals("person1", records.get(0).getTitle());

        Assertions.assertEquals(2015, records.get(1).getYear());
        Assertions.assertEquals(0, records.get(1).getMonth());
        Assertions.assertEquals(8d, records.get(1).getHours(), 0.1d);
        Assertions.assertEquals(20000L, records.get(1).getValueInCents());
        Assertions.assertEquals("person2", records.get(1).getTitle());

        Assertions.assertEquals(2015, records.get(2).getYear());
        Assertions.assertEquals(7, records.get(2).getMonth());
        Assertions.assertEquals(16d, records.get(2).getHours(), 0.1d);
        Assertions.assertEquals(0L, records.get(2).getValueInCents());
        Assertions.assertEquals("person2", records.get(2).getTitle());
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
        Assertions.assertEquals(6, (long) repository.countByProjectId(1L));
        Assertions.assertEquals(0, (long) repository.countByProjectId(2L));
    }

    @Test
    @DatabaseSetup("findManuallyEditedEntries.xml")
    @DatabaseTearDown(value = "findManuallyEditedEntries.xml", type = DatabaseOperation.DELETE_ALL)
    void testFindManuallyEditedEntries() throws ParseException {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        List<WorkRecordEntity> records = repository.findManuallyEditedEntries(2L, formatter.parse("2014-01-01"), new Date() );
        Assertions.assertEquals(0, records.size());

        records = repository.findManuallyEditedEntries(1L, formatter.parse("2014-01-01"), formatter.parse("2016-08-15") );
        Assertions.assertEquals(1, records.size());

        records = repository.findManuallyEditedEntries(1L, formatter.parse("2014-01-01"), formatter.parse("2016-08-14") );
        Assertions.assertEquals(0, records.size());

        records = repository.findManuallyEditedEntries(1L, formatter.parse("2012-01-01"), formatter.parse("2016-08-14") );
        Assertions.assertEquals(1, records.size());

        records = repository.findManuallyEditedEntries(1L, formatter.parse("2012-01-01"), formatter.parse("2016-08-16") );
        Assertions.assertEquals(2, records.size());
    }

    @Test
    @DatabaseSetup("findDuplicateEntries.xml")
    @DatabaseTearDown(value = "findDuplicateEntries.xml", type = DatabaseOperation.DELETE_ALL)
    void testFindDuplicateEntries() throws ParseException {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        BudgetEntity budget = new BudgetEntity(); budget.setId(1L);
        PersonEntity person = new PersonEntity(); person.setId(1L);


        List<WorkRecordEntity> records = repository.findDuplicateEntries(budget, person, formatter.parse("2014-01-01"), 480);
        Assertions.assertEquals(1, records.size());

        records = repository.findDuplicateEntries(budget, person, formatter.parse("2014-01-01"), 481);
        Assertions.assertEquals(0, records.size());

        person.setId(3L);
        budget.setId(4L);
        records = repository.findDuplicateEntries(budget, person, formatter.parse("2016-08-15"), 960);
        Assertions.assertEquals(2, records.size());

    }
}
