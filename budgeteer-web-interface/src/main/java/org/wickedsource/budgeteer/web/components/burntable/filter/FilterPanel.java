package org.wickedsource.budgeteer.web.components.burntable.filter;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.event.Broadcast;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.ListMultipleChoice;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.wickedsource.budgeteer.service.DateRange;
import org.wickedsource.budgeteer.service.DateUtil;
import org.wickedsource.budgeteer.service.budget.BudgetBaseData;
import org.wickedsource.budgeteer.service.budget.BudgetService;
import org.wickedsource.budgeteer.service.person.PersonBaseData;
import org.wickedsource.budgeteer.service.person.PersonService;
import org.wickedsource.budgeteer.service.record.WorkRecordFilter;
import org.wickedsource.budgeteer.web.BudgeteerSession;
import org.wickedsource.budgeteer.web.components.budget.BudgetBaseDataChoiceRenderer;
import org.wickedsource.budgeteer.web.components.daterange.DateRangeInputField;
import org.wickedsource.budgeteer.web.components.multiselect.MultiselectBehavior;
import org.wickedsource.budgeteer.web.components.person.PersonBaseDataChoiceRenderer;
import org.wicketstuff.lazymodel.LazyModel;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static org.wicketstuff.lazymodel.LazyModel.from;
import static org.wicketstuff.lazymodel.LazyModel.model;

public class FilterPanel extends Panel {

    private boolean personFilterEnabled = true;

    private boolean budgetFilterEnabled = true;

    private boolean daterangeFilterEnabled = true;

    private boolean sortingFilterEnabled = true;

    private final IModel<WorkRecordFilter> model;

    @SpringBean
    private PersonService personService;

    @SpringBean
    private BudgetService budgetService;

    public FilterPanel(String id, IModel<WorkRecordFilter> model) {
        super(id, model);
        this.model = model;
        Form<WorkRecordFilter> form = new Form<>("filterForm", model);
        form.add(createPersonFilter("personFilterContainer", form));
        form.add(createBudgetFilter("budgetFilterContainer", form));
        form.add(createDaterangeFilter("daterangeFilterContainer", form));
        form.add(createSortingFilter(form));
        form.add(new Button("resetButton") {
            @Override
            public void onSubmit() {
                model.getObject().clearFilter();
            }
        });
        add(form);
    }

    private WebMarkupContainer createSortingFilter(Form<WorkRecordFilter> form) {
        WebMarkupContainer container = new WebMarkupContainer("sortingFilterContainer") {
            @Override
            public boolean isVisible() {
                return isSortingFilterEnabled();
            }
        };
        container.setVisible(isSortingFilterEnabled());
        DropDownChoice<BurnTableSortColumn> columnSelect = new DropDownChoice<>("columnSelect", form.getModelObject().getColumnToSort(), Arrays.asList(BurnTableSortColumn.values()));

        List<String> possibleSortTypes = Arrays.asList("Ascending", "Descending");
        DropDownChoice<String> sortTypeSelect = new DropDownChoice<>("sortTypeSelect", form.getModelObject().getSortType(), possibleSortTypes);

        HashMap<String, String> options = MultiselectBehavior.getRecommendedOptions();
        options.put("buttonWidth", "'120px'");
        options.remove("buttonClass");
        options.put("enableCaseInsensitiveFiltering", "false");
        options.put("enableFiltering","false");

        columnSelect.add(new MultiselectBehavior(options));
        columnSelect.setRequired(false);

        sortTypeSelect.add(new MultiselectBehavior(options));
        sortTypeSelect.setRequired(false);

        container.add(columnSelect);
        container.add(sortTypeSelect);

        return container;
    }

