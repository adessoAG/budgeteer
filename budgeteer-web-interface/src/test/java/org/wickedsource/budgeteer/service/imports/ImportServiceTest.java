package org.wickedsource.budgeteer.service.imports;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.wickedsource.budgeteer.persistence.imports.ImportEntity;
import org.wickedsource.budgeteer.persistence.imports.ImportRepository;
import org.wickedsource.budgeteer.persistence.record.WorkRecordRepository;
import org.wickedsource.budgeteer.service.ServiceTestTemplate;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.mockito.Mockito.*;

class ImportServiceTest extends ServiceTestTemplate {

    @Autowired
    private ImportRepository importRepository;

    @Autowired
    private WorkRecordRepository workRecordRepository;

    @Autowired
    private ImportService importService;

    @Test
    void testLoadImports() throws Exception {
        when(importRepository.findByProjectId(1L)).thenReturn(Arrays.asList(createImportEntity()));
        List<Import> imports = importService.loadImports(1L);
        Assertions.assertEquals(1, imports.size());
        Assertions.assertEquals("TestImport", imports.get(0).getImportType());
    }

    @Test
    void testDeleteImport() throws Exception {
        importService.deleteImport(1L);
        verify(importRepository, times(1)).deleteById(1L);
        verify(workRecordRepository, times(1)).deleteByImport(1L);
    }

    private ImportEntity createImportEntity() {
        ImportEntity entity = new ImportEntity();
        entity.setEndDate(new Date());
        entity.setStartDate(new Date());
        entity.setImportType("TestImport");
        entity.setId(1L);
        return entity;
    }

}
