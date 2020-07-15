package org.wickedsource.budgeteer.web.pages.templates.table;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.event.IEvent;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.wickedsource.budgeteer.service.template.Template;
import org.wickedsource.budgeteer.service.template.TemplateService;
import org.wickedsource.budgeteer.web.BudgeteerSession;
import org.wickedsource.budgeteer.web.ClassAwareWrappingModel;
import org.wickedsource.budgeteer.web.components.dataTable.DataTableBehavior;
import org.wickedsource.budgeteer.web.pages.templates.TemplateFilter;
import org.wickedsource.budgeteer.web.pages.templates.TemplatesPage;
import org.wickedsource.budgeteer.web.pages.templates.edit.EditTemplatePage;
import org.wickedsource.budgeteer.web.pages.templates.templateimport.TemplateFormInputDto;

import java.util.List;

import static org.wicketstuff.lazymodel.LazyModel.from;
import static org.wicketstuff.lazymodel.LazyModel.model;

public class TemplatesTable extends Panel {

    private ListView<Template> rows;
    private WebMarkupContainer table;

    @SpringBean
    private TemplateService templateService;

    public TemplatesTable(String id, TemplateListModel model) {
        super(id, model);
        this.setDefaultModel(model);
        table = new WebMarkupContainer("table");
        table.add(new DataTableBehavior(DataTableBehavior.getRecommendedOptions()));
        rows = createList("templateList", model, table);
        table.add(rows);
        add(table);
        this.setOutputMarkupId(true);
    }

    private ListView<Template> createList(String id, final IModel<List<Template>> model, final WebMarkupContainer table) {
        return new ListView<Template>(id, model) {
            @Override
            protected void populateItem(final ListItem<Template> item) {
                item.setOutputMarkupId(true);
                item.add(new Label("name", model(from(item.getModel()).getName())));
                item.add(new Label("description", model(from(item.getModel()).getDescription())));
                item.add(new Label("type", model(from(item.getModel()).getType()).getObject().toString()));
                item.add(createCheckbox(item));
                item.add(new Link<Void>("editPage") {
                    @Override
                    public void onClick() {
                        WebPage page = new EditTemplatePage(TemplatesPage.class, getPage().getPageParameters(), TemplatesPage.createParameters(item.getModelObject().getId()));
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

    private AjaxLink createCheckbox(ListItem<Template> item){

        final AttributeModifier starChecked = new AttributeModifier("class", "btn btn-default btn-sm glyphicon glyphicon-star");
        final AttributeModifier starUnchecked = new AttributeModifier("class", "btn btn-default btn-sm glyphicon glyphicon-star-empty");

        Label checkBoxLabel = new Label("checkBoxLabel");
        if(item.getModelObject().isDefault()){
            checkBoxLabel.add(starChecked);
        }else{
            checkBoxLabel.add(starUnchecked);
        }

        //Unfortunately I came across quite a few issues with checkboxes, so
        //in the end they are simulated with Links + Labels (which in turn have their own problems,
        //like not being sortable in the table
        AjaxLink checkBox = new AjaxLink<Void>("setAsDefault"){

            @Override
            public void onClick(AjaxRequestTarget target){
                TemplateFormInputDto temp = new TemplateFormInputDto(BudgeteerSession.get().getProjectId());
                temp.setName(item.getModelObject().getName());
                temp.setDescription(item.getModelObject().getDescription());
                temp.setType(item.getModelObject().getType());

                item.getModelObject().setDefault(!item.getModelObject().isDefault());
                temp.setDefault(item.getModelObject().isDefault());

                templateService.editTemplate(temp.getProjectId(), item.getModelObject().getId(), null, model(from(temp)));
                List<Template> tempModel = ((TemplateListModel)TemplatesTable.this.getDefaultModel()).load();
                for(int i = 0; i < rows.getList().size(); i++){
                    Label check = (Label)table.get("templateList")
                            .get(Integer.toString(i)).get("setAsDefault").get("checkBoxLabel");
                    if(tempModel.get(i).isDefault()){
                        check.add(starChecked);
                    }else{
                        check.add(starUnchecked);
                    }
                    target.add(check, check.getMarkupId());
                }
            }
        };
        checkBoxLabel.setOutputMarkupId(true);
        checkBox.add(checkBoxLabel);
        return checkBox;
    }

    @Override
    public void onEvent(IEvent<?> event) {
        super.onEvent(event);
        Object payload = event.getPayload();
            if (payload instanceof TemplateFilter) {
            TemplateFilter filter = (TemplateFilter) payload;
            TemplateListModel model = (TemplateListModel) getDefaultModel();
            model.setFilter(filter);
            RequestCycle.get().find(AjaxRequestTarget.class).ifPresent(target -> target.add(this, this.getMarkupId()));
        }
    }
}
