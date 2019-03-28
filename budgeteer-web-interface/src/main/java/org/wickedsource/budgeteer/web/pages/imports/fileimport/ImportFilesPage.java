package org.wickedsource.budgeteer.web.pages.imports.fileimport;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.extensions.ajax.markup.html.form.upload.UploadProgressBar;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
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
import org.apache.wicket.request.Url;
import org.apache.wicket.request.handler.resource.ResourceStreamRequestHandler;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.ResourceReference;
import org.apache.wicket.request.resource.UrlResourceReference;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.util.io.IOUtils;
import org.apache.wicket.util.resource.AbstractResourceStreamWriter;
import org.apache.wicket.util.time.Duration;
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

import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Mount("import/importFiles")
public class ImportFilesPage extends DialogPageWithBacklink {

    @SpringBean
    private ImportService service;

    private Importer importer = new AprodaWorkRecordsImporter();

    private List<FileUpload> fileUploads = new ArrayList<>();

    private CustomFeedbackPanel feedback;

    private List<List<String>> skippedImports;

    private UploadProgressBar uploadProgressBar;

    public ImportFilesPage(PageParameters backlinkParameters) {
        this(ImportsOverviewPage.class, new PageParameters());
    }

    public ImportFilesPage(Class<? extends WebPage> backlinkPage, PageParameters backlinkParameters) {
        super(backlinkPage, backlinkParameters);
        add(createBacklink("backlink1"));
        createForm();
    }

    private void createForm() {
        final Form<ImportFormBean> form = new Form<ImportFormBean>("importForm", new ClassAwareWrappingModel<>(new Model<>(new ImportFormBean()), ImportFormBean.class)) {

            boolean uploadCompleted = false;

            @Override
            public void renderHead(IHeaderResponse response) {
                super.renderHead(response);
                if (uploadCompleted) {
                    //If the upload has been successfully completed, display the filled progress bar.
                    String barId = uploadProgressBar.get("bar").getMarkupId();
                    String statusId = uploadProgressBar.get("status").getMarkupId();
                    response.render(JavaScriptHeaderItem.forScript(
                            "var barDiv = document.getElementById(\"" + barId + "\")\n" +
                                    "var statusDiv = document.getElementById(\"" + statusId + "\")\n" +
                                    "barDiv.style.visibility = 'visible'\n" +
                                    "barDiv.style.display = 'block'\n" +
                                    "barDiv.innerHTML= " +
                                    "\"<div class=\\\"wupb-border\\\">" +
                                    "<div class=\\\"wupb-background\\\">" +
                                    "<div class=\\\"wupb-foreground\\\" style=\\\"text-align:center;\\\">" +
                                    "<label>Upload 100% Completed</label>" +
                                    "</div>" +
                                    "</div>" +
                                    "</div>\"\n" +
                                    "statusDiv.style.visibility = 'visible'\n" +
                                    "statusDiv.style.height = '20px'\n" +
                                    "statusDiv.style.display = 'block'\n", "id"));
                }
            }

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
                    uploadCompleted = true;
                } catch (IOException e) {
                    error(String.format(getString("message.ioError"), e.getMessage()));
                } catch (ImportException | IllegalArgumentException e) {
                    error(String.format(getString("message.importError"), e.getMessage()));
                } catch (InvalidFileFormatException e) {
                    error(String.format(getString("message.invalidFileException"), e.getFileName()));
                }
            }
        };


        WebMarkupContainer importFeedback = new WebMarkupContainer("importFeedback") {
            @Override
            public boolean isVisible() {
                return skippedImports != null && !skippedImports.isEmpty();
            }
        };
        IModel fileModel = new LoadableDetachableModel() {
            @Override
            protected Object load() {
                return ImportReportGenerator.generateReport(skippedImports);
            }
        };
        DownloadLink downloadButton = new DownloadLink("downloadButton", fileModel, "Not imported records.xlsx");
        downloadButton = downloadButton.setCacheDuration(Duration.NONE);
        downloadButton = downloadButton.setDeleteAfterDownload(true);
        importFeedback.add(downloadButton);
        form.add(importFeedback);
        add(form);
        feedback = new CustomFeedbackPanel("feedback");
        feedback.setOutputMarkupId(true);
        form.add(feedback);

        ImportersListModel importersListModel = new ImportersListModel();
        DropDownChoice<Importer> importerChoice = new DropDownChoice<>("importerChoice", new PropertyModel<>(this, "importer"), importersListModel, new ImporterChoiceRenderer());

        // Set the UBWWorkRecordsImporter as Default if available
        for (Importer importer : importersListModel.getObject()) {
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

        FileUploadField fileUpload = new FileUploadField("fileUpload", new PropertyModel<>(this, "fileUploads"));
        fileUpload.setRequired(true);
        fileUpload.add(new AttributeModifier("accept", new AcceptedFileExtensionsModel(importer)));
        fileUpload.add(new AjaxEventBehavior("change") {
            @Override
            protected void onEvent(AjaxRequestTarget target) {
                target.add(feedback);
            }
        });
        form.add(fileUpload);

        uploadProgressBar = new UploadProgressBar("progressBar", form, fileUpload) {
            @Override
            protected ResourceReference getCss() {
                return new UrlResourceReference(Url.parse("css/budgeteer/uploadProgressBar.css")).setContextRelative(true);
            }

            @Override
            public Locale getLocale() {
                return Locale.US;
            }
        };
        form.add(uploadProgressBar);
        form.add(createBacklink("backlink2"));
        form.add(createExampleFileButton("exampleFileButton"));
    }

    /**
     * Creates a button to download an example import file.
     */
    private Link createExampleFileButton(String wicketId) {
        return new Link(wicketId) {
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
}
