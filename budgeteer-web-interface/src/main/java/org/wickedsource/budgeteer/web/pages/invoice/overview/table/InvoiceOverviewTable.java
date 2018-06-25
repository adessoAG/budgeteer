package org.wickedsource.budgeteer.web.pages.invoice.overview.table;

import static org.wicketstuff.lazymodel.LazyModel.from;
import static org.wicketstuff.lazymodel.LazyModel.model;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.wickedsource.budgeteer.MoneyUtil;
import org.wickedsource.budgeteer.service.contract.DynamicAttributeField;
import org.wickedsource.budgeteer.service.invoice.InvoiceBaseData;
import org.wickedsource.budgeteer.web.BudgeteerSession;
import org.wickedsource.budgeteer.web.PropertyLoader;
import org.wickedsource.budgeteer.web.components.dataTable.DataTableBehavior;
import org.wickedsource.budgeteer.web.pages.base.basepage.BasePage;
import org.wickedsource.budgeteer.web.pages.base.basepage.breadcrumbs.BreadcrumbsModel;
import org.wickedsource.budgeteer.web.pages.contract.details.ContractDetailsPage;
import org.wickedsource.budgeteer.web.pages.invoice.details.InvoiceDetailsPage;
import org.wickedsource.budgeteer.web.pages.invoice.edit.EditInvoicePage;

public class InvoiceOverviewTable extends Panel{
	private final BreadcrumbsModel breadcrumbsModel;

	public InvoiceOverviewTable(String id, InvoiceOverviewTableModel invoiceOverviewTableModel, BreadcrumbsModel breadcrumbsModel) {
		super(id);
		addComponents(invoiceOverviewTableModel);
		this.breadcrumbsModel = breadcrumbsModel;
	}

	private void addComponents(final InvoiceOverviewTableModel data) {
		WebMarkupContainer table = new WebMarkupContainer("table");
		table.add(new DataTableBehavior(DataTableBehavior.getRecommendedOptions()));
		table.add(new ListView<String>("headerRow",  model(from(data).getHeadline()) ) {
			@Override
			protected void populateItem(ListItem<String> item) {
				item.add(new Label("headerItem", item.getModelObject()));
			}
		});
		table.add(new ListView<InvoiceBaseData>("invoiceRows", model(from(data).getInvoices())) {
			@Override
			protected void populateItem(final ListItem<InvoiceBaseData> item) {
				final long invoiceId = item.getModelObject().getInvoiceId();
				Link link = new Link("showInvoice"){
					@Override
					public void onClick() {
						WebPage page = new InvoiceDetailsPage(InvoiceDetailsPage.createParameters(invoiceId)){

							@Override
							protected BreadcrumbsModel getBreadcrumbsModel() {
								BreadcrumbsModel m = breadcrumbsModel;
								m.addBreadcrumb(InvoiceDetailsPage.class, InvoiceDetailsPage.createParameters(invoiceId));
								return m;
							}
						};
						setResponsePage(page);
					}
				};

				link.add(new Label("invoiceName", model(from(item.getModelObject()).getInvoiceName())));
				item.add(link);
				Link contractLink = new Link("contractLink"){
					@Override
					public void onClick() {
						WebPage page = new ContractDetailsPage(ContractDetailsPage.createParameters(item.getModelObject().getContractId())){

							@Override
							protected BreadcrumbsModel getBreadcrumbsModel() {
								BreadcrumbsModel m = breadcrumbsModel;
								m.addBreadcrumb(ContractDetailsPage.class, ContractDetailsPage.createParameters(item.getModelObject().getContractId()));
								return m;
							}
						};
						setResponsePage(page);
					}
				};
				contractLink.add(new Label("contractName", model(from(item.getModelObject()).getContractName())));
				item.add(contractLink);
				item.add(new Label("internalNumber", model(from(item.getModelObject()).getInternalNumber())));
				item.add(new Label("year", model(from(item.getModelObject()).getYear())));
				item.add(new Label("month_number", getMonthNumberAsString(item.getModelObject().getMonth())));
				item.add(new Label("month", PropertyLoader.getProperty(BasePage.class, "monthRenderer.name." + item.getModelObject().getMonth())));
				item.add(new Label("sum", Model.of(MoneyUtil.toDouble(item.getModelObject().getSum(), BudgeteerSession.get().getSelectedBudgetUnit()))));
				CheckBox paid = new CheckBox("paid", model(from(item.getModelObject()).isPaid()));
				paid.setEnabled(false);
				item.add(paid);
				item.add(new ListView<DynamicAttributeField>("invoiceRow", model(from(item.getModelObject()).getDynamicInvoiceFields())) {
					@Override
					protected void populateItem(ListItem<DynamicAttributeField> item) {
						item.add(new Label("invoiceRowText", item.getModelObject().getValue()));
					}
				});
				item.add(new BookmarkablePageLink<EditInvoicePage>("editLink", EditInvoicePage.class, EditInvoicePage.createEditInvoiceParameters(invoiceId)));
			}
		});
		table.add(new ListView<String>("footerRow", model(from(data).getFooter()) ) {
			@Override
			protected void populateItem(ListItem<String> item) {
				item.add(new Label("footerItem", item.getModelObject()));
			}
		});
		add(table);
	}

	private String getMonthNumberAsString(int month) {
		String r = "" + month;
		if(r.length() < 2){
			r = "0" + r;
		}
		return r;
	}
}
