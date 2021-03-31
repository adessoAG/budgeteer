package org.wickedsource.budgeteer.web.pages.hours;

import org.apache.wicket.markup.html.link.DownloadLink;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.wickedsource.budgeteer.service.exports.ExportService;
import org.wickedsource.budgeteer.service.record.RecordService;
import org.wickedsource.budgeteer.service.record.WorkRecord;
import org.wickedsource.budgeteer.service.record.WorkRecordFilter;
import org.wickedsource.budgeteer.web.BudgeteerSession;
import org.wickedsource.budgeteer.web.Mount;
import org.wickedsource.budgeteer.web.components.burntable.BurnTableWithFilter;
import org.wickedsource.budgeteer.web.pages.base.basepage.BasePage;
import org.wickedsource.budgeteer.web.pages.base.basepage.breadcrumbs.BreadcrumbsModel;
import org.wickedsource.budgeteer.web.pages.dashboard.DashboardPage;

import javax.inject.Inject;
import java.io.File;
import java.util.List;

@Mount("hours")
public class HoursPage extends BasePage {

    @Inject
    private RecordService recordService;

    @Inject
    private ExportService exportService;

    public HoursPage() {
        long projectId = BudgeteerSession.get().getProjectId();

        IModel<WorkRecordFilter> filter = Model.of(new WorkRecordFilter(projectId));
        IModel<List<WorkRecord>> records = new AbstractReadOnlyModel<List<WorkRecord>>() {
            @Override
            public List<WorkRecord> getObject() {
                return recordService.getFilteredRecords(filter.getObject());
            }
        };

        BurnTableWithFilter table = new BurnTableWithFilter("burnTable", records, filter);
        add(table);
        IModel<File> file = new AbstractReadOnlyModel<File>() {
            @Override
            public File getObject() {
                return exportService.generateCSVFileFromRecords(records.getObject(), BudgeteerSession.get().getLocale());
            }
        };
        add(new DownloadLink("export", file).setDeleteAfterDownload(true));
    }

    @Override
    protected BreadcrumbsModel getBreadcrumbsModel() {
        return new BreadcrumbsModel(DashboardPage.class, HoursPage.class);
    }

}
