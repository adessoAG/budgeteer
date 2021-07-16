package org.wickedsource.budgeteer.web.pages.contract.details;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.SubmitLink;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.wickedsource.budgeteer.service.contract.ContractService;
import org.wickedsource.budgeteer.web.components.confirm.ConfirmationForm;
import org.wickedsource.budgeteer.web.pages.base.basepage.BasePage;
import org.wickedsource.budgeteer.web.pages.base.basepage.breadcrumbs.BreadcrumbsModel;
import org.wickedsource.budgeteer.web.pages.base.delete.DeleteDialog;
import org.wickedsource.budgeteer.web.pages.contract.budgetOverview.BudgetForContractOverviewPage;
import org.wickedsource.budgeteer.web.pages.contract.details.contractDetailChart.ContractDetailChart;
import org.wickedsource.budgeteer.web.pages.contract.details.contractDetailChart.ContractDetailChartModel;
import org.wickedsource.budgeteer.web.pages.contract.details.differenceTable.DifferenceTable;
import org.wickedsource.budgeteer.web.pages.contract.details.differenceTable.DifferenceTableModel;
import org.wickedsource.budgeteer.web.pages.contract.details.highlights.ContractHighlightsPanel;
import org.wickedsource.budgeteer.web.pages.contract.edit.EditContractPage;
import org.wickedsource.budgeteer.web.pages.contract.overview.ContractOverviewPage;
import org.wickedsource.budgeteer.web.pages.dashboard.DashboardPage;
import org.wickedsource.budgeteer.web.pages.invoice.edit.EditInvoicePage;
import org.wicketstuff.annotation.mount.MountPath;

@MountPath("contracts/details/${id}")
public class ContractDetailsPage extends BasePage {

    @SpringBean
    private ContractService contractService;

    private static final int numberOfMonths = 6;

    private ContractDetailModel contractModel;

    public ContractDetailsPage(PageParameters parameters) {
        super(parameters);
        contractModel = new ContractDetailModel(getParameterId());

        add(new ContractHighlightsPanel("highlightsPanel", contractModel));
        add(new ContractDetailChart("comparisonChart", new ContractDetailChartModel(getParameterId(), numberOfMonths)));
        add(new DifferenceTable("differenceTable", new DifferenceTableModel(getParameterId(), contractModel.getObject().getStartDate())));

        add(new Link("editLink") {
            @Override
            public void onClick() {
                WebPage page = new EditContractPage(createParameters(getParameterId()), ContractDetailsPage.class, getPageParameters());
                setResponsePage(page);
            }
        });
        add(new Link("addInvoiceLink"){
            @Override
            public void onClick() {
                WebPage page = new EditInvoicePage(EditInvoicePage.createNewInvoiceParameters(getParameterId()), ContractDetailsPage.class, getPageParameters());
                setResponsePage(page);
            }
        });
        add(new ExternalLink("showInvoiceLink", "invoices/" + contractModel.getObject().getContractId()));
        add(new Link("showContractLink"){
            @Override
            public void onClick() {
                setResponsePage(BudgetForContractOverviewPage.class, createParameters(getParameterId()));
            }
        });
        Form deleteForm = new ConfirmationForm("deleteForm", this, "confirmation.delete") {
            @Override
            public void onSubmit() {
                setResponsePage(new DeleteDialog() {
                    @Override
                    protected void onYes() {
                        contractService.deleteContract(getParameterId());
                        setResponsePage(ContractOverviewPage.class);
                    }

                    @Override
                    protected void onNo() {
                        setResponsePage(ContractDetailsPage.class, ContractDetailsPage.this.getPageParameters());
                    }

                    @Override
                    protected String confirmationText() {
                        return ContractDetailsPage.this.getString("confirmation.delete");
                    }
                });
            }
        };
        deleteForm.add(new SubmitLink("deleteLink"));
        add(deleteForm);
    }


    @Override
    protected BreadcrumbsModel getBreadcrumbsModel() {
        BreadcrumbsModel model = new BreadcrumbsModel(DashboardPage.class, ContractOverviewPage.class);
        model.addBreadcrumb(ContractDetailsPage.class, getPageParameters());
        return model;
    }

    @Override
    protected void onDetach() {
        contractModel.detach();
        super.onDetach();
    }
}
