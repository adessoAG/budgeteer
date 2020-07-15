package org.wickedsource.budgeteer.web.pages.dashboard.dailyratechart;

import de.adesso.wickedcharts.wicket8.chartjs.Chart;

public class AverageDailyRateChart extends Chart {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private AverageDailyRateChartModel model;

    public AverageDailyRateChart(String id, AverageDailyRateChartModel model) {
        super(id, new AverageDailyRateChartConfiguration(model));
        this.model = model;
    }

    @Override
    protected void onBeforeRender() {
        super.onBeforeRender();
        // resetting options to force re-rendering with new parameters
        setChartConfiguration(new AverageDailyRateChartConfiguration(model));
    }

}
