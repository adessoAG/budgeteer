package org.wickedsource.budgeteer.web.pages.invoice.edit;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.util.string.StringValue;
import org.wickedsource.budgeteer.service.invoice.InvoiceBaseData;
import org.wickedsource.budgeteer.service.invoice.InvoiceService;
import org.wickedsource.budgeteer.web.pages.base.dialogpage.DialogPageWithBacklink;
import org.wickedsource.budgeteer.web.pages.invoice.edit.form.EditInvoiceForm;
import org.wickedsource.budgeteer.web.pages.invoice.overview.InvoiceOverviewPage;
import org.wicketstuff.annotation.mount.MountPath;

import static org.wicketstuff.lazymodel.LazyModel.from;
import static org.wicketstuff.lazymodel.LazyModel.model;

@MountPath("/invoices/edit/${invoiceId}/#{contractId}")
public class EditInvoicePage extends DialogPageWithBacklink {

    @SpringBean
    private InvoiceService service;

    public EditInvoicePage(PageParameters parameters, Class<? extends WebPage> backlinkPage, PageParameters backlinkParameters) {
        super(parameters, backlinkPage, backlinkParameters);
        InvoiceBaseData invoiceBaseData;
        if(getInvoiceId() == 0L){
            invoiceBaseData = service.getEmptyInvoiceModel(getContractId());
        } else {
            invoiceBaseData = service.getInvoiceById(getInvoiceId());
        }
        EditInvoiceForm form = new EditInvoiceForm("form", model(from(invoiceBaseData)));
        addComponents(form);
    }

    /**
     * This constructor is used when you click on a link or try to access the EditInvoicePage manually
     * (e.g. when you type the path "/invoices/edit" in the search bar)
     * @param parameters
     */
    public EditInvoicePage(PageParameters parameters) {
        super(parameters, InvoiceOverviewPage.class, new PageParameters());
        if(getInvoiceId() == 0) {
            setResponsePage(InvoiceOverviewPage.class);
            return;
        }
        InvoiceBaseData invoiceBaseData = service.getInvoiceById(getInvoiceId());
        EditInvoiceForm form = new EditInvoiceForm("form", model(from(invoiceBaseData)));
        addComponents(form);
    }

    private void addComponents(Form<InvoiceBaseData> form) {
        add(createBacklink("cancelButton1"));
        form.add(createBacklink("cancelButton2"));
        add(form);
    }

    private long getInvoiceId() {
        return getIdByName("invoiceId");
    }

    private long getContractId(){
        return getIdByName("contractId");
    }

    private long getIdByName(String name) {
        StringValue value = getPageParameters().get(name);
        if (value == null || value.isEmpty() || value.isNull()) {
            return 0L;
        } else {
            return value.toLong();
        }
    }

    public static PageParameters createEditInvoiceParameters(long invoiceId) {
        PageParameters parameters = new PageParameters();
        parameters.add("invoiceId", invoiceId);
        return parameters;
    }

    public static PageParameters createNewInvoiceParameters(long contractId) {
        PageParameters parameters = new PageParameters();
        parameters.add("invoiceId", 0L);
        parameters.add("contractId", contractId);
        return parameters;
    }
}
