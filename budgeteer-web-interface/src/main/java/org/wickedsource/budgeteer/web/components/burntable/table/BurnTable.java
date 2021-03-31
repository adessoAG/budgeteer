package org.wickedsource.budgeteer.web.components.burntable.table;

import org.apache.poi.poifs.crypt.temp.SXSSFWorkbookWithCustomZipEntrySource;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.NumberTextField;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.markup.repeater.data.ListDataProvider;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.joda.money.Money;
import org.wickedsource.budgeteer.MoneyUtil;
import org.wickedsource.budgeteer.service.record.WorkRecord;
import org.wickedsource.budgeteer.web.components.customFeedback.CustomFeedbackPanel;
import org.wickedsource.budgeteer.web.components.dataTable.DataTableBehavior;
import org.wickedsource.budgeteer.web.components.money.BudgetUnitMoneyModel;
import org.wickedsource.budgeteer.web.components.money.MoneyLabel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static org.wicketstuff.lazymodel.LazyModel.from;
import static org.wicketstuff.lazymodel.LazyModel.model;

public class BurnTable extends Panel {

    private final IModel<List<WorkRecord>> model;
    private DataView<WorkRecord> rows;
    private final Model<Long> recordsPerPageModel = Model.of(15L);
    private final WebMarkupContainer tableComponents;

    public BurnTable(String id, IModel<List<WorkRecord>> model) {
        super(id, model);
        this.model = model;

        tableComponents = createTableContainer();
        add(tableComponents);
        tableComponents.add(createFeedbackPanel());
        tableComponents.add(createTable());
        tableComponents.add(createMoneyLabel());
        tableComponents.add(createCurrentPageMoneyLabel());
        tableComponents.add(createItemsPerPageInput());
        tableComponents.add(createTableInfoLabel());
        createPageNavigation();
        add(tableComponents);
    }

    private WebMarkupContainer createTableContainer() {
        WebMarkupContainer container = new WebMarkupContainer("tableComponents");
        container.setOutputMarkupId(true);
        return container;
    }

    private Component createTable() {
        WebMarkupContainer table = new WebMarkupContainer("table");
        HashMap<String, String> options = DataTableBehavior.getRecommendedOptions();
        options.put("orderClasses", "false");
        options.put("ordering", "false");
        options.put("paging", "false");
        options.put("info", "false");
        table.add(new DataTableBehavior(options));
        rows = createDataView();
        table.add(rows);
        return table;
    }

    private Component createFeedbackPanel() {
        return new CustomFeedbackPanel("feedback").setOutputMarkupId(true);
    }

    private Component createTableInfoLabel() {
        Label pageLabel = new Label("pageLabel", new AbstractReadOnlyModel<String>() {
            @Override
            public String getObject() {
                long total = rows.getItemCount();
                long firstItemInView = rows.getFirstItemOffset();
                long itemsOnPage = rows.getCurrentPage() == rows.getPageCount() - 1 ? total % rows.getItemsPerPage() : rows.getItemsPerPage();
                long start = firstItemInView + 1;
                long end = firstItemInView + itemsOnPage;
                return String.format("Showing %d to %d entries from total %d", start, end, total);
            }
        });
        pageLabel.setOutputMarkupId(true);
        return pageLabel;
    }

    private Component createCurrentPageMoneyLabel() {
        IModel<Money> spentCurrentPage = new AbstractReadOnlyModel<Money>() {

            @Override
            public Money getObject() {
                long total = rows.getItemCount();
                long start = rows.getFirstItemOffset();
                long itemsOnPage = rows.getCurrentPage() == rows.getPageCount() - 1 ? total % rows.getItemsPerPage() : rows.getItemsPerPage();
                List<WorkRecord> recordsOnPage = total == 0 ? Collections.emptyList() : model.getObject().subList((int) start, (int) (start + itemsOnPage));
                return recordsOnPage.stream().map(WorkRecord::getBudgetBurned).reduce(MoneyUtil.ZERO, Money::plus);
            }
        };
        return new MoneyLabel("spentCurrentPage", new BudgetUnitMoneyModel(spentCurrentPage));
    }

    private Component createMoneyLabel() {
        return new MoneyLabel("total", new BudgetUnitMoneyModel(new TotalBudgetModel(model)));
    }

    private Component createItemsPerPageInput() {
        Form<Long> form = new Form<Long>("itemsPerPageForm") {
            @Override
            protected void onSubmit() {
                rows.setItemsPerPage(recordsPerPageModel.getObject());
                updatePreviousAndNextButtons();
            }
        };
        form.add(new NumberTextField<>("itemsPerPage", recordsPerPageModel).setMinimum(1L).setMaximum(rows.getItemCount()));
        return form;
    }


    private AjaxLink previousPageButton;
    private AjaxLink nextPageButton;

    private void updatePreviousAndNextButtons(){
        if(rows.getCurrentPage() == rows.getPageCount()-1){
            nextPageButton.add(new AttributeModifier("class", "disabled"));
            nextPageButton.setEnabled(false);
        }else {
            nextPageButton.add(new AttributeModifier("class", "enabled"));
            nextPageButton.setEnabled(true);
        }
        if(rows.getCurrentPage() == 0){
            previousPageButton.add(new AttributeModifier("class", "disabled"));
            previousPageButton.setEnabled(false);
        }else {
            previousPageButton.add(new AttributeModifier("class", "enabled"));
            previousPageButton.setEnabled(true);
        }
    }

