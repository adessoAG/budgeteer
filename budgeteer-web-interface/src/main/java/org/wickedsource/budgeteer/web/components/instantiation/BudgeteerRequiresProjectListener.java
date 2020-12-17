package org.wickedsource.budgeteer.web.components.instantiation;

import org.apache.wicket.Component;
import org.apache.wicket.RestartResponseAtInterceptPageException;
import org.apache.wicket.application.IComponentInstantiationListener;
import org.apache.wicket.application.IComponentOnBeforeRenderListener;
import org.apache.wicket.injection.Injector;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.wickedsource.budgeteer.web.BudgeteerSession;
import org.wickedsource.budgeteer.web.BudgeteerSettings;
import org.wickedsource.budgeteer.web.pages.user.selectproject.SelectProjectPage;
import org.wickedsource.budgeteer.web.pages.user.selectproject.SelectProjectWithKeycloakPage;

/**
 * {@link IComponentInstantiationListener} and {@link IComponentOnBeforeRenderListener} implementation
 * to validate the presence of a project if components, which are annotated with {@link NeedsProject},
 * are initiated or are going to be rendered.
 *
 * <i>OnBeforeRenderListener needed for cached stateful components to be rendered correctly.</i>
 *
 * @see NeedsProject
 * @see BudgeteerSession#isProjectSelected()
 */
public class BudgeteerRequiresProjectListener implements IComponentOnBeforeRenderListener, IComponentInstantiationListener {

    @SpringBean
    private BudgeteerSettings settings;

    public BudgeteerRequiresProjectListener() {
        Injector.get().inject(this);
    }

    @Override
    public void onBeforeRender(Component component) {
        this.redirectToSelectProjectIfNeeded(component);
    }

    @Override
    public void onInstantiation(Component component) {
        this.redirectToSelectProjectIfNeeded(component);
    }

    /**
     * Redirects the user to the {@link SelectProjectPage} (or {@link SelectProjectWithKeycloakPage} respectively
     * when using keycloak) when the component requires a project but it is not set.
     *
     * @param component
     *          The component to check.
     * @throws RestartResponseAtInterceptPageException
     *          When a {@link NeedsProject} annotation is present but no projet is currently selected.
     *
     * @see BudgeteerSession#isProjectSelected()
     */
    private void redirectToSelectProjectIfNeeded(Component component) throws RestartResponseAtInterceptPageException {
        boolean requiresProject = component != null && component.getClass().isAnnotationPresent(NeedsProject.class);

        if(requiresProject && !BudgeteerSession.get().isProjectSelected()) {
                    final WebPage respPage = new SelectProjectPage("projectForm.project.Required");
                    throw new RestartResponseAtInterceptPageException(respPage);
        }
    }

}
