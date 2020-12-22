package org.wickedsource.budgeteer.service.record;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.kubek2k.springockito.annotations.ReplaceWithMock;
import org.kubek2k.springockito.annotations.SpringockitoContextLoader;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.wickedsource.budgeteer.service.ServiceTestTemplate;
import org.wickedsource.budgeteer.service.budget.BudgetTagFilter;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.when;

class RecordServiceTest extends ServiceTestTemplate {

    private static final List<String> EMPTY_STRING_LIST = new ArrayList<>(0);

    @MockBean
    private RecordJoiner recordJoiner;

    @Autowired
    private RecordService service;

    @Test
    void testGetWeeklyAggregationForPerson() {
        List<AggregatedRecord> recordList = createAggregatedRecordList();
        when(recordJoiner.joinWeekly(ArgumentMatchers.anyList(), ArgumentMatchers.anyList())).thenReturn(recordList);
        List<AggregatedRecord> resultList = service.getWeeklyAggregationForPerson(1L);
        Assertions.assertEquals(recordList, resultList);
    }

    @Test
    void testGetMonthlyAggregationForPerson() {
        List<AggregatedRecord> recordList = createAggregatedRecordList();
        when(recordJoiner.joinMonthly(ArgumentMatchers.anyList(), ArgumentMatchers.anyList())).thenReturn(recordList);
        List<AggregatedRecord> resultList = service.getMonthlyAggregationForPerson(1L);
        Assertions.assertEquals(recordList, resultList);
    }

    @Test
    void testGetWeeklyAggregationForBudget() {
        List<AggregatedRecord> recordList = createAggregatedRecordList();
        when(recordJoiner.joinWeekly(ArgumentMatchers.anyList(), ArgumentMatchers.anyList())).thenReturn(recordList);
        List<AggregatedRecord> resultList = service.getWeeklyAggregationForBudget(1L);
        Assertions.assertEquals(recordList, resultList);
    }

    @Test
    void testGetMonthlyAggregationForBudget() {
        List<AggregatedRecord> recordList = createAggregatedRecordList();
        when(recordJoiner.joinWeekly(ArgumentMatchers.anyList(), ArgumentMatchers.anyList())).thenReturn(recordList);
        List<AggregatedRecord> resultList = service.getWeeklyAggregationForBudget(1L);
        Assertions.assertEquals(recordList, resultList);
    }

    @Test
    void testGetWeeklyAggregationForBudgets() {
        List<AggregatedRecord> recordList = createAggregatedRecordList();
        when(recordJoiner.joinWeekly(ArgumentMatchers.anyList(), ArgumentMatchers.anyList())).thenReturn(recordList);
        List<AggregatedRecord> resultList = service.getWeeklyAggregationForBudgets(new BudgetTagFilter(EMPTY_STRING_LIST, 1L));
        Assertions.assertEquals(recordList, resultList);
    }

    @Test
    void testGetMonthlyAggregationForBudgets() {
        List<AggregatedRecord> recordList = createAggregatedRecordList();
        when(recordJoiner.joinMonthly(ArgumentMatchers.anyList(), ArgumentMatchers.anyList())).thenReturn(recordList);
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
