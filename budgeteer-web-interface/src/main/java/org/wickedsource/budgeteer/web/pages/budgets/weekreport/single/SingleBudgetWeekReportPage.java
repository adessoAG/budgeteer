package org.wickedsource.budgeteer.web.pages.budgets.weekreport.single;

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
import org.wickedsource.budgeteer.web.pages.budgets.components.targetactualchart.BudgetsWeeklyAggregationModel;
import org.wickedsource.budgeteer.web.pages.budgets.components.weekreporttable.BudgetsWeeklyAggregatedRecordsModel;
import org.wickedsource.budgeteer.web.pages.budgets.overview.BudgetsOverviewPage;
import org.wickedsource.budgeteer.web.pages.dashboard.DashboardPage;
import org.wicketstuff.annotation.mount.MountPath;

import javax.inject.Inject;
import java.util.List;

@MountPath("budgets/weeks/${id}")
public class SingleBudgetWeekReportPage extends BasePage {

    @Inject
    private BudgetService budgetService;

    public SingleBudgetWeekReportPage(PageParameters parameters) {
        super(parameters);
        add(new Label("budgetName", budgetService.loadBudgetBaseData(getParameterId()).getName()));
        add(new Label("budgetName2", budgetService.loadBudgetBaseData(getParameterId()).getName()));

        IModel<TargetAndActual> model = new BudgetsWeeklyAggregationModel(getParameterId());
        add(new TargetAndActualChart("targetAndActualChart", model, TargetAndActualChartConfiguration.Mode.WEEKLY));

        IModel<List<AggregatedRecord>> tableModel = new BudgetsWeeklyAggregatedRecordsModel(getParameterId());
        add(new AggregatedRecordTable("table", tableModel));

        add(new BudgetLinkDropdownForm("budgetLinkDropdownForm").setLinkType(BudgetLinkDropdownForm.BudgetLinkType.WEEKLY));
        add(new NetGrossLink("netGrossLink"));
    }

    @Override
    @SuppressWarnings("unchecked")
    protected BreadcrumbsModel getBreadcrumbsModel() {
        return new BreadcrumbsModel(DashboardPage.class, BudgetsOverviewPage.class, SingleBudgetWeekReportPage.class);
    }
}
