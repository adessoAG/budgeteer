package org.wickedsource.budgeteer.web.planning;

import lombok.Getter;

import java.time.LocalDate;
import java.time.Month;
import java.time.Period;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Calendar in which weekends and optionally holidays are marked as non-working.
 */
public class DefaultCalendar extends Calendar {

    public static DefaultCalendar calendarYear(int year) {
        return new DefaultCalendar(LocalDate.of(year, Month.JANUARY, 1), Period.ofYears(1));
    }

    /**
     * Mapping every date to a Day object.
     */
    private Map<LocalDate, Day> days;

    @Getter
    private LocalDate start;

    @Getter
    private LocalDate end;

    @Getter
    private HolidayConfiguration holidayManager;

    @Getter
    private transient int numberOfWorkingDays;

    @Getter
    private transient int numberOfHolidays;

    private boolean initialized;

    public DefaultCalendar(LocalDate start, Period period) {
        this(start, start.plus(period), false);
    }

    public DefaultCalendar(LocalDate start, LocalDate end, boolean includingEnd) {
        if (!includingEnd) {
            end = end.minusDays(1);
        }
        if (start.isAfter(end)) {
            throw new IllegalArgumentException("end must not predate start");
        }
        this.start = start;
        this.end = end;
    }

    public void setHolidayManager(HolidayConfiguration holidayManager) {
        if (initialized) {
            throw new IllegalStateException("cannot set holidayManager after use");
        }
        this.holidayManager = holidayManager;
    }

    @Override
    public int getNumberOfDays() {
        initialize();
        return this.numberOfWorkingDays + this.numberOfHolidays;
    }

    @Override
    public int getNumberOfWorkingDays(List<TimePeriod> absences) {
        initialize();

        int workingDays = this.numberOfWorkingDays;
        for (TimePeriod absence : absences) {
            workingDays -= getNumberOfWorkingDaysInPeriod(absence);
        }
        return workingDays;
    }

    @Override
    public int getNumberOfWorkingDaysInPeriod(TimePeriod period) {
        initialize();

        LocalDate periodStart = period.getStart();
        LocalDate periodEnd = period.getEnd();

        if (period.isInfinite() || periodStart.isBefore(this.start) || periodEnd.isAfter(this.end)) {
            throw new IllegalArgumentException("time period must be fully enclosed in calendar");
        }

        int numberOfWorkingDays = 0;
        for (Day day : days.get(periodStart)) {

            if (day.getLocalDate().isAfter(periodEnd)) {
                break;
            }
            if (!day.isNonWorking()) {
                numberOfWorkingDays++;
            }
        }
        return numberOfWorkingDays;
    }

    private void initialize() {
        if (initialized) {
            return;
        }

        days = new HashMap<>();
        Day current = null;

        for (LocalDate day = start; !day.isAfter(end); day = day.plusDays(1)) {
            current = initializeDay(day, current);
        }
        initialized = true;
    }

    private Day initializeDay(LocalDate date, Day precedingDay) {

        boolean weekend = checkDateIsOnWeekend(date);
        boolean holiday = holidayManager != null && holidayManager.checkHoliday(date);

        Day day = new Day(date, weekend || holiday);
        if (precedingDay != null) {
            precedingDay.setNextDay(day);
        }
        return recordDay(date, day);
    }

    private boolean checkDateIsOnWeekend(LocalDate date) {
        switch (date.getDayOfWeek()) {
            case SATURDAY:
            case SUNDAY:
                return true;
            default:
                return false;
        }
    }

    private Day recordDay(LocalDate date, Day day) {
        days.put(date, day);
        if (day.isNonWorking()) {
            numberOfHolidays++;
        } else {
            numberOfWorkingDays++;
        }
        return day;
    }
}
