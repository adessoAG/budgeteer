package de.adesso.budgeteer.service.manualRecord;

import de.adesso.budgeteer.persistence.budget.BudgetEntity;
import de.adesso.budgeteer.persistence.budget.BudgetRepository;
import de.adesso.budgeteer.persistence.manualRecord.ManualRecordEntity;
import de.adesso.budgeteer.persistence.manualRecord.ManualRecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@Transactional
public class ManualRecordService {
    @Autowired
    private ManualRecordRepository manualRecordRepository;

    @Autowired
    private BudgetRepository budgetRepository;

    public List<ManualRecord> getManualRecords(long budgetId) {
        List<ManualRecordEntity> entities = manualRecordRepository.getManualRecordByBudgetId(budgetId);
        List<ManualRecord> result = new ArrayList<>();

        for (ManualRecordEntity entity : entities) {
            ManualRecord data = new ManualRecord(entity);
            result.add(data);
        }

        return result;
    }

    public ManualRecord loadManualRecord(long manualRecordId) {
        return new ManualRecord(manualRecordRepository.findOne(manualRecordId));
    }

    public long saveManualRecord(ManualRecord data) {
        assert data != null;

        ManualRecordEntity record = new ManualRecordEntity();
        record.setId(data.getId());
        record.setDescription(data.getDescription());
        record.setMoneyAmount(data.getMoneyAmount());
        record.setCreationDate(new Date());

        if (data.getBillingDate() == null) {
            data.setBillingDate(new Date());
        }

        record.setBillingDate(data.getBillingDate());
        BudgetEntity budgetEntity = budgetRepository.findOne(data.getBudgetId());
        record.setBudget(budgetEntity);

        manualRecordRepository.save(record);

        return record.getId();
    }

    public void deleteRecord(long id) {
        manualRecordRepository.delete(id);
    }
}
