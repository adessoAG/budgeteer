package de.adesso.budgeteer.web.pages.invoice.details;

import de.adesso.budgeteer.service.invoice.InvoiceService;
import de.adesso.budgeteer.web.Mount;
import de.adesso.budgeteer.web.components.confirm.ConfirmationForm;
import de.adesso.budgeteer.web.pages.base.basepage.BasePage;
import de.adesso.budgeteer.web.pages.base.basepage.breadcrumbs.BreadcrumbsModel;
import de.adesso.budgeteer.web.pages.contract.details.ContractDetailsPage;
import de.adesso.budgeteer.web.pages.dashboard.DashboardPage;
import de.adesso.budgeteer.web.pages.invoice.details.highlights.InvoiceHighlightsPanel;
import de.adesso.budgeteer.web.pages.invoice.edit.EditInvoicePage;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.SubmitLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;

@Mount("invoices/details/${id}")
public class InvoiceDetailsPage extends BasePage {

    @SpringBean
    private InvoiceService invoiceService;
    private boolean taxVisible;
    private InvoiceHighlightsPanel highlights;

    public InvoiceDetailsPage(PageParameters parameters) {
        super(parameters);
        taxVisible = false;
        highlights = new InvoiceHighlightsPanel("highlightsPanel", new InvoiceDetailModel(getParameterId()));
        highlights.setOutputMarkupId(true);
        add(highlights);

        add(new Link("editLink") {
            @Override
            public void onClick() {
                WebPage page = new EditInvoicePage(EditInvoicePage.createEditInvoiceParameters(getParameterId()), InvoiceDetailsPage.class, getPageParameters());
                setResponsePage(page);
            }
        });

        Label taxLinkTextLabel = new Label("taxLinkText", new ResourceModel("taxLinkTextShow"));
        taxLinkTextLabel.setOutputMarkupId(true);
        Label taxLinkDescriptionLabel = new Label("taxLinkDescription", new ResourceModel("taxLinkDescriptionShow"));
        taxLinkDescriptionLabel.setOutputMarkupId(true);

        AjaxLink taxLink = new AjaxLink("taxLink") {
            @Override
            protected void onConfigure() {
                highlights.setTaxInformationVisible(taxVisible);
            }

            @Override
            public void onClick(AjaxRequestTarget target) {
                taxVisible = !taxVisible;
                // Change visibility
                highlights.setTaxInformationVisible(taxVisible);

                // Change link text
                if (taxVisible) {
                    taxLinkTextLabel.setDefaultModel(new ResourceModel("taxLinkTextHide"));
                    taxLinkDescriptionLabel.setDefaultModel(new ResourceModel("taxLinkDescriptionHide"));
                } else {
                    taxLinkTextLabel.setDefaultModel(new ResourceModel("taxLinkTextShow"));
                    taxLinkDescriptionLabel.setDefaultModel(new ResourceModel("taxLinkDescriptionShow"));
                }

                target.add(taxLinkDescriptionLabel, taxLinkTextLabel, highlights.getSumGrossContainer(), highlights.getTaxAmountContainer(), highlights.getTaxRateContainer());
            }
        };
        taxLink.add(taxLinkTextLabel);
        taxLink.add(taxLinkDescriptionLabel);
        add(taxLink);

        Form deleteForm = new ConfirmationForm("deleteForm", this, "confirmation.delete") {
            @Override
            public void onSubmit() {
                invoiceService.deleteInvoice(getParameterId());
                setResponsePage(DashboardPage.class);
            }
        };
        deleteForm.add(new SubmitLink("deleteLink"));
        add(deleteForm);
    }

    @Override
    protected BreadcrumbsModel getBreadcrumbsModel() {
        BreadcrumbsModel model = new BreadcrumbsModel(DashboardPage.class, ContractDetailsPage.class);
        model.addBreadcrumb(InvoiceDetailsPage.class, getPageParameters());
        return model;
    }
}
