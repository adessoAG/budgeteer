package org.wickedsource.budgeteer.importer.ubw;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.wickedsource.budgeteer.imports.api.ExampleFile;
import org.wickedsource.budgeteer.imports.api.ImportFile;
import org.wickedsource.budgeteer.imports.api.ImportedWorkRecord;
import org.wickedsource.budgeteer.imports.api.InvalidFileFormatException;

import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UBWWorkRecordsImporterTest {

    private DateFormat format = new SimpleDateFormat("dd.MM.yyyy");

    @Test
    void testReadWithAANrColumn() throws Exception {
        UBWWorkRecordsImporter importer = new UBWWorkRecordsImporter();
        InputStream in = getClass().getResourceAsStream("/demo_ubw_report2.xlsx");
        List<ImportedWorkRecord> records = importer.importFile(new ImportFile("file.xslx", in));
        assertEquals(1225, records.size());
        assertEquals("Archie, Holmes", records.get(0).getPersonName());
        assertEquals("Collecting Requirements", records.get(0).getBudgetName());
        assertEquals(570d, records.get(0).getMinutesWorked(), 1d);
        assertEquals(format.parse("09.01.2017"), records.get(0).getDate());
    }

    @Test
    void testRead() throws Exception {
        UBWWorkRecordsImporter importer = new UBWWorkRecordsImporter();
        InputStream in = getClass().getResourceAsStream("/demo_ubw_report.xlsx");
        List<ImportedWorkRecord> records = importer.importFile(new ImportFile("file.xslx", in));
        assertEquals(1225, records.size());
        assertEquals("Archie, Holmes", records.get(0).getPersonName());
        assertEquals("Collecting Requirements", records.get(0).getBudgetName());
        assertEquals(570d, records.get(0).getMinutesWorked(), 1d);
        assertEquals(format.parse("09.01.2017"), records.get(0).getDate());
    }

    @Test
    void testReadOldFormat() throws Exception {
        UBWWorkRecordsImporter importer = new UBWWorkRecordsImporter();
        InputStream in = getClass().getResourceAsStream("/demo_ubw_report_old.xlsx");
        List<ImportedWorkRecord> records = importer.importFile(new ImportFile("file.xslx", in));
        assertEquals(1225, records.size());
        assertEquals("Archie, Holmes", records.get(0).getPersonName());
        assertEquals("Collecting Requirements", records.get(0).getBudgetName());
        assertEquals(570d, records.get(0).getMinutesWorked(), 1d);
        assertEquals(format.parse("09.01.2017"), records.get(0).getDate());
    }

    @Test
    void testGetSkippedDataSets() throws Exception {
        UBWWorkRecordsImporter importer = new UBWWorkRecordsImporter();
        importer.importFile(new ImportFile("file.xslx", importer.getExampleFile().getInputStream()));
        List<List<String>> skippedRecords = importer.getSkippedRecords();
        assertEquals(38, skippedRecords.size());

        assertEquals("file.xslx", skippedRecords.get(1).get(0));
    }

    @Test
    void testGetExampleFile() {
        UBWWorkRecordsImporter importer = new UBWWorkRecordsImporter();
        ExampleFile file = importer.getExampleFile();
        assertNotNull(file.getFileName());
        assertNotNull(file.getInputStream());
        assertNotNull(file.getContentType());
    }

    @Test
    void testValidity() throws IOException {
        UBWWorkRecordsImporter importer = new UBWWorkRecordsImporter();
        Workbook workbook = new XSSFWorkbook(importer.getExampleFile().getInputStream());
        assertTrue(importer.findTableInfoForValidSheet(workbook).isPresent());
    }

    @Test
    void testInvalidFile() {
        Assertions.assertThrows(InvalidFileFormatException.class, () -> {
            UBWWorkRecordsImporter importer = new UBWWorkRecordsImporter();
            InputStream in = getClass().getResourceAsStream("/demo_ubw_report_invalid.xlsx");
            importer.importFile(new ImportFile("demo_ubw_report_invalid.xslx", in));
        });
    }
}
