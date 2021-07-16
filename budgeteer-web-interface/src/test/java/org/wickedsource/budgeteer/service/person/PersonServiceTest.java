package org.wickedsource.budgeteer.service.person;

import org.joda.money.Money;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.wickedsource.budgeteer.MoneyUtil;
import org.wickedsource.budgeteer.persistence.person.PersonBaseDataBean;
import org.wickedsource.budgeteer.persistence.person.PersonDetailDataBean;
import org.wickedsource.budgeteer.persistence.person.PersonRepository;
import org.wickedsource.budgeteer.service.ServiceTestTemplate;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.mockito.Mockito.when;

class PersonServiceTest extends ServiceTestTemplate {

    private Date fixedDate = new Date();

    @Autowired
    private PersonService personService;

    @Autowired
    private PersonRepository personRepository;

    @Test
    void testLoadPeopleBaseData() {
        when(personRepository.findBaseDataByProjectId(1L)).thenReturn(Arrays.asList(createPersonBaseDataBean()));
        List<PersonBaseData> data = personService.loadPeopleBaseData(1L);
        Assertions.assertEquals(1, data.size());
        Assertions.assertEquals(Long.valueOf(1), data.get(0).getId());
        Assertions.assertEquals("person1", data.get(0).getName());
        Assertions.assertEquals(MoneyUtil.createMoneyFromCents(10000L), data.get(0).getAverageDailyRate());
        Assertions.assertEquals(fixedDate, data.get(0).getLastBooked());
        Assertions.assertEquals(MoneyUtil.createMoney(10), data.get(0).getDefaultDailyRate());
    }


    @Test
    void testLoadPersonDetailData() {
        when(personRepository.findDetailDataByPersonId(1L)).thenReturn(createPersonDetailDataBean());
        PersonDetailData data = personService.loadPersonDetailData(1L);
        Assertions.assertEquals("person1", data.getName());
        Assertions.assertEquals(MoneyUtil.createMoneyFromCents(123456L), data.getAverageDailyRate());
        Assertions.assertEquals(fixedDate, data.getLastBookedDate());
        Assertions.assertEquals(fixedDate, data.getFirstBookedDate());
        Assertions.assertEquals(MoneyUtil.createMoneyFromCents(654321L), data.getBudgetBurned());
        Assertions.assertEquals(5.0d, data.getHoursBooked(), 0.1d);
        Assertions.assertEquals(MoneyUtil.createMoney(100L), data.getDefaultDailyRate());
    }


    @Test
    void testLoadPersonBaseData() {
        when(personRepository.findBaseDataByPersonId(1L)).thenReturn(createPersonBaseDataBean());
        PersonBaseData bean = personService.loadPersonBaseData(1L);
        Assertions.assertEquals(Long.valueOf(1), bean.getId());
        Assertions.assertEquals("person1", bean.getName());
        Assertions.assertEquals(MoneyUtil.createMoneyFromCents(10000L), bean.getAverageDailyRate());
        Assertions.assertEquals(fixedDate, bean.getLastBooked());
        Assertions.assertEquals(MoneyUtil.createMoney(10), bean.getDefaultDailyRate());
    }

    private PersonBaseDataBean createPersonBaseDataBean() {
        return new PersonBaseDataBean(1L, "person1", 500000000L, 50000L, fixedDate, MoneyUtil.createMoney(10));
    }

    private PersonDetailDataBean createPersonDetailDataBean() {
        return new PersonDetailDataBean(1L, "person1", 6172800000L, 50000L, fixedDate, fixedDate, 5.0d, 654321L, MoneyUtil.createMoney(100L));
    }
}
