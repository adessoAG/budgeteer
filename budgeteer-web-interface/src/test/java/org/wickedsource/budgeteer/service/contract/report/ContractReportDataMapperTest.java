package org.wickedsource.budgeteer.service.contract.report;

import com.github.springtestdbunit.annotation.DatabaseOperation;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.github.springtestdbunit.annotation.DatabaseTearDown;
import org.assertj.core.api.Assertions;
import org.assertj.core.data.Percentage;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.wickedsource.budgeteer.IntegrationTestTemplate;
import org.wickedsource.budgeteer.persistence.contract.ContractEntity;
import org.wickedsource.budgeteer.persistence.contract.ContractRepository;

import java.util.Date;

class ContractReportDataMapperTest extends IntegrationTestTemplate {


    @Autowired
    private ContractRepository contractRepository;

    @Autowired
    private ContractReportDataMapper testSubject;

    @Test
    @DatabaseSetup("contractMapperTest.xml")
    @DatabaseTearDown(value = "contractMapperTest.xml", type = DatabaseOperation.DELETE_ALL)
    void whenTaxrateIsNull() {
        ContractEntity contractEntity = contractRepository.findById(3L).orElseThrow(RuntimeException::new);
        ContractReportData contractBaseData = testSubject.map(contractEntity, new Date());
        Assertions.assertThat(contractBaseData.getTaxRate()).isCloseTo(0.00, Percentage.withPercentage(10e-8));
    }

    @Test
    @DatabaseSetup("contractMapperTest.xml")
    @DatabaseTearDown(value = "contractMapperTest.xml", type = DatabaseOperation.DELETE_ALL)
    void whenTaxrateIsNotNull() {
        ContractEntity contractEntity = contractRepository.findById(4L).orElseThrow(RuntimeException::new);
        ContractReportData contractBaseData = testSubject.map(contractEntity, new Date());
        Assertions.assertThat(contractBaseData.getTaxRate()).isCloseTo(1.00, Percentage.withPercentage(10e-8));
    }
}
