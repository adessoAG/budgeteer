package de.adesso.budgeteer.web.pages.imports;

import de.adesso.budgeteer.service.imports.Import;
import de.adesso.budgeteer.service.imports.ImportService;
import de.adesso.budgeteer.web.BudgeteerSession;
import de.adesso.budgeteer.web.ClassAwareWrappingModel;
import de.adesso.budgeteer.web.Mount;
import de.adesso.budgeteer.web.pages.base.basepage.BasePage;
import de.adesso.budgeteer.web.pages.base.basepage.breadcrumbs.BreadcrumbsModel;
import de.adesso.budgeteer.web.pages.base.delete.DeleteDialog;
import de.adesso.budgeteer.web.pages.dashboard.DashboardPage;
import de.adesso.budgeteer.web.pages.imports.fileimport.ImportFilesPage;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.spring.injection.annot.SpringBean;

import java.util.List;

import static org.wicketstuff.lazymodel.LazyModel.from;
import static org.wicketstuff.lazymodel.LazyModel.model;
@Mount("imports")
public class ImportsOverviewPage extends BasePage {
    @SpringBean
    private ImportService importService;
    private WebMarkupContainer importListContainer;
    public ImportsOverviewPage() {
        importListContainer = new WebMarkupContainer("importListContainer");
        importListContainer.setOutputMarkupId(true);
        importListContainer.add(createImportsList("importsList", new ImportsModel(BudgeteerSession.get().getProjectId())));
        add(importListContainer);
        add(createImportLink("importLink"));
    }
    private Link createImportLink(String id) {
        final ImportFilesPage importPage = new ImportFilesPage(ImportsOverviewPage.class, getPageParameters());
        return new Link(id) {
            @Override
            public void onClick() {
                setResponsePage(importPage);
            }
        };
    }
    private ListView<Import> createImportsList(String id, IModel<List<Import>> model) {
        return new ListView<Import>(id, model) {
            @Override
            protected void populateItem(final ListItem<Import> item) {
                final Long impId = item.getModelObject().getId();
                item.add(new Label("importDate", model(from(item.getModel()).getImportDate())));
                item.add(new Label("importType", model(from(item.getModel()).getImportType())));
                item.add(new Label("numberOfFiles", model(from(item.getModel()).getNumberOfImportedFiles())));
                item.add(new Label("startDate", model(from(item.getModel()).getStartDate())));
                item.add(new Label("endDate", model(from(item.getModel()).getEndDate())));
                item.add(new AjaxLink("deleteButton") {
                    @Override
                    public void onClick(AjaxRequestTarget target) {
                        setResponsePage(new DeleteDialog() {
                            @Override
                            protected void onYes() {
                                importService.deleteImport(impId);
                                setResponsePage(ImportsOverviewPage.class);
                            }

                            @Override
                            protected void onNo() {
                                setResponsePage(ImportsOverviewPage.class);
                            }

                            @Override
                            protected String confirmationText() {
                                return ImportsOverviewPage.this.getString("delete.import.confirmation");
                            }
                        });
                    }
                });
            }
            @Override
            protected ListItem<Import> newItem(int index, IModel<Import> itemModel) {
                return super.newItem(index, new ClassAwareWrappingModel<>(itemModel, Import.class));
            }
        };
    }
    @Override
    protected BreadcrumbsModel getBreadcrumbsModel() {
        return new BreadcrumbsModel(DashboardPage.class, ImportsOverviewPage.class);
    }
}