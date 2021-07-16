package org.wickedsource.budgeteer.web.pages.budgets.edit;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.util.string.StringValue;
import org.wickedsource.budgeteer.service.budget.BudgetService;
import org.wickedsource.budgeteer.service.budget.EditBudgetData;
import org.wickedsource.budgeteer.web.pages.base.dialogpage.DialogPageWithBacklink;
import org.wickedsource.budgeteer.web.pages.budgets.edit.form.EditBudgetForm;
import org.wickedsource.budgeteer.web.pages.budgets.overview.BudgetsOverviewPage;
import org.wicketstuff.annotation.mount.MountPath;

import static org.wicketstuff.lazymodel.LazyModel.from;
import static org.wicketstuff.lazymodel.LazyModel.model;

@MountPath("budgets/edit/#{id}")
public class EditBudgetPage extends DialogPageWithBacklink {

    @SpringBean
    private BudgetService service;

    private final boolean isEditing;

    /**
     * Use this constructor to create a page with a form to create a new budget.
     */
    public EditBudgetPage(Class<? extends WebPage> backlinkPage, PageParameters backlinkParameters) {
        super(backlinkPage, backlinkParameters);
        isEditing = false;
        Form<EditBudgetData> form = new EditBudgetForm("form");
        addComponents(form);
    }

    /**
     * Use this constructor to create a page with a form to edit an existing budget.
     *
     * @param parameters page parameters containing the id of the budget to edit.
     */
    public EditBudgetPage(PageParameters parameters, Class<? extends WebPage> backlinkPage, PageParameters backlinkParameters, boolean isEditingNewBudget) {
        super(parameters, backlinkPage, backlinkParameters);
        isEditing = true;
        EditBudgetData budgetData = service.loadBudgetToEdit(getBudgetId());
        Form<EditBudgetData> form = new EditBudgetForm("form", model(from(budgetData)), isEditingNewBudget);
        addComponents(form);
    }

    /**
     * This constructor is used when you click on a link or try to access the EditBudgetPage manually
     * (e.g. when you type the path "/budgets/edit" in the search bar)
     * @param parameters
     */
    public EditBudgetPage(PageParameters parameters) {
        super(parameters, BudgetsOverviewPage.class, new PageParameters());
        if(getBudgetId() == 0){
            isEditing = false;
            Form<EditBudgetData> form = new EditBudgetForm("form");
            addComponents(form);
        }else{
            isEditing = true;
            EditBudgetData budgetData = service.loadBudgetToEdit(getBudgetId());
            Form<EditBudgetData> form = new EditBudgetForm("form", model(from(budgetData)), false);
            addComponents(form);
        }
    }

    private void addComponents(Form<EditBudgetData> form) {
        add(createBacklink("cancelButton1"));
        form.add(createBacklink("cancelButton2"));
        if(isEditing) {
            add(new Label("pageTitle", new ResourceModel("page.title.editmode")));
        }else{
            add(new Label("pageTitle", new ResourceModel("page.title.createmode")));
        }
        add(form);
    }

    /**
     * Creates a valid PageParameters object to pass into the constructor of this page class.
     *
     * @param budgetId id of the budget whose details to edit.
     * @return a valid PageParameters object.
     */
    public static PageParameters createParameters(long budgetId) {
        PageParameters parameters = new PageParameters();
        parameters.add("id", budgetId);
        return parameters;
    }

    private long getBudgetId() {
        StringValue value = getPageParameters().get("id");
        if (value == null || value.isEmpty() || value.isNull()) {
            return 0L;
        } else {
            return value.toLong();
        }
    }
}
