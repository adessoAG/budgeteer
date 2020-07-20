package org.wickedsource.budgeteer.service.person;

import com.github.springtestdbunit.annotation.DatabaseOperation;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.github.springtestdbunit.annotation.DatabaseTearDown;
import org.joda.money.CurrencyUnit;
import org.joda.money.Money;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.wickedsource.budgeteer.IntegrationTestTemplate;
import org.wickedsource.budgeteer.MoneyUtil;
import org.wickedsource.budgeteer.persistence.person.PersonEntity;
import org.wickedsource.budgeteer.persistence.person.PersonRepository;
import org.wickedsource.budgeteer.persistence.record.MissingDailyRateForBudgetBean;
import org.wickedsource.budgeteer.persistence.record.WorkRecordEntity;
import org.wickedsource.budgeteer.persistence.record.WorkRecordRepository;
import org.wickedsource.budgeteer.service.DateRange;
import org.wickedsource.budgeteer.service.budget.BudgetBaseData;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;

class PersonServiceIntegrationTest extends IntegrationTestTemplate {

    private DateFormat format = new SimpleDateFormat("dd.MM.yyyy");

    @Autowired
    private PersonService service;

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private WorkRecordRepository workRecordRepository;

    @Test
    @DatabaseSetup("personWithRates.xml")
    @DatabaseTearDown(value = "personWithRates.xml", type = DatabaseOperation.DELETE_ALL)
    void testLoadPersonWithRates() throws Exception {
        PersonWithRates person = service.loadPersonWithRates(2L);
        Assertions.assertEquals("person2", person.getName());
        Assertions.assertEquals(2L, person.getPersonId());
        Assertions.assertEquals("person2", person.getImportKey());
        Assertions.assertEquals(2, person.getRates().size());
        Assertions.assertNotNull(person.getRates().get(0).getBudget());
    }

    @Test
    @DatabaseSetup("personWithRates.xml")
    @DatabaseTearDown(value = "personWithRates.xml", type = DatabaseOperation.DELETE_ALL)
    void testSavePersonWithRates() throws Exception {
        PersonWithRates person = new PersonWithRates();
        person.setPersonId(1L);
        person.setName("name");
        person.setImportKey("name");
        PersonRate rate1 = new PersonRate(MoneyUtil.createMoneyFromCents(12300L),
                new BudgetBaseData(1L, "budget1"),
                new DateRange(format.parse("01.01.2014"), format.parse("15.08.2014")));
        person.getRates().add(rate1);
        PersonRate rate2 = new PersonRate(MoneyUtil.createMoneyFromCents(32100L),
                new BudgetBaseData(1L, "budget1"),
                new DateRange(format.parse("01.01.2015"), format.parse("15.08.2015")));
        person.getRates().add(rate2);
        service.savePersonWithRates(person);

        PersonEntity personEntity = personRepository.findById(1L).orElseThrow(RuntimeException::new);
        Assertions.assertEquals(2, personEntity.getDailyRates().size());

        WorkRecordEntity record = workRecordRepository.findById(1L).orElseThrow(RuntimeException::new);
        Assertions.assertEquals(MoneyUtil.createMoneyFromCents(32100L), record.getDailyRate());
        record = workRecordRepository.findById(2L).orElseThrow(RuntimeException::new);
        Assertions.assertEquals(MoneyUtil.createMoneyFromCents(12300L), record.getDailyRate());
        record = workRecordRepository.findById(3L).orElseThrow(RuntimeException::new);
        Assertions.assertEquals(MoneyUtil.createMoneyFromCents(32100L), record.getDailyRate());
    }

    @Test
    @DatabaseSetup("personWithRates.xml")
    @DatabaseTearDown(value = "personWithRates.xml", type = DatabaseOperation.DELETE_ALL)
    void testRemoveRateFromPerson() throws Exception{
        PersonWithRates person = service.loadPersonWithRates(1);
        Assertions.assertEquals(0, service.getMissingDailyRatesForPerson(person.getPersonId()).size());

        service.removeDailyRateFromPerson(person, person.getRates().get(0));

        List<MissingDailyRateForBudgetBean> missingRates = service.getMissingDailyRatesForPerson(person.getPersonId());
        Assertions.assertEquals(1, missingRates.size());
        Assertions.assertEquals("Budget 1", missingRates.get(0).getBudgetName());
        Assertions.assertEquals(1, missingRates.get(0).getPersonId());
        Assertions.assertEquals("2015-01-01", missingRates.get(0).getStartDate().toString());
        Assertions.assertEquals("2015-08-15", missingRates.get(0).getEndDate().toString());
    }

    @Test
    @DatabaseSetup("personWithRates.xml")
    @DatabaseTearDown(value = "personWithRates.xml", type = DatabaseOperation.DELETE_ALL)
    void testEditPersonRate() {
        //Load
        PersonWithRates person = service.loadPersonWithRates(2L);
        Assertions.assertEquals(2, person.getRates().size());
        Assertions.assertEquals(Money.of(CurrencyUnit.EUR, 600), person.getRates().get(0).getRate());

        //Save
        person.getRates().get(0).setRate(Money.of(CurrencyUnit.EUR, 100));
        service.savePersonWithRates(person);

        //Test
        person = service.loadPersonWithRates(2L);
        Assertions.assertEquals(2, person.getRates().size());
        Assertions.assertEquals(Money.of(CurrencyUnit.EUR, 100), person.getRates().get(0).getRate());
    }

    @Test
    @DatabaseSetup("personWithRates.xml")
    @DatabaseTearDown(value = "personWithRates.xml", type = DatabaseOperation.DELETE_ALL)
    void testWarnAboutManuallyEditedRates() {
        PersonWithRates person = service.loadPersonWithRates(1);

        List<String> warnings = service.getOverlapWithManuallyEditedRecords(person, 1);
        Assertions.assertEquals(1, warnings.size());
        SimpleDateFormat dateFormat = new SimpleDateFormat();
        Date startDate = Date.from(LocalDate.of(2015, 1, 1).atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
        Date endDate = Date.from(LocalDate.of(2015, 8, 16).atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
        Assertions.assertEquals("A work record in the range " +
                dateFormat.format(startDate) +" - " + dateFormat.format(endDate) +
                " (Exact Date and Amount: 2015-01-01, EUR 100.00) for budget \"Budget 1\" has " +
                "already been edited manually and will not be overwritten.", warnings.get(0));
    }
}
