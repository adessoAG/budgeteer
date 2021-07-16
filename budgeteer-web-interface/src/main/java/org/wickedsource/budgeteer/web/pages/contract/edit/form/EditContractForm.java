package org.wickedsource.budgeteer.web.pages.contract.edit.form;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.injection.Injector;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.*;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.validation.validator.RangeValidator;
import org.springframework.dao.DataIntegrityViolationException;
import org.wickedsource.budgeteer.persistence.contract.ContractEntity;
import org.wickedsource.budgeteer.service.DateUtil;
import org.wickedsource.budgeteer.service.contract.ContractBaseData;
import org.wickedsource.budgeteer.service.contract.ContractService;
import org.wickedsource.budgeteer.service.contract.DynamicAttributeField;
import org.wickedsource.budgeteer.web.BudgeteerSession;
import org.wickedsource.budgeteer.web.components.customFeedback.CustomFeedbackPanel;
import org.wickedsource.budgeteer.web.components.daterange.DateInputField;
import org.wickedsource.budgeteer.web.components.fileUpload.CustomFileUpload;
import org.wickedsource.budgeteer.web.components.money.MoneyTextField;

import java.math.BigDecimal;
import java.util.Arrays;

import static org.wicketstuff.lazymodel.LazyModel.from;
import static org.wicketstuff.lazymodel.LazyModel.model;

public class EditContractForm extends Form<ContractBaseData> {

    @SpringBean
    private ContractService service;

    private WebMarkupContainer table;

    private String submitButtonTextKey;

    private TextField<String> newAttributeField;
    private CustomFeedbackPanel feedbackPanel;

    public EditContractForm(String id){
        this(id, null, "button.save.createmode");
    }

    public EditContractForm(String id, IModel<ContractBaseData> model) {
        this(id, model, "button.save.editmode");
    }

    private EditContractForm(String id, IModel<ContractBaseData> model, String submitButtonTextKey) {
        super(id);
        if (model != null) {
            super.setDefaultModel(model);
        } else {
            super.setDefaultModel(Model.of(service.getEmptyContractModel(BudgeteerSession.get().getProjectId())));
        }
        Injector.get().inject(this);
        this.submitButtonTextKey = submitButtonTextKey;
        addComponents();
    }

    private void addComponents() {
        feedbackPanel = new CustomFeedbackPanel("feedback");
        feedbackPanel.setOutputMarkupId(true);
        add(feedbackPanel);

        TextField<String> nameTextfield = new TextField<>("contractName", model(from(getModelObject()).getContractName()));
        nameTextfield.setRequired(true);
        add(nameTextfield);

        TextField<String> internalNumberTextfield = new TextField<>("internalNumber", model(from(getModelObject()).getInternalNumber()));
        internalNumberTextfield.setRequired(true);
        add(internalNumberTextfield);

        MoneyTextField budgetTextfield = new MoneyTextField("budget", model(from(getModelObject()).getBudget()));
        budgetTextfield.setRequired(true);
        add(budgetTextfield);
        
        TextField<BigDecimal> taxrateTextfield = new TextField<>("taxrate", model(from(getModelObject()).getTaxRate()));
        taxrateTextfield.setRequired(true);
        taxrateTextfield.add(RangeValidator.minimum(BigDecimal.ZERO));
        add(taxrateTextfield);

        if(getModelObject().getStartDate() == null){
            getModelObject().setStartDate(DateUtil.getBeginOfYear());
        }
        DateInputField startDateInputField = new DateInputField("startDate", model(from(getModelObject()).getStartDate()), DateInputField.DROP_LOCATION.UP);
        startDateInputField.setRequired(true);
        add(startDateInputField);

        add(new DropDownChoice<ContractEntity.ContractType>("type",
                model(from(getModelObject()).getType()), Arrays.asList(ContractEntity.ContractType.values()),
                new EnumChoiceRenderer<ContractEntity.ContractType>(this)));

        final CustomFileUpload fileUpload = new CustomFileUpload("fileUpload", model(from(getModelObject()).getFileModel()));
        add(fileUpload);

        table = new WebMarkupContainer("attributeTable");
        table.setOutputMarkupId(true);
        table.setOutputMarkupPlaceholderTag(true);
        table.add(new ListView<DynamicAttributeField>("contractAttributes", model(from(getModelObject()).getContractAttributes())) {
            @Override
            protected void populateItem(ListItem<DynamicAttributeField> item) {
                item.add(new Label("attributeTitle", item.getModelObject().getName()));
                item.add(new TextField<String>("attributeValue", model(from(item.getModelObject()).getValue())));
            }
        });
        add(table);
        newAttributeField = new TextField<String>("nameOfNewAttribute", Model.of(" "));
        newAttributeField.setOutputMarkupId(true);
        add(newAttributeField);
        Button addAttribute = new AjaxButton("addAttribute") {
            @Override
            protected void onSubmit(AjaxRequestTarget target) {
                if(newAttributeField.getModelObject() != null) {
                    ((ContractBaseData) getForm().getModelObject()).getContractAttributes().add(new DynamicAttributeField(newAttributeField.getModelObject(), ""));
                    target.add(table, newAttributeField, feedbackPanel);
                } else {
                    this.error(getString("feedback.error.nameEmpty"));
                    target.add(feedbackPanel);
                }
            }
        };
        addAttribute.setOutputMarkupId(true);
        add(addAttribute);
        add(new AjaxButton("save", new StringResourceModel(submitButtonTextKey)) {
            @Override
            protected void onSubmit(AjaxRequestTarget target) {
                try {
                    ((ContractBaseData) getForm().getModelObject()).getFileModel().setFile(fileUpload.getFile());
                    ((ContractBaseData) getForm().getModelObject()).getFileModel().setFileName(fileUpload.getFileName());
                    ((ContractBaseData) getForm().getModelObject()).setContractId(service.save((ContractBaseData) getForm().getModelObject()));
                    if(submitButtonTextKey.equals("button.save.createmode")){
                        submitButtonTextKey = "button.save.editmode";
                        this.setDefaultModel(new StringResourceModel(submitButtonTextKey));
                        target.add(this);
                        this.success(getString("feedback.success.creation"));
                    }else {
                        this.success(getString("feedback.success"));
                    }
                } catch (DataIntegrityViolationException e) {
                    this.error(getString("feedback.error.dataformat.taxrate"));
                } catch (Exception e) {
                    e.printStackTrace();
                    this.error(getString("feedback.error"));
                }
                target.add(feedbackPanel);
            }

            @Override
            protected void onError(AjaxRequestTarget target) {
                target.add(feedbackPanel);
            }
        }.setOutputMarkupId(true));
    }
}
