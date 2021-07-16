package org.wickedsource.budgeteer.web.planning;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.Month;


class DefaultCalendarTest {

    @Test
    void testGetDays() throws Exception {
        DefaultCalendar cal = createDefaultCalendar();

        Assertions.assertEquals(365, cal.getNumberOfDays());
        Assertions.assertEquals(261, cal.getNumberOfWorkingDays());
        Assertions.assertEquals(104, cal.getNumberOfHolidays());

        LocalDate firstDay = cal.getStart();
        Assertions.assertEquals(2015, firstDay.getYear());
        Assertions.assertEquals(Month.JANUARY, firstDay.getMonth());
        Assertions.assertEquals(1, firstDay.getDayOfMonth());

        LocalDate lastDay = cal.getEnd();
        Assertions.assertEquals(2015, lastDay.getYear());
        Assertions.assertEquals(Month.DECEMBER, lastDay.getMonth());
        Assertions.assertEquals(31, lastDay.getDayOfMonth());
    }

    @Test
    void testGetDaysWithDeNwHolidays() throws Exception {
        DefaultCalendar cal = createDefaultCalendar();
        cal.setHolidayManager(new HolidayConfiguration("de", "nw"));

        Assertions.assertEquals(365, cal.getNumberOfDays());
        Assertions.assertEquals(251, cal.getNumberOfWorkingDays());
        Assertions.assertEquals(114, cal.getNumberOfHolidays());

        LocalDate firstDay = cal.getStart();
        Assertions.assertEquals(2015, firstDay.getYear());
        Assertions.assertEquals(Month.JANUARY, firstDay.getMonth());
        Assertions.assertEquals(1, firstDay.getDayOfMonth());

        LocalDate lastDay = cal.getEnd();
        Assertions.assertEquals(2015, lastDay.getYear());
        Assertions.assertEquals(Month.DECEMBER, lastDay.getMonth());
        Assertions.assertEquals(31, lastDay.getDayOfMonth());
    }

    @Test
    void testGetNumberOfWorkingDaysInPeriod() {
        DefaultCalendar cal = createDefaultCalendar();
        LocalDate start = LocalDate.of(2015, Month.JULY, 9);
        LocalDate end = LocalDate.of(2015, Month.JULY, 15);
        TimePeriod period = new TimePeriod(start, end);

        Assertions.assertEquals(5, cal.getNumberOfWorkingDaysInPeriod(period));
    }

    private DefaultCalendar createDefaultCalendar() {
        return DefaultCalendar.calendarYear(2015);
    }
}
