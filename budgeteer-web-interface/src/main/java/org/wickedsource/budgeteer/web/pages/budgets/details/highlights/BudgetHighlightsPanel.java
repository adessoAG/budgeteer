package org.wickedsource.budgeteer.web.pages.budgets.details.highlights;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.wickedsource.budgeteer.service.budget.BudgetDetailData;
import org.wickedsource.budgeteer.web.components.money.MoneyLabel;
import org.wickedsource.budgeteer.web.components.percent.PercentageLabel;

import static org.wicketstuff.lazymodel.LazyModel.from;
import static org.wicketstuff.lazymodel.LazyModel.model;

public class BudgetHighlightsPanel extends Panel {

    public BudgetHighlightsPanel(String id, IModel<BudgetDetailData> model) {
        super(id, model);
        add(new Label("name", model(from(model).getName())));
        add(new Label("contract", new PropertyModel<String>(model, "contractName"){
            @Override
            public String getObject() {
                String contract = super.getObject();
                contract = contract == null ? getString("no.contract") : contract;
                return contract;
            }
        }));
        add(new Label("description", new PropertyModel<String>(model, "description"){
            @Override
            public String getObject() {
                String description = super.getObject();
                description = description == null ? getString("no.description") : description;
                return description;
            }
        }));
        add(new MoneyLabel("total", model(from(model).getTotal()), true));
        add(new MoneyLabel("remaining", model(from(model).getRemaining()), true));
        add(new PercentageLabel("progress", model(from(model).getProgress())));
        add(new MoneyLabel("avgDailyRate", model(from(model).getAvgDailyRate()), true));
        add(new Label("lastUpdated", new PropertyModel<String>(model, "lastUpdated"){
            @Override
            public String getObject() {
                String lastUpdated = super.getObject();
                lastUpdated = lastUpdated == null ? getString("no.lastUpdated") : lastUpdated;
                return lastUpdated;
            }
        }));
    }

}
