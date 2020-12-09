package org.wickedsource.budgeteer.service.contract.report;

import lombok.Data;
import org.wickedsource.budgeteer.SheetTemplate.SheetTemplateSerializable;

import java.util.Date;
import java.util.List;

@Data
public
class ContractReportData {
	private long id;
	private String contract;
	private String contractId;
	private Date from;
	private Date until;
	private double budgetSpent_net;
	private double budgetLeft_net;
	private double budgetTotal_net;
	private double budgetSpent_gross;
	private double budgetLeft_gross;
	private double budgetTotal_gross;
	private Double taxRate;
	private Double progress;
	private List<? extends SheetTemplateSerializable> attributes;
}
