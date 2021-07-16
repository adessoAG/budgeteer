package org.wickedsource.budgeteer.service.person;

import org.hibernate.Hibernate;
import org.joda.money.CurrencyUnit;
import org.joda.money.Money;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.wickedsource.budgeteer.persistence.budget.BudgetRepository;
import org.wickedsource.budgeteer.persistence.person.DailyRateEntity;
import org.wickedsource.budgeteer.persistence.person.PersonEntity;
import org.wickedsource.budgeteer.persistence.person.PersonRepository;
import org.wickedsource.budgeteer.persistence.record.MissingDailyRateForBudgetBean;
import org.wickedsource.budgeteer.persistence.record.WorkRecordEntity;
import org.wickedsource.budgeteer.persistence.record.WorkRecordRepository;
import org.wickedsource.budgeteer.service.DateRange;
import org.wickedsource.budgeteer.service.DateUtil;
import org.wickedsource.budgeteer.service.budget.BudgetBaseData;
import org.wickedsource.budgeteer.web.planning.Person;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;


@Service
@Transactional
public class PersonService {

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private PersonBaseDataMapper personBaseDataMapper;

    @Autowired
    private PersonDetailDataMapper personDetailDataMapper;

    @Autowired
    private BudgetRepository budgetRepository;

    @Autowired
    private WorkRecordRepository workRecordRepository;

    /**
     * Returns all people the given user can make use of to manage budgets.
     *
     * @param projectId ID of the project.
     * @return list of PersonBaseData objects for all people the given user can make use of to manage budgets.
     */
    @PreAuthorize("canReadProject(#projectId)")
    public List<PersonBaseData> loadPeopleBaseData(long projectId) {
        return personBaseDataMapper.map(personRepository.findBaseDataByProjectId(projectId));
    }

    /**
     * Returns detailed data about the person with the given id.
     *
     * @param personId id of the person whose data to load.
     * @return the detailed data of the specified person.
     */
    @PreAuthorize("canReadPerson(#personId)")
    public PersonDetailData loadPersonDetailData(long personId) {
        return personDetailDataMapper.map(personRepository.findDetailDataByPersonId(personId));
    }

    /**
     * Loads the data of a person together with the daily rates of that person from the database.
     *
     * @param personId ID of the person whose data to load.
     * @return PersonWithRates object
     */
    @PreAuthorize("canReadPerson(#personId)")
    public PersonWithRates loadPersonWithRates(long personId) {
        PersonEntity personEntity = personRepository.findOneFetchDailyRates(personId);

        PersonWithRates person = new PersonWithRates();
        person.setName(personEntity.getName());
        person.setPersonId(personEntity.getId());
        person.setImportKey(personEntity.getImportKey());
        person.setDefaultDailyRate(personEntity.getDefaultDailyRate());

        for (DailyRateEntity rateEntity : personEntity.getDailyRates()) {
            BudgetBaseData budget = new BudgetBaseData();
            budget.setId(rateEntity.getBudget().getId());
            budget.setName(rateEntity.getBudget().getName());

            PersonRate rate = new PersonRate(rateEntity.getRate(),
                    budget,
                    new DateRange(rateEntity.getDateStart(), rateEntity.getDateEnd()));

            person.getRates().add(rate);
        }

        return person;
    }

    /**
     * Saves the given data about a person and his/her daily rates in the database.
     *
     * @param person the data to save in the database.
     */
    public List<String> savePersonWithRates(PersonWithRates person) {
        PersonEntity personEntity = personRepository.findById(person.getPersonId()).orElseThrow(RuntimeException::new);
        personEntity.setName(person.getName());
        personEntity.setImportKey(person.getImportKey());
        personEntity.setDefaultDailyRate(person.getDefaultDailyRate());

        List<DailyRateEntity> dailyRates = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        for (PersonRate rate : person.getRates()) {
            List<String> errorList = this.updateDailyRates(rate.getBudget(),
                    person,
                    rate.getDateRange(),
                    rate.getRate());

            if (!errorList.isEmpty()) {
                errors.addAll(errorList);
                continue;
            }

            DailyRateEntity rateEntity = new DailyRateEntity();
            rateEntity.setRate(rate.getRate());
            rateEntity.setBudget(budgetRepository.findById(rate.getBudget().getId()).orElse(null));
            rateEntity.setPerson(personEntity);
            rateEntity.setDateStart(rate.getDateRange().getStartDate());
            rateEntity.setDateEnd(rate.getDateRange().getEndDate());
            dailyRates.add(rateEntity);

        }
        personEntity.getDailyRates().clear();
        personEntity.getDailyRates().addAll(dailyRates);
        personRepository.save(personEntity);
        return errors;
    }

