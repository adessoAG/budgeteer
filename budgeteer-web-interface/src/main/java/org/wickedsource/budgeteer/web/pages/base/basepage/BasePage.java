package org.wickedsource.budgeteer.web.pages.base.basepage;

import org.apache.wicket.Component;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptReferenceHeaderItem;
import org.apache.wicket.markup.head.filter.HeaderResponseContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.util.string.StringValue;
import org.wickedsource.budgeteer.web.BudgeteerReferences;
import org.wickedsource.budgeteer.web.BudgeteerSession;
import org.wickedsource.budgeteer.web.BudgeteerSettings;
import org.wickedsource.budgeteer.web.components.instantiation.NeedsProject;
import org.wickedsource.budgeteer.web.components.security.NeedsLogin;
import org.wickedsource.budgeteer.web.pages.administration.ProjectAdministrationPage;
import org.wickedsource.budgeteer.web.pages.base.basepage.breadcrumbs.BreadcrumbsModel;
import org.wickedsource.budgeteer.web.pages.base.basepage.breadcrumbs.BreadcrumbsPanel;
import org.wickedsource.budgeteer.web.pages.base.basepage.budgetunitchoice.BudgetUnitChoice;
import org.wickedsource.budgeteer.web.pages.base.basepage.budgetunitchoice.BudgetUnitModel;
import org.wickedsource.budgeteer.web.pages.base.basepage.notifications.NotificationDropdown;
import org.wickedsource.budgeteer.web.pages.base.basepage.notifications.NotificationModel;
import org.wickedsource.budgeteer.web.pages.dashboard.DashboardPage;
import org.wickedsource.budgeteer.web.pages.user.edit.EditUserPage;
import org.wickedsource.budgeteer.web.pages.user.login.LoginPage;
import org.wickedsource.budgeteer.web.pages.user.selectproject.SelectProjectPage;

@NeedsLogin
@NeedsProject
public abstract class BasePage extends WebPage {
    protected NotificationDropdown notificationDropdown;

    @SpringBean
    private BudgeteerSettings settings;

    public BasePage(PageParameters parameters) {
        super(parameters);
        addComponents();
    }

    public BasePage() {
        addComponents();
    }

    /**
     * Creates a valid PageParameters object to pass into the constructor of this page class.
     *
     * @param objectId id of the budget whose details to display.
     * @return a valid PageParameters object.
     */
    public static PageParameters createParameters(long objectId) {
        PageParameters parameters = new PageParameters();
        parameters.add("id", objectId);
        return parameters;
    }

    @SuppressWarnings("unchecked")
    private void addComponents() {
        BreadcrumbsPanel breadcrumbs = new BreadcrumbsPanel("breadcrumbsPanel", getBreadcrumbsModel());
        long projectId = ((BudgeteerSession) getSession()).getProjectId();
        long userId = ((BudgeteerSession) getSession()).getLoggedInUser().getId();
        add(breadcrumbs);
        notificationDropdown = new NotificationDropdown("notificationDropdown", new NotificationModel(projectId, userId));
        notificationDropdown.setOutputMarkupId(true);
        add(notificationDropdown);
        add(new BudgetUnitChoice("budgetUnitDropdown", new BudgetUnitModel(projectId)));

        BookmarkablePageLink pageLink = new BookmarkablePageLink<ProjectAdministrationPage>("administrationLink", ProjectAdministrationPage.class);
        add(pageLink);
        pageLink.setVisible(false);
        add(createProjectChangeLink("changeProjectLink"));
        add(createMyProfileLink("myProfileLink"));
        add(createLogoutLink("logoutLink"));
        add(createDashboardLink("dashboardLink"));
        add(new HeaderResponseContainer("JavaScriptContainer", "JavaScriptContainer"));
    }

    @Override
    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);
        response.render(JavaScriptReferenceHeaderItem.forReference(BudgeteerReferences.getJQueryReference()));
        response.render(JavaScriptReferenceHeaderItem.forReference(BudgeteerReferences.getAdminLteAppReference()));
    }

    private Component createDashboardLink(String id) {
        return new BookmarkablePageLink<>(id, DashboardPage.class);
    }

    private Link createProjectChangeLink(String id) {
        return new Link<Void>(id) {
            @Override
            public void onClick() {
                setResponsePage(new SelectProjectPage(this.getWebPage().getClass(), getPageParameters()));
            }
        };
    }

    private Link createLogoutLink(String id) {
        return new Link<Void>(id) {
            @Override
            public void onClick() {
                BudgeteerSession.get().logout();
                setResponsePage(LoginPage.class);
            }
        };
    }

    private Link createMyProfileLink(String id) {
        return new Link<Void>(id) {
            @Override
            public void onClick() {
                setResponsePage(new EditUserPage(this.getWebPage().getClass(), new PageParameters().add("userId", BudgeteerSession.get().getLoggedInUser().getId())));
            }
        };
    }

    /**
     * Expects a BreadcrumbsModel to be returned that contains all pages that should be displayed in the breadcrumbs.
     */
    protected abstract BreadcrumbsModel getBreadcrumbsModel();

    public long getParameterId() {
        StringValue value = getPageParameters().get("id");
        if (value == null || value.isEmpty() || value.isNull()) {
            return 0L;
        } else {
            return value.toLong();
        }
    }
}
