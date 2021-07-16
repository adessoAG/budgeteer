package org.wickedsource.budgeteer.web.components.daterange;


import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.IModel;
import org.apache.wicket.util.convert.IConverter;
import org.wickedsource.budgeteer.service.DateRange;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

public class DateRangeInputField extends TextField<DateRange> {

    public enum DropLocation {UP, DOWN}

    public DateRangeInputField(String id, IModel<DateRange> model, DropLocation dropLocation) {
        this(id, model, null, dropLocation);
    }

    public DateRangeInputField(String id, IModel<DateRange> model) {
        this(id, model, null, DropLocation.DOWN);
    }

    /**
     * Creates a DateRangeInputField which displays the given dateRange when opened
     */
    public DateRangeInputField(String id, IModel<DateRange> model, DateRange defaultRange, DropLocation dropLocation) {
        super(id, model);
        Map<String, String> options = new HashMap<>();
        if(defaultRange != null && (defaultRange.getStartDate() != null || defaultRange.getEndDate() != null)){
            DateFormat format = new SimpleDateFormat("dd.MM.yyyy");
            DateRange modelObject = model.getObject();
            if(defaultRange.getStartDate() != null){
                options.put("startDate", String.format("'%s'", format.format(modelObject.getStartDate() == null ? defaultRange.getStartDate() : modelObject.getStartDate())));
            }
            if(defaultRange.getEndDate() != null){
                options.put("endDate", String.format("'%s'", format.format(modelObject.getEndDate() == null ? defaultRange.getEndDate() : modelObject.getEndDate())));
            }
        }
        options.put("format","'DD.MM.YYYY'");
        options.put("enableEmptyDate", "true");
        options.put("drops", dropLocation == DropLocation.DOWN ? "'down'" : "'up'");
        options.put("linkedCalendars", "false");
        super.add(new DateRangePickerBehavior(options, true));
    }


    @Override
    @SuppressWarnings("unchecked")
    public <DateRange> IConverter<DateRange> getConverter(Class<DateRange> type) {
        return (IConverter<DateRange>) new DateRangeConverter();
    }

    @Override
    protected String[] getInputTypes() {
        return new String[]{"text"};
    }


}
