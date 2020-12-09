package org.wickedsource.budgeteer.web.pages.template.templateimport;

import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.file.File;
import org.apache.wicket.util.tester.FormTester;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.wickedsource.budgeteer.service.ReportType;
import org.wickedsource.budgeteer.service.template.TemplateService;
import org.wickedsource.budgeteer.web.AbstractWebTestTemplate;
import org.wickedsource.budgeteer.web.pages.templates.TemplatesPage;
import org.wickedsource.budgeteer.web.pages.templates.templateimport.ImportTemplatesPage;

import java.net.URISyntaxException;

import static org.assertj.core.api.Java6Assertions.assertThat;

public class ImportTemplatesPageTest extends AbstractWebTestTemplate {

    @Autowired
    TemplateService templateServiceMock;

    @Test
    void testRender() {
        WicketTester tester = getTester();
        tester.startPage(new ImportTemplatesPage(TemplatesPage.class, new PageParameters()));
        tester.assertRenderedPage(ImportTemplatesPage.class);
    }

    @Test
    void testBacklink1Click() {
        WicketTester tester = getTester();
        tester.startPage(new ImportTemplatesPage(TemplatesPage.class, new PageParameters()));
        tester.clickLink("backlink1");
        tester.assertRenderedPage(TemplatesPage.class);
    }

    @Test
    void testBacklink2Click() {
        WicketTester tester = getTester();
        tester.startPage(new ImportTemplatesPage(TemplatesPage.class, new PageParameters()));
        tester.clickLink("importForm:backlink2");
        tester.assertRenderedPage(TemplatesPage.class);
    }

    @Test
    void NoDataFormSubmitTest(){
        WicketTester tester = getTester();
        ImportTemplatesPage testPage = new ImportTemplatesPage(TemplatesPage.class, new PageParameters());
        tester.startPage(testPage);
        Assertions.assertNotNull(testPage.get("importForm"));
        FormTester formTester = tester.newFormTester("importForm", false);
        formTester.setClearFeedbackMessagesBeforeSubmit(true);
        formTester.submit();
        tester.assertRenderedPage(ImportTemplatesPage.class);
        tester.assertErrorMessages("Please select a file to import");
        assertThat(tester.getFeedbackMessages(null)).hasSize(1);
    }

    @Test
    void NameAndDescriptionFormSubmitTest(){
        WicketTester tester = getTester();
        ImportTemplatesPage testPage = new ImportTemplatesPage(TemplatesPage.class, new PageParameters());
        tester.startPage(testPage);
        Assertions.assertNotNull(testPage.get("importForm"));
        FormTester formTester = tester.newFormTester("importForm", false);
        formTester.setClearFeedbackMessagesBeforeSubmit(true);
        formTester.setValue("name", "TEST_N");
        formTester.setValue("description", "TEST_D");
        formTester.submit();
        tester.assertRenderedPage(ImportTemplatesPage.class);
        tester.assertErrorMessages("Please select a file to import");
        assertThat(tester.getFeedbackMessages(null)).hasSize(1);
    }

    @Test
    void NoNameNoDescriptionWithFileTest(){ //yes I know this is a horrible name
        Mockito.doCallRealMethod().when(templateServiceMock).validateUploadedTemplateFile(Mockito.any(), Mockito.eq(ReportType.CONTRACT_REPORT));
        Mockito.doCallRealMethod().when(templateServiceMock).isFileExtensionAllowed(Mockito.any());

        WicketTester tester = getTester();
        ImportTemplatesPage testPage = new ImportTemplatesPage(TemplatesPage.class, new PageParameters());
        tester.startPage(testPage);
        Assertions.assertNotNull(testPage.get("importForm"));
        FormTester formTester = tester.newFormTester("importForm", true);
        formTester.setClearFeedbackMessagesBeforeSubmit(true);
        formTester.setFile("fileUpload", getExampleTemplate(), "xlsx");
        formTester.select("type", 1);
        tester.executeAjaxEvent("importForm:save", "onclick");
        tester.assertRenderedPage(ImportTemplatesPage.class);
        tester.assertErrorMessages("You have not entered a name.");

        assertThat(tester.getFeedbackMessages(null)).hasSize(1);
    }

