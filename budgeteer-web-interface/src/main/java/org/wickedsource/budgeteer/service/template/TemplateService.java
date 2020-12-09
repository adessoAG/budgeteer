package org.wickedsource.budgeteer.service.template;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.wicket.model.IModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.wickedsource.budgeteer.SheetTemplate.SheetTemplate;
import org.wickedsource.budgeteer.imports.api.ExampleFile;
import org.wickedsource.budgeteer.imports.api.ImportFile;
import org.wickedsource.budgeteer.persistence.template.TemplateEntity;
import org.wickedsource.budgeteer.persistence.template.TemplateRepository;
import org.wickedsource.budgeteer.service.ReportType;
import org.wickedsource.budgeteer.service.budget.report.BudgetReportData;
import org.wickedsource.budgeteer.service.budget.report.BudgetSummary;
import org.wickedsource.budgeteer.service.contract.report.ContractReportData;
import org.wickedsource.budgeteer.service.contract.report.ContractReportSummary;
import org.wickedsource.budgeteer.web.pages.templates.TemplateFilter;
import org.wickedsource.budgeteer.web.pages.templates.templateimport.TemplateFormInputDto;

import javax.transaction.Transactional;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
@Transactional
public class TemplateService {

    @Autowired
    private TemplateRepository templateRepository;

    private static final String[] allowedFileExtensions = {".xlsx", ".xlst", ".xslm", ".xltm"};

    /**
     *
     * @return All the templates from the repository.
     */
    public List<Template> getTemplates(){
        List<Template> result = new ArrayList<>();
        for(TemplateEntity E : templateRepository.findAll()){
            result.add(new Template(E.getId(), E.getName(), E.getDescription(), E.getType(), E.getWb(), E.isDefault(), E.getProjectId()));
        }
        return result;
    }

    /**
     *
     * @return True if template name is already taken. False if name is free.
     */
    public boolean isTemplateNameAlreadyTaken(long projectID, String templateName){
        return getTemplatesInProject(projectID)
                .stream()
                .anyMatch(templateEntity -> templateEntity.getName().equalsIgnoreCase(templateName));
    }

    /**
     *
     * @return True if template name is already taken. False if name is free.
     */
    public boolean isTemplateNameAlreadyTaken(long projectID, String templateName, TemplateFormInputDto templateForm){
        return getTemplatesInProject(projectID)
                .stream()
                .filter(templateInProject -> templateInProject.getName().equals(templateForm.getName())
                        && templateInProject.getDescription().equals(templateForm.getDescription()))
                .anyMatch(templateEntity -> templateEntity.getName().equalsIgnoreCase(templateName));
    }

    /**
     * @param projectID The ID of the current project.
     * @return All the templates in the current project.
     */
    public List<Template> getTemplatesInProject(long projectID){
        List<Template> result = new ArrayList<>();
        for(TemplateEntity E : templateRepository.findByProjectId(projectID)){
            result.add(new Template(E.getId(), E.getName(), E.getDescription(), E.getType() , E.getWb(), E.isDefault(), E.getProjectId()));
        }
        return result;
    }

    public Template getDefault(ReportType type, long projectID){
        for(Template E : getTemplatesInProject(projectID)){
            if(E.getType() == type){
                if(E.isDefault()){
                    return E;
                }
            }
        }
        return null;
    }

    /**
     * @param filter The Filter to use.
     * @return All the templates in the current project.
     */
    public List<Template> getFilteredTemplatesInProject(@NotNull TemplateFilter filter){
        List<Template> result = new ArrayList<>();
        for(TemplateEntity e : templateRepository.findByProjectId(filter.getProjectId())){
            for(ReportType type : filter.getTypesList()){
                if(type == e.getType()){
                    result.add(new Template(e.getId(), e.getName(), e.getDescription(), e.getType(), e.getWb(), e.isDefault(), e.getProjectId()));
                }
            }
        }
        return result;
    }

    /**
     * Returns a template from the database given it's ID.
     * @param templateID The ID of the template.
     * @return A new Template object.
     */
    public Template getById(long templateID){
        TemplateEntity e = templateRepository.findOne(templateID);
        if(e == null){
            return null;
        } else {
            return new Template(e.getId(), e.getName(), e.getDescription(), e.getType(), e.getWb(), e.isDefault(), e.getProjectId());
        }
    }

