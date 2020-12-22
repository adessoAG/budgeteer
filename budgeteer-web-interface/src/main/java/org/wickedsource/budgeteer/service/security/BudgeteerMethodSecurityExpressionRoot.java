package org.wickedsource.budgeteer.service.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.expression.DenyAllPermissionEvaluator;
import org.springframework.security.access.expression.SecurityExpressionRoot;
import org.springframework.security.access.expression.method.MethodSecurityExpressionOperations;
import org.springframework.security.authentication.AuthenticationTrustResolverImpl;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;
import org.wickedsource.budgeteer.persistence.budget.BudgetEntity;
import org.wickedsource.budgeteer.persistence.budget.BudgetRepository;
import org.wickedsource.budgeteer.persistence.contract.ContractEntity;
import org.wickedsource.budgeteer.persistence.contract.ContractRepository;
import org.wickedsource.budgeteer.persistence.invoice.InvoiceEntity;
import org.wickedsource.budgeteer.persistence.invoice.InvoiceRepository;
import org.wickedsource.budgeteer.persistence.person.PersonEntity;
import org.wickedsource.budgeteer.persistence.person.PersonRepository;
import org.wickedsource.budgeteer.web.BudgeteerSession;

/**
 * A custom {@link SecurityExpressionRoot} that implements the {@link MethodSecurityExpressionOperations} interface
 * for the default expressions like {@link #hasPermission(Object, Object)}.
 *
 * All expression evaluation is performed by the currently selected project of the user.
 *
 * @see BudgeteerSession#getProjectId()
 */
@Component
public class BudgeteerMethodSecurityExpressionRoot extends SecurityExpressionRoot implements MethodSecurityExpressionOperations {

    @Autowired
    private BudgetRepository budgetRepository;

    @Autowired
    private ContractRepository contractRepository;

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private PersonRepository personRepository;

    private Object filterObject;

    private Object returnObject;

    public BudgeteerMethodSecurityExpressionRoot() {
        // supply a placeholder token that will not be used by this root
        // all authorization is performed
        super(new UsernamePasswordAuthenticationToken("user", "password"));

        // set default values
        this.setPermissionEvaluator(new DenyAllPermissionEvaluator());
        this.setTrustResolver(new AuthenticationTrustResolverImpl());
    }

    @Override
    public Object getFilterObject() {
        return this.filterObject;
    }

    @Override
    public Object getReturnObject() {
        return this.returnObject;
    }

    @Override
    public Object getThis() {
        return this;
    }

    @Override
    public void setFilterObject(Object obj) {
        this.filterObject = obj;
    }

    @Override
    public void setReturnObject(Object obj) {
        this.returnObject = obj;
    }

    /**
     *
     * @return
     *          The currently selected project id.
     *
     * @see BudgeteerSession#getProjectId()
     */
    private long getCurrentProjectId() {
        return BudgeteerSession.get().getProjectId();
    }

    // custom security expressions

    /**
     * A custom expression to evaluate if the user is allowed to read
     * the budget.
     *
     * @param budgetId
     *          The id of the budget to retrieve.
     * @return
     *          <i>true</i>, if the id of the budget is valid and the
     *          {@link BudgetEntity}'s project has the same id as the currently
     *          selected project by the user, <i>false</i> otherwise.
     *
     * @see BudgeteerSession#getProjectId()
     */
    public boolean canReadBudget(Long budgetId) {
        BudgetEntity entity = budgetRepository.findById(budgetId).orElse(null);

        if(entity != null) {
            long budgetProjectId = entity.getProject().getId();
            long selectedProjectId = getCurrentProjectId();

            return selectedProjectId == budgetProjectId;
        }

        return false;
    }

    /**
     *
     * @param projectId
     *          The id of the project to retrieve.
     * @return
     *          <i>true</i>, if the currently selected project has the same
     *          id as the given one.
     *
     * @see BudgeteerSession#getProjectId()
     */
    public boolean canReadProject(Long projectId) {
        long selectedProjectId = getCurrentProjectId();
        return selectedProjectId == projectId;
    }

    /**
     *
     * @param contractId
     *          The id of the contract to retrieve.
     * @return
     *          <i>true</i>, if the associated project of the contract has
     *          the same id as the currently selected project by the user,
     *          <i>false</i> otherwise.
     *
     * @see BudgeteerSession#getProjectId()
     */
    public boolean canReadContract(Long contractId) {
        ContractEntity entity = contractRepository.findById(contractId).orElse(null);

        if(entity != null) {
            long contractProjectId = entity.getProject().getId();
            long selectedProjectId = getCurrentProjectId();

            return selectedProjectId == contractProjectId;
        }

        return false;
    }

    /**
     *
     * @param invoiceId
     *          The id of the invoice.
     * @return
     *          <i>true</i>, if the associated contract's project has the
     *          same id as the currently selected project by the user,
     *          <i>false</i> otherwise.
     *
     * @see BudgeteerSession#getProjectId()
     */
    public boolean canReadInvoice(Long invoiceId) {
        InvoiceEntity entity = invoiceRepository.findById(invoiceId).orElse(null);

        if(entity != null) {
            long contractProjectId = entity.getContract().getProject().getId();
            long selectedProjectId = getCurrentProjectId();

            return selectedProjectId == contractProjectId;
        }

        return false;
    }

    /**
     *
     * @param personId
     *          The id of the person to retrieve.
     * @return
     *          <i>true</i>, if the associated project of the person has
     *          the same id as the currently selected project by the user.
     *
     * @see BudgeteerSession#getProjectId()
     */
    public boolean canReadPerson(Long personId) {
        PersonEntity entity = personRepository.findById(personId).orElse(null);

        if(entity != null) {
            long personProjectId = entity.getProject().getId();
            long selectedProjectId = getCurrentProjectId();

            return selectedProjectId == personProjectId;
        }

        return false;
    }

}
