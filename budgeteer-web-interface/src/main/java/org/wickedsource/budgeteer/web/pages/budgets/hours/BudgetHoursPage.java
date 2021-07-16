package org.wickedsource.budgeteer.web.pages.budgets.hours;

import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.wickedsource.budgeteer.service.budget.BudgetBaseData;
import org.wickedsource.budgeteer.service.person.PersonService;
import org.wickedsource.budgeteer.service.record.WorkRecordFilter;
import org.wickedsource.budgeteer.web.BudgeteerSession;
import org.wickedsource.budgeteer.web.components.burntable.BurnTableWithFilter;
import org.wickedsource.budgeteer.web.pages.base.basepage.BasePage;
import org.wickedsource.budgeteer.web.pages.base.basepage.breadcrumbs.Breadcrumb;
import org.wickedsource.budgeteer.web.pages.base.basepage.breadcrumbs.BreadcrumbsModel;
import org.wickedsource.budgeteer.web.pages.budgets.BudgetNameModel;
import org.wickedsource.budgeteer.web.pages.budgets.details.BudgetDetailsPage;
import org.wickedsource.budgeteer.web.pages.budgets.overview.BudgetsOverviewPage;
import org.wickedsource.budgeteer.web.pages.dashboard.DashboardPage;
import org.wicketstuff.annotation.mount.MountPath;

import javax.inject.Inject;

@MountPath("budgets/hours/${id}")
public class BudgetHoursPage extends BasePage {

    @Inject
    private PersonService personService;

    public BudgetHoursPage(PageParameters parameters) {
        super(parameters);

        WorkRecordFilter filter = new WorkRecordFilter(BudgeteerSession.get().getProjectId());
        filter.getBudgetList().add(new BudgetBaseData(getParameterId(), ""));
        filter.getPossiblePersons().addAll(personService.loadPeopleBaseDataByBudget(getParameterId()));

        BurnTableWithFilter table = new BurnTableWithFilter("burnTable", filter, this, parameters);
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
