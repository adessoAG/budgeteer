package org.wickedsource.budgeteer.web.pages.user.selectproject;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.OnChangeAjaxBehavior;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.RequiredTextField;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.wickedsource.budgeteer.service.project.ProjectBaseData;
import org.wickedsource.budgeteer.service.project.ProjectNameAlreadyInUseException;
import org.wickedsource.budgeteer.service.project.ProjectService;
import org.wickedsource.budgeteer.service.user.UserService;
import org.wickedsource.budgeteer.web.BudgeteerSession;
import org.wickedsource.budgeteer.web.components.customFeedback.CustomFeedbackPanel;
import org.wickedsource.budgeteer.web.pages.base.dialogpage.DialogPage;
import org.wickedsource.budgeteer.web.pages.dashboard.DashboardPage;
import org.wickedsource.budgeteer.web.pages.user.login.LoginPage;
import org.wicketstuff.annotation.mount.MountPath;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;


@MountPath("/selectProjectWithKeycloak")
public class SelectProjectWithKeycloakPage extends DialogPage {

    @SpringBean
    private ProjectService projectService;

    @SpringBean
    private UserService userService;

    private CustomFeedbackPanel feedbackPanel;

    public SelectProjectWithKeycloakPage() {
        add(createLogoutLink("logoutLink"));
        add(createNewProjectForm("newProjectForm"));
        add(createChooseProjectForm("chooseProjectForm"));
        Form feedbackForm = new Form("feedbackForm", new Model<String>());
        feedbackPanel = new CustomFeedbackPanel("feedback");
        feedbackPanel.setOutputMarkupId(true);
        feedbackForm.add(feedbackPanel);
        add(feedbackForm);
    }

    private Form<String> createNewProjectForm(String id) {
        Form<String> form = new Form<String>(id, new Model<String>("")) {
            @Override
            protected void onSubmit() {
                try {
                    ProjectBaseData project = projectService.createProject(getModelObject(), BudgeteerSession.get().getLoggedInUser().getId());
                    BudgeteerSession.get().setProjectId(project.getId());
                    setResponsePage(DashboardPage.class);
                }catch (ProjectNameAlreadyInUseException exception){
                    this.error(getString("newProjectForm.projectName.AlreadyInUse"));
                }

            }
        };
        form.add(new RequiredTextField<String>("projectName", form.getModel()));
        return form;
    }

    private Form<ProjectBaseData> createChooseProjectForm(String id) {
        LoadableDetachableModel<ProjectBaseData> defaultProjectModel = new LoadableDetachableModel<ProjectBaseData>() {

            @Override
            protected ProjectBaseData load() {
                return projectService.getDefaultProjectForUser(BudgeteerSession.get().getLoggedInUser().getId());
            }
        };
        final Form<ProjectBaseData> form = new Form<ProjectBaseData>("chooseProjectForm", defaultProjectModel);

        DropDownChoice<ProjectBaseData> choice = new DropDownChoice<ProjectBaseData>("projectChoice", form.getModel(), new ProjectsForUserModel(BudgeteerSession.get().getLoggedInUser().getId()), new ProjectChoiceRenderer());
        choice.setRequired(true);
        choice.add(new OnChangeAjaxBehavior() {
            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                target.add(feedbackPanel);
            }
        });
        form.add(choice);

        AjaxSubmitLink markProjectAsDefault = new AjaxSubmitLink ("markProject") {
            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                try {
                    projectService.setDefaultProject(BudgeteerSession.get().getLoggedInUser().getId(), ((ProjectBaseData) form.getModelObject()).getId());
                    info(getString("chooseProjectForm.defaultProject.successful"));
                } catch(Exception e){
                    error(getString("chooseProjectForm.defaultProject.failed"));
                }
                target.add(form, feedbackPanel);
            }
        };

        AjaxSubmitLink goButton = new AjaxSubmitLink ("goButton") {
            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                BudgeteerSession.get().setProjectId(((ProjectBaseData) form.getModelObject()).getId());
                setResponsePage(DashboardPage.class);
            }
        };


        form.add(markProjectAsDefault);
        form.add(goButton);
        return form;
    }

    private Link createLogoutLink(String id) {
        return new Link(id) {
            @Override
            public void onClick() {
                HttpServletRequest request = (HttpServletRequest) getRequestCycle().getRequest().getContainerRequest();
                BudgeteerSession.get().logout();
                try {
                    request.logout();
                } catch (ServletException e) {
                    e.printStackTrace();
                }
                setResponsePage(LoginPage.class);
            }
        };
    }

}

