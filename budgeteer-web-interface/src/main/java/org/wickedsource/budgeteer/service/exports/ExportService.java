package org.wickedsource.budgeteer.service.exports;


import org.joda.money.format.MoneyFormatter;
import org.joda.money.format.MoneyFormatterBuilder;
import org.springframework.stereotype.Service;
import org.wickedsource.budgeteer.CSVUtils;
import org.wickedsource.budgeteer.service.record.WorkRecord;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

@Service
public class ExportService {

    private static final List<String> HEADER = Arrays.asList("Budget", "Name", "Daily Rate", "Date", "Hours", "Budget");

    public <T extends WorkRecord> File generateCSVFileFromRecords(Collection<T> records, Locale locale) {
        File csvFile = new File("export.csv");
        MoneyFormatter moneyFormatter = new MoneyFormatterBuilder().appendAmountLocalized().toFormatter(locale);
        try (FileWriter writer = new FileWriter(csvFile)) {
            CSVUtils.writeLine(writer, HEADER);
            for (WorkRecord record : records) {
                List<String> line = toList(record, moneyFormatter);
                CSVUtils.writeLine(writer, line);
            }
            writer.flush();
        } catch (IOException e) {
            System.err.println("Error while exporting to csv");
            e.printStackTrace();
            return null;
        }
        return csvFile;
    }

    private List<String> toList(WorkRecord workRecord, MoneyFormatter moneyFormatter) {
        return Arrays.asList(workRecord.getBudgetName(),
                workRecord.getPersonName(),
                moneyFormatter.print(workRecord.getDailyRate()),
                workRecord.getDate().toString(),
                Double.toString(workRecord.getHours()),
                moneyFormatter.print(workRecord.getBudgetBurned()));
    }

}
