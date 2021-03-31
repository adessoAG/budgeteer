package org.wickedsource.budgeteer;

import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.stream.Collectors;

public class CSVUtils {

    private CSVUtils() {}

    private static final char DEFAULT_SEPARATOR = ';';

    public static void writeLine(Writer writer, List<String> values) throws IOException {
        writeLine(writer, values, DEFAULT_SEPARATOR);
    }

    public static void writeLine(Writer writer, List<String> values, char separators) throws IOException {
        if (Character.isSpaceChar(separators)) {
            separators = DEFAULT_SEPARATOR;
        }
        String csv = values.stream().map(CSVUtils::followCVSformat)
                .collect(Collectors.joining(Character.toString(separators), "", "\n"));
        writer.append(csv);
    }

    //https://tools.ietf.org/html/rfc4180
    private static String followCVSformat(String value) {
        if (value == null) {
            return "";
        }
        if (StringUtils.containsAny(value, ",", "\"", "\r\n")) {
            return String.format("\"%s\"", value.replace("\"", "\"\""));
        }
        return value;
    }
}
