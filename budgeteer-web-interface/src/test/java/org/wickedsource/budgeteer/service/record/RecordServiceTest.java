package org.wickedsource.budgeteer.service.record;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.kubek2k.springockito.annotations.ReplaceWithMock;
import org.kubek2k.springockito.annotations.SpringockitoContextLoader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.wickedsource.budgeteer.persistence.record.MonthlyAggregatedRecordBean;
import org.wickedsource.budgeteer.persistence.record.WeeklyAggregatedRecordBean;
import org.wickedsource.budgeteer.service.ServiceTestTemplate;
import org.wickedsource.budgeteer.service.budget.BudgetTagFilter;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.anyListOf;
import static org.mockito.Mockito.when;

@ContextConfiguration(loader = SpringockitoContextLoader.class, locations = {"classpath:spring-service.xml", "classpath:spring-repository-mock.xml"})
class RecordServiceTest extends ServiceTestTemplate {

    private static final List<String> EMPTY_STRING_LIST = new ArrayList<>(0);

    @Autowired
    @ReplaceWithMock
    private RecordJoiner recordJoiner;

    @Autowired
    private RecordService service;

    @Test
    void testGetWeeklyAggregationForPerson() throws Exception {
        List<AggregatedRecord> recordList = createAggregatedRecordList();
        when(recordJoiner.joinWeekly(anyListOf(WeeklyAggregatedRecordBean.class), anyListOf(WeeklyAggregatedRecordBean.class))).thenReturn(recordList);
        List<AggregatedRecord> resultList = service.getWeeklyAggregationForPerson(1L);
        Assertions.assertEquals(recordList, resultList);
    }

    @Test
    void testGetMonthlyAggregationForPerson() throws Exception {
        List<AggregatedRecord> recordList = createAggregatedRecordList();
        when(recordJoiner.joinMonthly(anyListOf(MonthlyAggregatedRecordBean.class), anyListOf(MonthlyAggregatedRecordBean.class))).thenReturn(recordList);
        List<AggregatedRecord> resultList = service.getMonthlyAggregationForPerson(1L);
        Assertions.assertEquals(recordList, resultList);
    }

    @Test
    void testGetWeeklyAggregationForBudget() throws Exception {
        List<AggregatedRecord> recordList = createAggregatedRecordList();
        when(recordJoiner.joinWeekly(anyListOf(WeeklyAggregatedRecordBean.class), anyListOf(WeeklyAggregatedRecordBean.class))).thenReturn(recordList);
        List<AggregatedRecord> resultList = service.getWeeklyAggregationForBudget(1L);
        Assertions.assertEquals(recordList, resultList);
    }

    @Test
    void testGetMonthlyAggregationForBudget() throws Exception {
        List<AggregatedRecord> recordList = createAggregatedRecordList();
        when(recordJoiner.joinWeekly(anyListOf(WeeklyAggregatedRecordBean.class), anyListOf(WeeklyAggregatedRecordBean.class))).thenReturn(recordList);
        List<AggregatedRecord> resultList = service.getWeeklyAggregationForBudget(1L);
        Assertions.assertEquals(recordList, resultList);
    }

    @Test
    void testGetWeeklyAggregationForBudgets() throws Exception {
        List<AggregatedRecord> recordList = createAggregatedRecordList();
        when(recordJoiner.joinWeekly(anyListOf(WeeklyAggregatedRecordBean.class), anyListOf(WeeklyAggregatedRecordBean.class))).thenReturn(recordList);
        List<AggregatedRecord> resultList = service.getWeeklyAggregationForBudgets(new BudgetTagFilter(EMPTY_STRING_LIST, 1L));
        Assertions.assertEquals(recordList, resultList);
    }

    @Test
    void testGetMonthlyAggregationForBudgets() throws Exception {
        List<AggregatedRecord> recordList = createAggregatedRecordList();
        when(recordJoiner.joinMonthly(anyListOf(MonthlyAggregatedRecordBean.class), anyListOf(MonthlyAggregatedRecordBean.class))).thenReturn(recordList);
        List<AggregatedRecord> resultList = service.getMonthlyAggregationForBudgets(new BudgetTagFilter(EMPTY_STRING_LIST, 1L));
        Assertions.assertEquals(recordList, resultList);
    }

    private List<AggregatedRecord> createAggregatedRecordList() {
        List<AggregatedRecord> list = new ArrayList<>();
        list.add(new AggregatedRecord());
        list.add(new AggregatedRecord());
        list.add(new AggregatedRecord());
        return list;
    }
}
