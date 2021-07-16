package org.wickedsource.budgeteer.web.pages.templates.edit;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.string.StringValue;
import org.wickedsource.budgeteer.web.BudgeteerSession;
import org.wickedsource.budgeteer.web.pages.base.dialogpage.DialogPageWithBacklink;
import org.wickedsource.budgeteer.web.pages.templates.TemplatesPage;
import org.wickedsource.budgeteer.web.pages.templates.templateimport.TemplateFormInputDto;
import org.wicketstuff.annotation.mount.MountPath;

import static org.wicketstuff.lazymodel.LazyModel.from;
import static org.wicketstuff.lazymodel.LazyModel.model;

/**
 * Creates the form to edit a template. (Reupload, Download, Delete, Edit name/description)
 *
 * @author maximAtanasov
 */
@MountPath("templates/editTemplates/#{id}")
public class EditTemplatePage extends DialogPageWithBacklink {

    /**
     *
     * @param backlinkPage The page to go back to (Here, always the Template Overview page)
     * @param backlinkParameters The parameters to pass to that page.
     */
    public EditTemplatePage(Class<? extends WebPage> backlinkPage, PageParameters backlinkParameters, PageParameters parameters) {
        super(parameters, backlinkPage, backlinkParameters);
        this.getPage().
        add(createBacklink("backlink1"));
        IModel<TemplateFormInputDto> formModel = model(from(new TemplateFormInputDto(BudgeteerSession.get().getProjectId())));
        this.setDefaultModel(formModel);
        long templateId = getTemplateId();
        if(templateId <= 0){
            goBack();
        }else {
            Form<TemplateFormInputDto> form = new EditTemplateForm("editForm", formModel, templateId);
            add(form);
        }
    }

    public EditTemplatePage(PageParameters parameters) {
        this(TemplatesPage.class, new PageParameters(), parameters);
    }

    private long getTemplateId() {
        StringValue value = getPageParameters().get("id");
        if (value == null || value.isEmpty() || value.isNull()) {
            return 0L;
        } else {
            return value.toLong();
        }
    }
}
