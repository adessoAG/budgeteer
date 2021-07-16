package org.wickedsource.budgeteer.web.components.burntable.table;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.event.IEvent;
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
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.joda.money.Money;
import org.wickedsource.budgeteer.MoneyUtil;
import org.wickedsource.budgeteer.service.record.RecordService;
import org.wickedsource.budgeteer.service.record.WorkRecord;
import org.wickedsource.budgeteer.service.record.WorkRecordFilter;
import org.wickedsource.budgeteer.web.components.burntable.filter.FilteredRecordsModel;
import org.wickedsource.budgeteer.web.components.customFeedback.CustomFeedbackPanel;
import org.wickedsource.budgeteer.web.components.dataTable.DataTableBehavior;
import org.wickedsource.budgeteer.web.components.dataTable.editableMoneyField.EditableMoneyField;
import org.wickedsource.budgeteer.web.components.money.BudgetUnitMoneyModel;
import org.wickedsource.budgeteer.web.components.money.MoneyLabel;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import static org.wicketstuff.lazymodel.LazyModel.from;
import static org.wicketstuff.lazymodel.LazyModel.model;

public class BurnTable extends Panel {

    private CustomFeedbackPanel feedbackPanel;
    private boolean dailyRateIsEditable;
    private DataView<WorkRecord> rows;
    private Model<Long> recordsPerPageModel = new Model<>(15L);
    private WebMarkupContainer tableComponents = new WebMarkupContainer("tableComponents");

    @Inject
    private RecordService recordService;

    BurnTable(String id, FilteredRecordsModel model){
        this(id, model, false);
    }

    public BurnTable(String id, FilteredRecordsModel model, boolean dailyRateIsEditable) {
        super(id, model);
        tableComponents.setOutputMarkupId(true);
        this.dailyRateIsEditable = dailyRateIsEditable;
        feedbackPanel = new CustomFeedbackPanel("feedback");
        feedbackPanel.setOutputMarkupId(true);
        tableComponents.add(feedbackPanel);
        WebMarkupContainer table = new WebMarkupContainer("table");
        HashMap<String, String> options = DataTableBehavior.getRecommendedOptions();
        options.put("orderClasses", "false");
        options.put("ordering", "false");
        options.put("paging", "false");
        options.put("info", "false");
        table.add(new DataTableBehavior(options));
        rows = createList(model, table);
        table.add(rows);
        tableComponents.add(table);
        IModel<Money> totalSum = () -> model.getObject().stream().map(WorkRecord::getBudgetBurned).reduce(MoneyUtil.ZERO, Money::plus);
        tableComponents.add(new MoneyLabel("total", new BudgetUnitMoneyModel(totalSum)));
        createItemsPerPageInput();
        createTableInfoLabel();
        createPageNavigation();
        add(tableComponents);
    }

    private void createTableInfoLabel() {
        Label pageLabel = new Label("pageLabel", "Showing " + Long.toString(rows.getFirstItemOffset()+1) + " to "
                + Long.toString(rows.getFirstItemOffset() + rows.getItemsPerPage()) + " entries from total " + getRows().getItemCount()){
            @Override
            protected void onBeforeRender() {
                super.onBeforeRender();
                if(rows.getPageCount() == 1L){
                    this.setDefaultModelObject("Showing " + Long.toString(rows.getFirstItemOffset()+1) + " to "
                            + rows.getItemCount() + " entries from total " + getRows().getItemCount());
                }
                else if(rows.getCurrentPage() == rows.getPageCount()-1){
                    this.setDefaultModelObject("Showing " + Long.toString(rows.getFirstItemOffset()+1) + " to "
                            + Long.toString(rows.getFirstItemOffset() + (rows.getItemCount() % rows.getItemsPerPage())) + " entries from total " + getRows().getItemCount());
                }else{
                    this.setDefaultModelObject("Showing " + Long.toString(rows.getFirstItemOffset()+1) + " to "
                            + Long.toString(rows.getFirstItemOffset() + rows.getItemsPerPage()) + " entries from total " + getRows().getItemCount());
                }
            }
        };
        pageLabel.setOutputMarkupId(true);
        tableComponents.add(pageLabel);
    }

    private void createItemsPerPageInput() {
        Form form = new Form("itemsPerPageForm") {
            @Override
            protected void onSubmit() {
                getRows().setItemsPerPage(recordsPerPageModel.getObject());
                updatePreviousAndNextButtons();
            }
        };
        form.add(new NumberTextField<>("itemsPerPage", recordsPerPageModel).setMinimum(1L).setMaximum(rows.getItemCount()));
        tableComponents.add(form);
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
    private void createPageNavigation(){
        RepeatingView pageNavButtons = new RepeatingView("pageNavButtons"){
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
                }else { //If we are on the first 3 Pages
                    if (rows.getCurrentPage() < 3) {
                        for (int i = 0; i < 4; i++) {
                            AjaxLink link = createNumberedNavButton(newChildId(), i);
                            if(rows.getCurrentPage() == i){
                                link.add(new AttributeModifier("class", "active"));
                            }
                            add(link);
                        }
                        add(createDottedNavButton(newChildId()));
                        add(createNumberedNavButton(newChildId(), rows.getPageCount()-1));
                    }
                    else if (rows.getCurrentPage() > rows.getPageCount() - 4) { //If we are on the last 3 Pages
                        add(createNumberedNavButton(newChildId(), 0));
                        add(createDottedNavButton(newChildId()));
                        for (long i = rows.getPageCount() - 4; i < rows.getPageCount(); i++) {
                            AjaxLink link = createNumberedNavButton(newChildId(), i);
                            if(rows.getCurrentPage() == i){
                                link.add(new AttributeModifier("class", "active"));
                            }
                            add(link);
                        }
                    } else{ //If the selected page is anywhere in between.
                        add(createNumberedNavButton(newChildId(), 0));
                        add(createDottedNavButton(newChildId()));
                        add(createNumberedNavButton(newChildId(), rows.getCurrentPage()-1));
                        add(createNumberedNavButton(newChildId(), rows.getCurrentPage()).add(new AttributeModifier("class", "active")));
                        add(createNumberedNavButton(newChildId(), rows.getCurrentPage()+1));
                        add(createDottedNavButton(newChildId()));
                        add(createNumberedNavButton(newChildId(), rows.getPageCount()-1));
                    }
                }
                updatePreviousAndNextButtons();
            }
        };

