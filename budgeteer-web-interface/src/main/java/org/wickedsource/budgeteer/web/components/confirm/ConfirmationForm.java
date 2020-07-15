package org.wickedsource.budgeteer.web.components.confirm;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.markup.html.form.Form;

/**
 * A form that shows a javascript confirmation dialog before submitting.
 */
public abstract class ConfirmationForm<T> extends Form<T> {

    public ConfirmationForm(String id, String confirmationMessage) {
        super(id);
        addConfirmation(confirmationMessage);
    }

    public ConfirmationForm(String id, Component confirmationMessageAnchor, String confirmationMessageKey) {
        super(id);
        String confirmationMessage = confirmationMessageAnchor.getString(confirmationMessageKey);
        addConfirmation(confirmationMessage);
    }

    public void addConfirmation(String confirmationMessage) {
        add(new AttributeModifier("submit", String.format("return confirm('%s');", confirmationMessage)));
    }

    public abstract void onSubmit();

}
