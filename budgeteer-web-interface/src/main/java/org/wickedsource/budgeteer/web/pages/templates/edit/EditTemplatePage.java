package org.wickedsource.budgeteer.web.pages.templates.edit;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.markup.html.form.upload.FileUploadField;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.handler.resource.ResourceStreamRequestHandler;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.util.resource.AbstractResourceStreamWriter;
import org.wickedsource.budgeteer.imports.api.ImportFile;
import org.wickedsource.budgeteer.service.template.TemplateService;
import org.wickedsource.budgeteer.web.BudgeteerSession;
import org.wickedsource.budgeteer.web.ClassAwareWrappingModel;
import org.wickedsource.budgeteer.web.Mount;
import org.wickedsource.budgeteer.web.components.customFeedback.CustomFeedbackPanel;
import org.wickedsource.budgeteer.web.pages.base.dialogpage.DialogPageWithBacklink;
import org.wickedsource.budgeteer.web.pages.templates.templateimport.TemplateFormInputDto;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import static org.wicketstuff.lazymodel.LazyModel.from;
import static org.wicketstuff.lazymodel.LazyModel.model;

/**
 * Creates the form to edit a template. (Reupload, Download, Delete, Edit name/description)
 *
 * @author maximAtanasov
 */
@Mount("templates/editTemplates")
public class EditTemplatePage extends DialogPageWithBacklink {

    @SpringBean
    private TemplateService service;

    private long templateID;

    private List<FileUpload> fileUploads = new ArrayList<>();

    private TemplateFormInputDto templateFormInputDto = new TemplateFormInputDto(BudgeteerSession.get().getProjectId());

    /**
     *
     * @param backlinkPage The page to go back to (Here, always the Template Overview page)
     * @param backlinkParameters The parameters to pass to that page.
     * @param templateID the ID of the template we want to edit.
     */
    public EditTemplatePage(Class<? extends WebPage> backlinkPage, PageParameters backlinkParameters, long templateID) {
        super(backlinkPage, backlinkParameters);
        this.templateID = templateID;
        add(createBacklink("backlink1"));
        IModel formModel = model(from(templateFormInputDto));

        this.setDefaultModel(formModel);
        final Form<TemplateFormInputDto> form = new Form<TemplateFormInputDto>("editForm", new ClassAwareWrappingModel<>(new Model<>(new TemplateFormInputDto(BudgeteerSession.get().getProjectId())), TemplateFormInputDto.class)) {

            private long templateId = templateID;

            @Override
            protected void onSubmit() {
                try {
                    if(model(from(templateFormInputDto)).getObject().getName() == null){
                        error(getString("message.error.no.name"));
                    }
                    if(model(from(templateFormInputDto)).getObject().getDescription() == null){
                        error(getString("message.error.no.description"));
                    }
                    if(fileUploads != null && fileUploads.size() > 0){
                        ImportFile file = new ImportFile(fileUploads.get(0).getClientFileName(), fileUploads.get(0).getInputStream());
                        if(model(from(templateFormInputDto)).getObject().getName() != null && model(from(templateFormInputDto)).getObject().getDescription() != null){
                            templateId = service.editTemplate(BudgeteerSession.get().getProjectId(), templateId, file, model(from(templateFormInputDto)));
                            success(getString("message.success"));
                            updateTemplateID(templateId);
                        }
                    }else if(model(from(templateFormInputDto)).getObject().getName() != null && model(from(templateFormInputDto)).getObject().getDescription() != null){
                        templateId = service.editTemplate(BudgeteerSession.get().getProjectId(), templateId, null, model(from(templateFormInputDto)));
                        success(getString("message.success"));
                        updateTemplateID(templateId);
                    }
                } catch (IOException e) {
                    error(String.format(getString("message.ioError"), e.getMessage()));
                }  catch (IllegalArgumentException e) {
                    error(String.format(getString("message.importError"), e.getMessage()));
                }
            }
        };

        add(form);

        CustomFeedbackPanel feedback = new CustomFeedbackPanel("feedback");
        feedback.setOutputMarkupId(true);
        form.add(feedback);

        FileUploadField fileUpload = new FileUploadField("fileUpload", new PropertyModel<List<FileUpload>>(this, "fileUploads"));
        fileUpload.setRequired(false);

        form.add(fileUpload);
        form.add(createBacklink("backlink2"));

        templateFormInputDto.setName(service.getById(templateID).getName());
        templateFormInputDto.setDescription(service.getById(templateID).getDescription());
        form.add(DeleteTemplateButton("deleteButton"));
        form.add(DownloadFileButton("downloadFileButton"));
        form.add(new TextField<>("name", model(from(templateFormInputDto).getName())));
        form.add(new TextField<>("description", model(from(templateFormInputDto).getDescription())));
    }

    private void updateTemplateID(long newID){
        templateID = newID;
    }

    /**
     * Creates a button to download the template that is being edited.
     */
    private Link DownloadFileButton(String wicketId) {
        return new Link<Void>(wicketId) {
            @Override
            public void onClick() {
                XSSFWorkbook wb = service.getById(templateID).getWb();
                AbstractResourceStreamWriter streamWriter = new AbstractResourceStreamWriter() {
                    @Override
                    public void write(OutputStream output) throws IOException {
                        wb.write(output);
                    }
                };
                ResourceStreamRequestHandler handler = new ResourceStreamRequestHandler(streamWriter, service.getById(templateID).getName() + ".xlsx");
                getRequestCycle().scheduleRequestHandlerAfterCurrent(handler);
                HttpServletResponse response = (HttpServletResponse) getRequestCycle().getResponse().getContainerResponse();
                response.setContentType(null);
            }
        };
    }

    /**
     * Creates a button to delete the template that is being edited.
     */
    private Link DeleteTemplateButton(String wicketId) {
        return new Link<Void>(wicketId) {
            @Override
            public void onClick() {
                service.deleteTemplate(templateID);
                goBack(); //Go back to the templates overview after deleting the template
            }
        };
    }
}
