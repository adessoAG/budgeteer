package de.adesso.budgeteer.service.record;

import de.adesso.budgeteer.service.DateRange;
import de.adesso.budgeteer.service.budget.BudgetBaseData;
import de.adesso.budgeteer.service.person.PersonBaseData;
import de.adesso.budgeteer.web.components.burntable.filter.BurnTableSortColumn;
import lombok.Getter;
import lombok.Setter;
import org.apache.wicket.model.Model;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

public class WorkRecordFilter implements Serializable {

    @Getter
    @Setter
    private long projectId;

    @Getter
    private List<PersonBaseData> personList = new LinkedList<>();

    @Getter
    private List<PersonBaseData> possiblePersons = new LinkedList<>();

    @Getter
    private List<BudgetBaseData> budgetList = new LinkedList<>();

    @Getter
    private List<BudgetBaseData> possibleBudgets = new LinkedList<>();

    @Getter
    private Model<BurnTableSortColumn> columnToSort = new Model<>(BurnTableSortColumn.BUDGET);

    @Getter
    private Model<String> sortType = new Model<>("Ascending");

    @Getter
    @Setter
    private DateRange dateRange;

    /***
     * Clear the selected filter options.
     */
    public void clearFilter() {
        personList = new LinkedList<>();
        budgetList = new LinkedList<>();
        dateRange = null;
    }

    public WorkRecordFilter(long projectId) {
        this.projectId = projectId;
    }
}
