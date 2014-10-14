package org.wickedsource.budgeteer.web.usecase.base.component.notification;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.wickedsource.budgeteer.service.notification.Notification;

public class NotificationDropdown extends Panel {

    public NotificationDropdown(String id, NotificationModel model) {
        super(id, model);
        add(createNotificationCountLabel("notificationCountLabel"));
        add(createDropDownHeader("dropdownHeader"));
        add(createDropdownMenu("dropdownMenu"));
    }

    @SuppressWarnings("unchecked")
    private NotificationModel getModel() {
        return (NotificationModel) getDefaultModel();
    }

    private Label createNotificationCountLabel(String wicketId) {
        Label label = new Label(wicketId, getModel().getNotificationCountModel()) {
            @Override
            public boolean isVisible() {
                return 0 != (Integer) getDefaultModelObject();
            }
        };
        return label;
    }

    private Label createDropDownHeader(String wicketId) {
        return new Label(wicketId, getModel().getHeaderModel());
    }

    private WebMarkupContainer createDropdownMenu(String wicketId) {
        final ListView<Notification> listview = new ListView<Notification>("notificationList", getModel()) {
            @Override
            protected void populateItem(ListItem<Notification> item) {
                Label messageLabel = new Label("notificationMessage", item.getModel().getObject().getMessage());
                item.add(messageLabel);
            }
        };

        WebMarkupContainer menu = new WebMarkupContainer(wicketId) {
            @Override
            public boolean isVisible() {
                return 0 != listview.getModelObject().size();
            }
        };
        menu.add(listview);

        return menu;
    }

}