package org.wickedsource.budgeteer.service.project;

import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseOperation;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.github.springtestdbunit.annotation.DatabaseTearDown;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.wickedsource.budgeteer.IntegrationTestConfiguration;
import org.wickedsource.budgeteer.persistence.contract.ContractRepository;
import org.wickedsource.budgeteer.persistence.invoice.InvoiceRepository;
import org.wickedsource.budgeteer.persistence.project.ProjectRepository;
import org.wickedsource.budgeteer.persistence.record.PlanRecordRepository;
import org.wickedsource.budgeteer.persistence.record.WorkRecordRepository;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {IntegrationTestConfiguration.class})
@TestExecutionListeners({
        DbUnitTestExecutionListener.class,
        DirtiesContextTestExecutionListener.class,
        DependencyInjectionTestExecutionListener.class,
        TransactionalTestExecutionListener.class
})
public class ProjectServiceTestWithDBUnit {

    @Autowired
    private ProjectService projectService;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private WorkRecordRepository workRecordRepository;

    @Autowired
    private PlanRecordRepository planRecordRepository;

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private ContractRepository contractRepository;


    @Test
    @DatabaseSetup("deleteProject.xml")
    @DatabaseTearDown(value = "deleteProject.xml", type = DatabaseOperation.DELETE_ALL)
    public void deleteEmptyProject(){
        projectService.deleteProject(1);
        Assert.assertEquals(null, projectRepository.findOne(1l));

    }

    @Test
    @DatabaseSetup("deleteProject.xml")
    @DatabaseTearDown(value = "deleteProject.xml", type = DatabaseOperation.DELETE_ALL)
    public void deleteProject(){
        projectService.deleteProject(6);
        Assert.assertEquals(null, projectRepository.findOne(6l));
        Assert.assertEquals(0, planRecordRepository.findByProjectId(6l).size());
        Assert.assertEquals(0, workRecordRepository.findByProjectId(6l).size());
        Assert.assertEquals(0, invoiceRepository.findByProjectId(6l).size());
        Assert.assertEquals(0, contractRepository.findByProjectId(6l).size());


    }
}
