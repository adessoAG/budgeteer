package org.wickedsource.budgeteer.web.pages.budgets.details.chart;

import de.adesso.wickedcharts.wicket8.chartjs.Chart;

public class PeopleDistributionChart extends Chart {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private PeopleDistributionChartModel model;

    public PeopleDistributionChart(String id, PeopleDistributionChartModel model) {
        super(id, new PeopleDistributionChartConfiguration(model));
        this.model = model;
    }

    @Override
    protected void onBeforeRender() {
        super.onBeforeRender();
        // resetting options to force re-rendering with new parameters
        setChartConfiguration(new PeopleDistributionChartConfiguration(model));
    }

}
