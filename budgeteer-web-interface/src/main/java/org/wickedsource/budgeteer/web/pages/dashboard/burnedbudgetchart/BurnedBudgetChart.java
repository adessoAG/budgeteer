package org.wickedsource.budgeteer.web.pages.dashboard.burnedbudgetchart;

import de.adesso.wickedcharts.wicket8.chartjs.Chart;

import java.io.Serializable;

public class BurnedBudgetChart extends Chart implements Serializable {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private BurnedBudgetChartModel model;
    
	public BurnedBudgetChart(String id, BurnedBudgetChartModel model) {
		super(id, new BurnedBudgetChartConfiguration(model));
		this.model = model;
	}
	
    @Override
    protected void onBeforeRender() {
        super.onBeforeRender();
        // resetting options to force re-rendering with new parameters
        setChartConfiguration(new BurnedBudgetChartConfiguration(model));
    }

}
