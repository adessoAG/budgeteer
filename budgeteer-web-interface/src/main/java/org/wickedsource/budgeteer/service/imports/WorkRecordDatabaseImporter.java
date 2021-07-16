package org.wickedsource.budgeteer.service.imports;

import lombok.Getter;
import lombok.Setter;
import org.joda.money.Money;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.wickedsource.budgeteer.MoneyUtil;
import org.wickedsource.budgeteer.imports.api.ImportedWorkRecord;
import org.wickedsource.budgeteer.persistence.budget.BudgetEntity;
import org.wickedsource.budgeteer.persistence.person.DailyRateEntity;
import org.wickedsource.budgeteer.persistence.person.DailyRateRepository;
import org.wickedsource.budgeteer.persistence.project.ProjectEntity;
import org.wickedsource.budgeteer.persistence.record.WorkRecordEntity;
import org.wickedsource.budgeteer.persistence.record.WorkRecordRepository;

import javax.annotation.PostConstruct;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

@Component
@Scope("prototype")
public class WorkRecordDatabaseImporter extends RecordDatabaseImporter {

    @Autowired
    private WorkRecordRepository workRecordRepository;

    @Autowired
    private DailyRateRepository rateRepository;

    @Getter @Setter
    private Date earliestRecordDate = new Date(Long.MAX_VALUE);

    @Getter @Setter
    private Date latestRecordDate = new Date(1l);

    private List<DailyRateEntity> dailyRates;

    public WorkRecordDatabaseImporter(long projectId, String importType) {
        super(projectId, importType);
    }

    private List<List<String>> skippedRecords = new LinkedList<List<String>>();
    private SimpleDateFormat format = new SimpleDateFormat();

    @PostConstruct
    public void init() {
        super.init();
        dailyRates = getDailyRateCache(getProjectId());
    }

    @Override
    public List<List<String>> getSkippedRecords() {
        return skippedRecords;
    }

    public void importRecords(List<ImportedWorkRecord> records) {
        List<WorkRecordEntity> entitiesToImport = new ArrayList<WorkRecordEntity>();
        for (ImportedWorkRecord record : records) {

            // trimming import keys
            record.setPersonName(record.getPersonName().trim());
            record.setBudgetName(record.getBudgetName().trim());
            String budgetName = record.getBudgetName();
            ProjectEntity project = getProject();

            WorkRecordEntity entity = new WorkRecordEntity();
            entity.setPerson(getPerson(record.getPersonName()));
            entity.setMinutes(record.getMinutesWorked());
            entity.setDate(record.getDate());
            entity.setDailyRate(getDailyRateForRecord(record));
            entity.setImportRecord(getImportRecord());
            entity.setEditedManually(false);

            if((project.getProjectEnd() != null && record.getDate().after(project.getProjectEnd())) ||
                    (project.getProjectStart() != null && record.getDate().before(project.getProjectStart()))) {

                skippedRecords.add(getRecordAsString(budgetName, entity));

            } else {
                BudgetEntity budget = getBudget(budgetName);
                entity.setBudget(budget);

                if (record.getDate().before(earliestRecordDate)) {
                    earliestRecordDate = record.getDate();
                }
                if (record.getDate().after(latestRecordDate)) {
                    latestRecordDate = record.getDate();
                }
                entitiesToImport.add(entity);
            }
        }
        if(!entitiesToImport.isEmpty()) {
            workRecordRepository.saveAll(entitiesToImport);
        }

        //If all records haven been skipped the startDate of the import is new Date(Long.MAX_VALUE) and the EndDate is null.
        // This causes problems in our application, so they have to be set to properly values...
        if(earliestRecordDate == null || earliestRecordDate.equals(new Date(Long.MAX_VALUE))){
            earliestRecordDate = new Date();
        }
        if(latestRecordDate == null){
            latestRecordDate = new Date();
        }
        getImportRecord().setStartDate(earliestRecordDate);
        getImportRecord().setEndDate(latestRecordDate);
        getImportRecord().setNumberOfImportedFiles(getImportRecord().getNumberOfImportedFiles() + 1);
    }

