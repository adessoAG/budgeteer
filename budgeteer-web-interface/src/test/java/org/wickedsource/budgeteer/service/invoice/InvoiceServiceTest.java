package org.wickedsource.budgeteer.service.invoice;

import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseOperation;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.github.springtestdbunit.annotation.DatabaseTearDown;
import org.junit.jupiter.api.Assertions;
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
import org.wickedsource.budgeteer.persistence.contract.ContractInvoiceField;
import org.wickedsource.budgeteer.persistence.contract.ContractRepository;
import org.wickedsource.budgeteer.persistence.invoice.InvoiceRepository;
import org.wickedsource.budgeteer.service.contract.DynamicAttributeField;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;


@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {IntegrationTestConfiguration.class})
@TestExecutionListeners({DbUnitTestExecutionListener.class, DirtiesContextTestExecutionListener.class, DependencyInjectionTestExecutionListener.class,
        TransactionalTestExecutionListener.class})
class InvoiceServiceTest extends ServiceIntegrationTestTemplate {

    @Autowired
    private InvoiceService service;

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private ContractRepository contractRepository;

    /**
     * Save a new Invoice associated with a Contract that does not have any ContractInvoiceFields
     */
    @Test
    @DatabaseSetup("invoiceTest.xml")
    @DatabaseTearDown(value = "invoiceTest.xml", type = DatabaseOperation.DELETE_ALL)
    void testSaveNewInvoice() {
        InvoiceBaseData testObject = getDummyInvoice();
        testObject.setContractId(1);

        long newContractId = service.save(testObject);

        Assertions.assertTrue(newContractId != 0L);

        InvoiceBaseData savedInvoice = service.getInvoiceById(newContractId);

        Assertions.assertFalse(savedInvoice.isPaid());
        Assertions.assertEquals(1, savedInvoice.getContractId());
        Assertions.assertEquals("Test", savedInvoice.getContractName());
        Assertions.assertEquals("Internal Number", savedInvoice.getInternalNumber());
        Assertions.assertEquals("Invoice Name", savedInvoice.getInvoiceName());
        Assertions.assertEquals(2, savedInvoice.getMonth());
        Assertions.assertEquals(2015, savedInvoice.getYear());
        Assertions.assertEquals(MoneyUtil.createMoney(2000), savedInvoice.getSum());
        Assertions.assertTrue(savedInvoice.getInvoiceId() > 0);
        Assertions.assertEquals(5, savedInvoice.getDynamicInvoiceFields().size());
        Assertions.assertEquals(5, contractRepository.findByIdAndFetchInvoiceFields(savedInvoice.getContractId()).getInvoiceFields().size());
    }

    /**
     * Save a new Invoice associated with a Contract that has two ContractInvoiceFields
     */
    @Test
    @DatabaseSetup("invoiceTest.xml")
    @DatabaseTearDown(value = "invoiceTest.xml", type = DatabaseOperation.DELETE_ALL)
    void testSaveNewInvoice2() {
        InvoiceBaseData testObject = getDummyInvoice();
        testObject.setContractId(2);

        long newContractId = service.save(testObject);

        Assertions.assertTrue(newContractId != 0L);

        InvoiceBaseData savedInvoice = service.getInvoiceById(newContractId);

        Assertions.assertFalse(savedInvoice.isPaid());
        Assertions.assertEquals(2, savedInvoice.getContractId());
        Assertions.assertEquals("Test", savedInvoice.getContractName());
        Assertions.assertEquals("Internal Number", savedInvoice.getInternalNumber());
        Assertions.assertEquals("Invoice Name", savedInvoice.getInvoiceName());
        Assertions.assertEquals(2, savedInvoice.getMonth());
        Assertions.assertEquals(2015, savedInvoice.getYear());
        Assertions.assertEquals(MoneyUtil.createMoney(2000), savedInvoice.getSum());
        Assertions.assertTrue(savedInvoice.getInvoiceId() > 0);
        Assertions.assertEquals(7, savedInvoice.getDynamicInvoiceFields().size());

        ContractEntity contract = contractRepository.findByIdAndFetchInvoiceFields(savedInvoice.getContractId());
        Set<ContractInvoiceField> contractInvoiceFields = contract.getInvoiceFields();
        Assertions.assertEquals(7, contractInvoiceFields.size());
        Assertions.assertTrue(contractInvoiceFields.contains(new ContractInvoiceField(300, "Test Contract Field", contract)));
        Assertions.assertTrue(contractInvoiceFields.contains(new ContractInvoiceField(400, "Test Contract Field 2", contract)));
    }

