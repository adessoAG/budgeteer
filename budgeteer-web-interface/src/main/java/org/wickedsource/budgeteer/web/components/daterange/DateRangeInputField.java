package org.wickedsource.budgeteer.web.components.daterange;


import org.apache.wicket.markup.head.CssReferenceHeaderItem;
import org.apache.wicket.markup.head.JavaScriptReferenceHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.internal.HtmlHeaderContainer;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.apache.wicket.request.resource.ResourceReference;
import org.apache.wicket.util.convert.IConverter;
import org.wickedsource.budgeteer.service.DateRange;
import org.wickedsource.budgeteer.web.BudgeteerReferences;

public class DateRangeInputField extends TextField<DateRange> {

    public DateRangeInputField(String id) {
        super(id);
        setOutputMarkupId(true);
    }

    public DateRangeInputField(String id, IModel<DateRange> model) {
        super(id, model);
        setOutputMarkupId(true);
    }


    @Override
    @SuppressWarnings("unchecked")
    public <DateRange> IConverter<DateRange> getConverter(Class<DateRange> type) {
        return (IConverter<DateRange>) new DateRangeConverter();
    }

    @Override
    protected String getInputType() {
        return "text";
    }

    @Override
    public void renderHead(HtmlHeaderContainer container) {
        // jquery resource
        container.getHeaderResponse().render(JavaScriptReferenceHeaderItem.forReference(BudgeteerReferences.getJQueryReference()));
        container.getHeaderResponse().render(JavaScriptReferenceHeaderItem.forReference(BudgeteerReferences.getMomentJsReference()));
        // include daterangepicker.js
        ResourceReference jsResource = new PackageResourceReference(DateRangeInputField.class, "daterangepicker.js");
        container.getHeaderResponse().render(JavaScriptReferenceHeaderItem.forReference(jsResource));
        // include css
        ResourceReference cssResource = new PackageResourceReference(DateRangeInputField.class, "daterangepicker-bs3.css");
        container.getHeaderResponse().render(CssReferenceHeaderItem.forReference(cssResource));
        // activate daterangepicker on this input field
       container.getHeaderResponse().render(OnDomReadyHeaderItem.forScript(String.format("$('#%s').daterangepicker();", getMarkupId())));
    }

}
