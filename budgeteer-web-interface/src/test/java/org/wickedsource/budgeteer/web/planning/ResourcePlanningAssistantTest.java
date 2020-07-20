package org.wickedsource.budgeteer.web.planning;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.wickedsource.budgeteer.MoneyUtil;

import java.time.LocalDate;
import java.util.Calendar;

class ResourcePlanningAssistantTest {

    @Test
    void testAllocate() throws Exception {

        Task task1 = new Task("Task 1", MoneyUtil.createMoneyFromCents(10000000));
        Task task2 = new Task("Task 2", MoneyUtil.createMoneyFromCents(5000000));

        Person fullTimePerson = new Person("Felix Fulltime", new Percent(100), MoneyUtil.createMoneyFromCents(50000));
        fullTimePerson.addAbsence(new TimePeriod(getDate(2015, Calendar.JULY, 6), getDate(2015, Calendar.JULY, 10))); // 5 working days missing
        fullTimePerson.addAbsence(new TimePeriod(getDate(2015, Calendar.SEPTEMBER, 4), getDate(2015, Calendar.SEPTEMBER, 7))); // 2 working days missing
        Person partTimePerson = new Person("Paul Parttime", new Percent(50), MoneyUtil.createMoneyFromCents(60000));

        Configuration config = getConfiguration();

        ResourcePlanningAssistant assistant = new ResourcePlanningAssistant(config);

        assistant.allocate(task1, fullTimePerson, new Percent(50));
        assistant.allocate(task1, partTimePerson, new Percent(50));
        assistant.allocate(task2, fullTimePerson, new Percent(10));
        assistant.allocate(task2, partTimePerson, new Percent(10));

        Assertions.assertEquals(MoneyUtil.createMoneyFromCents(-4180000), task1.getRestBudget());
        Assertions.assertEquals(MoneyUtil.createMoneyFromCents(2164000), task2.getRestBudget());
        Assertions.assertTrue(partTimePerson.isOverloaded());
        Assertions.assertFalse(fullTimePerson.isOverloaded());
        Assertions.assertTrue(task1.isOverspent());
        Assertions.assertFalse(task2.isOverspent());

        assistant.deallocate(task1, partTimePerson);

        Assertions.assertEquals(MoneyUtil.createMoneyFromCents(3650000), task1.getRestBudget());
        Assertions.assertEquals(MoneyUtil.createMoneyFromCents(2164000), task2.getRestBudget());
        Assertions.assertFalse(partTimePerson.isOverloaded());
        Assertions.assertFalse(fullTimePerson.isOverloaded());
        Assertions.assertFalse(task1.isOverspent());
        Assertions.assertFalse(task2.isOverspent());
    }

    private Configuration getConfiguration() {
        Configuration config = new Configuration();
		LocalDate start = getDate(2015, Calendar.JANUARY, 1);
		LocalDate end = getDate(2015, Calendar.DECEMBER, 31);

		DefaultCalendar calendar = new DefaultCalendar(start, end, true);
		config.setCalendar(calendar);

        return config;
    }

	private LocalDate getDate(int year, int oldJUDMonth, int dayOfMonth) {
		return LocalDate.of(year, oldJUDMonth + 1, dayOfMonth);
    }
}
