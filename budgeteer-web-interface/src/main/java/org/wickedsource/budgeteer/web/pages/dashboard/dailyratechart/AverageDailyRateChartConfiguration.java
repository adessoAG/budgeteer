package org.wickedsource.budgeteer.web.pages.dashboard.dailyratechart;

import de.adesso.wickedcharts.chartjs.ChartConfiguration;
import de.adesso.wickedcharts.chartjs.chartoptions.CallbackFunction;
import de.adesso.wickedcharts.chartjs.chartoptions.ChartType;
import de.adesso.wickedcharts.chartjs.chartoptions.Data;
import de.adesso.wickedcharts.chartjs.chartoptions.Dataset;
import de.adesso.wickedcharts.chartjs.chartoptions.label.Label;
import de.adesso.wickedcharts.chartjs.chartoptions.label.TextLabel;
import de.adesso.wickedcharts.chartjs.chartoptions.valueType.DoubleValue;
import org.apache.wicket.injection.Injector;
import org.wickedsource.budgeteer.MoneyUtil;
import org.wickedsource.budgeteer.web.BudgeteerSession;
import org.wickedsource.budgeteer.web.PropertyLoader;
import org.wickedsource.budgeteer.web.charts.ChartStyling;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class AverageDailyRateChartConfiguration extends ChartConfiguration implements Serializable {

    public AverageDailyRateChartConfiguration(AverageDailyRateChartModel model) {
        Injector.get().inject(this);
        
        String dateFormat = "dd. MMM";
        
        setType(ChartType.LINE);
        
        setOptions(ChartStyling.getOptions());
        
        getOptions().getScales().getYAxes().get(0).getTicks()
										.setBeginAtZero(true)
										.setSuggestedMin(0)
										.setFontFamily(ChartStyling.getFontFamily())
										.setFontSize(ChartStyling.getFontSize())
										.setMaxTicksLimit(5);
        getOptions().getScales().getXAxes().get(0).getTicks()
										.setCallback(new CallbackFunction("function(dataLabel, index) {return index % 2 === 0 ? dataLabel : '';}"));

		Dataset dataset1 = new Dataset()
				.setFill(false)
				.setBackgroundColor(ChartStyling.getColors().get(0))
				.setBorderColor(ChartStyling.getColors().get(0))
				.setData(DoubleValue.of(MoneyUtil.toDouble(model.getObject(), BudgeteerSession.get().getSelectedBudgetUnit())))
				.setLabel(PropertyLoader.getProperty(AverageDailyRateChart.class, "chart.seriesName"));
		
		setData(new Data()
				.setDatasets(Collections.singletonList(dataset1))
				.setLabels(getLabels(model.getNumberOfDays(),dateFormat)));

    }
    
    private List<Label> getLabels(int numberOfDays, String dateFormat) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(dateFormat,Locale.ENGLISH);
    	ArrayList<Label> list = new ArrayList<Label>(numberOfDays);
    	LocalDateTime nowDate = LocalDateTime.now();
    	for(int i = 0; i < numberOfDays; i++) {
    		list.add(new TextLabel(nowDate.minus(i, ChronoUnit.DAYS).format(formatter)));
    	}
    	Collections.reverse(list);
    	return list;
    }

}
