package org.wickedsource.budgeteer.web.pages.user.login;

import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.markup.html.form.RequiredTextField;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.wickedsource.budgeteer.service.user.InvalidLoginCredentialsException;
import org.wickedsource.budgeteer.service.user.TokenStatus;
import org.wickedsource.budgeteer.service.user.User;
import org.wickedsource.budgeteer.service.user.UserService;
import org.wickedsource.budgeteer.web.BudgeteerSession;
import org.wickedsource.budgeteer.web.BudgeteerSettings;
import org.wickedsource.budgeteer.web.Mount;
import org.wickedsource.budgeteer.web.components.customFeedback.CustomFeedbackPanel;
import org.wickedsource.budgeteer.web.pages.base.dialogpage.DialogPage;
import org.wickedsource.budgeteer.web.pages.user.forgotpassword.ForgotPasswordPage;
import org.wickedsource.budgeteer.web.pages.user.register.RegisterPage;
import org.wickedsource.budgeteer.web.pages.user.resettoken.ResetTokenPage;
import org.wickedsource.budgeteer.web.pages.user.selectproject.SelectProjectPage;

import static org.wicketstuff.lazymodel.LazyModel.from;
import static org.wicketstuff.lazymodel.LazyModel.model;

@Mount("/login")
public class LoginPage extends DialogPage {

    @SpringBean
    private UserService userService;

    @SpringBean
    private BudgeteerSettings settings;

    public LoginPage() {
        build();
    }

    public LoginPage(PageParameters pageParameters) {
        // The token needed to verify the mail address is taken from the parameters and handled.
        String verificationToken = pageParameters.get("verificationtoken").toString();
        String verificationSent = pageParameters.get("verificationsent").toString();

        if (verificationToken != null) {
            int result = userService.validateVerificationToken(verificationToken);

            if (result == TokenStatus.VALID.statusCode()) {
                success(getString("message.tokenValid"));
            } else if (result == TokenStatus.INVALID.statusCode()) {
                setResponsePage(ResetTokenPage.class, new PageParameters().add("valid", TokenStatus.INVALID.statusCode()));
            } else if (result == TokenStatus.EXPIRED.statusCode()) {
                setResponsePage(ResetTokenPage.class, new PageParameters().add("valid", TokenStatus.EXPIRED.statusCode()));
            }
        } else if (verificationSent.equals("true")) {
            success(getString("message.mailSent"));
        }

        build();
    }

    private void build() {
            Form<LoginCredentials> form = new Form<LoginCredentials>("loginForm", model(from(new LoginCredentials()))) {
                @Override
                protected void onSubmit() {
                    try {
                        User user = userService.login(getModelObject().getUsername(), getModelObject().getPassword());
                        BudgeteerSession.get().login(user);
                        SelectProjectPage nextPage = new SelectProjectPage(LoginPage.class, getPageParameters());
                        setResponsePage(nextPage);
                    } catch (InvalidLoginCredentialsException e) {
                        error(getString("message.invalidLogin"));
                    }
                }
            };
            add(form);

            form.add(new CustomFeedbackPanel("feedback"));
            form.add(new RequiredTextField<String>("username", model(from(form.getModel()).getUsername())));
            form.add(new PasswordTextField("password", model(from(form.getModel()).getPassword())));
            form.add(new BookmarkablePageLink<RegisterPage>("registerLink", RegisterPage.class));
            form.add(new BookmarkablePageLink<ForgotPasswordPage>("forgotPasswordLink", ForgotPasswordPage.class));
    }
}
