package de.adesso.budgeteer.web.pages.base.basepage.notifications;

import de.adesso.budgeteer.service.notification.MissingDailyRateNotification;
import de.adesso.budgeteer.service.notification.Notification;
import de.adesso.budgeteer.service.notification.NotificationService;
import de.adesso.budgeteer.web.AbstractWebTestTemplate;
import de.adesso.budgeteer.web.PropertyLoader;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.when;

public class NotificationDropdownTest extends AbstractWebTestTemplate {

    private static final List<Notification> EMPTY_NOTIFICATION_LIST = new ArrayList<>(0);

    @Autowired
    private NotificationService service;

    @Test
    void testRenderFull() {
        WicketTester tester = getTester();
        when(service.getNotifications(1L, 1L)).thenReturn(createNotifications());
        NotificationModel model = new NotificationModel(1L, 1L);
        NotificationDropdown dropdown = new NotificationDropdown("dropdown", model);
        tester.startComponentInPage(dropdown);

        tester.assertContains(PropertyLoader.getProperty(NotificationDropdown.class, "header.whenFull"));
        tester.assertLabel("dropdown:notificationCountLabel", "2");
    }

    @Test
    void testRenderEmpty() {
        WicketTester tester = getTester();
        when(service.getNotifications(1L, 1L)).thenReturn(EMPTY_NOTIFICATION_LIST);
        NotificationModel model = new NotificationModel(1L, 1L);
        NotificationDropdown dropdown = new NotificationDropdown("dropdown", model);
        tester.startComponentInPage(dropdown);

        tester.assertContains(PropertyLoader.getProperty(NotificationDropdown.class, "header.whenEmpty"));
        tester.assertInvisible("dropdown:notificationCountLabel");
        tester.assertInvisible("dropdown:dropdownMenu");
    }

    private List<Notification> createNotifications() {
        List<Notification> notifications = new ArrayList<>();
        notifications.add(new MissingDailyRateNotification());
        notifications.add(new MissingDailyRateNotification());
        return notifications;
    }

    @Override
    protected void setupTest() {

    }
}