    public List<String> getOverlapWithManuallyEditedRecords(PersonWithRates person, long projectId){
        List<String> warnings = new ArrayList<>();
        //Check with manually edited entries and warn the user
        for(PersonRate rate : person.getRates()) {
            for (WorkRecordEntity e : workRecordRepository.findManuallyEditedEntries(projectId,
                    rate.getDateRange().getStartDate(), rate.getDateRange().getEndDate())) {

                //Warn about the editing of a rate only if a work record in this range has been edited manually and the amount is different
                if (DateUtil.isDateInDateRange(e.getDate(), rate.getDateRange())
                        && e.getBudget().getName().equals(rate.getBudget().getName())
                        && e.getPerson().getName().equals(person.getName())
                        && !e.getDailyRate().isEqual(() -> rate.getRate().toBigMoney())) {

                    warnings.add("A work record in the range "
                            + rate.getDateRange().toString()
                            + " (Exact Date and Amount: " + e.getDate() + ", " + e.getDailyRate().toString() +
                            ") for budget \"" + rate.getBudget().getName() +
                            "\" has already been edited manually and will not be overwritten.");
                }
            }
        }
        return warnings;
    }

    /**
     * Loads the base data of a single person from the database.
     *
     * @param personId ID of the person to load.
     * @return base data of the specified person.
     */
    @PreAuthorize("canReadPerson(#personId)")
    public PersonBaseData loadPersonBaseData(long personId) {
        return personBaseDataMapper.map(personRepository.findBaseDataByPersonId(personId));
    }

    @PreAuthorize("canReadPerson(#personId)")
    public void deletePerson(long personId) {
        personRepository.deleteById(personId);
    }

    @PreAuthorize("canReadBudget(#budgetId)")
    public List<PersonBaseData> loadPeopleBaseDataByBudget(long budgetId) {
        return personBaseDataMapper.map(personRepository.findBaseDataByBudgetId(budgetId));
    }

    public List<MissingDailyRateForBudgetBean> getMissingDailyRatesForPerson(long personId) {
        return workRecordRepository.getMissingDailyRatesForPerson(personId);
    }

    public void removeDailyRateFromPerson(PersonWithRates personWithRates, PersonRate rate) {
        this.updateDailyRates(rate.getBudget(), personWithRates, rate.getDateRange(), Money.zero(CurrencyUnit.EUR));
    }

    private List<String> updateDailyRates(BudgetBaseData budget, PersonWithRates person, DateRange dateRange, Money dailyRate) {
        List<String> errorMessages = new ArrayList<>();
        if (budget == null) {
            errorMessages.add("Budget shouldn't be null!");
        }
        if (person == null){
            errorMessages.add("Person shouldn't be null!");
        }
        if (dailyRate == null) {
            errorMessages.add("Daily Rate shouldn't be null!");
        }
        if (dateRange == null || dateRange.getStartDate() == null || dateRange.getEndDate() == null) {
            errorMessages.add("Date Range shouldn't be null!");
        }

        if (!errorMessages.isEmpty()) {
            errorMessages.add("The Daily Rate wasn't updated because of invalid values");
            return errorMessages;
        }
        workRecordRepository.updateDailyRates(budget.getId(),
                person.getPersonId(),
                dateRange.getStartDate(),
                dateRange.getEndDate(),
                dailyRate);
        return Collections.emptyList();
    }
}
