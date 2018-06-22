package org.wickedsource.budgeteer.web.pages.person.details;

import org.apache.wicket.util.tester.WicketTester;
import org.junit.jupiter.api.Test;
import org.wickedsource.budgeteer.web.AbstractWebTestTemplate;

public class PersonDetailsPageTest extends AbstractWebTestTemplate {

    @Test
    void render() {
        WicketTester tester = getTester();
        tester.startPage(PersonDetailsPage.class, PersonDetailsPage.createParameters(1L));
        tester.assertRenderedPage(PersonDetailsPage.class);
    }

    @Override
    protected void setupTest() {

    }
}