package org.wickedsource.budgeteer.web.pages.imports.fileimport;

import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.markup.html.form.upload.FileUploadField;
import org.apache.wicket.markup.html.link.DownloadLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.handler.resource.ResourceStreamRequestHandler;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.util.io.IOUtils;
import org.apache.wicket.util.resource.AbstractResourceStreamWriter;
import org.wickedsource.budgeteer.ListUtil;
import org.wickedsource.budgeteer.importer.aproda.AprodaWorkRecordsImporter;
import org.wickedsource.budgeteer.importer.ubw.UBWWorkRecordsImporter;
import org.wickedsource.budgeteer.imports.api.*;
import org.wickedsource.budgeteer.service.imports.ImportService;
import org.wickedsource.budgeteer.web.BudgeteerSession;
import org.wickedsource.budgeteer.web.ClassAwareWrappingModel;
import org.wickedsource.budgeteer.web.Mount;
import org.wickedsource.budgeteer.web.components.customFeedback.CustomFeedbackPanel;
import org.wickedsource.budgeteer.web.pages.base.dialogpage.DialogPageWithBacklink;
import org.wickedsource.budgeteer.web.pages.imports.ImportsOverviewPage;
import org.wickedsource.budgeteer.web.pages.person.overview.PeopleOverviewPage;
import org.wicketstuff.lazymodel.LazyModel;

import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

@Mount("import/importFiles")
public class ImportFilesPage extends DialogPageWithBacklink {

    @SpringBean
    private ImportService service;

    private Importer importer = new AprodaWorkRecordsImporter();

    private List<FileUpload> fileUploads = new ArrayList<>();

    private CustomFeedbackPanel feedback;

    private List<List<String>> skippedImports;

    public ImportFilesPage(PageParameters backlinkParameters) {
        this(ImportsOverviewPage.class, new PageParameters());
    }

    public ImportFilesPage(Class<? extends WebPage> backlinkPage, PageParameters backlinkParameters) {
        super(backlinkPage, backlinkParameters);
        add(createBacklink("backlink1"));
        createForm();
    }

    /**
     * Creates a button to download an example import file.
     */
    private Link createExampleFileButton(String wicketId) {
        return new Link<Void>(wicketId) {
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
                response.setContentType(downloadFile.getContentType());
            }
        };
    }

    public void createForm() {
        final Form<ImportFormBean> form = new Form<ImportFormBean>("importForm", new ClassAwareWrappingModel<ImportFormBean>(new Model<ImportFormBean>(new ImportFormBean()), ImportFormBean.class)) {
            @Override
            protected void onSubmit() {
                try {
                    skippedImports = null;
                    List<ImportFile> files = new ArrayList<>();
                    for (FileUpload file : fileUploads) {
                        if (file.getContentType().equals("application/x-zip-compressed")) {
                            ImportFileUnzipper unzipper = new ImportFileUnzipper(file.getInputStream());
                            files.addAll(unzipper.readImportFiles());
                        } else {
                            files.add(new ImportFile(file.getClientFileName(), file.getInputStream()));
                        }
                    }
                    service.doImport(BudgeteerSession.get().getProjectId(), importer, files);
                    skippedImports = service.getSkippedRecords();
                    success(getString("message.success"));
                } catch (IOException e) {
                    error(String.format(getString("message.ioError"), e.getMessage()));
                } catch (ImportException e) {
                    error(String.format(getString("message.importError"), e.getMessage()));
                } catch (IllegalArgumentException e) {
                    error(String.format(getString("message.importError"), e.getMessage()));
                } catch(InvalidFileFormatException e){
                    error(String.format(getString("message.invalidFileException"), e.getFileName()));
                }
            }


        };
        WebMarkupContainer importFeedback = new WebMarkupContainer("importFeedback"){
            @Override
            public boolean isVisible() {
                return skippedImports != null && !skippedImports.isEmpty();
            }
        };
        IModel fileModel = new LoadableDetachableModel(){
            @Override
            protected Object load() {
                return ImportReportGenerator.generateReport(skippedImports);
            }
        };
        DownloadLink downloadButton = new DownloadLink("downloadButton", fileModel, "Not imported records.xlsx")
                .setCacheDuration(Duration.ZERO)
                .setDeleteAfterDownload(true);
        importFeedback.add(downloadButton);
        form.add(importFeedback);
        add(form);
        feedback = new CustomFeedbackPanel("feedback");
        feedback.setOutputMarkupId(true);
        form.add(feedback);

        var importers =  service.getAvailableImporters();
        DropDownChoice<Importer> importerChoice = new DropDownChoice<>("importerChoice", new PropertyModel<>(this, "importer"), importers, new ImporterChoiceRenderer());

        // Set the UBWWorkRecordsImporter as Default if available
        for (Importer importer : importers) {
            if (importer.getClass() == UBWWorkRecordsImporter.class) {
                importerChoice.setDefaultModelObject(importer);
            }
        }

        importerChoice.add(new AjaxFormComponentUpdatingBehavior("change") {
            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                skippedImports = null;
            }
        });
        importerChoice.setRequired(true);
        form.add(importerChoice);

        FileUploadField fileUpload = new FileUploadField("fileUpload", new PropertyModel<List<FileUpload>>(this, "fileUploads"));
        fileUpload.setRequired(true);
        List<String> supportedExtensions = new ArrayList<>(importer.getSupportedFileExtensions());
        supportedExtensions.add(".zip");
        fileUpload.add(new AttributeModifier("accept", () -> StringUtils.join(supportedExtensions, ",")));
        fileUpload.add(new AjaxEventBehavior("change") {
            @Override
            protected void onEvent(AjaxRequestTarget target) {
                target.add(feedback);
            }
        });
        form.add(fileUpload);

        form.add(createBacklink("backlink2"));
        form.add(createExampleFileButton("exampleFileButton"));
    }

}
