package org.wickedsource.budgeteer.service.budget.report;

import lombok.Data;

@Data
public class BudgetSummary {
	public BudgetSummary(String description) {
		this.name = description;
	}

	private String name;
}
