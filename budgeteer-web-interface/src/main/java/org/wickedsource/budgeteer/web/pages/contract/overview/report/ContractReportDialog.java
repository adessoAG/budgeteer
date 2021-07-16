package org.wickedsource.budgeteer.web.pages.contract.overview.report;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.wickedsource.budgeteer.web.pages.base.dialogpage.DialogPageWithBacklink;
import org.wickedsource.budgeteer.web.pages.contract.overview.report.form.ContractReportForm;
import org.wicketstuff.annotation.mount.MountPath;

@MountPath("contracts/report")
public class ContractReportDialog extends DialogPageWithBacklink {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ContractReportDialog(Class<? extends WebPage> backlinkPage, PageParameters backlinkParameters) {
        super(backlinkPage, backlinkParameters);
        Form<ContractReportMetaInformation> form = new ContractReportForm("form");
        addComponents(form);
	}

    private void addComponents(Form<ContractReportMetaInformation> form) {
        add(createBacklink("cancelButton1"));
        form.add(createBacklink("cancelButton2"));
        add(form);
    }
}
