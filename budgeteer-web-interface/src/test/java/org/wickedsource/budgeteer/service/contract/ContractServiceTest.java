package org.wickedsource.budgeteer.service.contract;

import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseOperation;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.github.springtestdbunit.annotation.DatabaseTearDown;
import org.apache.commons.lang3.time.DateUtils;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.wickedsource.budgeteer.IntegrationTestConfiguration;
import org.wickedsource.budgeteer.MoneyUtil;
import org.wickedsource.budgeteer.ServiceIntegrationTestTemplate;
import org.wickedsource.budgeteer.persistence.contract.ContractEntity;
import org.wickedsource.budgeteer.persistence.contract.ContractRepository;
import org.wickedsource.budgeteer.persistence.project.ProjectRepository;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.Assert.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {IntegrationTestConfiguration.class})
@TestExecutionListeners({
        DbUnitTestExecutionListener.class,
        DirtiesContextTestExecutionListener.class,
        DependencyInjectionTestExecutionListener.class,
        TransactionalTestExecutionListener.class
})
class ContractServiceTest extends ServiceIntegrationTestTemplate {


    @Autowired
    private ContractService service;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private ContractRepository contractRepository;

    /**
     * Save a new Contract associated with a Project that does not have any ProjectContractFields
     */
    @Test
    @DatabaseSetup("contractTest.xml")
    @DatabaseTearDown(value = "contractTest.xml", type = DatabaseOperation.DELETE_ALL)
    void testSaveNewContract() {
        ContractBaseData testObject = new ContractBaseData();
        testObject.setBudget(MoneyUtil.createMoney(12));
        testObject.setContractId(0);
        testObject.setProjectId(100);
        testObject.setContractName("Test Contract");
        testObject.setContractAttributes(getListOfContractFields());
        testObject.getContractAttributes().add(new DynamicAttributeField("test4", "test4"));
        testObject.setTaxRate(BigDecimal.ZERO);

        long newContractId = service.save(testObject);

        assertTrue(newContractId != 0L);

        ContractBaseData savedContract = service.getContractById(newContractId);
        assertEquals(MoneyUtil.createMoney(12), savedContract.getBudget());
        assertTrue(savedContract.getContractId() > 0);
        assertEquals(5, savedContract.getContractAttributes().size());

        assertEquals(5, projectRepository.findByIdAndFetchContractFields(savedContract.getProjectId()).getContractFields().size());
    }

    /**
     * Save a new Contract associated with a Project that already has some ProjectContractFields
     */
    @Test
    @DatabaseSetup("contractTest.xml")
    @DatabaseTearDown(value = "contractTest.xml", type = DatabaseOperation.DELETE_ALL)
    void testSaveNewContract2() {
        ContractBaseData testObject = new ContractBaseData();
        testObject.setContractId(0);
        testObject.setProjectId(200);
        testObject.setContractName("Test Contract");
        testObject.setContractAttributes(getListOfContractFields());
        testObject.getContractAttributes().add(new DynamicAttributeField("test4", "test4"));
        testObject.setTaxRate(BigDecimal.ZERO);

        long newContractId = service.save(testObject);

        assertTrue(newContractId != 0L);

        ContractBaseData savedContract = service.getContractById(newContractId);
        assertTrue(savedContract.getContractId() > 0);
        assertEquals(5, savedContract.getContractAttributes().size());

        assertEquals(5, projectRepository.findByIdAndFetchContractFields(savedContract.getProjectId()).getContractFields().size());
    }

