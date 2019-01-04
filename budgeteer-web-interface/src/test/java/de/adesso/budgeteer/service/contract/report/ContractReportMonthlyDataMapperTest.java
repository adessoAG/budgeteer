package de.adesso.budgeteer.service.contract.report;

import com.github.springtestdbunit.annotation.DatabaseOperation;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.github.springtestdbunit.annotation.DatabaseTearDown;
import de.adesso.budgeteer.IntegrationTestTemplate;
import de.adesso.budgeteer.persistence.contract.ContractEntity;
import de.adesso.budgeteer.persistence.contract.ContractRepository;
import org.assertj.core.api.Assertions;
import org.assertj.core.data.Percentage;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;

class ContractReportMonthlyDataMapperTest extends IntegrationTestTemplate {

    @Autowired
    private ContractRepository contractRepository;

    @Autowired
    private ContractReportMonthlyDataMapper testSubject;

    @Test
    @DatabaseSetup("contractMapperTest.xml")
    @DatabaseTearDown(value = "contractMapperTest.xml", type = DatabaseOperation.DELETE_ALL)
    void whenTaxrateIsNull() {
        ContractEntity contractEntity = contractRepository.findOne(3L);
        ContractReportData contractBaseData = testSubject.map(contractEntity, new Date());
        Assertions.assertThat(contractBaseData.getTaxRate()).isCloseTo(0.00, Percentage.withPercentage(10e-8));
    }

    @Test
    @DatabaseSetup("contractMapperTest.xml")
    @DatabaseTearDown(value = "contractMapperTest.xml", type = DatabaseOperation.DELETE_ALL)
    void whenTaxrateIsNotNull() {
        ContractEntity contractEntity = contractRepository.findOne(4L);
        ContractReportData contractBaseData = testSubject.map(contractEntity, new Date());
        Assertions.assertThat(contractBaseData.getTaxRate()).isCloseTo(1.00, Percentage.withPercentage(10e-8));
    }
}