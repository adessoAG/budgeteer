package org.wickedsource.budgeteer.web.pages.person.details;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.SubmitLink;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.wickedsource.budgeteer.service.person.PersonService;
import org.wickedsource.budgeteer.web.Mount;
import org.wickedsource.budgeteer.web.components.confirm.ConfirmationForm;
import org.wickedsource.budgeteer.web.pages.base.basepage.BasePage;
import org.wickedsource.budgeteer.web.pages.base.basepage.breadcrumbs.Breadcrumb;
import org.wickedsource.budgeteer.web.pages.base.basepage.breadcrumbs.BreadcrumbsModel;
import org.wickedsource.budgeteer.web.pages.base.delete.DeleteDialog;
import org.wickedsource.budgeteer.web.pages.dashboard.DashboardPage;
import org.wickedsource.budgeteer.web.pages.person.PersonNameModel;
import org.wickedsource.budgeteer.web.pages.person.details.chart.BudgetDistributionChart;
import org.wickedsource.budgeteer.web.pages.person.details.chart.BudgetDistributionChartModel;
import org.wickedsource.budgeteer.web.pages.person.details.highlights.PersonHighlightsModel;
import org.wickedsource.budgeteer.web.pages.person.details.highlights.PersonHighlightsPanel;
import org.wickedsource.budgeteer.web.pages.person.edit.EditPersonPage;
import org.wickedsource.budgeteer.web.pages.person.hours.PersonHoursPage;
import org.wickedsource.budgeteer.web.pages.person.monthreport.PersonMonthReportPage;
import org.wickedsource.budgeteer.web.pages.person.overview.PeopleOverviewPage;
import org.wickedsource.budgeteer.web.pages.person.weekreport.PersonWeekReportPage;

@Mount("people/details/${id}")
public class PersonDetailsPage extends BasePage {

    @SpringBean
    private PersonService personService;

    public PersonDetailsPage(PageParameters parameters) {
        super(parameters);
        add(new PersonHighlightsPanel("highlightsPanel", new PersonHighlightsModel(getParameterId())));
        add(new BudgetDistributionChart("distributionChart", new BudgetDistributionChartModel(getParameterId())));
        add(createEditPersonLink("editPersonLink"));
        add(new BookmarkablePageLink<PersonWeekReportPage>("weekReportLink", PersonWeekReportPage.class, PersonWeekReportPage.createParameters(getParameterId())));
        add(new BookmarkablePageLink<PersonWeekReportPage>("monthReportLink", PersonMonthReportPage.class, PersonMonthReportPage.createParameters(getParameterId())));
        add(new BookmarkablePageLink<PersonWeekReportPage>("hoursLink", PersonHoursPage.class, PersonHoursPage.createParameters(getParameterId())));

        Form<Void> deleteForm = new ConfirmationForm<Void>("deleteForm", this, "confirmation.delete") {
            @Override
            public void onSubmit() {

                setResponsePage(new DeleteDialog() {
                    @Override
                    protected void onYes() {
                        personService.deletePerson(getParameterId());
                        setResponsePage(PeopleOverviewPage.class);
                    }

                    @Override
                    protected void onNo() {
                        setResponsePage(PersonDetailsPage.class, getPageParameters());
                    }

                    @Override
                    protected String confirmationText() {
                        return PersonDetailsPage.this.getString("confirmation.delete");
                    }
                });
            }
        };
        deleteForm.add(new SubmitLink("deletePersonLink"));
        add(deleteForm);
    }

    private Link createEditPersonLink(String id) {
        return new Link<Void>(id) {
            @Override
            public void onClick() {
                WebPage page = new EditPersonPage(EditPersonPage.createParameters(getParameterId()), PersonDetailsPage.class, getPageParameters());
                setResponsePage(page);
            }
        };
    }

    @Override
    @SuppressWarnings("unchecked")
    protected BreadcrumbsModel getBreadcrumbsModel() {
        BreadcrumbsModel model = new BreadcrumbsModel(DashboardPage.class, PeopleOverviewPage.class);
        model.addBreadcrumb(new Breadcrumb(PersonDetailsPage.class, getPageParameters(), new PersonNameModel(getParameterId())));
        return model;
    }

}
