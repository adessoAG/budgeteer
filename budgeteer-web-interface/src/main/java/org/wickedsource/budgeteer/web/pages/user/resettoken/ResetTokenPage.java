package org.wickedsource.budgeteer.web.pages.user.resettoken;

import org.apache.wicket.injection.Injector;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.EmailTextField;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.wickedsource.budgeteer.persistence.user.UserEntity;
import org.wickedsource.budgeteer.service.user.MailNotFoundException;
import org.wickedsource.budgeteer.service.user.UserService;
import org.wickedsource.budgeteer.service.user.TokenStatus;
import org.wickedsource.budgeteer.web.Mount;
import org.wickedsource.budgeteer.web.components.customFeedback.CustomFeedbackPanel;
import org.wickedsource.budgeteer.web.pages.base.dialogpage.DialogPage;
import org.wickedsource.budgeteer.web.pages.dashboard.DashboardPage;

import static org.wicketstuff.lazymodel.LazyModel.from;
import static org.wicketstuff.lazymodel.LazyModel.model;

@Mount("resettoken")
public class ResetTokenPage extends DialogPage {

    @SpringBean
    private UserService service;

    public ResetTokenPage() {
        addComponents();
    }

    public ResetTokenPage(PageParameters pageParameters) {
        handleStatusCode(pageParameters);
        addComponents();
    }

    private void addComponents() {
        Injector.get().inject(this);
        Form<ResetTokenData> form = new Form<ResetTokenData>("resetTokenForm", model(from(new ResetTokenData()))) {
            @Override
            protected void onSubmit() {
                try {
                    UserEntity userEntity = service.getUserByMail(getModelObject().getMail());

                    if (!userEntity.isMailVerified()) {
                        service.createNewVerificationTokenForUser(userEntity);
                        success(getString("message.mailSent"));
                    } else {
                        error(getString("message.alreadyEnabled"));
                    }
                } catch (MailNotFoundException e) {
                    error(getString("message.mailNotFound"));
                }
            }
        };
        add(form);
        form.add(new CustomFeedbackPanel("feedback"));
        form.add(new EmailTextField("mail", model(from(form.getModel()).getMail())).setRequired(true));
        form.add(new Button("submitButton"));
        form.add(new BookmarkablePageLink("backlink", DashboardPage.class));
    }

    private void handleStatusCode(PageParameters pageParameters) {
        int result = pageParameters.get("valid").toInt();

        if (result == TokenStatus.INVALID.statusCode()) {
            error(getString("message.tokenInvalid"));
        } else if (result == TokenStatus.EXPIRED.statusCode()) {
            error(getString("message.tokenExpired"));
        }
    }
}