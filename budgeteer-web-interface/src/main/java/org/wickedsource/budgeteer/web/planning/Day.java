package org.wickedsource.budgeteer.web.planning;

import java.time.LocalDate;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class Day implements Iterable<Day> {

    private final LocalDate localDate;

    private final boolean nonWorking;

    private Day nextDay;

    public Day(LocalDate localDate, boolean nonWorking) {
        this.localDate = localDate;
        this.nonWorking = nonWorking;
    }

    protected void setNextDay(Day nextDay) {
        // be almost immutable
        if (this.nextDay != null) {
            throw new IllegalStateException("cannot reset next day");
        }
        this.nextDay = nextDay;
    }

    public LocalDate getLocalDate() {
        return localDate;
    }

    public boolean isNonWorking() {
        return this.nonWorking;
    }

    @Override
    public Iterator<Day> iterator() {
        return new DayIterator();
    }

    private class DayIterator implements Iterator<Day> {

        Day current;

        @Override
        public boolean hasNext() {
            return current == null || current.nextDay != null;
        }

        @Override
        public Day next() {
            if (current == null) {
                current = Day.this;
            } else {
                if (current.nextDay == null) {
                    throw new NoSuchElementException("no further day found");
                }
                current = current.nextDay;
            }
            return current;
        }
    }
}
