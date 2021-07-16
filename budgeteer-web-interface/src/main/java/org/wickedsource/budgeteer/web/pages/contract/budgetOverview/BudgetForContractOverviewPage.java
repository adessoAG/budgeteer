package org.wickedsource.budgeteer.web.pages.contract.budgetOverview;

import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.wickedsource.budgeteer.web.pages.base.basepage.BasePage;
import org.wickedsource.budgeteer.web.pages.base.basepage.breadcrumbs.BreadcrumbsModel;
import org.wickedsource.budgeteer.web.pages.budgets.overview.table.BudgetOverviewTable;
import org.wickedsource.budgeteer.web.pages.budgets.overview.table.FilteredBudgetModelByContract;
import org.wickedsource.budgeteer.web.pages.contract.details.ContractDetailsPage;
import org.wickedsource.budgeteer.web.pages.contract.overview.ContractOverviewPage;
import org.wickedsource.budgeteer.web.pages.dashboard.DashboardPage;
import org.wicketstuff.annotation.mount.MountPath;

@MountPath("/contracts/details/budgets/${id}")
public class BudgetForContractOverviewPage extends BasePage {

    public BudgetForContractOverviewPage(PageParameters pageParameters) {
        super(pageParameters);
        add(new BudgetOverviewTable("budgetTable", new FilteredBudgetModelByContract(getParameterId()), getBreadcrumbsModel()));
    }

    @SuppressWarnings("unchecked")
    @Override
    protected BreadcrumbsModel getBreadcrumbsModel() {
        BreadcrumbsModel model =  new BreadcrumbsModel(DashboardPage.class, ContractOverviewPage.class);
        model.addBreadcrumb(ContractDetailsPage.class, getPageParameters());
        model.addBreadcrumb(BudgetForContractOverviewPage.class, getPageParameters());
        return model;
    }
}
