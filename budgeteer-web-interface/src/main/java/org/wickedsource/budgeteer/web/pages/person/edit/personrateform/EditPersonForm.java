package org.wickedsource.budgeteer.web.pages.person.edit.personrateform;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.event.IEvent;
import org.apache.wicket.injection.Injector;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.joda.money.CurrencyUnit;
import org.joda.money.Money;
import org.wickedsource.budgeteer.MoneyUtil;
import org.wickedsource.budgeteer.persistence.record.MissingDailyRateForBudgetBean;
import org.wickedsource.budgeteer.service.DateRange;
import org.wickedsource.budgeteer.service.DateUtil;
import org.wickedsource.budgeteer.service.budget.BudgetBaseData;
import org.wickedsource.budgeteer.service.budget.BudgetService;
import org.wickedsource.budgeteer.service.notification.MissingDailyRateForBudgetNotification;
import org.wickedsource.budgeteer.service.notification.Notification;
import org.wickedsource.budgeteer.service.person.PersonRate;
import org.wickedsource.budgeteer.service.person.PersonService;
import org.wickedsource.budgeteer.service.person.PersonWithRates;
import org.wickedsource.budgeteer.web.BudgeteerSession;
import org.wickedsource.budgeteer.web.components.customFeedback.CustomFeedbackPanel;
import org.wickedsource.budgeteer.web.components.dataTable.DataTableBehavior;
import org.wickedsource.budgeteer.web.components.listMultipleChoiceWithGroups.OptionGroup;
import org.wickedsource.budgeteer.web.components.money.MoneyTextField;
import org.wickedsource.budgeteer.web.pages.person.edit.IEditPersonPageStrategy;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;

import static org.wicketstuff.lazymodel.LazyModel.from;
import static org.wicketstuff.lazymodel.LazyModel.model;

public class EditPersonForm extends Form<PersonWithRates> {

    @SpringBean
    private PersonService personService;

    @SpringBean
    private BudgetService budgetService;

    private final IEditPersonPageStrategy strategy;

    private final List<MissingDailyRateForBudgetBean> missingDailyRateForBudgetBeans;

    private TextField<String> nameTextField;
    private TextField<String> importKeyTextField;
    private TextField<Money> importDefaultDailyRate;

    private WebMarkupContainer missingDailyRateTableContainer;

    /**
     * Use this constructor for a form that creates a new user.
     */
    public EditPersonForm(String id, IEditPersonPageStrategy strategy) {
        super(id, new PersonWithRatesModel(new PersonWithRates()));
        this.missingDailyRateForBudgetBeans = personService.getMissingDailyRatesForPerson(getModelObject().getPersonId());
        this.strategy = strategy;
        Injector.get().inject(this);
        addComponents();
    }

    /**
     * Use this constructor for a form that edits and existing user.
     */
    public EditPersonForm(String id, PersonWithRates person, IEditPersonPageStrategy strategy) {
        super(id, new PersonWithRatesModel(person));
        this.missingDailyRateForBudgetBeans = personService.getMissingDailyRatesForPerson(getModelObject().getPersonId());
        this.strategy = strategy;
        addComponents();
    }

