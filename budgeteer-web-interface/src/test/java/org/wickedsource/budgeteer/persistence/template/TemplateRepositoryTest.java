package org.wickedsource.budgeteer.persistence.template;

import com.github.springtestdbunit.annotation.DatabaseOperation;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.github.springtestdbunit.annotation.DatabaseTearDown;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.wickedsource.budgeteer.IntegrationTestTemplate;

import java.util.List;

class TemplateRepositoryTest extends IntegrationTestTemplate {

    @Autowired
    TemplateRepository templateRepository;

    @Test
    @DatabaseSetup("templateTest.xml")
    @DatabaseTearDown(value = "templateTest.xml", type = DatabaseOperation.DELETE_ALL)
    void testFindInProject() {
        List<TemplateEntity> templatesInProject = templateRepository.findByProjectId(1L);
        Assertions.assertEquals(1, templatesInProject.size());
        Assertions.assertEquals("test", templatesInProject.get(0).getName());
    }

    @Test
    @DatabaseSetup("templateTest.xml")
    @DatabaseTearDown(value = "templateTest.xml", type = DatabaseOperation.DELETE_ALL)
    void deleteTemplateTest() {
        List<TemplateEntity> templatesInProject = templateRepository.findByProjectId(1L);
        Assertions.assertEquals(1, templatesInProject.size());
        templateRepository.deleteById(templatesInProject.get(0).getId());
        Assertions.assertEquals(0, templateRepository.findByProjectId(1L).size());
    }

    @Test
    @DatabaseSetup("templateTestMany.xml")
    @DatabaseTearDown(value = "templateTest.xml", type = DatabaseOperation.DELETE_ALL)
    void deleteAllTest() {
        List<TemplateEntity> templatesInProject = templateRepository.findByProjectId(1L);
        Assertions.assertEquals(5, templatesInProject.size());
        templateRepository.deleteAll();
        Assertions.assertEquals(0, templateRepository.findByProjectId(1L).size());
    }
}
