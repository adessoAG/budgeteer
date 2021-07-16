package org.wickedsource.budgeteer.web.pages.budgets.overview.table.progressbar;

import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.protocol.http.WebSession;

public class ProgressBar extends Panel {

    public ProgressBar(String id, IModel<Double> percentModel) {
        super(id, percentModel);
        add(createBar("bar"));
    }

    private Label createBar(String id) {
        Label bar = new Label(id, () -> String.format(WebSession.get().getLocale(), "%.2f%%", getModel().getObject()));
        bar.add(new AttributeAppender("class", () -> {
            Double progress = getModel().getObject();
            if (progress == null) {
                return " progress-bar-success";
            } else if (progress > 90d) {
                return " progress-bar-danger";
            } else if (progress > 75d) {
                return " progress-bar-warning";
            } else {
                return " progress-bar-success";
            }
        }));
        bar.add(new AttributeAppender("style", () -> {
            int value = (getModel().getObject() == null) ? 0 : getModel().getObject().intValue();
            if (value < 0 ){
                value = 0;
            }
            if (value > 100){
                value = 100;
            }
            return String.format("width: %d%%", value);
        }));
        return bar;
    }

    @SuppressWarnings("unchecked")
    private IModel<Double> getModel() {
        return (IModel<Double>) getDefaultModel();
    }
}
