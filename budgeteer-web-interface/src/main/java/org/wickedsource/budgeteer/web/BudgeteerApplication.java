package org.wickedsource.budgeteer.web;

import de.adesso.wickedcharts.wicket7.JavaScriptResourceRegistry;
import org.apache.wicket.RuntimeConfigurationType;
import org.apache.wicket.Session;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.filter.JavaScriptFilteredIntoFooterHeaderResponse;
import org.apache.wicket.markup.html.IHeaderResponseDecorator;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.request.IExceptionMapper;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.Response;
import org.apache.wicket.spring.injection.annot.SpringComponentInjector;
import org.apache.wicket.util.IProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import org.wickedsource.budgeteer.web.components.instantiation.BudgeteerRequiresProjectListener;
import org.wickedsource.budgeteer.web.components.security.BudgeteerAuthorizationStrategy;
import org.wickedsource.budgeteer.web.components.security.BudgeteerUnauthorizedComponentInstantiationListener;
import org.wickedsource.budgeteer.web.pages.dashboard.DashboardPage;
import org.wicketstuff.annotation.scan.AnnotatedMountScanner;

@Component
public class BudgeteerApplication extends WebApplication implements ApplicationContextAware {

    private ApplicationContext context;

    @Autowired
    private BudgeteerSettings settings;

    @Override
    public Class<? extends WebPage> getHomePage() {
        return DashboardPage.class;
    }

    @Override
    public void init() {
        super.init();

        getMarkupSettings().setStripWicketTags(true);
        getComponentInstantiationListeners().add(new SpringComponentInjector(this, context));
        initWickedCharts();
        getJavaScriptLibrarySettings().setJQueryReference(BudgeteerReferences.getJQueryReference());
        new AnnotatedMountScanner().scanPackage("org.wickedsource.budgeteer.web.pages").mount(this);

        getSecuritySettings().setAuthorizationStrategy(new BudgeteerAuthorizationStrategy());
        getSecuritySettings().setUnauthorizedComponentInstantiationListener(new BudgeteerUnauthorizedComponentInstantiationListener());
        setHeaderResponseDecorator(new JavaScriptToBucketResponseDecorator("JavaScriptContainer"));

        // add component instantiation/onBeforeRender listener
        final BudgeteerRequiresProjectListener listener = new BudgeteerRequiresProjectListener();
        getComponentInstantiationListeners().add(listener);
        getComponentPreOnBeforeRenderListeners().add(listener);
    }

    /** * Decorates an original IHeaderResponse and renders all javascript items * (JavaScriptHeaderItem), to a specific container in the page. */
    static class JavaScriptToBucketResponseDecorator implements IHeaderResponseDecorator {

        private String bucketName;

        public JavaScriptToBucketResponseDecorator(String bucketName){
            this.bucketName = bucketName;
        }

        @Override public IHeaderResponse decorate(IHeaderResponse response){
            return new JavaScriptFilteredIntoFooterHeaderResponse(response, bucketName);
        }

    }

    @Override
    public RuntimeConfigurationType getConfigurationType() {
        String configuration = settings.getConfigurationType();
        if (RuntimeConfigurationType.DEVELOPMENT.name().equalsIgnoreCase(configuration)) {
            return RuntimeConfigurationType.DEVELOPMENT;
        } else {
            return RuntimeConfigurationType.DEPLOYMENT;
        }
    }

    private void initWickedCharts() {
        JavaScriptResourceRegistry.getInstance().setChartJsReference(BudgeteerReferences.getChartjsReference());
        JavaScriptResourceRegistry.getInstance().setChartJsBundleReference(BudgeteerReferences.getChartjsReference());
        JavaScriptResourceRegistry.getInstance().setMomentJsReference(BudgeteerReferences.getMomentJsReference());
        JavaScriptResourceRegistry.getInstance().setJQueryReference(BudgeteerReferences.getJQueryReference());
    }

    @Override
    public Session newSession(Request request, Response response) {
        return new BudgeteerSession(request);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.context = applicationContext;
    }

    @Override
    public IProvider<IExceptionMapper> getExceptionMapperProvider() {
        return BudgeteerExceptionMapper::new;
    }

}


