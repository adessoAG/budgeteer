package org.wickedsource.budgeteer.web.pages.budgets.overview.table;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.event.IEvent;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.wickedsource.budgeteer.service.budget.BudgetDetailData;
import org.wickedsource.budgeteer.service.budget.BudgetService;
import org.wickedsource.budgeteer.service.budget.BudgetTagFilter;
import org.wickedsource.budgeteer.service.contract.ContractService;
import org.wickedsource.budgeteer.web.BudgeteerSession;
import org.wickedsource.budgeteer.web.ClassAwareWrappingModel;
import org.wickedsource.budgeteer.web.components.dataTable.DataTableBehavior;
import org.wickedsource.budgeteer.web.components.money.BudgetUnitMoneyModel;
import org.wickedsource.budgeteer.web.components.money.MoneyLabel;
import org.wickedsource.budgeteer.web.components.tax.TaxBudgetUnitMoneyModel;
import org.wickedsource.budgeteer.web.components.tax.TaxLabelModel;
import org.wickedsource.budgeteer.web.pages.base.basepage.breadcrumbs.BreadcrumbsModel;
import org.wickedsource.budgeteer.web.pages.base.delete.DeleteDialog;
import org.wickedsource.budgeteer.web.pages.budgets.details.BudgetDetailsPage;
import org.wickedsource.budgeteer.web.pages.budgets.edit.EditBudgetPage;
import org.wickedsource.budgeteer.web.pages.budgets.overview.BudgetsOverviewPage;
import org.wickedsource.budgeteer.web.pages.budgets.overview.table.progressbar.ProgressBar;
import org.wickedsource.budgeteer.web.pages.contract.details.ContractDetailsPage;

import java.util.List;

import static org.wicketstuff.lazymodel.LazyModel.from;
import static org.wicketstuff.lazymodel.LazyModel.model;

public class BudgetOverviewTable extends Panel {

    private final BreadcrumbsModel breadcrumbsModel;
  
    @SpringBean
    private ContractService contractService;

    @SpringBean
    private BudgetService budgetService;
  
    public BudgetOverviewTable(String id, IModel<List<BudgetDetailData>> model, BreadcrumbsModel breadcrumbsModel) {
        super(id, model);
        this.breadcrumbsModel = breadcrumbsModel;
        WebMarkupContainer table = new WebMarkupContainer("table");
        table.add(new DataTableBehavior(DataTableBehavior.getRecommendedOptions()));

        createNetGrossOverviewLabels(table);

        table.add(createBudgetList("budgetList", model));
        addTableSummaryLabels(table, model);

        add(table);
    }

    private void addTableSummaryLabels(WebMarkupContainer table, IModel<List<BudgetDetailData>> model) {

        IModel<BudgetDetailData> totalModel = new TotalBudgetDetailsModel(model);
        table.add(new Label("totalLastUpdated", model(from(totalModel).getLastUpdated())));

        table.add(new MoneyLabel("totalAmount",
                new TaxBudgetUnitMoneyModel(
                        new BudgetUnitMoneyModel(model(from(totalModel).getTotal())),
                        new BudgetUnitMoneyModel(model(from(totalModel).getTotal_gross()))
                )));
        table.add(new MoneyLabel("totalSpent",
                new TaxBudgetUnitMoneyModel(
                        new BudgetUnitMoneyModel(model(from(totalModel).getSpent())),
                        new BudgetUnitMoneyModel(model(from(totalModel).getSpent_gross()))
                )));
        table.add(new MoneyLabel("totalRemaining",
                new TaxBudgetUnitMoneyModel(
                        new BudgetUnitMoneyModel(model(from(totalModel).getRemaining())),
                        new BudgetUnitMoneyModel(model(from(totalModel).getRemaining_gross()))
                )));
        table.add(new MoneyLabel("totalUnplanned",
                new TaxBudgetUnitMoneyModel(
                        new BudgetUnitMoneyModel(model(from(totalModel).getUnplanned())),
                        new BudgetUnitMoneyModel(model(from(totalModel).getUnplanned_gross()))
                )));

        table.add(new ProgressBar("totalProgressBar", model(from(totalModel).getProgressInPercent())));
    }

    @Override
    public void onEvent(IEvent<?> event) {
        super.onEvent(event);
        Object payload = event.getPayload();
        if (payload instanceof BudgetTagFilter) {
            BudgetTagFilter filter = (BudgetTagFilter) event.getPayload();
            FilteredBudgetModel model = (FilteredBudgetModel) getDefaultModel();
            model.setFilter(model(from(filter)));
        } else if (payload instanceof String) {
            Long remainingFilter;
            try {
                remainingFilter = Long.parseLong((String) event.getPayload());
            } catch (NumberFormatException e) {
                return;
            }
            FilteredBudgetModel model = (FilteredBudgetModel) getDefaultModel();
            model.setRemainingFilterModel(model(from(remainingFilter)));
            BudgeteerSession.get().setRemainingBudetFilterValue(remainingFilter);
        }
    }

