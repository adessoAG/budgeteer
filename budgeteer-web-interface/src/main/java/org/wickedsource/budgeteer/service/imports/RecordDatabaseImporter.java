package org.wickedsource.budgeteer.service.imports;

import org.springframework.beans.factory.annotation.Autowired;
import org.wickedsource.budgeteer.MoneyUtil;
import org.wickedsource.budgeteer.persistence.budget.BudgetEntity;
import org.wickedsource.budgeteer.persistence.budget.BudgetRepository;
import org.wickedsource.budgeteer.persistence.imports.ImportEntity;
import org.wickedsource.budgeteer.persistence.imports.ImportRepository;
import org.wickedsource.budgeteer.persistence.person.PersonEntity;
import org.wickedsource.budgeteer.persistence.person.PersonRepository;
import org.wickedsource.budgeteer.persistence.project.ProjectEntity;
import org.wickedsource.budgeteer.persistence.project.ProjectRepository;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class RecordDatabaseImporter {

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private BudgetRepository budgetRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private ImportRepository importRepository;

    private Map<String, PersonEntity> personsByImportKey;

    private Map<String, BudgetEntity> budgetsByImportKey;

    private ProjectEntity project;

    private long projectId;

    private String importType;

    private ImportEntity importRecord;

    public RecordDatabaseImporter(long projectId, String importType) {
        this.projectId = projectId;
        this.importType = importType;
    }

    public void init() {
        project = projectRepository.findById(projectId).orElse(null);
        budgetsByImportKey = getBudgetCache(projectId);
        personsByImportKey = getPersonCache(projectId);

        importRecord = new ImportEntity();
        importRecord.setProject(project);
        importRecord.setImportType(importType);
        importRecord.setImportDate(new Date());
        importRepository.save(importRecord);
    }

    protected ProjectEntity getProject() {
        return project;
    }

    protected long getProjectId() {
        return projectId;
    }

    protected String getImportType() {
        return importType;
    }

    protected ImportEntity getImportRecord() {
        return importRecord;
    }

    protected PersonEntity getPerson(String personName) {
        PersonEntity person = personsByImportKey.get(personName);
        if (person == null) {
            PersonEntity newPerson = new PersonEntity();
            newPerson.setImportKey(personName);
            newPerson.setProject(project);
            newPerson.setName(personName);
            personRepository.save(newPerson);
            personsByImportKey.put(newPerson.getImportKey(), newPerson);
            person = newPerson;
        }
        return person;
    }

    /**
     * Fetches the budget by its import key.
     *
     * As a side effect, creates any nonexistent budget and associates it with the current project.
     *
     * @param budgetName The technical budget name.
     * @return The budget database record.
     */
    protected BudgetEntity getBudget(String budgetName) {
        BudgetEntity budget = budgetsByImportKey.get(budgetName);
        if (budget == null) {
            BudgetEntity newBudget = new BudgetEntity();
            newBudget.setImportKey(budgetName);
            newBudget.setName(budgetName);
            newBudget.setProject(project);
            newBudget.setTotal(MoneyUtil.createMoneyFromCents(0L));
            budgetRepository.save(newBudget);
            budgetsByImportKey.put(newBudget.getImportKey(), newBudget);
            budget = newBudget;
        }
        return budget;
    }

    /**
     * Creates a map of persons import keys to the person entities to be used as a cache. This cache can be used to prevent
     * repeated access to the database.
     */
    private Map<String, PersonEntity> getPersonCache(long projectId) {
        Map<String, PersonEntity> map = new HashMap<String, PersonEntity>();
        List<PersonEntity> persons = personRepository.findByProjectIdOrderByNameAsc(projectId);
        for (PersonEntity person : persons) {
            map.put(person.getImportKey(), person);
        }
        return map;
    }

    /**
     * Creates a map of budgets import keys to the budget entities to be used as a cache. This cache can be used to prevent
     * repeated access to the database.
     */
    private Map<String, BudgetEntity> getBudgetCache(long projectId) {
        Map<String, BudgetEntity> map = new HashMap<String, BudgetEntity>();
        List<BudgetEntity> budgets = budgetRepository.findByProjectIdOrderByNameAsc(projectId);
        for (BudgetEntity budget : budgets) {
            map.put(budget.getImportKey(), budget);
        }
        return map;
    }

    /**
     * Returns a List of Entries that were skipped during the import process according to internal rules
     * @return List of List with the skipped values converted to strings
     */
    public abstract List<List<String>> getSkippedRecords();
}
