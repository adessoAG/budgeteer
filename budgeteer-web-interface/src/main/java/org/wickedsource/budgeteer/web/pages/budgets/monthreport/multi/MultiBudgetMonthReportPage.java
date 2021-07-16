package org.wickedsource.budgeteer.web.pages.budgets.monthreport.multi;

import org.apache.wicket.model.IModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.wickedsource.budgeteer.service.record.AggregatedRecord;
import org.wickedsource.budgeteer.service.statistics.TargetAndActual;
import org.wickedsource.budgeteer.web.BudgeteerSession;
import org.wickedsource.budgeteer.web.components.aggregatedrecordtable.AggregatedRecordTable;
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

import java.util.List;

import static org.wicketstuff.lazymodel.LazyModel.from;
import static org.wicketstuff.lazymodel.LazyModel.model;

@MountPath("budgets/months")
public class MultiBudgetMonthReportPage extends BasePage {

    public MultiBudgetMonthReportPage(PageParameters parameters) {
        super(parameters);

        IModel<TargetAndActual> model = new BudgetsMonthlyAggregationModel(model(from(BudgeteerSession.get().getBudgetFilter())));
        add(new TargetAndActualChart("targetAndActualChart", model, TargetAndActualChartConfiguration.Mode.MONTHLY));

        IModel<List<AggregatedRecord>> tableModel = new BudgetsMonthlyAggregatedRecordsModel(model(from(BudgeteerSession.get().getBudgetFilter())));
        add(new AggregatedRecordTable("table", tableModel));
        add(new NetGrossLink("netGrossLink"));
    }

    @Override
    @SuppressWarnings("unchecked")
    protected BreadcrumbsModel getBreadcrumbsModel() {
        return new BreadcrumbsModel(DashboardPage.class, BudgetsOverviewPage.class, MultiBudgetMonthReportPage.class);
    }
}
