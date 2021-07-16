package org.wickedsource.budgeteer.web.planning;

import de.jollyday.HolidayManager;
import de.jollyday.ManagerParameter;
import de.jollyday.ManagerParameters;

import java.time.LocalDate;

import static de.jollyday.HolidayType.OFFICIAL_HOLIDAY;
import static de.jollyday.HolidayType.UNOFFICIAL_HOLIDAY;

public class HolidayConfiguration {

	final HolidayManager countryManager;
	final String[] regionDetails;

	public HolidayConfiguration(String country, String... regionDetails) {

		final ManagerParameter parameter = ManagerParameters.create(country);
		this.countryManager = HolidayManager.getInstance(parameter);

		this.regionDetails = regionDetails;
	}

	public boolean checkHoliday(LocalDate date) {
		return countryManager.isHoliday(date, regionDetails);
	}

	public boolean checkOfficialHoliday(LocalDate date) {
		return countryManager.isHoliday(date, OFFICIAL_HOLIDAY, regionDetails);
	}

	public boolean checkUnofficialHoliday(LocalDate date) {
		return countryManager.isHoliday(date, UNOFFICIAL_HOLIDAY, regionDetails);
	}
}
