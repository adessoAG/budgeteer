package org.wickedsource.budgeteer.web.components.burntable;

import org.apache.wicket.Page;
import org.apache.wicket.markup.html.link.DownloadLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.wickedsource.budgeteer.service.exports.ExportService;
import org.wickedsource.budgeteer.service.record.WorkRecordFilter;
import org.wickedsource.budgeteer.web.components.burntable.filter.FilterPanel;
import org.wickedsource.budgeteer.web.components.burntable.filter.FilteredRecordsModel;
import org.wickedsource.budgeteer.web.components.burntable.table.BurnTable;

import javax.inject.Inject;
import java.io.File;

public class BurnTableWithFilter extends Panel {

    @Inject
    private ExportService exportService;

    private FilterPanel filterPanel;

    private BurnTable burnTable;

    public BurnTableWithFilter(String id, WorkRecordFilter initialFilter, Page page, PageParameters pageParameters) {
        this(id, initialFilter, false, page, pageParameters);
    }

    public BurnTableWithFilter(String id, WorkRecordFilter initialFilter, boolean dailyRateIsEditable, Page page, PageParameters pageParameters) {
        super(id);

        filterPanel = new FilterPanel("filter", initialFilter, page, pageParameters);
        add(filterPanel);

        FilteredRecordsModel tableModel = new FilteredRecordsModel(initialFilter);
        burnTable = new BurnTable("table", tableModel, dailyRateIsEditable);
        add(burnTable);

        IModel<File> fileModel = () -> exportService.generateCSVFileFromRecords(burnTable.getRows());

        DownloadLink downloadlink = new DownloadLink("export", fileModel);
        downloadlink.setDeleteAfterDownload(true);
        add(downloadlink);
    }

    public boolean isPersonFilterEnabled() {
        return filterPanel.isPersonFilterEnabled();
    }

    public void setPersonFilterEnabled(boolean personFilterEnabled) {
        filterPanel.setPersonFilterEnabled(personFilterEnabled);
    }

    public boolean isBudgetFilterEnabled() {
        return filterPanel.isBudgetFilterEnabled();
    }

    public void setBudgetFilterEnabled(boolean budgetFilterEnabled) {
        filterPanel.setBudgetFilterEnabled(budgetFilterEnabled);
    }

    public boolean isDaterangeFilterEnabled() {
        return filterPanel.isDaterangeFilterEnabled();
    }

    public void setDaterangeFilterEnabled(boolean daterangeFilterEnabled) {
        filterPanel.setDaterangeFilterEnabled(daterangeFilterEnabled);
    }

}
