package org.wickedsource.budgeteer.web;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.util.tester.TagTester;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.Assert;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:spring-web.xml", "classpath:spring-service-mock.xml"})
public abstract class AbstractWebTestTemplate {

    @Autowired
    BudgeteerApplication application;

    private static WicketTester tester;

    protected WicketTester getTester() {
        if (tester == null) {
            tester = new WicketTester(application);
        }
        return tester;
    }

    protected void assertLink(String id, Class<? extends WebPage> pageClass) {
        TagTester tagTester = getTester().getTagById(id);
        String mountUrl = pageClass.getAnnotation(Mount.class).value();
        String href = tagTester.getAttribute("href").replaceFirst("\\./", "");
        Assert.assertEquals(mountUrl, href);
    }

}
