package org.wickedsource.budgeteer.web.pages.contract.overview.table;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.wicket.injection.Injector;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.wickedsource.budgeteer.service.contract.ContractBaseData;
import org.wickedsource.budgeteer.service.contract.ContractComparator;
import org.wickedsource.budgeteer.service.contract.ContractSortingService;
import org.wickedsource.budgeteer.service.contract.DynamicAttributeField;
import org.wickedsource.budgeteer.web.BudgeteerSession;

import java.util.LinkedList;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper=false)
public class ContractOverviewTableModel extends LoadableDetachableModel<List<ContractBaseData>> {

    @SpringBean
    private ContractSortingService contractSortingService;

    private List<ContractBaseData> contracts;
    private List<String> footer = new LinkedList<>();
    private boolean taxRateEnabled;

    public ContractOverviewTableModel(){
        Injector.get().inject(this);
    }

    public List<String> getHeadline() {
        contracts = contractSortingService.getSortedContracts(BudgeteerSession.get().getProjectId(), BudgeteerSession.get().getLoggedInUser().getId());
        List<String> result = new LinkedList<>();
        if(!contracts.isEmpty()){
            for(DynamicAttributeField attribute : contracts.get(0).getContractAttributes()){
                result.add(attribute.getName());
            }
        }
        return result;
    }

    @Override
    protected List<ContractBaseData> load() {
        List<ContractBaseData> contractBaseData = contractSortingService.getSortedContracts(BudgeteerSession.get().getProjectId(), BudgeteerSession.get().getLoggedInUser().getId());
        // Sort by sorting index
        contractBaseData.sort(new ContractComparator());

        // Give every contract a unique sequential sorting index
        for (int i = 0; i < contractBaseData.size(); i++) {
            contractBaseData.get(i).setSortingIndex(i);
        }
        return contractBaseData;
    }
}
