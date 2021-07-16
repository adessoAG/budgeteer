package org.wickedsource.budgeteer.web.planning;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

class DefaultCalendarSpeedTest {

	private Logger logger = LoggerFactory.getLogger(DefaultCalendarSpeedTest.class);

	private static final int TIMES = 50000;

	/*
	 * Do not run in regular regressions, as speed depends on platform and
	 * parallel jobs
	 */
	@Disabled
	@Test
	void findsHolidaysInFeasibleTime() {

		DefaultCalendar calWithoutHolidays = DefaultCalendar.calendarYear(2015);

		DefaultCalendar calWithHolidays = DefaultCalendar.calendarYear(2015);
		calWithHolidays.setHolidayManager(new HolidayConfiguration("de", "nw"));

		List<TimePeriod> periods = Arrays.asList(new TimePeriod(LocalDate.of(2015, 3, 1), LocalDate.of(2015, 3, 31)),
				new TimePeriod(LocalDate.of(2015, 4, 30), LocalDate.of(2015, 4, 30)),
				new TimePeriod(LocalDate.of(2015, 5, 3), LocalDate.of(2015, 5, 7)),
				new TimePeriod(LocalDate.of(2015, 7, 19), LocalDate.of(2015, 8, 6)));

		long beforeStart = System.currentTimeMillis();

		for (int i = 0; i < TIMES; i++) {
			calWithoutHolidays.getNumberOfWorkingDays(periods);
		}

		long inBetween = System.currentTimeMillis();

		for (int i = 0; i < TIMES; i++) {
			calWithHolidays.getNumberOfWorkingDays(periods);
		}

		long after = System.currentTimeMillis();

		long millisWith = after - inBetween;
		logger.warn("to calculate working days with holidays " + TIMES + " times takes " + millisWith + "ms");
		long millisWithout = inBetween - beforeStart;
		logger.warn("to calculate working days without holidays " + TIMES + " times takes " + millisWithout + "ms");

		Assertions.assertTrue(Double.compare(millisWithout * 1.2 + 5, millisWith) > 0);
	}
}
