package org.wickedsource.budgeteer.web.pages.contract.details.contractDetailChart;

import de.adesso.wickedcharts.chartjs.ChartConfiguration;
import de.adesso.wickedcharts.wicket8.chartjs.Chart;

import java.io.Serializable;

public class ContractDetailChart extends Chart implements Serializable {

    private ContractDetailChartModel model;

    public ContractDetailChart(String id, ContractDetailChartModel model) {
        super(id, new ContractDetailChartConfiguration(model));
        this.model = model;
    }

    @Override
    protected void onBeforeRender() {
        super.onBeforeRender();
        // resetting options to force re-rendering with new parameters
        ChartConfiguration chartConfiguration = new ContractDetailChartConfiguration(model);
        chartConfiguration.getOptions().setMaintainAspectRatio(false);
        setChartConfiguration(chartConfiguration);
    }
}
