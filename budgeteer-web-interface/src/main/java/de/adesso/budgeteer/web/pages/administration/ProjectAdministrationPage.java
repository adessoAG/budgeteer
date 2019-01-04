package de.adesso.budgeteer.web.pages.administration;

import de.adesso.budgeteer.service.DateRange;
import de.adesso.budgeteer.service.DateUtil;
import de.adesso.budgeteer.service.project.ProjectService;
import de.adesso.budgeteer.service.user.User;
import de.adesso.budgeteer.service.user.UserService;
import de.adesso.budgeteer.web.BudgeteerSession;
import de.adesso.budgeteer.web.BudgeteerSettings;
import de.adesso.budgeteer.web.ClassAwareWrappingModel;
import de.adesso.budgeteer.web.Mount;
import de.adesso.budgeteer.web.components.customFeedback.CustomFeedbackPanel;
import de.adesso.budgeteer.web.components.daterange.DateRangeInputField;
import de.adesso.budgeteer.web.pages.base.basepage.BasePage;
import de.adesso.budgeteer.web.pages.base.basepage.breadcrumbs.BreadcrumbsModel;
import de.adesso.budgeteer.web.pages.base.delete.DeleteDialog;
import de.adesso.budgeteer.web.pages.dashboard.DashboardPage;
import de.adesso.budgeteer.web.pages.user.login.LoginPage;
import de.adesso.budgeteer.web.pages.user.selectproject.SelectProjectPage;
import de.adesso.budgeteer.web.pages.user.selectproject.SelectProjectWithKeycloakPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;

import java.util.List;

import static org.wicketstuff.lazymodel.LazyModel.from;
import static org.wicketstuff.lazymodel.LazyModel.model;

@Mount("/administration")
public class ProjectAdministrationPage extends BasePage {

    @SpringBean
    private UserService userService;

    @SpringBean
    private ProjectService projectService;

    @SpringBean
    private BudgeteerSettings settings;

    public ProjectAdministrationPage() {
        add(new CustomFeedbackPanel("feedback"));
        add(createUserList("userList", new UsersInProjectModel(BudgeteerSession.get().getProjectId())));
        add(createDeleteProjectButton("deleteProjectButton"));
        add(createAddUserForm("addUserForm"));
        add(createEditProjectForm("projectChangeForm"));
    }

    private Form<Project> createEditProjectForm(String formId) {
        Form<Project> form = new Form<Project>(formId, model(from(projectService.findProjectById(BudgeteerSession.get().getProjectId())))) {
            @Override
            protected void onSubmit() {
                super.onSubmit();
                if(getModelObject().getName() == null){
                    error(getString("error.no.name"));
                }else {
                    Project ent = getModelObject();
                    projectService.save(ent);
                    success(getString("project.saved"));
                }
            }
        };
        form.add(new TextField<>("projectTitle", model(from(form.getModelObject()).getName())));
        DateRange defaultDateRange = new DateRange(DateUtil.getBeginOfYear(), DateUtil.getEndOfYear());
        form.add(new DateRangeInputField("projectStart", model(from(form.getModelObject()).getDateRange()), defaultDateRange, DateRangeInputField.DROP_LOCATION.DOWN));
        return form;
    }

    private ListView<User> createUserList(String id, IModel<List<User>> model) {
        User thisUser = BudgeteerSession.get().getLoggedInUser();
        return new ListView<User>(id, model) {
            @Override
            protected void populateItem(final ListItem<User> item) {
                item.add(new Label("username", model(from(item.getModel()).getName())));
                Link deleteButton = new Link("deleteButton") {
                    @Override
                    public void onClick() {

                        setResponsePage(new DeleteDialog() {
                            @Override
                            protected void onYes() {
                                userService.removeUserFromProject(BudgeteerSession.get().getProjectId(), item.getModelObject().getId());
                                setResponsePage(ProjectAdministrationPage.class, getPageParameters());
                            }

                            @Override
                            protected void onNo() {
                                setResponsePage(ProjectAdministrationPage.class, getPageParameters());
                            }

                            @Override
                            protected String confirmationText() {
                                return ProjectAdministrationPage.this.getString("delete.person.confirmation");
                            }
                        });
                    }
                };
                // a user may not delete herself/himself
                if (item.getModelObject().equals(thisUser))
                    deleteButton.setVisible(false);
                item.add(deleteButton);
            }

            @Override
            protected ListItem<User> newItem(int index, IModel<User> itemModel) {
                return super.newItem(index, new ClassAwareWrappingModel<>(itemModel, User.class));
            }
        };
    }

    private Form<User> createAddUserForm(String id) {
        Form<User> form = new Form<User>(id, new Model<>(new User())) {
            @Override
            protected void onSubmit() {
                userService.addUserToProject(BudgeteerSession.get().getProjectId(), getModelObject().getId());
            }
        };

        DropDownChoice<User> userChoice = new DropDownChoice<>("userChoice", form.getModel(), new UsersNotInProjectModel(BudgeteerSession.get().getProjectId()), new UserChoiceRenderer());
        userChoice.setRequired(true);
        form.add(userChoice);
        return form;
    }

    private Link createDeleteProjectButton(String id) {
        return new Link(id) {
            @Override
            public void onClick() {
                setResponsePage(new DeleteDialog() {
                    @Override
                    protected void onYes() {
                        projectService.deleteProject(BudgeteerSession.get().getProjectId());
                        BudgeteerSession.get().setProjectSelected(false);

                        if (settings.isKeycloakActivated()) {
                            setResponsePage(new SelectProjectWithKeycloakPage());
                        } else {
                            setResponsePage(new SelectProjectPage(LoginPage.class, new PageParameters()));
                        }
                    }

                    @Override
                    protected void onNo() {
                        setResponsePage(ProjectAdministrationPage.class, getPageParameters());
                    }

                    @Override
                    protected String confirmationText() {
                        return ProjectAdministrationPage.this.getString("delete.project.confirmation");
                    }
                });
            }
        };
    }

    @Override
    protected BreadcrumbsModel getBreadcrumbsModel() {
        return new BreadcrumbsModel(DashboardPage.class, ProjectAdministrationPage.class);
    }

}
