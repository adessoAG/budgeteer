package org.wickedsource.budgeteer.web.components.dataTable.editableMoneyField;

import org.apache.wicket.MarkupContainer;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;
import org.joda.money.Money;
import org.wickedsource.budgeteer.web.components.dataTable.CustomDataTableEventBehavior;
import org.wickedsource.budgeteer.web.components.money.MoneyLabel;
import org.wickedsource.budgeteer.web.components.money.MoneyTextField;
import org.wicketstuff.lambda.components.ComponentFactory;

public abstract class EditableMoneyField extends GenericPanel<Money> {

    private boolean isEditable;
    private Label label;

    private final WebMarkupContainer container;
    private TextField<Money> rateField;
    private Form<Money> form;

    public EditableMoneyField(final String id, final MarkupContainer table, final IModel<Money> model) {
        this(id, table, model, false);
    }

    public EditableMoneyField(final String id, final MarkupContainer table, final IModel<Money> model, boolean editable) {
        super(id, model);
        this.isEditable = editable;
        container = new WebMarkupContainer("container");
        container.setOutputMarkupId(true);
        label = new MoneyLabel("label", getModel()) {
            @Override
            public boolean isVisible() {
                return !isEditable;
            }
        };
        label.setOutputMarkupId(true);
        this.add(label);

        form = new Form<Money>("editor", getModel()) {
            @Override
            public boolean isVisible() {
                return isEditable;
            }
        };
        rateField = new MoneyTextField("input", getModel());
        rateField.setOutputMarkupId(true);

        AjaxButton save = new AjaxButton("save", form) {
            @Override
            protected void onSubmit(AjaxRequestTarget target) {
                isEditable = false;
                target.add(container);
                save(target, EditableMoneyField.this.form);
            }
            @Override
            protected void onError(AjaxRequestTarget target){
                isEditable = true;
                target.add(container);
                convertError(target);
            }
        };
        AjaxButton cancel = new AjaxButton("cancel"){
            @Override
            protected void onSubmit(AjaxRequestTarget target) {
                isEditable = false;
                target.add(container);
            }
        };
        cancel.setDefaultFormProcessing(false);

        form.setOutputMarkupId(true);
        rateField.setRequired(true);
        form.add(rateField);
        form.add(save);
        form.add(cancel);

        container.add(label);
        container.add(form);

        super.add(container);
        super.setOutputMarkupPlaceholderTag(true);
        label.add(new CustomDataTableEventBehavior(table, "click") {
            @Override
            protected void onEvent(AjaxRequestTarget target) {
                isEditable = true;
                target.add(container);
            }
        });
    }

    protected abstract void save(AjaxRequestTarget target, Form<Money> form);

    protected abstract void cancel(AjaxRequestTarget target, Form<Money> form);

    protected abstract void convertError(AjaxRequestTarget target);


}
