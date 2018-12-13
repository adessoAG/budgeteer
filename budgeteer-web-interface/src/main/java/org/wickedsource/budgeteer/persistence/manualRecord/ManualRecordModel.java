package org.wickedsource.budgeteer.persistence.manualRecord;

import lombok.Getter;
import lombok.Setter;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.wickedsource.budgeteer.service.manualRecord.ManualRecord;
import org.wickedsource.budgeteer.service.manualRecord.ManualRecordService;

import java.util.List;

public class ManualRecordModel extends LoadableDetachableModel<List<ManualRecord>> {
    @SpringBean
    private ManualRecordService service;

    @Getter
    @Setter
    private long budgetId;

    public ManualRecordModel(long budgetId, ManualRecordService service){
        this.budgetId = budgetId;
        this.service = service;
    }

    @Override
    protected List<ManualRecord> load() {
        return service.getManualRecords(budgetId);
    }
}
