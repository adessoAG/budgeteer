package org.wickedsource.budgeteer.web.pages.user.register;

import org.apache.wicket.injection.Injector;
import org.apache.wicket.markup.html.form.EmailTextField;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.markup.html.form.RequiredTextField;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.wickedsource.budgeteer.service.user.MailAlreadyInUseException;
import org.wickedsource.budgeteer.service.user.UserService;
import org.wickedsource.budgeteer.service.user.UsernameAlreadyInUseException;
import org.wickedsource.budgeteer.web.components.customFeedback.CustomFeedbackPanel;
import org.wickedsource.budgeteer.web.pages.base.dialogpage.DialogPage;
import org.wickedsource.budgeteer.web.pages.user.login.LoginPage;
import org.wicketstuff.annotation.mount.MountPath;

import static org.wicketstuff.lazymodel.LazyModel.from;
import static org.wicketstuff.lazymodel.LazyModel.model;

@MountPath("/register")
public class RegisterPage extends DialogPage {

    @SpringBean
    private UserService service;

    public RegisterPage() {
        Injector.get().inject(this);
        Form<RegistrationData> form = new Form<RegistrationData>("registrationForm", model(from(new RegistrationData()))) {
            @Override
            protected void onSubmit() {
                if (!getModelObject().getPassword().equals(getModelObject().getPasswordConfirmation())) {
                    error(getString("message.wrongPasswordConfirmation"));
                    return;
                }
                try {
                    service.registerUser(getModelObject().getUsername(), getModelObject().getMail(), getModelObject().getPassword());
                    setResponsePage(LoginPage.class, new PageParameters().add("verificationsent", "true"));
                } catch (UsernameAlreadyInUseException e) {
                    this.error(getString("message.duplicateUserName"));
                } catch (MailAlreadyInUseException e) {
                    this.error(getString("message.duplicateMail"));
                }
            }
        };
        add(form);
        form.add(new CustomFeedbackPanel("feedback"));
        form.add(new RequiredTextField<String>("username", model(from(form.getModel()).getUsername())));
        form.add(new EmailTextField("mail", model(from(form.getModel()).getMail())).setRequired(true));
        form.add(new PasswordTextField("password", model(from(form.getModel()).getPassword())));
        form.add(new PasswordTextField("passwordConfirmation", model(from(form.getModel()).getPasswordConfirmation())));
        form.add(new BookmarkablePageLink<LoginPage>("loginLink", LoginPage.class));
    }
}