    private ListView<BudgetDetailData> createBudgetList(String id, IModel<List<BudgetDetailData>> model) {
        return new ListView<BudgetDetailData>(id, model) {
            @Override
            protected void populateItem(final ListItem<BudgetDetailData> item) {
                BookmarkablePageLink link = new BookmarkablePageLink("detailLink", BudgetDetailsPage.class,
                        BudgetDetailsPage.createParameters(item.getModelObject().getId()));
                Label linkTitle = new Label("detailLinkTitle", model(from(item.getModel()).getName()));
                link.add(linkTitle);
                item.add(link);
                item.add(new Label("lastUpdated", model(from(item.getModel()).getLastUpdated())));
                Link clink = new Link("contractLink") {
                    @Override
                    public void onClick() {
                        setResponsePage(ContractDetailsPage.class, ContractDetailsPage.createParameters(item.getModelObject().getContractId()));
                    }
                };
                Label clinkTitle = new Label("contractTitle", model(from(item.getModel()).getContractName()));
                clink.add(clinkTitle);
                item.add(clink);

                createBudgetListEntry(item);

                item.add(new ProgressBar("progressBar", model(from(item.getModel()).getProgressInPercent())));

                Link deleteBudgetButton = new Link("deleteBudget") {
                    @Override
                    public void onClick() {
                        setResponsePage(new DeleteDialog() {
                            @Override
                            protected void onYes() {
                                budgetService.deleteBudget(item.getModelObject().getId());
                                setResponsePage(BudgetsOverviewPage.class);
                            }

                            @Override
                            protected void onNo() {
                                setResponsePage(BudgetsOverviewPage.class);
                            }

                            @Override
                            protected String confirmationText() {
                                return BudgetOverviewTable.this.getString("delete.budget.confirmation");
                            }
                        });
                    }
                };
                //Creating a separate tooltip is necessary because disabling the button also causes tooltips to disappear.
                WebMarkupContainer deleteBudgetTooltip = new WebMarkupContainer("deleteBudgetTooltip");
                deleteBudgetTooltip.add(new AttributeModifier("title", getString("delete.budget")));
                deleteBudgetTooltip.add(deleteBudgetButton);
                item.add(deleteBudgetTooltip);

                Link editBudgetLink = new Link("editPage") {
                    @Override
                    public void onClick() {
                        WebPage page = new EditBudgetPage(EditBudgetPage.createParameters(
                                item.getModelObject().getId()), BudgetsOverviewPage.class, null, false);
                        setResponsePage(page);
                    }
                };
                item.add(editBudgetLink);
            }

            @Override
            protected ListItem<BudgetDetailData> newItem(int index, IModel<BudgetDetailData> itemModel) {
                return super.newItem(index,
                        new ClassAwareWrappingModel<>(itemModel, BudgetDetailData.class));
            }
        };
    }

    private void createBudgetListEntry(ListItem<BudgetDetailData> item) {
        item.add(new MoneyLabel("amount",
                new TaxBudgetUnitMoneyModel(
                        new BudgetUnitMoneyModel(model(from(item.getModel()).getTotal())),
                        new BudgetUnitMoneyModel(model(from(item.getModel()).getTotal_gross()))
                )));

        MoneyLabel spentMoneyLabel = new MoneyLabel("spent",
                new TaxBudgetUnitMoneyModel(
                        new BudgetUnitMoneyModel(model(from(item.getModel()).getSpent())),
                        new BudgetUnitMoneyModel(model(from(item.getModel()).getSpent_gross()))
                ));
        if (item.getModelObject().getSpent().compareTo(item.getModelObject().getLimit()) >= 0 && !item.getModelObject().getLimit().isZero())
            spentMoneyLabel.add(new AttributeModifier("style", "color: red"));
        item.add(spentMoneyLabel);

        item.add(new MoneyLabel("remaining",
                new TaxBudgetUnitMoneyModel(
                        new BudgetUnitMoneyModel(model(from(item.getModel()).getRemaining())),
                        new BudgetUnitMoneyModel(model(from(item.getModel()).getRemaining_gross()))
                )));
        item.add(new MoneyLabel("unplanned",
                new TaxBudgetUnitMoneyModel(
                        new BudgetUnitMoneyModel(model(from(item.getModel()).getUnplanned())),
                        new BudgetUnitMoneyModel(model(from(item.getModel()).getUnplanned_gross()))
                )));
    }


    private void createNetGrossOverviewLabels(WebMarkupContainer table) {
        table.add(new Label("totalLabel", new TaxLabelModel(
                new StringResourceModel("overview.table.budget.totalLabel", this))));
        table.add(new Label("leftLabel", new TaxLabelModel(
                new StringResourceModel("overview.table.budget.leftLabel", this))));
        table.add(new Label("spentLabel", new TaxLabelModel(
                new StringResourceModel("overview.table.budget.spentLabel", this))));
        table.add(new Label("unplannedLabel", new TaxLabelModel(
                new StringResourceModel("overview.table.budget.unplannedLabel", this))));
    }
}
