package org.wickedsource.budgeteer.web.pages.person.edit;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.string.StringValue;
import org.wickedsource.budgeteer.web.pages.base.dialogpage.DialogPageWithBacklink;
import org.wickedsource.budgeteer.web.pages.person.edit.personrateform.EditPersonForm;
import org.wickedsource.budgeteer.web.pages.person.overview.PeopleOverviewPage;
import org.wicketstuff.annotation.mount.MountPath;

@MountPath("people/edit/${id}")
public class EditPersonPage extends DialogPageWithBacklink {

    private IEditPersonPageStrategy strategy;

    /**
     * Use this constructor to create a page with a form to edit an existing person.
     *
     * @param parameters page parameters containing the id of the person to edit.
     */
    public EditPersonPage(PageParameters parameters, Class<? extends WebPage> backlinkPage, PageParameters backlinkParameters) {
        super(parameters, backlinkPage, backlinkParameters);
        if(getPersonId() == 0L){
            setResponsePage(backlinkPage, backlinkParameters);
            return;
        }
        strategy = new UpdateStrategy(this);
        addComponents();
    }

    /**
     * This constructor is used when you click on a link or try to access the EditPersonPage manually
     * (e.g. when you type the path "/people/edit" in the search bar)
     * @param parameters
     */
    public EditPersonPage(PageParameters parameters) {
        this(createParameters(0), PeopleOverviewPage.class, new PageParameters());
    }

    private void addComponents() {
        EditPersonForm form = strategy.createForm("form", getPersonId());
        add(form);
        add(strategy.createPageTitleLabel("pageTitle"));
        form.add(strategy.createNotificationList("notificationList", getPersonId()));
        add(createBacklink("backlink1"));
        form.add(createBacklink("backlink2"));
    }

    /**
     * Creates a valid PageParameters object to pass into the constructor of this page class.
     *
     * @param personId id of the person whose details to display.
     * @return a valid PageParameters object.
     */
    public static PageParameters createParameters(long personId) {
        PageParameters parameters = new PageParameters();
        parameters.add("id", personId);
        return parameters;
    }

    private long getPersonId() {
        StringValue value = getPageParameters().get("id");
        if (value == null || value.isEmpty() || value.isNull()) {
            return 0L;
        } else {
            return value.toLong();
        }
    }
}
