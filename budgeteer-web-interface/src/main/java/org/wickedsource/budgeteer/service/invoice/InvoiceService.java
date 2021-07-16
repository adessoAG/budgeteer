package org.wickedsource.budgeteer.service.invoice;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.wickedsource.budgeteer.persistence.contract.ContractEntity;
import org.wickedsource.budgeteer.persistence.contract.ContractInvoiceField;
import org.wickedsource.budgeteer.persistence.contract.ContractRepository;
import org.wickedsource.budgeteer.persistence.invoice.InvoiceEntity;
import org.wickedsource.budgeteer.persistence.invoice.InvoiceFieldEntity;
import org.wickedsource.budgeteer.persistence.invoice.InvoiceRepository;
import org.wickedsource.budgeteer.service.contract.DynamicAttributeField;
import org.wickedsource.budgeteer.web.pages.invoice.overview.table.InvoiceOverviewTableModel;

import javax.transaction.Transactional;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Set;

@Service
@Transactional
public class InvoiceService {
    private static final SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy");

    @Autowired
    private InvoiceDataMapper mapper;

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private ContractRepository contractRepository;

    @PreAuthorize("canReadProject(#projectId)")
    public InvoiceOverviewTableModel getInvoiceOverviewByProject(long projectId){
        InvoiceOverviewTableModel result = new InvoiceOverviewTableModel();
        result.setInvoices(mapper.map(invoiceRepository.findByProjectId(projectId), true));
        return result;
    }

    @PreAuthorize("canReadContract(#contractId)")
    public InvoiceOverviewTableModel getInvoiceOverviewByContract(long contractId) {
        InvoiceOverviewTableModel result = new InvoiceOverviewTableModel();
        result.setInvoices(mapper.map(invoiceRepository.findByContractId(contractId)));
        return result;
    }

    @PreAuthorize("canReadInvoice(#invoiceId)")
    public InvoiceBaseData getInvoiceById(long invoiceId) {
        return mapper.map(invoiceRepository.findById(invoiceId).orElse(null));
    }

    @PreAuthorize("canReadContract(#contractId)")
    public InvoiceBaseData getEmptyInvoiceModel(long contractId) {
        ContractEntity contract = contractRepository.findById(contractId).orElseThrow(RuntimeException::new);
        InvoiceBaseData model = new InvoiceBaseData(contractId, contract.getName());
        Set<ContractInvoiceField> fields = contract.getInvoiceFields();
        for(ContractInvoiceField field : fields){
            model.getDynamicInvoiceFields().add(new DynamicAttributeField(field.getFieldName(), ""));
        }
        return model;
    }

    public long save(InvoiceBaseData invoiceBaseData) {
        ContractEntity contract = contractRepository.findById(invoiceBaseData.getContractId()).orElseThrow(RuntimeException::new);
        InvoiceEntity invoiceEntity = new InvoiceEntity();
        invoiceEntity.setId(0);
        invoiceEntity.setContract(contract);

        if(invoiceBaseData.getInvoiceId() != 0){
            invoiceEntity = invoiceRepository.findById(invoiceBaseData.getInvoiceId()).orElseThrow(RuntimeException::new);
        }
        //Update basic information
        invoiceEntity.setName(invoiceBaseData.getInvoiceName());
        invoiceEntity.setInvoiceSum(invoiceBaseData.getSum());
        invoiceEntity.setInternalNumber(invoiceBaseData.getInternalNumber());
        invoiceEntity.setYear(invoiceBaseData.getYear());
        invoiceEntity.setMonth(invoiceBaseData.getMonth());
        try {
            String date = "01." + invoiceBaseData.getMonth() + "." + invoiceBaseData.getYear();
            invoiceEntity.setDate(formatter.parse(date));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        invoiceEntity.setPaidDate(invoiceBaseData.getPaidDate());
        invoiceEntity.setDueDate(invoiceBaseData.getDueDate());

        invoiceEntity.setFileName(invoiceBaseData.getFileUploadModel().getFileName());
        invoiceEntity.setFile(invoiceBaseData.getFileUploadModel().getFile());
        invoiceEntity.setLink(invoiceBaseData.getFileUploadModel().getLink());

        //update additional information of the current contract
        for(DynamicAttributeField fields : invoiceBaseData.getDynamicInvoiceFields()){
            if(fields.getValue() != null && !fields.getValue().isEmpty()) {
                boolean attributeFound = false;
                //see, if the attribute already exists -> Update the value
                for (InvoiceFieldEntity invoiceFieldEntity : invoiceEntity.getDynamicFields()) {
                    if (invoiceFieldEntity.getField().getFieldName().equals(fields.getName().trim())) {
                        invoiceFieldEntity.setValue(fields.getValue());
                        attributeFound = true;
                        break;
                    }
                }
                // Create a new Attribute
                if (!attributeFound) {
                    // see if the Project already contains a field with this name. If not, create a new one
                    ContractInvoiceField contractInvoiceField = contractRepository.findInvoiceFieldByName(invoiceBaseData.getContractId(), fields.getName().trim());
                    if (contractInvoiceField == null) {
                        contractInvoiceField = new ContractInvoiceField(0, fields.getName().trim(), contract);
                    }
                    InvoiceFieldEntity field = new InvoiceFieldEntity();
                    field.setId(0);
                    field.setField(contractInvoiceField);
                    field.setValue(fields.getValue() == null ? "" : fields.getValue().trim());
                    invoiceEntity.getDynamicFields().add(field);
                }
            }
        }
        invoiceRepository.save(invoiceEntity);
        return invoiceEntity.getId();

    }

    @PreAuthorize("canReadInvoice(#invoiceId)")
    public void deleteInvoice(long invoiceId) {
        invoiceRepository.deleteById(invoiceId);
    }
}
