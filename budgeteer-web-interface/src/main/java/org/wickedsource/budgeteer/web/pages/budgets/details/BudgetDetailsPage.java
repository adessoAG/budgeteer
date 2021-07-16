package org.wickedsource.budgeteer.web.pages.budgets.details;

import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.SubmitLink;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.wickedsource.budgeteer.service.budget.BudgetDetailData;
import org.wickedsource.budgeteer.service.budget.BudgetService;
import org.wickedsource.budgeteer.web.Mount;
import org.wickedsource.budgeteer.web.components.confirm.ConfirmationForm;
import org.wickedsource.budgeteer.web.pages.base.basepage.BasePage;
import org.wickedsource.budgeteer.web.pages.base.basepage.breadcrumbs.Breadcrumb;
import org.wickedsource.budgeteer.web.pages.base.basepage.breadcrumbs.BreadcrumbsModel;
import org.wickedsource.budgeteer.web.pages.base.delete.DeleteDialog;
import org.wickedsource.budgeteer.web.pages.budgets.BudgetNameModel;
import org.wickedsource.budgeteer.web.pages.budgets.details.chart.PeopleDistributionChart;
import org.wickedsource.budgeteer.web.pages.budgets.details.chart.PeopleDistributionChartModel;
import org.wickedsource.budgeteer.web.pages.budgets.details.highlights.BudgetHighlightsModel;
import org.wickedsource.budgeteer.web.pages.budgets.details.highlights.BudgetHighlightsPanel;
import org.wickedsource.budgeteer.web.pages.budgets.edit.EditBudgetPage;
import org.wickedsource.budgeteer.web.pages.budgets.hours.BudgetHoursPage;
import org.wickedsource.budgeteer.web.pages.budgets.monthreport.single.SingleBudgetMonthReportPage;
import org.wickedsource.budgeteer.web.pages.budgets.notes.BudgetNotesPage;
import org.wickedsource.budgeteer.web.pages.budgets.overview.BudgetsOverviewPage;
import org.wickedsource.budgeteer.web.pages.budgets.weekreport.single.SingleBudgetWeekReportPage;
import org.wickedsource.budgeteer.web.pages.contract.details.ContractDetailsPage;
import org.wickedsource.budgeteer.web.pages.dashboard.DashboardPage;

@Mount("budgets/details/${id}")
public class BudgetDetailsPage extends BasePage {

    @SpringBean
    private BudgetService budgetService;

    private IModel<BudgetDetailData> model;

    public BudgetDetailsPage(PageParameters parameters) {
        super(parameters);
        model = () -> budgetService.loadBudgetDetailData(getParameterId());
        add(new BudgetHighlightsPanel("highlightsPanel", new BudgetHighlightsModel(getParameterId())));
        add(new PeopleDistributionChart("distributionChart", new PeopleDistributionChartModel(getParameterId())));
        add(new BookmarkablePageLink<SingleBudgetWeekReportPage>("weekReportLink", SingleBudgetWeekReportPage.class, createParameters(getParameterId())));
        add(new BookmarkablePageLink<SingleBudgetMonthReportPage>("monthReportLink", SingleBudgetMonthReportPage.class, createParameters(getParameterId())));
        addContractLinks();
        add(new BookmarkablePageLink<BudgetHoursPage>("hoursLink", BudgetHoursPage.class, createParameters(getParameterId())));
        add(new BookmarkablePageLink<BudgetNotesPage>("notesLink", BudgetNotesPage.class, createParameters(getParameterId())));
        add(createEditLink("editLink"));

        Form deleteForm = new ConfirmationForm("deleteForm", this, "confirmation.delete") {
            @Override
            public void onSubmit() {
                setResponsePage(new DeleteDialog() {
                    @Override
                    protected void onYes() {
                        budgetService.deleteBudget(getParameterId());
                        setResponsePage(BudgetsOverviewPage.class);
                    }

                    @Override
                    protected void onNo() {
                        setResponsePage(new BudgetDetailsPage(BudgetDetailsPage.this.getPageParameters()));
                    }

                    @Override
                    protected String confirmationText() {
                        return BudgetDetailsPage.this.getString("confirmation.delete");
                    }
                });
            }
        };
        if(this.model.getObject().getContractName() != null){
            deleteForm.setEnabled(false);
            deleteForm.add(new AttributeAppender("style", "cursor: not-allowed;", " "));
            deleteForm.add(new AttributeModifier("title", getString("contract.still.exist")));
        }
        deleteForm.add(new SubmitLink("deleteLink"));
        add(deleteForm);
    }

    public static PageParameters createParameters(long budgetId) {
        PageParameters parameters = new PageParameters();
        parameters.add("id", budgetId);
        return parameters;
    }

    private void addContractLinks() {
        BookmarkablePageLink<ContractDetailsPage> contractLinkName = new BookmarkablePageLink<ContractDetailsPage>("contractLink", ContractDetailsPage.class, ContractDetailsPage.createParameters(model.getObject().getContractId())) {
            @Override
            protected void onConfigure() {
                super.onConfigure();
                if (model.getObject().getContractId() == 0) {
                    setEnabled(false);
                    add(new AttributeAppender("style", "cursor: not-allowed;", " "));
                    add(new AttributeModifier("title", BudgetDetailsPage.this.getString("links.contract.label.no.contract")));
                }
            }
        };
        contractLinkName.add(new Label("contractName", () -> StringUtils.isBlank(model.getObject().getContractName()) ? getString("links.contract.label.no.contract") : model.getObject().getContractName())
        );

        add(contractLinkName);
    }

    private Link createEditLink(String id) {
        return new Link(id) {
            @Override
            public void onClick() {
                WebPage page = new EditBudgetPage(BasePage.createParameters(getParameterId()), BudgetDetailsPage.class, getPageParameters(), false);
                setResponsePage(page);
            }
        };
    }

    @Override
    protected BreadcrumbsModel getBreadcrumbsModel() {
        BreadcrumbsModel model = new BreadcrumbsModel(DashboardPage.class, BudgetsOverviewPage.class);
        model.addBreadcrumb(new Breadcrumb(BudgetDetailsPage.class, getPageParameters(), new BudgetNameModel(getParameterId())));
        return model;
    }

    @Override
    protected void onDetach() {
        model.detach();
        super.onDetach();
    }
}