    private void addComponents() {
        setOutputMarkupId(true);
        CustomFeedbackPanel feedbackPanel = new CustomFeedbackPanel("mainFormFeedback");
        feedbackPanel.setOutputMarkupId(true);
        add(feedbackPanel);
        WebMarkupContainer table = new WebMarkupContainer("ratesTable");
        missingDailyRateTableContainer = new WebMarkupContainer("missingDailyRateTableContainer");
        HashMap<String, String> options = DataTableBehavior.getRecommendedOptions();
        options.put("info", Boolean.toString(false));
        options.put("paging", Boolean.toString(false));
        options.put("searching", Boolean.toString(false));
        options.put("order", " [[ 1, \"asc\" ]]");
        table.add(new DataTableBehavior(options));
        nameTextField = new TextField<>("name", model(from(getModelObject()).getName()));
        importKeyTextField = new TextField<>("importKey", model(from(getModelObject()).getImportKey()));
        importDefaultDailyRate = new MoneyTextField("importDefaultRate", Model.of(getModelObject().getDefaultDailyRate()));
        add(nameTextField, importKeyTextField, importDefaultDailyRate);


        missingDailyRateTableContainer.add(createMissingDailyRateTable());
        missingDailyRateTableContainer.add(createMissingDailyRateSubmitButton());
        showMissingDailyRateContainer();
        add(missingDailyRateTableContainer);

        table.add(createRatesList("ratesList").setOutputMarkupId(true));
        PersonEditPanel personEditPanel = new PersonEditPanel("newRatePanel", new PersonRateFormDto(getModelObject().getPersonId(),
                new ArrayList<>(), Money.zero(CurrencyUnit.EUR), new DateRange(new Date(), new Date())), false) {

            @Override
            protected void rateAdded(PersonRate rate) {
                EditPersonForm.this.getModelObject().getRates().add(rate);
                missingDailyRateForBudgetBeans.removeIf(missingDailyRateForBudgetBean ->
                        (missingDailyRateForBudgetBean.getBudgetName().equals(rate.getBudget().getName()) &&
                                missingDailyRateForBudgetBean.getStartDate().compareTo(rate.getDateRange().getStartDate()) == 0 &&
                                missingDailyRateForBudgetBean.getEndDate().compareTo(rate.getDateRange().getEndDate()) == 0)
                );
                missingDailyRateTableContainer.setVisible(false);
            }

            @Override
            protected List<PersonRate> getOverlappingRates(DateRange dateRange, BudgetBaseData budget) {
                List<PersonRate> result = new LinkedList<>();
                for (PersonRate p : EditPersonForm.this.getModelObject().getRates()) {
                    if (p.getBudget().equals(budget) && DateUtil.isDateRangeOverlapping(p.getDateRange(), dateRange)) {
                        result.add(p);
                    }
                }
                return result;
            }
        };

        table.add(personEditPanel);

        Button submitButton = new Button("submitButton") {
            @Override
            public void onSubmit() {
                if (importKeyTextField.getInput() == null || importKeyTextField.getInput().isEmpty()) {
                    EditPersonForm.this.error(getString("form.missing.import.key"));
                }
                if (nameTextField.getInput() == null || nameTextField.getInput().isEmpty()) {
                    EditPersonForm.this.error(getString("form.missing.name"));
                }

                Money defaultDailyRate = getDefaultDailyRate();
                if (!EditPersonForm.this.hasErrorMessage()) {
                    EditPersonForm.this.getModelObject().setName(nameTextField.getInput());
                    EditPersonForm.this.getModelObject().setImportKey(importKeyTextField.getInput());
                    EditPersonForm.this.getModelObject().setDefaultDailyRate(defaultDailyRate);
                    List<String> errors = personService.savePersonWithRates(EditPersonForm.this.getModelObject());
                    List<String> warnings = personService.getOverlapWithManuallyEditedRecords(EditPersonForm.this.getModelObject(),
                            BudgeteerSession.get().getProjectId());
                    this.success(getString("form.success"));
                    for (String warning : warnings) {
                        this.info(warning);
                    }
                    for (String error : errors) {
                        this.error(error);
                    }
                }
                showMissingDailyRateContainer();
            }
        };
        submitButton.setDefaultFormProcessing(false);
        submitButton.add(strategy.createSubmitButtonLabel("submitButtonTitle"));
        add(submitButton);
        add(table);
    }

    private ListView<MissingDailyRateForBudgetBean> createMissingDailyRateTable() {
        ListView<MissingDailyRateForBudgetBean> missingDailyRatesListView = new ListView<MissingDailyRateForBudgetBean>
                ("missingDailyRates", missingDailyRateForBudgetBeans) {
            public void populateItem(final ListItem<MissingDailyRateForBudgetBean> item) {
                final MissingDailyRateForBudgetBean data = item.getModelObject();
                item.add(new Label("budgetComponent", data.getBudgetName()));
                item.add(new Label("personComponent", data.getPersonName()));
                item.add(new Label("startDateComponent", data.getStartDate()));
                item.add(new Label("endDateComponent", data.getEndDate()));
            }
        };
        missingDailyRatesListView.setOutputMarkupId(true);
        return missingDailyRatesListView;
    }

    private Button createMissingDailyRateSubmitButton() {
        return new Button("fixRates") {
            @Override
            public void onSubmit() {
                Money currentDefaultDailyRate = getDefaultDailyRate();
                if (currentDefaultDailyRate == null) {
                    EditPersonForm.this.error(getString("form.missing.defaultDailyRate"));
                    return;
                }

                List<PersonRate> missingRates = missingDailyRateForBudgetBeans.stream()
                        .map(missingDailyRate -> new PersonRate(currentDefaultDailyRate,
                                budgetService.loadBudgetBaseData(missingDailyRate.getBudgetId()),
                                new DateRange(missingDailyRate.getStartDate(), missingDailyRate.getEndDate())
                        ))
                        .collect(Collectors.toList());

                EditPersonForm.this.getModelObject().getRates().addAll(missingRates);
                missingDailyRateForBudgetBeans.clear();
                showMissingDailyRateContainer();
            }
        }.setDefaultFormProcessing(false);
    }

