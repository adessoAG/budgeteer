package org.wickedsource.budgeteer.web.pages.budgets.edit.form;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.injection.Injector;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.RequiredTextField;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.springframework.dao.DataIntegrityViolationException;
import org.wickedsource.budgeteer.service.budget.BudgetService;
import org.wickedsource.budgeteer.service.budget.EditBudgetData;
import org.wickedsource.budgeteer.service.contract.ContractBaseData;
import org.wickedsource.budgeteer.service.contract.ContractService;
import org.wickedsource.budgeteer.web.BudgeteerSession;
import org.wickedsource.budgeteer.web.ClassAwareWrappingModel;
import org.wickedsource.budgeteer.web.components.customFeedback.CustomFeedbackPanel;
import org.wickedsource.budgeteer.web.components.money.MoneyTextField;
import org.wickedsource.budgeteer.web.components.notificationlist.NotificationListPanel;
import org.wickedsource.budgeteer.web.pages.base.AbstractChoiceRenderer;
import org.wickedsource.budgeteer.web.pages.budgets.BudgetTagsModel;
import org.wickedsource.budgeteer.web.pages.budgets.edit.EditBudgetPage;
import org.wickedsource.budgeteer.web.pages.budgets.edit.tagsfield.TagsTextField;
import org.wickedsource.budgeteer.web.pages.budgets.exception.InvalidBudgetImportKeyAndNameException;
import org.wickedsource.budgeteer.web.pages.budgets.exception.InvalidBudgetImportKeyException;
import org.wickedsource.budgeteer.web.pages.budgets.exception.InvalidBudgetNameException;
import org.wickedsource.budgeteer.web.pages.budgets.overview.BudgetsOverviewPage;

import java.util.List;

import static org.wicketstuff.lazymodel.LazyModel.from;
import static org.wicketstuff.lazymodel.LazyModel.model;

public class EditBudgetForm extends Form<EditBudgetData> {

    @SpringBean
    private BudgetService service;

    @SpringBean
    private ContractService contractService;

    private boolean isEditing;

    public EditBudgetForm(String id) {
        super(id, new ClassAwareWrappingModel<>(Model.of(new EditBudgetData(BudgeteerSession.get().getProjectId())), EditBudgetData.class));
        addComponents();
        this.isEditing = false;
    }

    public EditBudgetForm(String id, IModel<EditBudgetData> model, boolean isEditingNewBudget) {
        super(id, model);
        this.isEditing = true;
        Injector.get().inject(this);
        addComponents();
        if (isEditingNewBudget) {
            this.success("Budget successfully created.");
        }
    }

    private void addComponents() {
        TagsTextField tagsField = new TagsTextField("tagsInput", model(from(getModel()).getTags()));
        tagsField.setOutputMarkupId(true);
        add(tagsField);
        add(new CustomFeedbackPanel("feedback"));
        add(new RequiredTextField<>("name", model(from(getModel()).getTitle())));
        add(new TextField<>("description", model(from(getModel()).getDescription())));
        add(new RequiredTextField<>("importKey", model(from(getModel()).getImportKey())));
        MoneyTextField totalField = new MoneyTextField("total", model(from(getModel()).getTotal()));
        totalField.setRequired(true);
        add(totalField);
        MoneyTextField limitField = new MoneyTextField("limit", model(from(getModel()).getLimit()));
        add(limitField);
        DropDownChoice<ContractBaseData> contractDropDown = new DropDownChoice<>("contract", model(from(getModel()).getContract()),
                contractService.getContractsByProject(BudgeteerSession.get().getProjectId()),
                new AbstractChoiceRenderer<ContractBaseData>() {
                    @Override
                    public Object getDisplayValue(ContractBaseData object) {
                        return object == null ? getString("no.contract") : object.getContractName();
                    }
                });
        contractDropDown.setNullValid(true);
        add(contractDropDown);
        add(createTagsList("tagsList", new BudgetTagsModel(BudgeteerSession.get().getProjectId()), tagsField));
        add(new NotificationListPanel("notificationList", new BudgetNotificationsModel(getModel().getObject().getId())));

        //Label for the submit button
        Label submitButtonLabel;
        if (isEditing) {
            submitButtonLabel = new Label("submitButtonLabel", new ResourceModel("button.save.editmode"));
        } else {
            submitButtonLabel = new Label("submitButtonLabel", new ResourceModel("button.save.createmode"));
        }
        add(submitButtonLabel);
    }

    private ListView<String> createTagsList(String id, IModel<List<String>> model, final Component tagsField) {
        return new ListView<String>(id, model) {
            @Override
            protected void populateItem(final ListItem<String> item) {
                Label label = new Label("tag", model(from(item.getModel())));
                label.setRenderBodyOnly(true);
                item.add(label);
                item.add(new AjaxEventBehavior("click") {
                    @SuppressWarnings("unchecked")
                    @Override
                    protected void onEvent(AjaxRequestTarget target) {
                        ((List<String>) tagsField.getDefaultModelObject()).add(item.getModelObject());
                        target.appendJavaScript(String.format("$('#%s').tagsinput('add', '%s');", tagsField.getMarkupId(), item.getModelObject()));
                    }
                });
            }

            @Override
            protected ListItem<String> newItem(int index, IModel<String> itemModel) {
                return super.newItem(index, new ClassAwareWrappingModel<String>(itemModel, String.class));
            }
        };
    }

    @Override
    protected void onSubmit() {
        try {
            if (!isEditing) {
                //This prevents the user from creating a completely new budget when trying to
                //edit a newly created budget from the same form
                isEditing = true;
                long newID = service.saveBudget(getModelObject());
                setResponsePage(new EditBudgetPage(EditBudgetPage.createParameters(
                        newID), BudgetsOverviewPage.class, new PageParameters(), true));
            } else {
                service.saveBudget(getModelObject());
                this.success(getString("feedback.success"));
            }
        } catch (InvalidBudgetImportKeyAndNameException e) {
            this.error(getString("feedback.error.duplicateImportKey"));
            this.error(getString("feedback.error.duplicateName"));
        } catch (InvalidBudgetImportKeyException e) {
            this.error(getString("feedback.error.duplicateImportKey"));
        } catch (InvalidBudgetNameException e) {
            this.error(getString("feedback.error.duplicateName"));
        }
    }
}
