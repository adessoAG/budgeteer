package org.wickedsource.budgeteer.web.pages.budgets.monthreport.single;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.wickedsource.budgeteer.service.budget.BudgetService;
import org.wickedsource.budgeteer.service.record.AggregatedRecord;
import org.wickedsource.budgeteer.service.statistics.TargetAndActual;
import org.wickedsource.budgeteer.web.components.aggregatedrecordtable.AggregatedRecordTable;
import org.wickedsource.budgeteer.web.components.budget.dropdown.BudgetLinkDropdownForm;
import org.wickedsource.budgeteer.web.components.links.NetGrossLink;
import org.wickedsource.budgeteer.web.components.targetactualchart.TargetAndActualChart;
import org.wickedsource.budgeteer.web.components.targetactualchart.TargetAndActualChartConfiguration;
import org.wickedsource.budgeteer.web.pages.base.basepage.BasePage;
import org.wickedsource.budgeteer.web.pages.base.basepage.breadcrumbs.BreadcrumbsModel;
import org.wickedsource.budgeteer.web.pages.budgets.components.targetactualchart.BudgetsMonthlyAggregationModel;
import org.wickedsource.budgeteer.web.pages.budgets.components.weekreporttable.BudgetsMonthlyAggregatedRecordsModel;
import org.wickedsource.budgeteer.web.pages.budgets.overview.BudgetsOverviewPage;
import org.wickedsource.budgeteer.web.pages.dashboard.DashboardPage;
import org.wicketstuff.annotation.mount.MountPath;

import javax.inject.Inject;
import java.util.List;

@MountPath("budgets/months/${id}")
public class SingleBudgetMonthReportPage extends BasePage {

    @Inject
    private BudgetService budgetService;

    public SingleBudgetMonthReportPage(PageParameters parameters) {
        super(parameters);

        IModel<TargetAndActual> model = new BudgetsMonthlyAggregationModel(getParameterId());
        add(new Label("budgetName", budgetService.loadBudgetBaseData(getParameterId()).getName()));
        add(new Label("budgetName2", budgetService.loadBudgetBaseData(getParameterId()).getName()));
        add(new TargetAndActualChart("targetAndActualChart", model, TargetAndActualChartConfiguration.Mode.MONTHLY));

        IModel<List<AggregatedRecord>> tableModel = new BudgetsMonthlyAggregatedRecordsModel(getParameterId());
        add(new AggregatedRecordTable("table", tableModel));

        add(new BudgetLinkDropdownForm("budgetLinkDropdownForm").setLinkType(BudgetLinkDropdownForm.BudgetLinkType.MONTHLY));
        add(new NetGrossLink("netGrossLink"));
    }

    @Override
    @SuppressWarnings("unchecked")
    protected BreadcrumbsModel getBreadcrumbsModel() {
        return new BreadcrumbsModel(DashboardPage.class, BudgetsOverviewPage.class, SingleBudgetMonthReportPage.class);
    }
}