    /**
     * Update a Invoice
     */
    @Test
    @DatabaseSetup("invoiceTest.xml")
    @DatabaseTearDown(value = "invoiceTest.xml", type = DatabaseOperation.DELETE_ALL)
    void testUpdateInvoice() {
        InvoiceBaseData testObject = getDummyInvoice();
        testObject.setContractId(4);
        testObject.setInvoiceId(4);

        long newContractId = service.save(testObject);

        Assertions.assertTrue(newContractId != 0L);

        InvoiceBaseData savedInvoice = service.getInvoiceById(newContractId);

        Assertions.assertFalse(savedInvoice.isPaid());
        Assertions.assertEquals(4, savedInvoice.getContractId());
        Assertions.assertEquals("Test", savedInvoice.getContractName());
        Assertions.assertEquals("Internal Number", savedInvoice.getInternalNumber());
        Assertions.assertEquals("Invoice Name", savedInvoice.getInvoiceName());
        Assertions.assertEquals(2, savedInvoice.getMonth());
        Assertions.assertEquals(2015, savedInvoice.getYear());
        Assertions.assertEquals(MoneyUtil.createMoney(2000), savedInvoice.getSum());
        Assertions.assertTrue(savedInvoice.getInvoiceId() > 0);
        Assertions.assertEquals(5, savedInvoice.getDynamicInvoiceFields().size());

        ContractEntity contract = contractRepository.findByIdAndFetchInvoiceFields(savedInvoice.getContractId());
        Set<ContractInvoiceField> contractInvoiceFields = contract.getInvoiceFields();
        Assertions.assertEquals(5, contractInvoiceFields.size());
    }

    /**
     * Update a Invoice
     */
    @Test
    @DatabaseSetup("invoiceTest.xml")
    @DatabaseTearDown(value = "invoiceTest.xml", type = DatabaseOperation.DELETE_ALL)
    void testUpdateInvoice2() {
        InvoiceBaseData testObject = getDummyInvoice();
        testObject.setContractId(3);
        testObject.setInvoiceId(3);

        long newContractId = service.save(testObject);

        Assertions.assertTrue(newContractId != 0L);

        InvoiceBaseData savedInvoice = service.getInvoiceById(newContractId);

        Assertions.assertFalse(savedInvoice.isPaid());
        Assertions.assertEquals(3, savedInvoice.getContractId());
        Assertions.assertEquals("Test", savedInvoice.getContractName());
        Assertions.assertEquals("Internal Number", savedInvoice.getInternalNumber());
        Assertions.assertEquals("Invoice Name", savedInvoice.getInvoiceName());
        Assertions.assertEquals(2, savedInvoice.getMonth());
        Assertions.assertEquals(2015, savedInvoice.getYear());
        Assertions.assertEquals(MoneyUtil.createMoney(2000), savedInvoice.getSum());
        Assertions.assertTrue(savedInvoice.getInvoiceId() > 0);
        Assertions.assertEquals(7, savedInvoice.getDynamicInvoiceFields().size());
        Assertions.assertTrue(savedInvoice.getDynamicInvoiceFields().contains(new DynamicAttributeField("Test Contract Field", "Test")));
        Assertions.assertTrue(savedInvoice.getDynamicInvoiceFields().contains(new DynamicAttributeField("Test Contract Field 2", "Test 2")));

        ContractEntity contract = contractRepository.findByIdAndFetchInvoiceFields(savedInvoice.getContractId());
        Set<ContractInvoiceField> contractInvoiceFields = contract.getInvoiceFields();
        Assertions.assertEquals(7, contractInvoiceFields.size());
    }

