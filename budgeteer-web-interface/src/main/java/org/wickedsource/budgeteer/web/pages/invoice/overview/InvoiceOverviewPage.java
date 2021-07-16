package org.wickedsource.budgeteer.web.pages.invoice.overview;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.wickedsource.budgeteer.service.invoice.InvoiceService;
import org.wickedsource.budgeteer.web.BudgeteerSession;
import org.wickedsource.budgeteer.web.pages.base.basepage.BasePage;
import org.wickedsource.budgeteer.web.pages.base.basepage.breadcrumbs.BreadcrumbsModel;
import org.wickedsource.budgeteer.web.pages.contract.details.ContractDetailsPage;
import org.wickedsource.budgeteer.web.pages.contract.overview.ContractOverviewPage;
import org.wickedsource.budgeteer.web.pages.dashboard.DashboardPage;
import org.wickedsource.budgeteer.web.pages.invoice.edit.EditInvoicePage;
import org.wickedsource.budgeteer.web.pages.invoice.overview.table.InvoiceOverviewTable;
import org.wicketstuff.annotation.mount.MountPath;


@MountPath(value = "/invoices", alt = {"contracts/details/invoices/${id}"})
public class InvoiceOverviewPage extends BasePage {

    @SpringBean
    private InvoiceService invoiceService;

    /**
     * Shows the overview page of all invoices beloging to the given contract or the current project
     * @param parameters if the id property of the pageparameters is 0l, the pages shows the invoices for the current project <br />
     *                   otherwise the given id will be considered as contract id and the pages shows the invoices associated with this contract
     */
    public InvoiceOverviewPage(PageParameters parameters) {
        super(parameters);
        InvoiceOverviewTable table;
        if(getParameterId() == 0L){
             table = new InvoiceOverviewTable("invoiceTable", invoiceService.getInvoiceOverviewByProject(BudgeteerSession.get().getProjectId()), getBreadcrumbsModel());
        } else {
             table = new InvoiceOverviewTable("invoiceTable", invoiceService.getInvoiceOverviewByContract(getParameterId()), getBreadcrumbsModel());
        }
        add(table);
        add(new Link("createInvoiceLink") {
            @Override
            public void onClick() {
                WebPage page = new EditInvoicePage(EditInvoicePage.createNewInvoiceParameters(getParameterId()), InvoiceOverviewPage.class, getPageParameters());
                setResponsePage(page);
            }

            @Override
            public boolean isVisible() {
                return getParameterId() != 0L;
            }
        });
    }

    @SuppressWarnings("unchecked")
    @Override
    protected BreadcrumbsModel getBreadcrumbsModel() {
        if(getParameterId() != 0L){
            BreadcrumbsModel model = new BreadcrumbsModel(DashboardPage.class);
            model.addBreadcrumb(ContractOverviewPage.class);
            model.addBreadcrumb(ContractDetailsPage.class, getPageParameters());
            model.addBreadcrumb(InvoiceOverviewPage.class, getPageParameters());
            return model;
        }else {
            return new BreadcrumbsModel(DashboardPage.class, InvoiceOverviewPage.class);
        }
    }
}
