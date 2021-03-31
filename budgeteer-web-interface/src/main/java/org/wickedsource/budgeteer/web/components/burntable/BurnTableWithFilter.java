package org.wickedsource.budgeteer.web.components.burntable;

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.wickedsource.budgeteer.service.record.WorkRecord;
import org.wickedsource.budgeteer.service.record.WorkRecordFilter;
import org.wickedsource.budgeteer.web.components.burntable.filter.FilterPanel;
import org.wickedsource.budgeteer.web.components.burntable.table.BurnTable;

import java.util.List;

public class BurnTableWithFilter extends Panel {

    private final FilterPanel filterPanel;

    public BurnTableWithFilter(String id, IModel<List<WorkRecord>> model, IModel<WorkRecordFilter> filter) {
        super(id);

        filterPanel = new FilterPanel("filter", filter);
        add(filterPanel);
        add(new BurnTable("table", model));
    }

    public boolean isPersonFilterEnabled() {
        return filterPanel.isPersonFilterEnabled();
    }

    public void setPersonFilterEnabled(boolean personFilterEnabled) {
        filterPanel.setPersonFilterEnabled(personFilterEnabled);
    }

    public boolean isBudgetFilterEnabled() {
        return filterPanel.isBudgetFilterEnabled();
    }

    public void setBudgetFilterEnabled(boolean budgetFilterEnabled) {
        filterPanel.setBudgetFilterEnabled(budgetFilterEnabled);
    }

    public boolean isDaterangeFilterEnabled() {
        return filterPanel.isDaterangeFilterEnabled();
    }

    public void setDaterangeFilterEnabled(boolean daterangeFilterEnabled) {
        filterPanel.setDaterangeFilterEnabled(daterangeFilterEnabled);
    }


}
