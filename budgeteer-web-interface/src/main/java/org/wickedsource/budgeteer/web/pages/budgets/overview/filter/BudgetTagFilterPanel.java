package org.wickedsource.budgeteer.web.pages.budgets.overview.filter;

import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.event.Broadcast;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.wickedsource.budgeteer.service.budget.BudgetTagFilter;
import org.wickedsource.budgeteer.web.BudgeteerSession;

import java.util.List;

public class BudgetTagFilterPanel extends Panel {

    public BudgetTagFilterPanel(String id, IModel<List<String>> tagsModel) {
        super(id);
        Form<BudgetTagFilter> form = new Form<BudgetTagFilter>("filterForm") {
            @Override
            protected void onSubmit() {
                send(getPage(), Broadcast.BREADTH, getFilter());
            }
        };
        form.add(createTagButtonList("tagButtonList", tagsModel));
        add(form);
    }

    private ListView<String> createTagButtonList(String id, IModel<List<String>> tagsModel) {
        return new ListView<String>(id, tagsModel) {
            @Override
            protected void populateItem(final ListItem<String> item) {

                Button button = new Button("button") {
                    @Override
                    public void onSubmit() {
                        getFilter().toggleTag(item.getModelObject());
                    }
                };

                button.add(new AttributeAppender("class", () -> getFilter().isTagSelected(item.getModelObject()) ? " bg-light-blue" : " bg-light-gray"));

                Label buttonLabel = new Label("buttonLabel", item.getModelObject());
                buttonLabel.setRenderBodyOnly(true);
                button.add(buttonLabel);
                item.add(button);
                item.setRenderBodyOnly(true);
            }
        };
    }

    @SuppressWarnings("unchecked")
    private BudgetTagFilter getFilter() {
       return BudgeteerSession.get().getBudgetFilter();
    }

}