    private WebMarkupContainer createPersonFilter(String id, Form<WorkRecordFilter> form) {
        WebMarkupContainer container = new WebMarkupContainer(id) {
            @Override
            public boolean isVisible() {
                return isPersonFilterEnabled();
            }
        };
        container.setVisible(isPersonFilterEnabled());
        LazyModel<List<PersonBaseData>> chosenPersons = model(from(form.getModelObject().getPersonList()));
        List<PersonBaseData> possiblePersonsFromFilter = form.getModelObject().getPossiblePersons();
        List<PersonBaseData> possiblePersons = possiblePersonsFromFilter.isEmpty() ? personService.loadPeopleBaseData(BudgeteerSession.get().getProjectId()) : possiblePersonsFromFilter;
        ListMultipleChoice<PersonBaseData> selectedPersons =
                new ListMultipleChoice<>("personSelect", chosenPersons,
                        possiblePersons, new PersonBaseDataChoiceRenderer());


        selectedPersons.setRequired(false);
        HashMap<String, String> options = MultiselectBehavior.getRecommendedOptions();
        options.put("buttonWidth", "'250px'");
        options.remove("buttonClass");
        selectedPersons.add(new MultiselectBehavior(options));
        container.add(selectedPersons);
        return container;
    }

    private WebMarkupContainer createBudgetFilter(String id, Form<WorkRecordFilter> form) {
        WebMarkupContainer container = new WebMarkupContainer(id) {
            @Override
            public boolean isVisible() {
                return isBudgetFilterEnabled();
            }
        };
        container.setVisible(isBudgetFilterEnabled());
        List<BudgetBaseData> possibleBudgetsFromFilter = form.getModelObject().getPossibleBudgets();
        List<BudgetBaseData> possibleBudgets = possibleBudgetsFromFilter.isEmpty() ? budgetService.loadBudgetBaseDataForProject(BudgeteerSession.get().getProjectId()) : possibleBudgetsFromFilter;
        LazyModel<List<BudgetBaseData>> chosenBudgets = model(from(form.getModelObject().getBudgetList()));
        ListMultipleChoice<BudgetBaseData> selectedBudgets = new ListMultipleChoice<>("budgetSelect", chosenBudgets, possibleBudgets, new BudgetBaseDataChoiceRenderer());

        HashMap<String, String> options = MultiselectBehavior.getRecommendedOptions();
        options.put("buttonWidth", "'250px'");
        options.remove("buttonClass");
        selectedBudgets.add(new MultiselectBehavior(options));
        selectedBudgets.setRequired(false);
        container.add(selectedBudgets);
        return container;
    }

    private WebMarkupContainer createDaterangeFilter(String id, Form<WorkRecordFilter> form) {
        WebMarkupContainer container = new WebMarkupContainer(id) {
            @Override
            public boolean isVisible() {
                return isDaterangeFilterEnabled();
            }
        };
        DateRangeInputField field = new DateRangeInputField("daterangeInput", model(from(form.getModel()).getDateRange()));
        field.setRequired(false);
        AjaxLink<Void> allTimeFilter = createDateRangeFilterLink("dateRangeAllTime", null, field);
        AjaxLink<Void> currentMonthFilter = createDateRangeFilterLink("dateRangeCurrentMonth", DateUtil.dateRangeForCurrentMonth(), field);
        AjaxLink<Void> lastMonthFilter = createDateRangeFilterLink("dateRangeLastMonth", DateUtil.dateRangeForLastMonth(), field);
        container.add(field);
        container.add(allTimeFilter);
        container.add(currentMonthFilter);
        container.add(lastMonthFilter);
        return container;
    }

    private boolean isSortingFilterEnabled() {
        return sortingFilterEnabled;
    }

    public boolean isPersonFilterEnabled() {
        return personFilterEnabled;
    }

    public void setPersonFilterEnabled(boolean personFilterEnabled) {
        this.personFilterEnabled = personFilterEnabled;
    }

    public boolean isBudgetFilterEnabled() {
        return budgetFilterEnabled;
    }

    public void setBudgetFilterEnabled(boolean budgetFilterEnabled) {
        this.budgetFilterEnabled = budgetFilterEnabled;
    }

    public boolean isDaterangeFilterEnabled() {
        return daterangeFilterEnabled;
    }

    public void setDaterangeFilterEnabled(boolean daterangeFilterEnabled) {
        this.daterangeFilterEnabled = daterangeFilterEnabled;
    }

    private AjaxLink<Void> createDateRangeFilterLink(String id, DateRange dateRange, DateRangeInputField field) {
        return new AjaxLink<Void>(id) {
            @Override
            public void onClick(AjaxRequestTarget target) {
                model.getObject().setDateRange(dateRange);
                target.add(field);
            }
        };
    }
}
