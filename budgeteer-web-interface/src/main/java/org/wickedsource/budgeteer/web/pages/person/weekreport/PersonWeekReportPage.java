package org.wickedsource.budgeteer.web.pages.person.weekreport;

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
import org.wickedsource.budgeteer.web.pages.person.overview.PeopleOverviewPage;
import org.wickedsource.budgeteer.web.pages.person.weekreport.chart.PersonWeeklyAggregationModel;
import org.wickedsource.budgeteer.web.pages.person.weekreport.table.PersonWeeklyAggregatedRecordsModel;
import org.wicketstuff.annotation.mount.MountPath;

import java.util.List;

@MountPath("people/weeks/${id}")
public class PersonWeekReportPage extends BasePage {

    public PersonWeekReportPage(PageParameters parameters) {
        super(parameters);
        PersonNameModel personNameModel = new PersonNameModel(getPersonId());
        add(new Label("personName", personNameModel));
        add(new Label("personName2", personNameModel));

        IModel<TargetAndActual> model = new PersonWeeklyAggregationModel(getPersonId());
        add(new TargetAndActualChart("targetAndActualChart", model, TargetAndActualChartConfiguration.Mode.WEEKLY));

        IModel<List<AggregatedRecord>> tableModel = new PersonWeeklyAggregatedRecordsModel(getPersonId());
        add(new AggregatedRecordTable("table", tableModel));
    }

    /**
     * Creates a valid PageParameters object to pass into the constructor of this page class.
     *
     * @param personId id of the person whose details to display.
     * @return a valid PageParameters object.
     */
    public static PageParameters createParameters(long personId) {
        PageParameters parameters = new PageParameters();
        parameters.add("id", personId);
        return parameters;
    }

    private long getPersonId() {
        return getPageParameters().get("id").toLong();
    }


    @Override
    @SuppressWarnings("unchecked")
    protected BreadcrumbsModel getBreadcrumbsModel() {
        BreadcrumbsModel model = new BreadcrumbsModel(DashboardPage.class, PeopleOverviewPage.class);
        model.addBreadcrumb(new Breadcrumb(PersonDetailsPage.class, PersonDetailsPage.createParameters(getPersonId()), new PersonNameModel(getPersonId())));
        model.addBreadcrumb(new Breadcrumb(PersonWeekReportPage.class, getPageParameters(), getString("breadcrumb.title")));
        return model;
    }
}
