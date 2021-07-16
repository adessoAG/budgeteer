package org.wickedsource.budgeteer.web.pages.person.details.highlights;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;
import org.wickedsource.budgeteer.MoneyUtil;
import org.wickedsource.budgeteer.service.DateUtil;
import org.wickedsource.budgeteer.service.person.PersonDetailData;
import org.wickedsource.budgeteer.web.components.datelabel.DateLabel;
import org.wickedsource.budgeteer.web.components.money.MoneyLabel;
import org.wickedsource.budgeteer.web.components.nullmodel.NullsafeModel;

import static org.wicketstuff.lazymodel.LazyModel.from;
import static org.wicketstuff.lazymodel.LazyModel.model;

public class PersonHighlightsPanel extends GenericPanel<PersonDetailData> {

    public PersonHighlightsPanel(String id, IModel<PersonDetailData> model) {
        super(id, model);
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();
        add(new Label("name", nullsafeModel(model(from(getModel()).getName()))));
        add(new MoneyLabel("avgDailyRate", model(from(getModel()).getAverageDailyRate()), true));
        add(getModelObject().getDefaultDailyRate() == null ?
                new Label("defaultDailyRate", getString("nullString"))
                : new MoneyLabel("defaultDailyRate", model(from(getModel()).getDefaultDailyRate()), true));
        add(new DateLabel("firstBookedDate", model(from(getModel()).getFirstBookedDate()), true));
        add(new DateLabel("lastBookedDate", model(from(getModel()).getLastBookedDate()), true));
        add(new Label("hoursBooked", nullsafeDoubleModel(model(from(getModel()).getHoursBooked()))));
        add(new MoneyLabel("budgetBurned_net", model(from(getModel()).getBudgetBurned()), true));
    }

    private IModel<String> nullsafeModel(IModel<String> wrappedModel){
        return new NullsafeModel<>(wrappedModel, getString("nullString"));
    }

    private IModel<Double> nullsafeDoubleModel(IModel<Double> wrappedModel){
        return new NullsafeModel<>(wrappedModel, 0d);
    }

}
