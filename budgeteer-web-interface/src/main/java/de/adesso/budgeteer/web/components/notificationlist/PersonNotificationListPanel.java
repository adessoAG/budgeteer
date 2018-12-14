package de.adesso.budgeteer.web.components.notificationlist;

import de.adesso.budgeteer.service.notification.Notification;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.event.Broadcast;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

import java.util.List;

public class PersonNotificationListPanel extends Panel {

    private NotificationMessageFactory notificationMessageFactory = new NotificationMessageFactory();

    public PersonNotificationListPanel(String id, IModel<? extends List<Notification>> model) {
        super(id, model);
        setRenderBodyOnly(true);
        add(createNotificationList("notificationList", model));
    }

    private ListView<Notification> createNotificationList(String id, IModel<? extends List<Notification>> model) {
        return new ListView<Notification>(id, model) {
            @Override
            protected void populateItem(ListItem<Notification> item) {
                item.add(new Label("text", notificationMessageFactory.getMessageForNotification(item.getModelObject())));
                item.add(new AjaxLink("addMissingRate") {
                    @Override
                    public void onClick(AjaxRequestTarget target) {
                        send(getPage(), Broadcast.BREADTH, item.getModelObject());
                    }
                });
            }
        };
    }
}
