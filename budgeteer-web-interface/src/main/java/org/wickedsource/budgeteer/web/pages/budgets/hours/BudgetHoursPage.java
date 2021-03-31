package org.wickedsource.budgeteer.web.pages.budgets.hours;

import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.wickedsource.budgeteer.service.budget.BudgetBaseData;
import org.wickedsource.budgeteer.service.person.PersonService;
import org.wickedsource.budgeteer.service.record.RecordService;
import org.wickedsource.budgeteer.service.record.WorkRecord;
import org.wickedsource.budgeteer.service.record.WorkRecordFilter;
import org.wickedsource.budgeteer.web.BudgeteerSession;
import org.wickedsource.budgeteer.web.Mount;
import org.wickedsource.budgeteer.web.components.burntable.BurnTableWithFilter;
import org.wickedsource.budgeteer.web.pages.base.basepage.BasePage;
import org.wickedsource.budgeteer.web.pages.base.basepage.breadcrumbs.Breadcrumb;
import org.wickedsource.budgeteer.web.pages.base.basepage.breadcrumbs.BreadcrumbsModel;
import org.wickedsource.budgeteer.web.pages.budgets.BudgetNameModel;
import org.wickedsource.budgeteer.web.pages.budgets.details.BudgetDetailsPage;
import org.wickedsource.budgeteer.web.pages.budgets.overview.BudgetsOverviewPage;
import org.wickedsource.budgeteer.web.pages.dashboard.DashboardPage;

import javax.inject.Inject;
import java.util.List;

@Mount("budgets/hours/${id}")
public class BudgetHoursPage extends BasePage {

    @Inject
    private PersonService personService;

    @Inject
    private RecordService recordService;

    public BudgetHoursPage(PageParameters parameters) {
        super(parameters);

        IModel<WorkRecordFilter> filter = Model.of(new WorkRecordFilter(BudgeteerSession.get().getProjectId()));
        filter.getObject().getBudgetList().add(new BudgetBaseData(getParameterId(), ""));
        filter.getObject().getPossiblePersons().addAll(personService.loadPeopleBaseDataByBudget(getParameterId()));

        IModel<List<WorkRecord>> records = new AbstractReadOnlyModel<List<WorkRecord>>() {
            @Override
            public List<WorkRecord> getObject() {
                return recordService.getFilteredRecords(filter.getObject());
            }
        };

        BurnTableWithFilter table = new BurnTableWithFilter("burnTable", records, Model.of(filter));
        table.setBudgetFilterEnabled(false);
        add(table);
    }

    protected BreadcrumbsModel getBreadcrumbsModel() {
        BreadcrumbsModel model = new BreadcrumbsModel(DashboardPage.class, BudgetsOverviewPage.class);
        model.addBreadcrumb(new Breadcrumb(BudgetDetailsPage.class, getPageParameters(), new BudgetNameModel(getParameterId())));
        model.addBreadcrumb(new Breadcrumb(BudgetHoursPage.class, getPageParameters(), getString("breadcrumb.title")));
        return model;
    }

}
