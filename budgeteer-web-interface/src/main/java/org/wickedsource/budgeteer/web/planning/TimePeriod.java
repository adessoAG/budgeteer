package org.wickedsource.budgeteer.web.planning;

import lombok.Getter;

import java.time.LocalDate;

public class TimePeriod {

	@Getter
	private final LocalDate start;

	@Getter
	private final LocalDate end;

	public TimePeriod(LocalDate start, LocalDate end) {
        this.start = start;
        this.end = end;
		if (start != null && end != null && start.isAfter(end)) {
			throw new IllegalArgumentException("end must not predate start");
		}
    }

	public boolean isInfinite() {
		return isOpenEnd() || isOpenStart();
    }

	public boolean isOpenEnd() {
		return end == null;
	}

	public boolean isOpenStart() {
		return start == null;
	}

}
