package org.wickedsource.budgeteer.web.pages.person.monthreport;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.wickedsource.budgeteer.service.record.AggregatedRecord;
import org.wickedsource.budgeteer.service.statistics.TargetAndActual;
import org.wickedsource.budgeteer.web.components.aggregatedrecordtable.AggregatedRecordTable;
import org.wickedsource.budgeteer.web.components.targetactualchart.TargetAndActualChart;
import org.wickedsource.budgeteer.web.components.targetactualchart.TargetAndActualChartConfiguration;
import org.wickedsource.budgeteer.web.pages.base.basepage.BasePage;
import org.wickedsource.budgeteer.web.pages.base.basepage.breadcrumbs.Breadcrumb;
import org.wickedsource.budgeteer.web.pages.base.basepage.breadcrumbs.BreadcrumbsModel;
import org.wickedsource.budgeteer.web.pages.dashboard.DashboardPage;
import org.wickedsource.budgeteer.web.pages.person.PersonNameModel;
import org.wickedsource.budgeteer.web.pages.person.details.PersonDetailsPage;
import org.wickedsource.budgeteer.web.pages.person.monthreport.chart.PersonMonthlyAggregationModel;
import org.wickedsource.budgeteer.web.pages.person.monthreport.table.PersonMonthlyAggregatedRecordsModel;
import org.wickedsource.budgeteer.web.pages.person.overview.PeopleOverviewPage;
import org.wicketstuff.annotation.mount.MountPath;

import java.util.List;

@MountPath("people/months/${id}")
public class PersonMonthReportPage extends BasePage {

    public PersonMonthReportPage(PageParameters parameters) {
        super(parameters);
        PersonNameModel personNameModel = new PersonNameModel(getParameterId());
        add(new Label("personName", personNameModel));
        add(new Label("personName2", personNameModel));

        IModel<TargetAndActual> model = new PersonMonthlyAggregationModel(getParameterId());
        add(new TargetAndActualChart("targetAndActualChart", model, TargetAndActualChartConfiguration.Mode.MONTHLY));

        IModel<List<AggregatedRecord>> tableModel = new PersonMonthlyAggregatedRecordsModel(getParameterId());
        add(new AggregatedRecordTable("table", tableModel));
    }

    @Override
    @SuppressWarnings("unchecked")
    protected BreadcrumbsModel getBreadcrumbsModel() {
        BreadcrumbsModel model = new BreadcrumbsModel(DashboardPage.class, PeopleOverviewPage.class);
        model.addBreadcrumb(new Breadcrumb(PersonDetailsPage.class, PersonDetailsPage.createParameters(getParameterId()), new PersonNameModel(getParameterId())));
        model.addBreadcrumb(new Breadcrumb(PersonMonthReportPage.class, getPageParameters(), getString("breadcrumb.title")));
        return model;
    }
}
