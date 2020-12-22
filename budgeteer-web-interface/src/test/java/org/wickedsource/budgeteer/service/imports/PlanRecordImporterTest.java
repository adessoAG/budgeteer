package org.wickedsource.budgeteer.service.imports;

import com.github.springtestdbunit.annotation.DatabaseOperation;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.github.springtestdbunit.annotation.DatabaseTearDown;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.kubek2k.springockito.annotations.SpringockitoContextLoader;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.wickedsource.budgeteer.importer.resourceplan.ResourcePlanImporter;
import org.wickedsource.budgeteer.imports.api.ImportException;
import org.wickedsource.budgeteer.imports.api.ImportFile;
import org.wickedsource.budgeteer.imports.api.InvalidFileFormatException;
import org.wickedsource.budgeteer.persistence.project.ProjectEntity;
import org.wickedsource.budgeteer.persistence.project.ProjectRepository;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(loader = SpringockitoContextLoader.class, locations = {"classpath:spring-service.xml", "classpath:spring-repository-mock.xml"})
class PlanRecordImporterTest {

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private ImportService importService;


    private void doImport() throws ImportException, InvalidFileFormatException {
        List<ImportFile> importFiles = new ArrayList<ImportFile>();
        importFiles.add(new ImportFile("resource_plan2.xlsx", getClass().getResourceAsStream("resource_plan2.xlsx")));
        importService.doImport(1l, new ResourcePlanImporter(), importFiles);
    }

    @Test
    @DatabaseSetup("doImportWithEmptyDatabase.xml")
    @DatabaseTearDown(value = "doImportWithEmptyDatabase.xml", type = DatabaseOperation.DELETE_ALL)
    void testGetSkippedRecordsNoSkippedRecords() throws Exception {
        Mockito.when(projectRepository.findById(Mockito.anyLong())).thenReturn(Optional.of(new ProjectEntity()));
        doImport();
        List<List<String>> skippedRecords = importService.getSkippedRecords();
        Assertions.assertEquals(0, skippedRecords.size());
    }

    @Test
    @DatabaseSetup("doImportWithEmptyDatabase.xml")
    @DatabaseTearDown(value = "doImportWithEmptyDatabase.xml", type = DatabaseOperation.DELETE_ALL)
    void testGetSkippedRecordsSomeSkippedRecords() throws Exception {
        SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yy");
        ProjectEntity project = new ProjectEntity();
        project.setProjectStart(formatter.parse("02.01.2014"));
        project.setProjectEnd(formatter.parse("12.01.2014"));
        Mockito.when(projectRepository.findById(Mockito.anyLong())).thenReturn(Optional.of(project));
        doImport();
        List<List<String>> skippedRecords = importService.getSkippedRecords();
        Assertions.assertEquals(75, skippedRecords.size());
    }
}
