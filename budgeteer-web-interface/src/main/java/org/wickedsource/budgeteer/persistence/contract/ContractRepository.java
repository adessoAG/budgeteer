package org.wickedsource.budgeteer.persistence.contract;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;


import java.util.List;

public interface ContractRepository extends CrudRepository<ContractEntity, Long> {

    List<ContractEntity> findByProjectId(long projectId);

    @Query("select cif from ContractInvoiceField cif where cif.contract.id = :contractId AND cif.fieldName = :fieldName")
    ContractInvoiceField findInvoiceFieldByName(@Param("contractId") long contractID, @Param("fieldName") String fieldName);

    @Modifying
    @Query("delete from ContractEntity c where c.project.id = :projectId")
    void deleteByProjectId(@Param(value = "projectId") long projectId);

    @Query("select c from ContractEntity c join fetch c.invoiceFields where c.id = :id")
    ContractEntity findById(@Param("id") long id);

    @Modifying
    @Query("delete from ContractFieldEntity c where c.id in (select s.id from ContractFieldEntity s where  s.field.project.id = :projectId)")
    void deleteContractFieldByProjectId(@Param("projectId") long projectId);

    @Query("Select e from ContractFieldEntity e where e.contract.id = :contractID")
    List<ContractFieldEntity> findContractFieldsByContractId(@Param("contractID") Long contractID);

    @Query("select coalesce(sum(wr.minutes * wr.dailyRate/ 60 / 8),0) from WorkRecordEntity wr where wr.budget.contract.id = :contractId AND (wr.year < :year OR (wr.year = :year AND wr.month <= :month))")
    Double getSpentBudgetByContractIdUntilDate(@Param("contractId") Long contractId, @Param("month") Integer month, @Param("year") Integer year);

    @Query("select coalesce(cast(c.budget AS double), 0) from ContractEntity c " +
            "where c.id = :contractId")
    Double getBudgetOfContract(@Param("contractId") Long contractId);

    @Query("select coalesce(sum(i.invoiceSum),0l) from InvoiceEntity i where i.contract.id = :contractId AND (i.year < :year OR (i.year = :year AND i.month <= :month))")
    Double getInvoicedBudgetTillMonthAndYear(@Param("contractId") Long contractId, @Param("month") Integer month, @Param("year") Integer year);

    @Query("select coalesce(sum(i.invoiceSum),0l) from InvoiceEntity i where i.contract.id = :contractId AND (i.year = :year AND i.month = :month)")
    Double getInvoicedBudgetOfMonth(@Param("contractId") Long contractId, @Param("month") Integer month, @Param("year") Integer year);
}