    /**
     * Update a Contract that already has some dynamic fields
     */
    @Test
    @DatabaseSetup("contractTest.xml")
    @DatabaseTearDown(value = "contractTest.xml", type = DatabaseOperation.DELETE_ALL)
    void testUpdateContract() {
        ContractBaseData testObject = service.getContractById(400L);

        testObject.setBudget(MoneyUtil.createMoney(12.0));
        testObject.setContractName("Test Contract");
        testObject.setInternalNumber("Test Contract");
        testObject.setStartDate(new Date());
        testObject.setType(ContractEntity.ContractType.T_UND_M);
        testObject.setContractAttributes(getListOfContractFields());
        testObject.getContractAttributes().add(new DynamicAttributeField("test5", "test5"));

        long newContractId = service.save(testObject);

        assertEquals(400L, newContractId);
        ContractBaseData savedContract = service.getContractById(newContractId);

        assertEquals("Test Contract", savedContract.getContractName());
        assertEquals("Test Contract", savedContract.getInternalNumber());
        assertEquals(MoneyUtil.createMoney(12), savedContract.getBudget());
        assertEquals(DateUtils.truncate(new Date(), Calendar.DAY_OF_MONTH), savedContract.getStartDate());
        assertEquals(ContractEntity.ContractType.T_UND_M, savedContract.getType());
        assertEquals(6, savedContract.getContractAttributes().size());
        for (int i = 0; i < 6; i++) {
            boolean found = false;
            for (DynamicAttributeField field : savedContract.getContractAttributes()) {
                if (field.getName().equals(field.getValue()) && field.getValue().equals("test" + i)) {
                    found = true;
                    break;
                }
            }
            assertTrue(found);
        }
        assertEquals(1, savedContract.getBelongingBudgets().size());
        assertEquals(6, projectRepository.findByIdAndFetchContractFields(savedContract.getProjectId()).getContractFields().size());
    }

    /**
     * Update a Contract that does not has dynamic fields, but is associated with a project that has already ProjectContractFields
     */
    @Test
    @DatabaseSetup("contractTest.xml")
    @DatabaseTearDown(value = "contractTest.xml", type = DatabaseOperation.DELETE_ALL)
    void testUpdateContract1() {
        ContractBaseData testObject = service.getContractById(500L);

        testObject.setBudget(MoneyUtil.createMoney(12));
        testObject.setContractName("Test Contract");
        testObject.setInternalNumber("Test Contract");
        testObject.setStartDate(new Date());
        testObject.setType(ContractEntity.ContractType.T_UND_M);
        testObject.setContractAttributes(getListOfContractFields());
        testObject.getContractAttributes().add(new DynamicAttributeField("test5", "test5"));
        testObject.getContractAttributes().add(new DynamicAttributeField("test6", "test6"));

        long newContractId = service.save(testObject);

        assertEquals(500L, newContractId);
        ContractBaseData savedContract = service.getContractById(newContractId);

        assertEquals("Test Contract", savedContract.getContractName());
        assertEquals("Test Contract", savedContract.getInternalNumber());
        assertEquals(MoneyUtil.createMoney(12), savedContract.getBudget());
        assertEquals(DateUtils.truncate(new Date(), Calendar.DAY_OF_MONTH), savedContract.getStartDate());
        assertEquals(ContractEntity.ContractType.T_UND_M, savedContract.getType());
        assertEquals(7, savedContract.getContractAttributes().size());
        for (int i = 0; i < 7; i++) {
            boolean found = false;
            for (DynamicAttributeField field : savedContract.getContractAttributes()) {
                if (field.getName().equals(field.getValue()) && field.getValue().equals("test" + i)) {
                    found = true;
                    break;
                }
            }
            assertTrue(found);
        }
        assertEquals(0, savedContract.getBelongingBudgets().size());
        assertEquals(7, projectRepository.findByIdAndFetchContractFields(savedContract.getProjectId()).getContractFields().size());
    }

    @Test
    @DatabaseSetup("contractTest.xml")
    @DatabaseTearDown(value = "contractTest.xml", type = DatabaseOperation.DELETE_ALL)
    void testGetEmptyContractModel() {
        ContractBaseData baseData = service.getEmptyContractModel(100L);
        assertEquals(0, baseData.getContractAttributes().size());

        baseData = service.getEmptyContractModel(300L);
        assertEquals(2, baseData.getContractAttributes().size());
        Assertions.assertThat(baseData.getContractAttributes())
                .extracting(DynamicAttributeField::getName)
                .containsAll(Arrays.asList("test0", "test1"));
    }

