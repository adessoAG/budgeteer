package org.wickedsource.budgeteer.web.pages.person.hours;

import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.wickedsource.budgeteer.service.budget.BudgetService;
import org.wickedsource.budgeteer.service.person.PersonBaseData;
import org.wickedsource.budgeteer.service.record.RecordService;
import org.wickedsource.budgeteer.service.record.WorkRecord;
import org.wickedsource.budgeteer.service.record.WorkRecordFilter;
import org.wickedsource.budgeteer.web.BudgeteerSession;
import org.wickedsource.budgeteer.web.Mount;
import org.wickedsource.budgeteer.web.components.burntable.BurnTableWithFilter;
import org.wickedsource.budgeteer.web.pages.base.basepage.BasePage;
import org.wickedsource.budgeteer.web.pages.base.basepage.breadcrumbs.Breadcrumb;
import org.wickedsource.budgeteer.web.pages.base.basepage.breadcrumbs.BreadcrumbsModel;
import org.wickedsource.budgeteer.web.pages.dashboard.DashboardPage;
import org.wickedsource.budgeteer.web.pages.person.PersonNameModel;
import org.wickedsource.budgeteer.web.pages.person.details.PersonDetailsPage;
import org.wickedsource.budgeteer.web.pages.person.overview.PeopleOverviewPage;

import javax.inject.Inject;
import java.util.List;

@Mount("people/hours/${id}")
public class PersonHoursPage extends BasePage {

    @Inject
    private BudgetService budgetService;

    @Inject
    private RecordService recordService;

    public PersonHoursPage(PageParameters parameters) {
        super(parameters);

        IModel<WorkRecordFilter> filter = Model.of(new WorkRecordFilter(BudgeteerSession.get().getProjectId()));
        filter.getObject().getPersonList().add(new PersonBaseData(getParameterId()));
        filter.getObject().getPossibleBudgets().addAll(budgetService.loadBudgetBaseDataByPersonId(getParameterId()));

        IModel<List<WorkRecord>> records = new AbstractReadOnlyModel<List<WorkRecord>>() {
            @Override
            public List<WorkRecord> getObject() {
                return recordService.getFilteredRecords(filter.getObject());
            }
        };

        BurnTableWithFilter table = new BurnTableWithFilter("burnTable", records, filter);
        table.setPersonFilterEnabled(false);
        add(table);
    }

    protected BreadcrumbsModel getBreadcrumbsModel() {
        BreadcrumbsModel model = new BreadcrumbsModel(DashboardPage.class, PeopleOverviewPage.class);
        model.addBreadcrumb(new Breadcrumb(PersonDetailsPage.class, PersonDetailsPage.createParameters(getParameterId()), new PersonNameModel(getParameterId())));
        model.addBreadcrumb(new Breadcrumb(PersonHoursPage.class, getPageParameters(), getString("breadcrumb.title")));
        return model;
    }

}
