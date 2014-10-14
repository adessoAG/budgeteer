package org.wickedsource.budgeteer.web.usecase.dashboard.component.dailyratechart;

import org.apache.wicket.util.tester.WicketTester;
import org.junit.Test;
import org.wickedsource.budgeteer.web.AbstractWebTestTemplate;
import org.wickedsource.budgeteer.web.charts.BudgeteerChartTheme;

public class AverageDailyRateChartTest extends AbstractWebTestTemplate {

    @Test
    public void testRender() {
        WicketTester tester = getTester();
        AverageDailyRateChartModel model = new AverageDailyRateChartModel(1l, 5);
        tester.startComponentInPage(new AverageDailyRateChart("chart", model, new BudgeteerChartTheme()));
    }

}