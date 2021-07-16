package org.wickedsource.budgeteer.web.pages.monitoring;

import org.apache.wicket.markup.MarkupType;
import org.apache.wicket.markup.html.WebPage;
import org.wicketstuff.annotation.mount.MountPath;

/**
 * A dedicated page to display the current version of the application.
 *
 * <p>This page is primarily used for monitoring purposes. Currently we do not check any further resources, so that the
 * version is displayed if at least the Spring container is up and running.</p>
 */
@MountPath("/version")
public class VersionPage extends WebPage {

    /**
     * @return The plain text markup type.
     */
    @Override
    public MarkupType getMarkupType() {
        return new MarkupType("txt", "text/plain");
    }
}
