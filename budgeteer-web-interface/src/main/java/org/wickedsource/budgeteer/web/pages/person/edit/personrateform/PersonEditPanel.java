package org.wickedsource.budgeteer.web.pages.person.edit.personrateform;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.extensions.markup.html.form.select.IOptionRenderer;
import org.apache.wicket.extensions.markup.html.form.select.Select;
import org.apache.wicket.extensions.markup.html.form.select.SelectOptions;
import org.apache.wicket.injection.Injector;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.joda.money.CurrencyUnit;
import org.joda.money.Money;
import org.wickedsource.budgeteer.service.DateRange;
import org.wickedsource.budgeteer.service.budget.BudgetBaseData;
import org.wickedsource.budgeteer.service.budget.BudgetService;
import org.wickedsource.budgeteer.service.person.PersonRate;
import org.wickedsource.budgeteer.web.BudgeteerSession;
import org.wickedsource.budgeteer.web.components.daterange.DateRangeInputField;
import org.wickedsource.budgeteer.web.components.listMultipleChoiceWithGroups.OptionGroup;
import org.wickedsource.budgeteer.web.components.money.MoneyTextField;
import org.wickedsource.budgeteer.web.components.multiselect.MultiselectBehavior;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public abstract class PersonEditPanel extends Panel {

    private MoneyTextField rateField;
    private DateRangeInputField dateRangeField;
    private Model<ArrayList<BudgetBaseData>> selectedBudgets;

    PersonEditPanel(String id, PersonRateFormDto data, boolean isEditing) {
        super(id);
        this.isEditing = isEditing;

        Injector.get().inject(this);

        selectedBudgets = new Model<>(new ArrayList<>(data.getChosenBudgets()));

        rateField = new MoneyTextField("rateField", new Model<>(data.getRate()));
        rateField.setRequired(true);

        dateRangeField = new DateRangeInputField("dateRangeField", new Model<>(data.getDateRange()), DateRangeInputField.DropLocation.UP);
        dateRangeField.setRequired(true);

        List<OptionGroup<BudgetBaseData>> possibleBudgets =
                budgetService.getPossibleBudgetDataForPersonAndProject(BudgeteerSession.get().getProjectId(), data.getPersonId());

        Form<PersonRateFormDto> form = new Form<>("addRateForm", new Model<>(data));
        form.setOutputMarkupId(true);
        form.add(dateRangeField);
        form.add(rateField);
        form.add(createBudgetsDropdown(possibleBudgets).setOutputMarkupId(true));
        form.add(createSubmitButton());
        add(form);
    }

    public void setData(PersonRateFormDto dto) {
        selectedBudgets.setObject(new ArrayList<>(dto.getChosenBudgets()));
        rateField.setModelObject(dto.getRate());
        dateRangeField.setModelObject(dto.getDateRange());
    }

    private WebMarkupContainer createSubmitButton() {
        Button submitButton = new Button("submitButton") {

            @Override
            public void onSubmit() {
                boolean containsError = false;
                if (rateField.getModelObject().isNegativeOrZero()) {
                    containsError = true;
                    PersonEditPanel.this.getParent().getParent().error(getString("rate.not.zero"));
                }
                if (selectedBudgets.getObject().isEmpty()) {
                    containsError = true;
                    PersonEditPanel.this.getParent().getParent().error(getString("select.budget"));
                } else {
                    for (BudgetBaseData budget : selectedBudgets.getObject()) {
                        List<PersonRate> overlappingRate = getOverlappingRates(dateRangeField.getModelObject(), budget);
                        if (overlappingRate == null || overlappingRate.isEmpty()) {
                            if (!containsError) {
                                rateAdded(new PersonRate(rateField.getModelObject(), budget, dateRangeField.getModelObject()));
                            }
                        } else {
                            containsError = true;
                            PersonEditPanel.this.getParent().getParent().error(String.format(getString("personRateForm.overlappingRates"),
                                    constructOverlappingRatesString(overlappingRate)));
                        }
                    }
                }
                if (!containsError) {
                    clearFormData();
                }
            }
        };
        return addIconToSubmitButton(submitButton);
    }

    private String constructOverlappingRatesString(List<PersonRate> overlappingRate) {
        StringBuilder overlappingEntryNames = new StringBuilder();
        overlappingEntryNames.append(System.getProperty("line.separator"));
        for (int i = 0; i < overlappingRate.size(); i++) {
            PersonRate p = overlappingRate.get(i);
            overlappingEntryNames.append(p.getBudget().getName());
            overlappingEntryNames.append(" ");
            overlappingEntryNames.append(p.getDateRange().toString());
            if (i < overlappingRate.size() - 1) {
                overlappingEntryNames.append(",");
                overlappingEntryNames.append(System.getProperty("line.separator"));
            }
        }
        return overlappingEntryNames.toString();
    }

    private void clearFormData() {
        selectedBudgets.getObject().clear();
        rateField.setModelObject(Money.zero(CurrencyUnit.EUR));
        dateRangeField.setModelObject(new DateRange(new Date(), new Date()));
    }

    private WebMarkupContainer addIconToSubmitButton(WebMarkupContainer submitButton) {

        Label submitIcon = new Label("submitIcon");
        if (isEditing) {
            submitIcon.add(new AttributeModifier("class", "fa fa-check"));
            submitIcon.add(new AttributeModifier("title", "Add this rate"));
        } else {
            submitIcon.add(new AttributeModifier("class", "fa fa-plus-circle"));
            submitIcon.add(new AttributeModifier("title", "Save this rate"));
        }
        submitButton.add(submitIcon);
        submitButton.setOutputMarkupId(true);
        submitIcon.setOutputMarkupId(true);
        return submitButton;
    }

    @SpringBean
    private BudgetService budgetService;

    private boolean isEditing;

    protected abstract void rateAdded(PersonRate rate);

    /**
     * Returns a List<PersonRate> where the startDate or the endDate is in the given dateRange (alternatively a empty List)
     *
     * @param dateRange the dateRange to be checked
     * @return a List of PersonRates (if there are no PersonRates that end or start in the dateRange, a empty List will be returned)
     */
    protected abstract List<PersonRate> getOverlappingRates(DateRange dateRange, BudgetBaseData budget);

    /**
     * This methods adds a dropdown menu for the available budgets.
     *
     * @param possibleBudgets A list of grouped possible budgets
     */
    private Select<ArrayList<BudgetBaseData>> createBudgetsDropdown(List<OptionGroup<BudgetBaseData>> possibleBudgets) {
        Select<ArrayList<BudgetBaseData>> select = new Select<>("select", selectedBudgets);
        HashMap<String, String> multiselectOptions = MultiselectBehavior.getRecommendedOptions();
        multiselectOptions.put("includeSelectAllOption", "false");
        multiselectOptions.put("numberDisplayed","6");
        RepeatingView rv = new RepeatingView("repeatingView");
        rv.setOutputMarkupId(true);
        rv.setOutputMarkupPlaceholderTag(true);
        select.add(rv);
        for (OptionGroup<BudgetBaseData> group : possibleBudgets) {
            WebMarkupContainer overOptGroup = new WebMarkupContainer(rv.newChildId());
            rv.add(overOptGroup);
            WebMarkupContainer optGroup = new WebMarkupContainer("optGroup");
            overOptGroup.add(optGroup);
            optGroup.add(new AttributeModifier("label", new Model<>(group.getGroupNameResourceKey())));
            Model<ArrayList<BudgetBaseData>> a = new Model<>(new ArrayList<>(group.getOptions()));
            optGroup.add(
                    new SelectOptions<>("selectOptions", a, new IOptionRenderer<BudgetBaseData>() {
                        @Override
                        public String getDisplayValue(BudgetBaseData object) {
                            return object.getName();
                        }
                        @Override
                        public IModel<BudgetBaseData> getModel(BudgetBaseData value) {
                            return new Model<>(value);
                        }
                    })
            );
        }
        select.add(new MultiselectBehavior(multiselectOptions));
        return select;
    }
}
