package org.wickedsource.budgeteer.web.components.daterange;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.head.CssReferenceHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptReferenceHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.apache.wicket.request.resource.ResourceReference;
import org.wickedsource.budgeteer.web.BudgeteerReferences;

import java.util.Map;

public class DateRangePickerBehavior extends Behavior{
    private final Map<String, String> options;
    private final boolean clearable;

    /**
    * Implements the DateRangePicker
    * @param options @see <a href="http://www.daterangepicker.com/#options">Options</a>
    */
    public DateRangePickerBehavior(Map<String, String> options){
        this(options, false);
    }

    public DateRangePickerBehavior(Map<String, String> options, boolean clearable){
        this.options = options;
        this.clearable = clearable;
    }

    @Override
    public void renderHead(Component component, IHeaderResponse response) {
        response.render(JavaScriptReferenceHeaderItem.forReference(BudgeteerReferences.getJQueryReference()));
        response.render(JavaScriptReferenceHeaderItem.forReference(BudgeteerReferences.getMomentJsReference()));
        // include daterangepicker.js
        ResourceReference jsResource = new PackageResourceReference(DateRangeInputField.class, "daterangepicker.js");
        response.render(JavaScriptReferenceHeaderItem.forReference(jsResource));
        // include css
        ResourceReference cssResource = new PackageResourceReference(DateRangeInputField.class, "daterangepicker-bs3.css");
        response.render(CssReferenceHeaderItem.forReference(cssResource));
        // activate daterangepicker on this input field
        component.setOutputMarkupId(true);
        response.render(OnDomReadyHeaderItem.forScript(getScript(component)));
    }

    public String getScript(Component component) {
        StringBuilder script = new StringBuilder(String.format("$('#%s').daterangepicker(", component.getMarkupId()));
        if(options != null && ! options.isEmpty()){
            script.append("{");
            int index = 1;
            for(Map.Entry<String, String> entry : options.entrySet()) {
                script.append(entry.getKey()).append(": ").append(entry.getValue());
                if(index < options.size() || clearable){
                    script.append(",");
                }
                index++;
            }
            if (clearable) {
                script.append("locale: { cancelLabel: 'Clear' }");
            }
            script.append("}");
        }
        script.append(");");
        if (clearable) {
            script.append(String.format("$('#%s').on('cancel.daterangepicker', function(ev, picker) { $(this).val(''); })", component.getMarkupId()));
        }
        return script.toString();
    }
}
