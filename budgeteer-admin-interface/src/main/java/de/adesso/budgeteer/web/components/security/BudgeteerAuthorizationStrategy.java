package de.adesso.budgeteer.web.components.security;

import de.adesso.budgeteer.web.pages.user.login.LoginPage;
import org.apache.wicket.Component;
import org.apache.wicket.RestartResponseAtInterceptPageException;
import org.apache.wicket.authorization.Action;
import org.apache.wicket.authorization.IAuthorizationStrategy;
import org.apache.wicket.request.component.IRequestableComponent;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.IResource;
import org.wickedsource.budgeteer.web.BudgeteerSession;

public class BudgeteerAuthorizationStrategy implements IAuthorizationStrategy {

    @Override
    public <T extends IRequestableComponent> boolean isInstantiationAuthorized(Class<T> componentClass) {
        boolean needsLogin = componentClass.isAnnotationPresent(NeedsLogin.class);
        if(needsLogin && !BudgeteerSession.get().isLoggedIn()) {
            throw new RestartResponseAtInterceptPageException(LoginPage.class);
        }
        return true;
    }

    @Override
    public boolean isActionAuthorized(Component component, Action action) {
        return true;
    }

    @Override
    public boolean isResourceAuthorized(IResource resource, PageParameters parameters) {
        return true;
    }
}
