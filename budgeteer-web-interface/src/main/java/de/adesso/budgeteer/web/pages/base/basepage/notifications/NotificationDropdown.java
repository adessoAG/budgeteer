package de.adesso.budgeteer.web.pages.base.basepage.notifications;

import de.adesso.budgeteer.service.notification.Notification;
import de.adesso.budgeteer.web.components.notificationlist.NotificationLinkFactory;
import de.adesso.budgeteer.web.components.notificationlist.NotificationMessageFactory;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;

public class NotificationDropdown extends Panel {

    private NotificationLinkFactory notificationLinkFactory = new NotificationLinkFactory();

    private NotificationMessageFactory notificationMessageFactory = new NotificationMessageFactory();

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
            protected void populateItem(final ListItem<Notification> item) {
                Link link = new Link("notificationLink") {
                    @SuppressWarnings("unchecked")
                    @Override
                    public void onClick() {
                        setResponsePage(notificationLinkFactory.getLinkForNotification(item.getModelObject(), (Class<? extends WebPage>) getPage().getClass(), getPage().getPageParameters()));
                    }
                };
                item.add(link);

                Label iconLabel = new Label("iconLabel", "");

                switch (notificationMessageFactory.getNotificationTypeForNotification(item.getModelObject()).toString()) {
                    case "warning":
                        iconLabel.add(new AttributeModifier("class", "fa fa-warning warning"));
                        break;
                    case "info":
                        iconLabel.add(new AttributeModifier("class", "fa fa-info info"));
                        break;
                    default:
                        iconLabel.add(new AttributeModifier("class", "fa fa-warning warning"));
                        break;
                }

                link.add(iconLabel);
                Label messageLabel = new Label("notificationMessage", notificationMessageFactory.getMessageForNotification(item.getModelObject()));
                link.add(messageLabel);
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
