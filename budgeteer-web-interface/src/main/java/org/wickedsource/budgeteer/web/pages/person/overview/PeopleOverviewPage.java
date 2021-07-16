package org.wickedsource.budgeteer.web.pages.person.overview;

import org.wickedsource.budgeteer.web.BudgeteerSession;
import org.wickedsource.budgeteer.web.pages.base.basepage.BasePage;
import org.wickedsource.budgeteer.web.pages.base.basepage.breadcrumbs.BreadcrumbsModel;
import org.wickedsource.budgeteer.web.pages.dashboard.DashboardPage;
import org.wickedsource.budgeteer.web.pages.person.overview.table.PeopleModel;
import org.wickedsource.budgeteer.web.pages.person.overview.table.PeopleOverviewTable;
import org.wicketstuff.annotation.mount.MountPath;

@MountPath("people")
public class PeopleOverviewPage extends BasePage {

    public PeopleOverviewPage() {
        PeopleModel model = new PeopleModel(BudgeteerSession.get().getProjectId());
        PeopleOverviewTable table = new PeopleOverviewTable("peopleOverviewTable", model);
        add(table);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected BreadcrumbsModel getBreadcrumbsModel() {
        return new BreadcrumbsModel(DashboardPage.class, PeopleOverviewPage.class);
    }

}