    private void showMissingDailyRateContainer() {
        missingDailyRateTableContainer.setVisible(!missingDailyRateForBudgetBeans.isEmpty());
    }

    private Money getDefaultDailyRate() {
        if ((importDefaultDailyRate.getInput() == null || importDefaultDailyRate.getInput().isEmpty())) {
            return null;
        }

        DecimalFormat df = new DecimalFormat();
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setDecimalSeparator(',');
        symbols.setGroupingSeparator('.');
        df.setDecimalFormatSymbols(symbols);

        double defaultDailyRate;
        try {
            defaultDailyRate = df.parse(importDefaultDailyRate.getInput()).doubleValue();
        } catch (ParseException e) {
            EditPersonForm.this.error(getString("form.invalid.defaultDailyRate"));
            return null;
        }
        if (defaultDailyRate < 0) {
            EditPersonForm.this.error(getString("form.negative.defaultDailyRate"));
            return null;
        }
        return MoneyUtil.createMoney(defaultDailyRate);
    }

    @Override
    public void onEvent(IEvent<?> event) {
        if (event.getPayload() instanceof Notification) {
            MissingDailyRateForBudgetNotification notification = (MissingDailyRateForBudgetNotification) event.getPayload();
            DateRange dateRange = new DateRange(notification.getStartDate(), notification.getEndDate());
            String budgetName = notification.getBudgetName();
            BudgetBaseData budgetData;
            for (OptionGroup<BudgetBaseData> e : budgetService.getPossibleBudgetDataForPersonAndProject(BudgeteerSession.get().getProjectId(), getModelObject().getPersonId())) {
                for (BudgetBaseData budget : e.getOptions()) {
                    if (budget.getName().equals(budgetName)) {
                        budgetData = budget;
                        ((PersonEditPanel) this.get("ratesTable").get("newRatePanel")).setData(new PersonRateFormDto(this.getModelObject().getPersonId(),
                                Collections.singletonList(budgetData), Money.zero(CurrencyUnit.EUR), dateRange));
                    }
                }
            }
            RequestCycle.get().find(AjaxRequestTarget.class).ifPresent(target -> target.add(this));
        }
    }

    private ListView<PersonRate> createRatesList(String id) {
        return new ListView<PersonRate>(id, model(from(getModel()).getRates())) {

            private String infoOrEditPanel = "infoOrEditPanel";

            @Override
            protected void populateItem(final ListItem<PersonRate> item) {
                item.setOutputMarkupId(true);
                item.add(new PersonInfoPanel(infoOrEditPanel, EditPersonForm.this.getModelObject(), item.getModelObject(), EditPersonForm.this.getModelObject().getRates()) {

                    @Override
                    protected ListItem<PersonRate> getEditPanel() {

                        PersonRateFormDto dto = new PersonRateFormDto(EditPersonForm.this.getModelObject().getPersonId(),
                                Collections.singletonList(item.getModelObject().getBudget()),
                                item.getModelObject().getRate(),
                                item.getModelObject().getDateRange());
                        PersonEditPanel editPanel = createPanelToEditExistingPerson(infoOrEditPanel, dto, item.getModelObject());
                        editPanel.setOutputMarkupId(true);
                        item.remove(infoOrEditPanel);
                        item.add(editPanel);
                        return item;
                    }
                }.setOutputMarkupId(true));
            }

            @Override
            protected ListItem<PersonRate> newItem(int index, IModel<PersonRate> itemModel) {
                // wrap into Model that can be used with LazyModel
                return super.newItem(index, new PersonRateModel(itemModel));
            }
        };
    }

    private PersonEditPanel createPanelToEditExistingPerson(String id, PersonRateFormDto dto, PersonRate previousRate) {
        return new PersonEditPanel(id, dto, true) {

            boolean newRateIsAdded = false;

            @Override
            protected void rateAdded(PersonRate rate) {
                if (newRateIsAdded) {
                    getModelObject().getRates().add(rate);
                } else {
                    getModelObject().getRates().replaceAll(personRate -> {
                        if (personRate.equals(previousRate)) {
                            newRateIsAdded = true;
                            return rate;
                        } else {
                            return personRate;
                        }
                    });
                }
            }

            @Override
            protected List<PersonRate> getOverlappingRates(DateRange dateRange, BudgetBaseData budget) {
                List<PersonRate> result = new LinkedList<>();
                for (PersonRate p : getModelObject().getRates()) {
                    if (!p.equals(previousRate) && p.getBudget().equals(budget) && DateUtil.isDateRangeOverlapping(p.getDateRange(), dateRange)) {
                        result.add(p);
                    }
                }
                return result;
            }
        };
    }
}
