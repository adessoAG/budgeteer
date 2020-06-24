package org.wickedsource.budgeteer.service.record;

import com.github.springtestdbunit.annotation.DatabaseOperation;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.github.springtestdbunit.annotation.DatabaseTearDown;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.wickedsource.budgeteer.IntegrationTestTemplate;
import org.wickedsource.budgeteer.ListUtil;
import org.wickedsource.budgeteer.persistence.record.WorkRecordEntity;
import org.wickedsource.budgeteer.persistence.record.WorkRecordRepository;
import org.wickedsource.budgeteer.service.DateRange;
import org.wickedsource.budgeteer.service.budget.BudgetBaseData;
import org.wickedsource.budgeteer.service.person.PersonBaseData;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;

class WorkRecordQueriesIntegrationTest extends IntegrationTestTemplate {

    private DateFormat format = new SimpleDateFormat("dd.MM.yyyy");

    @Autowired
    private WorkRecordRepository repository;

    @Test
    @DatabaseSetup("findByFilter.xml")
    @DatabaseTearDown(value = "findByFilter.xml", type = DatabaseOperation.DELETE_ALL)
    void testFindByEmptyFilter() throws Exception {
        WorkRecordFilter filter = new WorkRecordFilter(1L);
        Specification<WorkRecordEntity> query = WorkRecordQueries.findByFilter(filter);
        List<WorkRecordEntity> records = ListUtil.toArrayList(repository.findAll(query));
        Assertions.assertEquals(4, records.size());
    }

    @Test
    @DatabaseSetup("findByFilter.xml")
    @DatabaseTearDown(value = "findByFilter.xml", type = DatabaseOperation.DELETE_ALL)
    void testFindByPersonFilter() throws Exception {
        WorkRecordFilter filter = new WorkRecordFilter(1L);
        filter.getPersonList().add(new PersonBaseData(1L));
        Specification<WorkRecordEntity> query = WorkRecordQueries.findByFilter(filter);
        List<WorkRecordEntity> records = ListUtil.toArrayList(repository.findAll(query));
        Assertions.assertEquals(2, records.size());
    }

    @Test
    @DatabaseSetup("findByFilter.xml")
    @DatabaseTearDown(value = "findByFilter.xml", type = DatabaseOperation.DELETE_ALL)
    void testFindByBudgetFilter() throws Exception {
        WorkRecordFilter filter = new WorkRecordFilter(1L);
        filter.getBudgetList().add(new BudgetBaseData(1L, "budget1"));
        Specification<WorkRecordEntity> query = WorkRecordQueries.findByFilter(filter);
        List<WorkRecordEntity> records = ListUtil.toArrayList(repository.findAll(query));
        Assertions.assertEquals(2, records.size());
    }

    @Test
    @DatabaseSetup("findByFilter.xml")
    @DatabaseTearDown(value = "findByFilter.xml", type = DatabaseOperation.DELETE_ALL)
    void testFindByDateFilter() throws Exception {
        WorkRecordFilter filter = new WorkRecordFilter(1L);
        filter.setDateRange(new DateRange(format.parse("01.01.2015"), format.parse("15.08.2015")));
        Specification<WorkRecordEntity> query = WorkRecordQueries.findByFilter(filter);
        List<WorkRecordEntity> records = ListUtil.toArrayList(repository.findAll(query));
        Assertions.assertEquals(3, records.size());
    }

    @Test
    @DatabaseSetup("findByFilter.xml")
    @DatabaseTearDown(value = "findByFilter.xml", type = DatabaseOperation.DELETE_ALL)
    void testFindByMixedFilter() throws Exception {
        WorkRecordFilter filter = new WorkRecordFilter(1L);
        filter.getPersonList().add(new PersonBaseData(1L));
        filter.getBudgetList().add(new BudgetBaseData(1L, "budget1"));
        filter.setDateRange(new DateRange(format.parse("01.01.2015"), format.parse("02.01.2015")));
        Specification<WorkRecordEntity> query = WorkRecordQueries.findByFilter(filter);
        List<WorkRecordEntity> records = ListUtil.toArrayList(repository.findAll(query));
        Assertions.assertEquals(2, records.size());
    }

}
