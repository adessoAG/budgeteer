package org.wickedsource.budgeteer.web.pages.dashboard;


import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.wickedsource.budgeteer.web.BudgeteerSession;
import org.wickedsource.budgeteer.web.pages.base.basepage.BasePage;
import org.wickedsource.budgeteer.web.pages.base.basepage.breadcrumbs.BreadcrumbsModel;
import org.wickedsource.budgeteer.web.pages.budgets.overview.BudgetsOverviewPage;
import org.wickedsource.budgeteer.web.pages.contract.overview.ContractOverviewPage;
import org.wickedsource.budgeteer.web.pages.dashboard.burnedbudgetchart.BurnedBudgetChart;
import org.wickedsource.budgeteer.web.pages.dashboard.burnedbudgetchart.BurnedBudgetChartModel;
import org.wickedsource.budgeteer.web.pages.dashboard.dailyratechart.AverageDailyRateChart;
import org.wickedsource.budgeteer.web.pages.dashboard.dailyratechart.AverageDailyRateChartModel;
import org.wickedsource.budgeteer.web.pages.hours.HoursPage;
import org.wickedsource.budgeteer.web.pages.imports.ImportsOverviewPage;
import org.wickedsource.budgeteer.web.pages.invoice.overview.InvoiceOverviewPage;
import org.wickedsource.budgeteer.web.pages.person.overview.PeopleOverviewPage;
import org.wickedsource.budgeteer.web.pages.templates.TemplatesPage;
import org.wicketstuff.annotation.mount.MountPath;

@MountPath("dashboard")
public class DashboardPage extends BasePage {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public DashboardPage() {
        BurnedBudgetChartModel burnedBudgetModel = new BurnedBudgetChartModel(BudgeteerSession.get().getProjectId(), 8);
        add(new BurnedBudgetChart("burnedBudgetChart", burnedBudgetModel));
        
        add(new Label("username", new UsernameModel()));

        add(new Label("projectname", new ProjectnameModel()));

        AverageDailyRateChartModel avgDailyRateModel = new AverageDailyRateChartModel(BudgeteerSession.get().getProjectId(), 30);
        add(new AverageDailyRateChart("averageDailyRateChart", avgDailyRateModel));
        add(new BookmarkablePageLink<PeopleOverviewPage>("peopleLink", PeopleOverviewPage.class));

        add(new BookmarkablePageLink<HoursPage>("hoursLink", HoursPage.class));

        add(new BookmarkablePageLink<BudgetsOverviewPage>("budgetsLink", BudgetsOverviewPage.class));

        add(new BookmarkablePageLink<BudgetsOverviewPage>("contractsLink", ContractOverviewPage.class));

        add(new BookmarkablePageLink<InvoiceOverviewPage>("invoiceLink", InvoiceOverviewPage.class));

        add(new BookmarkablePageLink<ImportsOverviewPage>("importsLink", ImportsOverviewPage.class));

        add(new BookmarkablePageLink<ImportsOverviewPage>("templatesLink", TemplatesPage.class));
    }

    @Override
    protected BreadcrumbsModel getBreadcrumbsModel() {
        return new BreadcrumbsModel(DashboardPage.class);
    }
}