    /**
     * Update a Invoice
     */
    @Test
    @DatabaseSetup("invoiceTest.xml")
    @DatabaseTearDown(value = "invoiceTest.xml", type = DatabaseOperation.DELETE_ALL)
    void testUpdateInvoice3() {
        InvoiceBaseData testObject = getDummyInvoice();
        testObject.setContractId(5);
        testObject.setInvoiceId(5);
        for (DynamicAttributeField field : testObject.getDynamicInvoiceFields()) {
            field.setValue("");
        }

        long newContractId = service.save(testObject);

        Assertions.assertTrue(newContractId != 0L);

        InvoiceBaseData savedInvoice = service.getInvoiceById(newContractId);

        Assertions.assertFalse(savedInvoice.isPaid());
        Assertions.assertEquals(5, savedInvoice.getContractId());
        Assertions.assertEquals("Test", savedInvoice.getContractName());
        Assertions.assertEquals("Internal Number", savedInvoice.getInternalNumber());
        Assertions.assertEquals("Invoice Name", savedInvoice.getInvoiceName());
        Assertions.assertEquals(2, savedInvoice.getMonth());
        Assertions.assertEquals(2015, savedInvoice.getYear());
        Assertions.assertEquals(MoneyUtil.createMoney(2000), savedInvoice.getSum());
        Assertions.assertTrue(savedInvoice.getInvoiceId() > 0);
        Assertions.assertEquals(2, savedInvoice.getDynamicInvoiceFields().size());

        ContractEntity contract = contractRepository.findByIdAndFetchInvoiceFields(savedInvoice.getContractId());
        Set<ContractInvoiceField> contractInvoiceFields = contract.getInvoiceFields();
        Assertions.assertEquals(2, contractInvoiceFields.size());
    }

    private InvoiceBaseData getDummyInvoice() {
        InvoiceBaseData result = new InvoiceBaseData();
        result.setInvoiceId(0);
        result.setPaidDate(null);
        result.setContractId(1);
        result.setContractName("Test");
        result.setInternalNumber("Internal Number");
        result.setInvoiceName("Invoice Name");
        result.setMonth(2);
        result.setSum(MoneyUtil.createMoney(2000));
        result.setYear(2015);
        result.setDynamicInvoiceFields(getDummyDynamicInvoiceFields());
        return result;
    }

    private List<DynamicAttributeField> getDummyDynamicInvoiceFields() {
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
    @DatabaseSetup("invoiceTest.xml")
    @DatabaseTearDown(value = "invoiceTest.xml", type = DatabaseOperation.DELETE_ALL)
    // Invoice with two InvoiceFields
    void testFindInvoiceFieldByName() {
        Assertions.assertNotNull(contractRepository.findInvoiceFieldByName(3, "Test Contract Field"));
        Assertions.assertNotNull(contractRepository.findInvoiceFieldByName(3, "Test Contract Field 2"));
    }

    @Test
    @DatabaseSetup("invoiceTest.xml")
    @DatabaseTearDown(value = "invoiceTest.xml", type = DatabaseOperation.DELETE_ALL)
    // Invoice with two InvoiceFields
    void testDeleteInvoice() {
        service.deleteInvoice(3);

        Assertions.assertNotNull(contractRepository.findByIdAndFetchInvoiceFields(3).getInvoiceFields());
        Assertions.assertEquals(2, contractRepository.findByIdAndFetchInvoiceFields(3).getInvoiceFields().size());
        Assertions.assertNotNull(contractRepository.findInvoiceFieldByName(3, "Test Contract Field"));
        Assertions.assertNotNull(contractRepository.findInvoiceFieldByName(3, "Test Contract Field 2"));

        Assertions.assertNull(invoiceRepository.findInvoiceFieldById(1));
        Assertions.assertNull(invoiceRepository.findInvoiceFieldById(2));
    }

    @Test
    @DatabaseSetup("invoiceTest.xml")
    @DatabaseTearDown(value = "invoiceTest.xml", type = DatabaseOperation.DELETE_ALL)
    // Invoice without any InvoiceFields
    void testDeleteInvoiceWithoutFields() {
        service.deleteInvoice(4);
        Assertions.assertFalse(invoiceRepository.findById(4L).isPresent());
    }

    @Test
    @DatabaseSetup("invoiceTest.xml")
    @DatabaseTearDown(value = "invoiceTest.xml", type = DatabaseOperation.DELETE_ALL)
    // Invoice without any InvoiceFields but with a contract containing ContractInvoiceFields
    void testDeleteInvoiceWithContractInvoiceFields() {
        service.deleteInvoice(5);
        Assertions.assertNotNull(contractRepository.findInvoiceFieldByName(5, "Test Contract Field"));
        Assertions.assertNotNull(contractRepository.findInvoiceFieldByName(5, "Test Contract Field 2"));
    }

}
