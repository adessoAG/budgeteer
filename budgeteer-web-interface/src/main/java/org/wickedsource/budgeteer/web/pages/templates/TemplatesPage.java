package org.wickedsource.budgeteer.web.pages.templates;

import org.apache.wicket.markup.html.link.Link;
import org.wickedsource.budgeteer.web.BudgeteerSession;
import org.wickedsource.budgeteer.web.pages.base.basepage.BasePage;
import org.wickedsource.budgeteer.web.pages.base.basepage.breadcrumbs.BreadcrumbsModel;
import org.wickedsource.budgeteer.web.pages.dashboard.DashboardPage;
import org.wickedsource.budgeteer.web.pages.templates.table.TemplateListModel;
import org.wickedsource.budgeteer.web.pages.templates.table.TemplatesTable;
import org.wickedsource.budgeteer.web.pages.templates.templateimport.ImportTemplatesPage;
import org.wicketstuff.annotation.mount.MountPath;

@MountPath("templates")
public class TemplatesPage extends BasePage {

    public TemplatesPage() {
        TemplatesTable table = new TemplatesTable("templateTable", new TemplateListModel(BudgeteerSession.get().getTemplateFilter()));
        add(table);
        add(new TemplateFilterPanel("filter", BudgeteerSession.get().getTemplateFilter()));
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
