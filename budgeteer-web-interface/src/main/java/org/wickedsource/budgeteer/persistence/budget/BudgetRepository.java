package org.wickedsource.budgeteer.persistence.budget;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.wickedsource.budgeteer.service.notification.MissingContractForBudgetNotification;

import java.util.List;

public interface BudgetRepository extends CrudRepository<BudgetEntity, Long> {

    @Query("select distinct t.tag from BudgetTagEntity t where t.budget.project.id = :projectId")
    public List<String> getAllTagsInProject(@Param("projectId") long projectId);

    public List<BudgetEntity> findByProjectIdOrderByNameAsc(long projectId);

    /**
     * Searches for Budgets that are tagged with AT LEAST ONE of the given tags.
     *
     * @param tags the tags to search for
     * @return all Budgets that match the search criteria
     */
    @Query("select distinct b from BudgetEntity b join b.tags t where t.tag in (:tags) and b.project.id=:projectId order by b.name")
    public List<BudgetEntity> findByAtLeastOneTag(@Param("projectId") long projectId, @Param("tags") List<String> tags);

    @Query("select new org.wickedsource.budgeteer.persistence.budget.MissingBudgetTotalBean(b.id, b.name) from BudgetEntity b where b.total = 0 and b.project.id=:projectId order by b.name")
    public List<MissingBudgetTotalBean> getMissingBudgetTotalsForProject(@Param("projectId") long projectId);

    /**
     * Returns a MissingBudgetTotalBean object for the given Budget if it's budget total is zero. Returns null, if the budget is not zero!
     */
    @Query("select new org.wickedsource.budgeteer.persistence.budget.MissingBudgetTotalBean(b.id, b.name) from BudgetEntity b where b.total = 0 and b.id=:budgetId order by b.name")
    public MissingBudgetTotalBean getMissingBudgetTotalForBudget(@Param("budgetId") long budgetId);


    @Query("select new org.wickedsource.budgeteer.service.notification.MissingContractForBudgetNotification(b.id) from BudgetEntity b where b.contract = null and b.id=:budgetId")
    public MissingContractForBudgetNotification getMissingContractForBudget(@Param("budgetId") long budgetId);

    @Query("select new org.wickedsource.budgeteer.service.notification.MissingContractForBudgetNotification(b.id) from BudgetEntity b where b.contract = null and b.project.id=:projectId")
    public List<MissingContractForBudgetNotification> getMissingContractForProject(@Param("projectId")long projectId);


    @Modifying
    @Query("delete from BudgetEntity b where b.project.id = :projectId")
    public void deleteByProjectId(@Param("projectId") long projectId);

    @Query("select b from BudgetEntity b where b.contract.id = :contractId")
    List<BudgetEntity> findByContractId(@Param("contractId") long cId);

    @Query("select distinct wr.budget from WorkRecordEntity wr where wr.person.id = :personId")
    List<BudgetEntity> findByPersonId(@Param("personId") long personId);
}
