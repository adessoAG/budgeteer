package org.wickedsource.budgeteer.web.pages.budgets.notes;

import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.util.string.StringValue;
import org.wickedsource.budgeteer.service.budget.BudgetService;
import org.wickedsource.budgeteer.service.budget.EditBudgetData;
import org.wickedsource.budgeteer.web.pages.base.basepage.BasePage;
import org.wickedsource.budgeteer.web.pages.base.basepage.breadcrumbs.Breadcrumb;
import org.wickedsource.budgeteer.web.pages.base.basepage.breadcrumbs.BreadcrumbsModel;
import org.wickedsource.budgeteer.web.pages.budgets.BudgetNameModel;
import org.wickedsource.budgeteer.web.pages.budgets.details.BudgetDetailsPage;
import org.wickedsource.budgeteer.web.pages.budgets.notes.form.BudgetNotesForm;
import org.wickedsource.budgeteer.web.pages.budgets.overview.BudgetsOverviewPage;
import org.wickedsource.budgeteer.web.pages.dashboard.DashboardPage;
import org.wicketstuff.annotation.mount.MountPath;

import static org.wicketstuff.lazymodel.LazyModel.from;
import static org.wicketstuff.lazymodel.LazyModel.model;

@MountPath("budgets/notes/${id}")
public class BudgetNotesPage extends BasePage {

    @SpringBean
    private BudgetService service;

    public BudgetNotesPage() {
        super();

        Form<EditBudgetData> form = new BudgetNotesForm("form");
        add(form);
    }

    /**
     * Load the budget to edit and add a form to the view
     *
     * @param pageParameters PageParameters object
     */
    public BudgetNotesPage(PageParameters pageParameters) {
        super(pageParameters);

        EditBudgetData budgetData = service.loadBudgetToEdit(getBudgetId());
        Form<EditBudgetData> form = new BudgetNotesForm("form", model(from(budgetData)));
        add(form);
    }

    @Override
    protected BreadcrumbsModel getBreadcrumbsModel() {
        BreadcrumbsModel model = new BreadcrumbsModel(DashboardPage.class, BudgetsOverviewPage.class);
        model.addBreadcrumb(new Breadcrumb(BudgetDetailsPage.class, getPageParameters(), new BudgetNameModel(getParameterId())));
        model.addBreadcrumb(new Breadcrumb(BudgetNotesPage.class, getPageParameters(), getString("breadcrumb.title")));
        return model;
    }

    /**
     * Returns the ID of the selected budget
     *
     * @return ID of the selected budget
     */
    private long getBudgetId() {
        StringValue value = getPageParameters().get("id");
        if (value == null || value.isEmpty() || value.isNull()) {
            return 0L;
        } else {
            return value.toLong();
        }
    }
}
