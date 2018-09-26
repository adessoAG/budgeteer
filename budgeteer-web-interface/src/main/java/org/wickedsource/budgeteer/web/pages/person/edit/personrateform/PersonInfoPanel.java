package org.wickedsource.budgeteer.web.pages.person.edit.personrateform;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.wickedsource.budgeteer.MoneyUtil;
import org.wickedsource.budgeteer.service.person.PersonBaseData;
import org.wickedsource.budgeteer.service.person.PersonRate;
import org.wickedsource.budgeteer.service.person.PersonService;
import org.wickedsource.budgeteer.service.person.PersonWithRates;

import java.util.List;

public abstract class PersonInfoPanel extends Panel {

    @SpringBean
    private PersonService personService;

    PersonInfoPanel(String id, PersonWithRates personWithRates, PersonRate rate, List<PersonRate> rates) {
        super(id);
        add(new Label("rate", Model.of(MoneyUtil.toDouble(rate.getRate()))));
        add(new Label("budget", rate.getBudget().getName()));
        add(new Label("startDate", rate.getDateRange().getStartDate()));
        add(new Label("endDate", rate.getDateRange().getEndDate()));
        Button deleteButton = new Button("deleteButton") {
            @Override
            public void onSubmit() {
                rates.remove(rate);
                personService.removeDailyRateFromPerson(personWithRates, rate);
            }
        };
        AjaxLink editButton = new AjaxLink("editButton") {
            @Override
            public void onClick(AjaxRequestTarget target) {
                target.add(getEditPanel());
            }
        };
        add(editButton);
        setOutputMarkupId(true);
        deleteButton.setDefaultFormProcessing(false);
        add(deleteButton);
    }

    protected abstract ListItem<PersonRate> getEditPanel();

}