    @Test
    public void NoTypeTest(){
        Mockito.doCallRealMethod().when(templateServiceMock).validateUploadedTemplateFile(Mockito.any(), Mockito.any());
        Mockito.doCallRealMethod().when(templateServiceMock).isFileExtensionAllowed(Mockito.any());

        WicketTester tester = getTester();
        ImportTemplatesPage testPage = new ImportTemplatesPage(TemplatesPage.class, new PageParameters());
        tester.startPage(testPage);
        Assertions.assertNotNull(testPage.get("importForm"));
        FormTester formTester = tester.newFormTester("importForm", true);
        formTester.setClearFeedbackMessagesBeforeSubmit(true);
        formTester.setFile("fileUpload", getExampleTemplate(), "xlsx");
        formTester.setValue("name", "TEST_N");
        formTester.setValue("description", "TEST_D");
        tester.executeAjaxEvent("importForm:save", "onclick");
        tester.assertRenderedPage(ImportTemplatesPage.class);
        tester.assertErrorMessages( "You have not selected a type");
        assertThat(tester.getFeedbackMessages(null)).hasSize(1);
    }

    @Test
    void AllCorrectInputForm(){
        Mockito.doCallRealMethod().when(templateServiceMock).validateUploadedTemplateFile(Mockito.any(), Mockito.eq(ReportType.CONTRACT_REPORT));
        Mockito.doCallRealMethod().when(templateServiceMock).isFileExtensionAllowed(Mockito.any());

        WicketTester tester = getTester();
        ImportTemplatesPage testPage = new ImportTemplatesPage(TemplatesPage.class, new PageParameters());
        tester.startPage(testPage);
        Assertions.assertNotNull(testPage.get("importForm"));
        FormTester formTester = tester.newFormTester("importForm", true);
        formTester.setClearFeedbackMessagesBeforeSubmit(true);
        formTester.setFile("fileUpload", getExampleTemplate(), "xlsx");
        formTester.setValue("name", "TEST_N");
        formTester.setValue("description", "TEST_D");
        formTester.select("type", 1);
        tester.executeAjaxEvent("importForm:save", "onclick");
        tester.assertRenderedPage(ImportTemplatesPage.class);
        tester.assertNoErrorMessage();
        tester.assertFeedbackMessages(null, "The selected files were imported successfully");
        assertThat(tester.getFeedbackMessages(null)).hasSize(1);
    }

    @Test
    void incorrectFileExtension(){
        Mockito.doCallRealMethod().when(templateServiceMock).validateUploadedTemplateFile(Mockito.any(), Mockito.eq(ReportType.BUDGET_REPORT));
        Mockito.doCallRealMethod().when(templateServiceMock).isFileExtensionAllowed(Mockito.any());

        WicketTester tester = getTester();
        ImportTemplatesPage testPage = new ImportTemplatesPage(TemplatesPage.class, new PageParameters());
        tester.startPage(testPage);
        Assertions.assertNotNull(testPage.get("importForm"));
        FormTester formTester = tester.newFormTester("importForm", true);
        formTester.setClearFeedbackMessagesBeforeSubmit(true);
        formTester.setFile("fileUpload", getIncorrectTemplateFile(), "txt");
        formTester.setValue("name", "TEST_N");
        formTester.setValue("description", "TEST_D");
        formTester.select("type", 1);
        tester.executeAjaxEvent("importForm:save", "onclick");
        tester.assertRenderedPage(ImportTemplatesPage.class);
        tester.assertErrorMessages( "The file did not match the expected file format. Please choose a .xlsx-file.");
        assertThat(tester.getFeedbackMessages(null)).hasSize(1);
    }

    private File getIncorrectTemplateFile(){
        try {
            return new File(getClass().getResource("incorrectTemplateFile.txt").toURI());
        }catch (URISyntaxException e){
            e.printStackTrace();
            return null;
        }
    }

    private File getExampleTemplate(){
        try {
            return new File(getClass().getResource("exampleTemplate1.xlsx").toURI());
        }catch (URISyntaxException e){
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void setupTest(){

    }
}
