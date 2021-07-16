package org.wickedsource.budgeteer.web.pages.budgets.edit.tagsfield;

import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.util.convert.ConversionException;
import org.apache.wicket.util.convert.IConverter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class TagsConverter implements IConverter<List<String>> {

    @Override
    public List<String> convertToObject(String value, Locale locale) throws ConversionException {
        if (value == null || value.trim().isEmpty()) {
            return new ArrayList<>();
        }
        String[] values = value.split(",");
        return Arrays.asList(values);
    }

    @Override
    public String convertToString(List<String> value, Locale locale) {
        return StringUtils.join(value, ",");
    }
}
