package org.wickedsource.budgeteer.web.pages.base.basepage.notifications;

import org.apache.wicket.injection.Injector;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.wickedsource.budgeteer.service.notification.Notification;
import org.wickedsource.budgeteer.service.notification.NotificationService;
import org.wickedsource.budgeteer.web.PropertyLoader;

import java.util.List;

public class NotificationModel extends LoadableDetachableModel<List<Notification>> {

    @SpringBean
    private NotificationService service;

    private long projectId;

    private long userId;

    public NotificationModel(long projectId, long userId) {
        Injector.get().inject(this);
        this.projectId = projectId;
        this.userId = userId;
    }

    @Override
    protected List<Notification> load() {
        return service.getNotifications(projectId, userId);
    }

    public IModel<Integer> getNotificationCountModel() {
        return new LoadableDetachableModel<Integer>() {
            @Override
            protected Integer load() {
                return NotificationModel.this.getObject().size();
            }
        };
    }

    public IModel<String> getHeaderModel() {
        return new AbstractReadOnlyModel<String>() {

            @Override
            public String getObject() {
                List<Notification> notificationList = NotificationModel.this.getObject();
                if (notificationList.isEmpty()) {
                    return PropertyLoader.getProperty(NotificationDropdown.class, "header.whenEmpty");
                } else {
                    return PropertyLoader.getProperty(NotificationDropdown.class, "header.whenFull");
                }
            }

            @Override
            public void detach() {
            }
        };
    }

}