    @Test
    @DatabaseSetup("contractTest.xml")
    @DatabaseTearDown(value = "contractTest.xml", type = DatabaseOperation.DELETE_ALL)
    void testGetContractById() {
        ContractBaseData testObject = service.getContractById(300L);

        assertEquals(300L, testObject.getContractId());
        assertEquals(300L, testObject.getProjectId());

        assertEquals(2, testObject.getContractAttributes().size());
        Assertions.assertThat(testObject.getContractAttributes())
                .extracting(DynamicAttributeField::getName)
                .containsAll(Arrays.asList("test0", "test1"));
        Assertions.assertThat(testObject.getContractAttributes())
                .extracting(DynamicAttributeField::getValue)
                .containsAll(Arrays.asList("test0", "test1"));

        assertEquals(1, testObject.getBelongingBudgets().size());
        assertEquals("Budget 1", testObject.getBelongingBudgets().get(0).getName());

        assertEquals("Test", testObject.getContractName());
        assertEquals("Test", testObject.getInternalNumber());
        assertEquals(MoneyUtil.createMoney(1), testObject.getBudget());
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTime(testObject.getStartDate());
        assertEquals(2015, cal.get(Calendar.YEAR));
        assertEquals(ContractEntity.ContractType.FIXED_PRICE, testObject.getType());
    }

    @Test
    @DatabaseSetup("contractTest.xml")
    @DatabaseTearDown(value = "contractTest.xml", type = DatabaseOperation.DELETE_ALL)
    void testGetContractByIdWithInvoices() {
        ContractBaseData testObject = service.getContractById(600L);
        assertEquals(2, testObject.getBelongingInvoices().size());
    }

    private List<DynamicAttributeField> getListOfContractFields() {
        List<DynamicAttributeField> result = new LinkedList<DynamicAttributeField>();
        DynamicAttributeField data = new DynamicAttributeField();
        for (int i = 0; i < 5; i++) {
            data = new DynamicAttributeField();
            data.setName("test" + i);
            data.setValue("test" + i);
            result.add(data);
        }
        return result;
    }

    @Test
    @DatabaseSetup("contractDeletionTest.xml")
    @DatabaseTearDown(value = "contractDeletionTest.xml", type = DatabaseOperation.DELETE_ALL)
    void testDeleteContract() {
        assertNotNull(service.getContractById(300));
        assertEquals(2, contractRepository.findContractFieldsByContractId(300L).size());
        assertEquals(1, contractRepository.findContractFieldsByContractId(400L).size());
        service.deleteContract(300);
        assertEquals(0, contractRepository.findContractFieldsByContractId(300L).size());
        assertEquals(1, contractRepository.findContractFieldsByContractId(400L).size());
        assertNull(service.getContractById(300));
    }

    @Test
    @DatabaseSetup("contractTest.xml")
    @DatabaseTearDown(value = "contractTest.xml", type = DatabaseOperation.DELETE_ALL)
    void shouldUpdateAttributeWithEmptyValue() {
        ContractBaseData testObject = service.getContractById(400L);
        testObject.setContractAttributes(getListOfContractFields());
        DynamicAttributeField field = testObject.getContractAttributes().get(0);
        field.setValue(null);

        long newContractId = service.save(testObject);

        ContractBaseData savedContract = service.getContractById(newContractId);

        Assertions.assertThat(savedContract.getContractAttributes())
                .contains(new DynamicAttributeField(field.getName(), ""));
    }

    @Test
    @DatabaseSetup("contractTest.xml")
    @DatabaseTearDown(value = "contractTest.xml", type = DatabaseOperation.DELETE_ALL)
    void shouldSaveAttributeWithEmptyValue() {
        ContractBaseData testObject = service.getContractById(400L);
        testObject.setContractAttributes(getListOfContractFields());
        DynamicAttributeField field = new DynamicAttributeField("new-key", null);
        testObject.getContractAttributes().add(field);

        long newContractId = service.save(testObject);

        ContractBaseData savedContract = service.getContractById(newContractId);

        Assertions.assertThat(savedContract.getContractAttributes())
                .contains(new DynamicAttributeField(field.getName(), ""));
    }
}