        previousPageButton = new AjaxLink<Void>("previousPage") {
            @Override
            public void onClick(AjaxRequestTarget target) {
                if(rows.getCurrentPage() > 0){
                    rows.setCurrentPage(rows.getCurrentPage()-1);
                    updatePreviousAndNextButtons();
                    target.add(tableComponents);
                }
            }
        };
        nextPageButton = new AjaxLink<Void>("nextPage") {
            @Override
            public void onClick(AjaxRequestTarget target) {
                if(rows.getCurrentPage() < rows.getPageCount()){
                    rows.setCurrentPage(rows.getCurrentPage()+1);
                    updatePreviousAndNextButtons();
                    target.add(tableComponents);
                }
            }
        };

        tableComponents.add(nextPageButton);
        tableComponents.add(previousPageButton);
        tableComponents.add(pageNavButtons);
    }

    private AjaxLink createNumberedNavButton(String id, long pageIndex){
        return (AjaxLink)new AjaxLink<Void>(id) {
            @Override
            public void onClick(AjaxRequestTarget target) {
                rows.setCurrentPage(pageIndex);
                target.add(tableComponents);
            }
        }.add(new Label("pageNavLabel", Long.toString(pageIndex+1)));
    }

    private Link createDottedNavButton(String id){
        return (Link)new Link<Void>(id) {
            @Override
            public void onClick(){}
        }.add(new Label("pageNavLabel", "...")).add(new AttributeModifier("class", "disabled"))
                .setEnabled(false);
    }

    @Override
    public void onEvent(IEvent<?> event) {
        super.onEvent(event);
        Object payload = event.getPayload();
        if (payload instanceof WorkRecordFilter) {
            WorkRecordFilter filter = (WorkRecordFilter) payload;
            FilteredRecordsModel model = (FilteredRecordsModel) getDefaultModel();
            model.setFilter(filter);
        }
    }

    private DataView <WorkRecord> createList(final FilteredRecordsModel model, final WebMarkupContainer table) {
        return new DataView<WorkRecord>("recordList", new ListDataProvider<WorkRecord>(model.getObject()){
            @Override
            protected List<WorkRecord> getData() {
                return model.getObject();
            }
        },recordsPerPageModel.getObject()) {

            @Override
            protected void populateItem(Item<WorkRecord> item) {
                item.setOutputMarkupId(true);
                item.add(new Label("budget", model(from(item.getModel()).getBudgetName())));
                item.add(new Label("person", model(from(item.getModel()).getPersonName()) ));
                if(dailyRateIsEditable) {
                    final EditableMoneyField editableMoneyField = new EditableMoneyField("dailyRate", table, model(from(item.getModelObject()).getDailyRate())) {
                        @Override
                        protected void save(AjaxRequestTarget target, Form<Money> form) {
                            item.getModelObject().setEditedManually(true);
                            item.getModelObject().setDailyRate(form.getModelObject());
                            recordService.saveDailyRateForWorkRecord(item.getModelObject());
                            target.add(tableComponents);
                            long elementIndex = 0;
                            int i = 0;
                            Iterator<? extends  WorkRecord> iterator = rows.getDataProvider().iterator(0, rows.getItemCount());
                            while (iterator.hasNext()){
                                if(iterator.next().getId() == item.getModelObject().getId()){
                                    elementIndex = i;
                                }
                                i++;
                            }
                            rows.setCurrentPage(elementIndex/rows.getItemsPerPage());
                        }

                        @Override
                        protected void cancel(AjaxRequestTarget target, Form<Money> form) {
                            item.getModelObject().setEditedManually(item.getModelObject().isEditedManually());
                            target.add(item);
                        }

                        @Override
                        protected void convertError(AjaxRequestTarget target) {
                            target.add(feedbackPanel);
                        }
                    };
                    item.add(editableMoneyField);
                } else {
                    item.add(new MoneyLabel("dailyRate", model(from(item.getModel()).getDailyRate())));
                }
                item.add(new Label("edited"){
                    @Override
                    public boolean isVisible() {
                        return item.getModelObject().isEditedManually();
                    }
                });
                item.add(new Label("date", model(from(item.getModel()).getDate())));
                item.add(new Label("hours", model(from(item.getModel()).getHours())));
                item.add(new MoneyLabel("burnedBudget", new BudgetUnitMoneyModel(model(from(item.getModel()).getBudgetBurned()))));
            }
        };
    }

    public DataView<WorkRecord> getRows() {
        return rows;
    }
}
