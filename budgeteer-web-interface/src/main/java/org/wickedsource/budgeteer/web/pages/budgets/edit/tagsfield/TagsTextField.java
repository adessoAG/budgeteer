package org.wickedsource.budgeteer.web.pages.budgets.edit.tagsfield;

import org.apache.wicket.markup.head.CssReferenceHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.JavaScriptReferenceHeaderItem;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.apache.wicket.request.resource.ResourceReference;
import org.apache.wicket.util.convert.IConverter;
import org.wickedsource.budgeteer.web.BudgeteerReferences;

import java.util.List;

public class TagsTextField extends TextField<List<String>> {

    public TagsTextField(String id, IModel<List<String>> model) {
        super(id, model);
    }

    @Override
    protected IConverter<?> createConverter(Class<?> type) {
        return new TagsConverter();
    }

    @Override
    public void convertInput() {
        IConverter<List<String>> converter = getConverter(getType());
        setConvertedInput(converter.convertToObject(getValue(), getLocale()));
    }

    @Override
    protected String[] getInputTypes() {
        return new String[]{"text"};
    }

    @Override
    public void renderHead(IHeaderResponse response) {
        // jquery resource
        response.render(JavaScriptReferenceHeaderItem.forReference(BudgeteerReferences.getJQueryReference()));
        // include javascript
        ResourceReference jsResource = new PackageResourceReference(TagsTextField.class, "bootstrap-tagsinput.min.js");
        response.render(JavaScriptReferenceHeaderItem.forReference(jsResource));
        // include css
        ResourceReference cssResource = new PackageResourceReference(TagsTextField.class, "bootstrap-tagsinput.css");
        response.render(CssReferenceHeaderItem.forReference(cssResource));
        // activate tagsinput on this input field
        response.render(JavaScriptHeaderItem.forScript(String.format("window.onload = function () {\n" +
                "     $('#%s').tagsinput({\n" +
                "            maxChars: 15,\n" +
                "            tagClass: function (item) {\n" +
                "                return 'badge bg-light-blue';\n" +
                "            }\n" +
                "        });\n" +
                "}", getMarkupId()), "activate-tagsinput"));
    }
}
