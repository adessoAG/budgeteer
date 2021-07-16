package org.wickedsource.budgeteer.web.pages.budgets.overview;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.wickedsource.budgeteer.service.budget.BudgetService;
import org.wickedsource.budgeteer.service.budget.BudgetTagFilter;
import org.wickedsource.budgeteer.web.BudgeteerSession;
import org.wickedsource.budgeteer.web.components.links.NetGrossLink;
import org.wickedsource.budgeteer.web.pages.base.basepage.BasePage;
import org.wickedsource.budgeteer.web.pages.base.basepage.breadcrumbs.BreadcrumbsModel;
import org.wickedsource.budgeteer.web.pages.budgets.BudgetTagsModel;
import org.wickedsource.budgeteer.web.pages.budgets.RemainingBudgetFilterModel;
import org.wickedsource.budgeteer.web.pages.budgets.edit.EditBudgetPage;
import org.wickedsource.budgeteer.web.pages.budgets.monthreport.multi.MultiBudgetMonthReportPage;
import org.wickedsource.budgeteer.web.pages.budgets.overview.filter.BudgetRemainingFilterPanel;
import org.wickedsource.budgeteer.web.pages.budgets.overview.filter.BudgetTagFilterPanel;
import org.wickedsource.budgeteer.web.pages.budgets.overview.report.BudgetReportPage;
import org.wickedsource.budgeteer.web.pages.budgets.overview.table.BudgetOverviewTable;
import org.wickedsource.budgeteer.web.pages.budgets.overview.table.FilteredBudgetModel;
import org.wickedsource.budgeteer.web.pages.budgets.weekreport.multi.MultiBudgetWeekReportPage;
import org.wickedsource.budgeteer.web.pages.dashboard.DashboardPage;
import org.wicketstuff.annotation.mount.MountPath;

import java.util.ArrayList;

import static org.wicketstuff.lazymodel.LazyModel.from;
import static org.wicketstuff.lazymodel.LazyModel.model;

@MountPath("budgets")
public class BudgetsOverviewPage extends BasePage {

    @SpringBean
    private BudgetService budgetService;

    public BudgetsOverviewPage() {
        BudgetTagsModel tagsModel = new BudgetTagsModel(BudgeteerSession.get().getProjectId());
        if (BudgeteerSession.get().getBudgetFilter() == null) {
            BudgetTagFilter filter = new BudgetTagFilter(new ArrayList<>(), BudgeteerSession.get().getProjectId());
            BudgeteerSession.get().setBudgetFilter(filter);
        }
        add(new BudgetRemainingFilterPanel("remainingFilter", new RemainingBudgetFilterModel(BudgeteerSession.get().getProjectId())));
        add(new BudgetTagFilterPanel("tagFilter", tagsModel));
        FilteredBudgetModel filteredBudgetModel = new FilteredBudgetModel(BudgeteerSession.get().getProjectId(), model(from(BudgeteerSession.get().getBudgetFilter())));
        filteredBudgetModel.setRemainingFilterModel(model(from(BudgeteerSession.get().getRemainingBudgetFilterValue())));
        add(new BudgetOverviewTable("budgetTable", filteredBudgetModel ,getBreadcrumbsModel()));
        add(new BookmarkablePageLink<MultiBudgetWeekReportPage>("weekReportLink", MultiBudgetWeekReportPage.class));
        add(new BookmarkablePageLink<MultiBudgetMonthReportPage>("monthReportLink", MultiBudgetMonthReportPage.class));
        add(createNewBudgetLink("createBudgetLink"));
        add(createReportLink("createReportLink"));
        add(new NetGrossLink("netGrossLink"));
        add(createResetButton("resetButton"));
    }

    private Component createReportLink(String string) {
        Link link = new Link(string) {
			@Override
			public void onClick() {
				setResponsePage(new BudgetReportPage(BudgetsOverviewPage.class, new PageParameters()));
			}
		};

        if (!budgetService.projectHasBudgets(BudgeteerSession.get().getProjectId())) {
            link.setEnabled(false);
            link.add(new AttributeAppender("style", "cursor: not-allowed;", " "));
            link.add(new AttributeModifier("title", BudgetsOverviewPage.this.getString("links.budget.label.no.budget")));
        }
        return link;
	}

    private Link createNewBudgetLink(String id) {
        return new Link(id) {
            @Override
            public void onClick() {
                WebPage page = new EditBudgetPage(BudgetsOverviewPage.class, getPageParameters());
                setResponsePage(page);
            }
        };
    }

    @SuppressWarnings("unchecked")
    @Override
    protected BreadcrumbsModel getBreadcrumbsModel() {
        return new BreadcrumbsModel(DashboardPage.class, BudgetsOverviewPage.class);
    }

    private Link createResetButton(String id) {
        return new Link(id) {
            @Override
            public void onClick() {
                BudgeteerSession.get().getBudgetFilter().getSelectedTags().clear();
                BudgeteerSession.get().setRemainingBudetFilterValue(0L);
                setResponsePage(BudgetsOverviewPage.class);
            }
        };
    }
}