    /**
     * This mimics the exact paging functionality of DataTables.js.
     * I could not use the standard DataTables paging, because that plugin has no
     * idea the table even has more pages to it (as Wicket doesn't load eveything at once) -
     * it just wants to have all the data at page creation time.
     */
    private void createPageNavigation() {
        RepeatingView pageNavButtons = new RepeatingView("pageNavButtons") {
            @Override
            protected void onBeforeRender() {
                super.onBeforeRender();
                this.removeAll();

                //If there are less than 5 Pages
                if(rows.getPageCount() < 5){
                    for(int i = 0; i < rows.getPageCount(); i++){
                        AjaxLink link = createNumberedNavButton(newChildId(), i);
                        if(rows.getCurrentPage() == i){
                            link.add(new AttributeModifier("class", "active"));
                        }
                        add(link);
                    }
                } else { //If we are on the first 3 Pages
                    if (rows.getCurrentPage() < 3) {
                        for (int i = 0; i < 4; i++) {
                            AjaxLink link = createNumberedNavButton(newChildId(), i);
                            if(rows.getCurrentPage() == i){
                                link.add(new AttributeModifier("class", "active"));
                            }
                            add(link);
                        }
                        add(createDottedNavButton(newChildId()));
                        add(createNumberedNavButton(newChildId(), rows.getPageCount() - 1));
                    } else if (rows.getCurrentPage() > rows.getPageCount() - 4) { //If we are on the last 3 Pages
                        add(createNumberedNavButton(newChildId(), 0));
                        add(createDottedNavButton(newChildId()));
                        for (long i = rows.getPageCount() - 4; i < rows.getPageCount(); i++) {
                            AjaxLink link = createNumberedNavButton(newChildId(), i);
                            if(rows.getCurrentPage() == i){
                                link.add(new AttributeModifier("class", "active"));
                            }
                            add(link);
                        }
                    } else { //If the selected page is anywhere in between.
                        add(createNumberedNavButton(newChildId(), 0));
                        add(createDottedNavButton(newChildId()));
                        add(createNumberedNavButton(newChildId(), rows.getCurrentPage()-1));
                        add(createNumberedNavButton(newChildId(), rows.getCurrentPage()).add(new AttributeModifier("class", "active")));
                        add(createNumberedNavButton(newChildId(), rows.getCurrentPage()+1));
                        add(createDottedNavButton(newChildId()));
                        add(createNumberedNavButton(newChildId(), rows.getPageCount() - 1));
                    }
                }
                updatePreviousAndNextButtons();
            }
        };

        previousPageButton = new AjaxLink<Void>("previousPage") {
            @Override
            public void onClick(AjaxRequestTarget target) {
                if (rows.getCurrentPage() > 0) {
                    rows.setCurrentPage(rows.getCurrentPage() - 1);
                    updatePreviousAndNextButtons();
                    target.add(tableComponents);
                }
            }
        };
        nextPageButton = new AjaxLink<Void>("nextPage") {
            @Override
            public void onClick(AjaxRequestTarget target) {
                if (rows.getCurrentPage() < rows.getPageCount()) {
                    rows.setCurrentPage(rows.getCurrentPage() + 1);
                    updatePreviousAndNextButtons();
                    target.add(tableComponents);
                }
            }
        };

        tableComponents.add(nextPageButton);
        tableComponents.add(previousPageButton);
        tableComponents.add(pageNavButtons);
    }

    private AjaxLink<Void> createNumberedNavButton(String id, long pageIndex) {
        AjaxLink<Void> link = new AjaxLink<Void>(id) {
            @Override
            public void onClick(AjaxRequestTarget target) {
                rows.setCurrentPage(pageIndex);
                target.add(tableComponents);
            }
        };
        link.add(new Label("pageNavLabel", Long.toString(pageIndex + 1)));
        return link;
    }

    private Link<Void> createDottedNavButton(String id) {
        Link<Void> link = new Link<Void>(id) {
            @Override
            public void onClick() {
                // Do nothing
            }
        };
        link.add(new Label("pageNavLabel", "..."))
                .add(new AttributeModifier("class", "disabled"))
                .setEnabled(false);
        return link;
    }

    private DataView<WorkRecord> createDataView() {
        ListDataProvider<WorkRecord> dataProvider = new ListDataProvider<WorkRecord>() {
            @Override
            protected List<WorkRecord> getData() {
                return model.getObject();
            }
        };
        return new DataView<WorkRecord>("recordList", dataProvider, recordsPerPageModel.getObject()) {
            @Override
            protected void populateItem(Item<WorkRecord> item) {
                item.setOutputMarkupId(true);
                item.add(new Label("budget", model(from(item.getModel()).getBudgetName())));
                item.add(new Label("person", model(from(item.getModel()).getPersonName())));
                item.add(new MoneyLabel("dailyRate", model(from(item.getModel()).getDailyRate())));
                item.add(new Label("date", model(from(item.getModel()).getDate())));
                item.add(new Label("hours", model(from(item.getModel()).getHours())));
                item.add(new MoneyLabel("burnedBudget", new BudgetUnitMoneyModel(model(from(item.getModel()).getBudgetBurned()))));
            }
        };
    }
}