    /**
     * If there are any manually edited entries, there would be duplicate records in the database.
     * This methods seeks and deletes these entries. If there is both a manually edited and a newly imported entry, the imported one will be deleted.
     * If there are any manually edited entries without corresponding imported entries, the manually edited entry will be seen as obsolete and be deleted too.
     * @return a list of entries that were deleted during checking for duplicates.
     * See Issue #72
     */
    public List<List<String>> findAndRemoveManuallyEditedEntries() {
        List<List<String>> deletedRecordList = new LinkedList<List<String>>();
        deletedRecordList.add(new LinkedList<String>());
        LinkedList<String> headline = new LinkedList<String>(); headline.add("Miscellaneous");
        deletedRecordList.add(headline);
        //Find duplicate Records, where the dailyRate was edited manually
        List<WorkRecordEntity> manuallyEditedEntries = workRecordRepository.findManuallyEditedEntries(super.getProjectId(), getEarliestRecordDate(), getLatestRecordDate() );
        for(int i = manuallyEditedEntries.size() -1; i >= 0; i--) {
            WorkRecordEntity editedEntry = manuallyEditedEntries.get(i);
            List<WorkRecordEntity> duplicateEntryList = workRecordRepository.findDuplicateEntries(editedEntry.getBudget(), editedEntry.getPerson(), editedEntry.getDate(), editedEntry.getMinutes());
            if (!duplicateEntryList.isEmpty()) {
                deletedRecordList.add(getRecordAsString(duplicateEntryList.get(0), "There was a manually edited entry associated with this one already in the database"));
                workRecordRepository.delete(duplicateEntryList.get(0));
                //Update the import-reference of the the manually edited record so that it belongs to the current import
                editedEntry.setImportRecord(duplicateEntryList.get(0).getImportRecord());
                workRecordRepository.save(editedEntry);

                manuallyEditedEntries.remove(editedEntry);
            }
        }
        //if there are still any manually edited records in the list, there weren't any associated entries in the current import
        // -> remove all  left manually-edited-records in the database
        for(WorkRecordEntity leftRecord : manuallyEditedEntries){
            workRecordRepository.delete(leftRecord);
            deletedRecordList.add(getRecordAsString(leftRecord, "The record was edited manually in the application but didn't has a corresponding entry in the import -> deleted"));
        }
        //If just an empty row and the headline was added, return a empty list
        if(deletedRecordList.size() <= 2)
            return  new LinkedList<List<String>>();
        return deletedRecordList;
    }

    private List<String> getRecordAsString(String budgetName, WorkRecordEntity entity){
        return getRecordAsString(budgetName, entity, "Record is out of project-date-range");
    }

    private List<String> getRecordAsString(WorkRecordEntity entity, String reason) {
        String budgetName = entity.getBudget().getName();
        return getRecordAsString(budgetName, entity, reason);
    }

    private List<String> getRecordAsString(String budgetName, WorkRecordEntity entity, String reason) {
        LinkedList<String> result = new LinkedList<String>();
        result.add(entity.getPerson().getName());
        result.add(budgetName);
        result.add("" + entity.getMinutes());
        result.add(format.format(entity.getDate()));
        result.add(entity.getDailyRate().toString());
        result.add("");
        result.add(reason);
        return  result;
    }

    private Money getDailyRateForRecord(ImportedWorkRecord record) {
        for (DailyRateEntity rate : dailyRates) {
            // TODO: this is far from performant, should be reworked when large data proves to be an issue
            if (rate.getBudget().getImportKey().equals(record.getBudgetName()) &&
                    rate.getPerson().getImportKey().equals(record.getPersonName()) &&
                    !rate.getDateStart().after(record.getDate()) &&
                    !rate.getDateEnd().before(record.getDate())) {
                return rate.getRate();
            }
        }
        return MoneyUtil.createMoneyFromCents(0l);
    }


    private List<DailyRateEntity> getDailyRateCache(long projectId) {
        return rateRepository.findByProjectIdFetch(projectId);
    }

}
