package org.wickedsource.budgeteer.web.pages.templates.table;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.wickedsource.budgeteer.service.template.Template;
import org.wickedsource.budgeteer.web.ClassAwareWrappingModel;
import org.wickedsource.budgeteer.web.components.dataTable.DataTableBehavior;
import org.wickedsource.budgeteer.web.pages.templates.TemplatesPage;
import org.wickedsource.budgeteer.web.pages.templates.edit.EditTemplatePage;

import java.util.List;

import static org.wicketstuff.lazymodel.LazyModel.from;
import static org.wicketstuff.lazymodel.LazyModel.model;

public class TemplatesTable extends Panel {

    private ListView<Template> rows;

    public TemplatesTable(String id, TemplateListModel model) {
        super(id, model);

        WebMarkupContainer table = new WebMarkupContainer("table");
        table.add(new DataTableBehavior(DataTableBehavior.getRecommendedOptions()));
        rows = createList("templateList", model, table);

        table.add(rows);
        add(table);
    }

    private ListView<Template> createList(String id, final IModel<List<Template>> model, final WebMarkupContainer table) {
        return new ListView<Template>(id, model) {
            @Override
            protected void populateItem(final ListItem<Template> item) {
                item.setOutputMarkupId(true);
                item.add(new Label("name", model(from(item.getModel()).getName())));
                item.add(new Label("description", model(from(item.getModel()).getDescription())));
                item.add(new Link("editPage") {
                    @Override
                    public void onClick() {
                        WebPage page = new EditTemplatePage(TemplatesPage.class, getPage().getPageParameters(), item.getModelObject().getId());
                        setResponsePage(page);
                    }
                });}

            @Override
            protected ListItem<Template> newItem(int index, IModel<Template> itemModel) {
                // wrap model to work with LazyModel
                return super.newItem(index, new ClassAwareWrappingModel<>(itemModel, Template.class));
            }
        };
    }

    public ListView<Template> getRows() {
        return rows;
    }
}
