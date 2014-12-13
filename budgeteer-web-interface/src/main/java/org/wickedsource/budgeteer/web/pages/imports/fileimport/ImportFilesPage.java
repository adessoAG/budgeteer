package org.wickedsource.budgeteer.web.pages.imports.fileimport;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.markup.html.form.upload.FileUploadField;
import org.apache.wicket.markup.html.link.DownloadLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.handler.resource.ResourceStreamRequestHandler;
import org.apache.wicket.request.http.WebResponse;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.util.io.IOUtils;
import org.apache.wicket.util.resource.AbstractResourceStreamWriter;
import org.wickedsource.budgeteer.importer.aproda.AprodaWorkRecordsImporter;
import org.wickedsource.budgeteer.imports.api.ExampleFile;
import org.wickedsource.budgeteer.imports.api.ImportException;
import org.wickedsource.budgeteer.imports.api.ImportFile;
import org.wickedsource.budgeteer.imports.api.Importer;
import org.wickedsource.budgeteer.service.imports.ImportService;
import org.wickedsource.budgeteer.web.BudgeteerSession;
import org.wickedsource.budgeteer.web.ClassAwareWrappingModel;
import org.wickedsource.budgeteer.web.Mount;
import org.wickedsource.budgeteer.web.pages.base.dialogpage.DialogPageWithBacklink;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

@Mount("import/importFiles")
public class ImportFilesPage extends DialogPageWithBacklink {

    @SpringBean
    private ImportService service;

    private Importer importer = new AprodaWorkRecordsImporter();

    private List<FileUpload> fileUploads = new ArrayList<FileUpload>();

    public ImportFilesPage(Class<? extends WebPage> backlinkPage, PageParameters backlinkParameters) {
        super(backlinkPage, backlinkParameters);
        add(createBacklink("backlink1"));

        Form<ImportFormBean> form = new Form<ImportFormBean>("importForm", new ClassAwareWrappingModel<ImportFormBean>(new Model<ImportFormBean>(new ImportFormBean()), ImportFormBean.class)) {
            @Override
            protected void onSubmit() {
                try {
                    List<ImportFile> files = new ArrayList<ImportFile>();
                    for (FileUpload file : fileUploads) {
                        files.add(new ImportFile(file.getClientFileName(), file.getInputStream()));
                    }
                    service.doImport(BudgeteerSession.get().getProjectId(), importer, files);
                    info(getString("message.success"));
                } catch (IOException e) {
                    error(String.format(getString("message.ioError"), e.getMessage()));
                } catch (ImportException e) {
                    error(String.format(getString("message.importError"), e.getMessage()));
                }
            }
        };
        add(form);

        form.add(new FeedbackPanel("feedback"));

        DropDownChoice<Importer> importerChoice = new DropDownChoice<Importer>("importerChoice", new PropertyModel<Importer>(this, "importer"), new ImportersListModel(), new ImporterChoiceRenderer());
        importerChoice.add(new AjaxFormComponentUpdatingBehavior("onchange") {
            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                // simply update model on server side, which Wicket has done for us
            }
        });
        importerChoice.setRequired(true);
        form.add(importerChoice);

        FileUploadField fileUpload = new FileUploadField("fileUpload", new PropertyModel<List<FileUpload>>(this, "fileUploads"));
        fileUpload.setRequired(true);
        fileUpload.add(new AttributeModifier("accept", new AcceptedFileExtensionsModel(importer)));
        form.add(fileUpload);

        form.add(createBacklink("backlink2"));
        form.add(createExampleFileButton("exampleFileButton"));
    }

    private Link createExampleFileButton(String wicketId) {
        Link<Void> downloadLink = new Link<Void>(wicketId) {
            @Override
            public void onClick() {
                final ExampleFile downloadFile = importer.getExampleFile();
                AbstractResourceStreamWriter streamWriter = new AbstractResourceStreamWriter() {
                    @Override
                    public void write(OutputStream output) throws IOException {
                        ByteArrayOutputStream out = new ByteArrayOutputStream();
                        IOUtils.copy(downloadFile.getInputStream(), out);
                        output.write(out.toByteArray());
                    }
                };

                ResourceStreamRequestHandler handler = new ResourceStreamRequestHandler(streamWriter, downloadFile.getFileName());
                getRequestCycle().scheduleRequestHandlerAfterCurrent(handler);
                HttpServletResponse response = (HttpServletResponse) getRequestCycle().getResponse().getContainerResponse();
                response.setContentType(downloadFile.getMimeType());
            }
        };
        return downloadLink;
//
//
//        Button button = new Button(wicketId) {
//            @Override
//            public void onSubmit() {
//                try {
//                    ExampleFile exampleFile = importer.getExampleFile();
//                    HttpServletResponse response = (HttpServletResponse) getRequestCycle().getResponse().getContainerResponse();
//                    response.setContentType(exampleFile.getMimeType());
//                    response.setHeader("Content-Disposition", String.format("attachment;filename=%s", exampleFile.getFileName()));
//                    ServletOutputStream out = response.getOutputStream();
//                    IOUtils.copy(exampleFile.getInputStream(), out);
//                } catch (IOException e) {
//                    throw new WicketRuntimeException(e);
//                }
//            }
//        };
//        button.setDefaultFormProcessing(false);
//        return button;
    }

}
