package de.adesso.budgeteer.service.budget;

import de.adesso.budgeteer.service.contract.ContractBaseData;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.joda.money.Money;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@Data
public class EditBudgetData implements Serializable {

    private long id;
    private long projectId;
    private String title;
    private String description;
    private String note;
    private Money total;
    private Money limit;
    private String importKey;
    private List<String> tags;
    private ContractBaseData contract;

    public EditBudgetData(long projectId) {
        this.projectId = projectId;
        this.tags = new ArrayList<>();
    }

    public void setTags(List<String> tags) {
        if (tags != null) {
            this.tags = new ArrayList<>(tags);
        }
    }
}
