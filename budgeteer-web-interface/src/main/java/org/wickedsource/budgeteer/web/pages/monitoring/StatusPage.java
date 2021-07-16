package org.wickedsource.budgeteer.web.pages.monitoring;

import org.apache.wicket.markup.MarkupType;
import org.apache.wicket.markup.html.WebPage;
import org.wicketstuff.annotation.mount.MountPath;

/**
 * A dedicated page to display the current status of the application.
 *
 * <p>This page is solely used for monitoring purposes. Currently we do not check any further resources, so that the
 * status is “operable” whenever the Spring container is up and running.</p>
 */
@MountPath("/status")
public class StatusPage extends WebPage {

    /**
     * @return The plain text markup type.
     */
    @Override
    public MarkupType getMarkupType() {
        return new MarkupType("txt", "text/plain");
    }
}
