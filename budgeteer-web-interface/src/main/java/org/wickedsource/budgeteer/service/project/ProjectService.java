package org.wickedsource.budgeteer.service.project;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.wickedsource.budgeteer.persistence.budget.BudgetRepository;
import org.wickedsource.budgeteer.persistence.contract.ContractRepository;
import org.wickedsource.budgeteer.persistence.imports.ImportRepository;
import org.wickedsource.budgeteer.persistence.invoice.InvoiceRepository;
import org.wickedsource.budgeteer.persistence.person.DailyRateRepository;
import org.wickedsource.budgeteer.persistence.person.PersonRepository;
import org.wickedsource.budgeteer.persistence.project.ProjectEntity;
import org.wickedsource.budgeteer.persistence.project.ProjectRepository;
import org.wickedsource.budgeteer.persistence.record.PlanRecordRepository;
import org.wickedsource.budgeteer.persistence.record.WorkRecordRepository;
import org.wickedsource.budgeteer.persistence.user.UserEntity;
import org.wickedsource.budgeteer.persistence.user.UserRepository;
import org.wickedsource.budgeteer.service.DateRange;
import org.wickedsource.budgeteer.web.pages.administration.Project;

import javax.transaction.Transactional;
import java.util.List;

@Service
@Transactional
public class ProjectService {

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BudgetRepository budgetRepository;

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private ImportRepository importRepository;

    @Autowired
    private PlanRecordRepository planRecordRepository;

    @Autowired
    private WorkRecordRepository workRecordRepository;

    @Autowired
    private ProjectBaseDataMapper mapper;

    @Autowired
    private DailyRateRepository dailyRateRepository;

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private ContractRepository contractRepository;

    /**
     * Creates a new empty project with the given name.
     *
     * @param projectName name of the project.
     * @return the base data of the newly create project.
     */
    public ProjectBaseData createProject(String projectName, long initialUserId) throws ProjectNameAlreadyInUseException {
        UserEntity user = userRepository.findById(initialUserId).orElseThrow(RuntimeException::new);
        ProjectEntity project = new ProjectEntity();
        for(ProjectEntity e : projectRepository.findAll()){
            if(e.getName().equals(projectName)){
                throw new ProjectNameAlreadyInUseException();
            }
        }
        project.setName(projectName);
        project.getAuthorizedUsers().add(user);
        ProjectEntity savedProject = projectRepository.save(project);
        user.getAuthorizedProjects().add(savedProject);
        return mapper.map(savedProject);
    }

    /**
     * Returns all projects the given user has access to.
     *
     * @param userId ID of the user
     * @return list of all projects the user has access to.
     */
    public List<ProjectBaseData> getProjectsForUser(long userId) {
        UserEntity user = userRepository.findById(userId).orElseThrow(RuntimeException::new);
        return mapper.map(user.getAuthorizedProjects());
    }

    /**
     * Returns the default project for a given user
     * @param userId ID of the user
     * @return the default project respectively null if no project is set as default
     */
    public ProjectBaseData getDefaultProjectForUser(long userId){
        UserEntity user = userRepository.findById(userId).orElseThrow(RuntimeException::new);
        ProjectEntity defaultProject = user.getDefaultProject();
        if(defaultProject == null){
            return null;
        }
        return mapper.map(defaultProject);
    }

    /**
     * Deletes the given project and all its data from the database.
     *
     * @param projectId ID of the project to delete.
     */
    @PreAuthorize("canReadProject(#projectId)")
    public void deleteProject(long projectId) {
        dailyRateRepository.deleteByProjectId(projectId);
        planRecordRepository.deleteByImportAndProjectId(projectId);
        workRecordRepository.deleteByImportAndProjectId(projectId);
        importRepository.deleteByProjectId(projectId);
        budgetRepository.deleteByProjectId(projectId);
        personRepository.deleteByProjectId(projectId);
        invoiceRepository.deleteInvoiceFieldByProjectId(projectId);
        invoiceRepository.deleteContractInvoiceFieldByProject(projectId);
        invoiceRepository.deleteByProjectId(projectId);
        contractRepository.deleteContractFieldByProjectId(projectId);
        contractRepository.deleteByProjectId(projectId);
        ProjectEntity projectEntity = projectRepository.findById(projectId).orElse(null);
        if(projectEntity != null) {
            List<UserEntity> userList = projectEntity.getAuthorizedUsers();
            if (userList != null) {
                for (UserEntity u : userList) {
                    if (u.getDefaultProject() != null && u.getDefaultProject().getId() == projectId) {
                        u.setDefaultProject(null);
                        userRepository.save(u);
                    }
                }
            }
        }
        projectRepository.deleteById(projectId);
    }

    /**
     * Sets the given project as the default project for the given user
     * @param userId ID of the user for that the default project should be set
     * @param projectId ID of the project that should become the default-project
     */
    public void setDefaultProject(long userId, long projectId){
        ProjectEntity project = projectRepository.findById(projectId).orElseThrow(RuntimeException::new);
        UserEntity user = userRepository.findById(userId).orElseThrow(RuntimeException::new);
        user.setDefaultProject(project);
        userRepository.save(user);
    }

    public Project findProjectById(long projectId){
        ProjectEntity entity = projectRepository.findById(projectId).orElseThrow(RuntimeException::new);
        return new Project(entity.getId(), entity.getProjectStart(), entity.getProjectEnd(), entity.getName());
    }

    public void save(Project project) {
        ProjectEntity projectEntity = projectRepository.findById(project.getProjectId()).orElseThrow(RuntimeException::new);
        projectEntity.setName(project.getName());
        DateRange dateRange = project.getDateRange();
        projectEntity.setProjectStart(dateRange == null ? null : dateRange.getStartDate());
        projectEntity.setProjectEnd(dateRange == null ? null : dateRange.getEndDate());
        projectRepository.save(projectEntity);
    }


}
