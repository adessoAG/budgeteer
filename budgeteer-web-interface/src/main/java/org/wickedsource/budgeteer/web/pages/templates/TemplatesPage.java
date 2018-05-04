package org.wickedsource.budgeteer.web.pages.templates;

import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.IModel;
import org.wickedsource.budgeteer.service.template.Template;
import org.wickedsource.budgeteer.web.BudgeteerSession;
import org.wickedsource.budgeteer.web.ClassAwareWrappingModel;
import org.wickedsource.budgeteer.web.Mount;
import org.wickedsource.budgeteer.web.pages.base.basepage.BasePage;
import org.wickedsource.budgeteer.web.pages.base.basepage.breadcrumbs.BreadcrumbsModel;
import org.wickedsource.budgeteer.web.pages.dashboard.DashboardPage;
import org.wickedsource.budgeteer.web.pages.templates.table.TemplateListModel;
import org.wickedsource.budgeteer.web.pages.templates.table.TemplatesTable;
import org.wickedsource.budgeteer.web.pages.templates.templateimport.ImportTemplatesPage;

import java.util.List;

@Mount("templates")
public class TemplatesPage extends BasePage {

    public TemplatesPage() {
        TemplatesTable table = new TemplatesTable("templateTable", new TemplateListModel(BudgeteerSession.get().getProjectId()));
        add(table);
        add(createImportLink("importLink"));
    }

    @Override
    protected BreadcrumbsModel getBreadcrumbsModel() {
        return new BreadcrumbsModel(DashboardPage.class, TemplatesPage.class);
    }

    private Link createImportLink(String id) {
        final ImportTemplatesPage importPage = new ImportTemplatesPage(TemplatesPage.class, getPageParameters());
        return new Link(id) {
            @Override
            public void onClick() {
                setResponsePage(importPage);
            }
        };
    }
}