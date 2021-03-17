package org.wickedsource.budgeteer.web.components.tax;

import org.apache.wicket.model.IModel;
import org.joda.money.Money;
import org.wickedsource.budgeteer.web.BudgeteerSession;

public class TaxBudgetUnitMoneyModel implements IModel<Money> {

    private final IModel<Money> netModel;
    private final IModel<Money> grossModel;

    public TaxBudgetUnitMoneyModel(IModel<Money> netModel, IModel<Money> grossModel) {
        this.netModel = netModel;
        this.grossModel = grossModel;
    }

    @Override
    public Money getObject() {
        if (BudgeteerSession.get().isTaxEnabled()) {
            return grossModel.getObject();
        } else {
            return netModel.getObject();
        }
    }
}