    /**
     * Delete a template given it's id.
     * @param templateID The ID of the template.
     */
    public void deleteTemplate(long templateID){
        templateRepository.delete(templateID);
    }

    public void resolveDefaults(long templateId, IModel<TemplateFormInputDto> temModel){
        if(temModel.getObject().isDefault()){
            for(TemplateEntity E : templateRepository.findAll()){
                if(E.getType() == temModel.getObject().getType() && E.getId() != templateId){
                    if(E.isDefault()){
                        templateRepository.save(new TemplateEntity(E.getId(), E.getName(), E.getDescription(), E.getType(),
                                E.getWb(), false, E.getProjectId()));
                    }
                }
            }
        }
    }

    /**
     * @param projectId  The id of the current project
     * @param templateId The id of the template to edit
     * @param workbook The file if containing the Workbook (can be null if we do not want to reupload)
     * @param temModel   The model for the template that is being edited
     */
    public void editTemplate(long projectId, long templateId, XSSFWorkbook workbook, IModel<TemplateFormInputDto> temModel) {
        resolveDefaults(templateId, temModel);
        TemplateEntity temp = new TemplateEntity(templateId, temModel.getObject().getName(),
                        temModel.getObject().getDescription(),
                        temModel.getObject().getType(),
                        workbook != null ? workbook : getById(templateId).getWb(),
                        temModel.getObject().isDefault(),
                        projectId);
        templateRepository.save(temp);
    }

    /**
     * Imports a template into the repository
     *
     * @param projectId  The id of the current project
     * @param uploadedWorkbook The workbook to import
     * @param temModel   The data model (name, description, id).
     */
    public void doImport(long projectId, XSSFWorkbook uploadedWorkbook, IModel<TemplateFormInputDto> temModel) {
            TemplateEntity temp = new TemplateEntity(temModel.getObject().getName(),
                    temModel.getObject().getDescription(),
                    temModel.getObject().getType(),
                    uploadedWorkbook,
                    temModel.getObject().isDefault(),
                    projectId);
            resolveDefaults(temp.getId(), temModel);
            templateRepository.save(temp);
    }

    /**
     * Reads an example template file from disk.
     * The file must be named like in the following format:
     * type-report-template.xlsx
     * <p>
     * where type is any of the available template types.
     *
     * @return An example file that shows how a template could look
     */
    public ExampleFile getExampleFile(ReportType type) {
        ExampleFile file = new ExampleFile();
        file.setFileName("Example_" + type.toString().toLowerCase() +"_template.xlsx");
        file.setInputStream(getClass().getResourceAsStream("/" + type.toString().toLowerCase() + "-report-template.xlsx"));
        return file;
    }

    public boolean validateUploadedTemplateFile(XSSFWorkbook uploadedWorkbook, ReportType type) {
        if (type == ReportType.BUDGET_REPORT) {
            return validateSheetTemplateCreationWithClasses(uploadedWorkbook, Arrays.asList(BudgetSummary.class, BudgetReportData.class));
        } else if (type == ReportType.CONTRACT_REPORT) {
            return validateSheetTemplateCreationWithClasses(uploadedWorkbook, Arrays.asList(ContractReportSummary.class, ContractReportData.class));
        }

        return false;
    }

    public boolean isFileExtensionAllowed(ImportFile file) {
        return Arrays.stream(allowedFileExtensions)
                .anyMatch(s -> file.getFilename().endsWith(s));
    }

    private boolean validateSheetTemplateCreationWithClasses(XSSFWorkbook workbook, List<Class<?>> classesToVerfiy) {
        if (workbook.getNumberOfSheets() < 2) {
            return false;
        }
        List<Sheet> sheetsToVerify = Arrays.asList(workbook.getSheetAt(0),
                workbook.getSheetAt(1));

        try {
            for (Sheet sheet : sheetsToVerify) {
                for (Class<?> clazz : classesToVerfiy) {
                    new SheetTemplate(clazz, sheet);
                }
            }
        } catch (Exception e) {
            return false;
        }

        return true;
    }
}
