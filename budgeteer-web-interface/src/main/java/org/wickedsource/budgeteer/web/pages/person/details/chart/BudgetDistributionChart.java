package org.wickedsource.budgeteer.web.pages.person.details.chart;

import de.adesso.wickedcharts.wicket8.chartjs.Chart;

public class BudgetDistributionChart extends Chart {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private BudgetDistributionChartModel model;

    public BudgetDistributionChart(String id, BudgetDistributionChartModel model) {
        super(id, new BudgetDistributionChartConfiguration(model));
        this.model = model;
    }

    @Override
    protected void onBeforeRender() {
        super.onBeforeRender();
        // resetting options to force re-rendering with new parameters
        setChartConfiguration(new BudgetDistributionChartConfiguration(model));
    }

}
