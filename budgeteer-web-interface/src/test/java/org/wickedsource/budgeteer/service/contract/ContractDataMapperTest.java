package org.wickedsource.budgeteer.service.contract;

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

import java.math.BigDecimal;

class ContractDataMapperTest extends IntegrationTestTemplate {

    @Autowired
    private ContractRepository contractRepository;

    @Autowired
    private ContractDataMapper testSubject;

    @Test
    @DatabaseSetup("contractMapperTest.xml")
    @DatabaseTearDown(value = "contractMapperTest.xml", type = DatabaseOperation.DELETE_ALL)
    void whenTaxrateIsNotNull() {
        ContractEntity contractEntity = contractRepository.findById(4L).orElseThrow(RuntimeException::new);
        ContractBaseData contractBaseData = testSubject.map(contractEntity);
        Assertions.assertThat(contractBaseData.getTaxRate()).isCloseTo(BigDecimal.valueOf(100), Percentage.withPercentage(10e-8));
    }

}
