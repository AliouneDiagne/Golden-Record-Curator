package it.aliounediagne.epicode.goldenrecord.manifest.spi;

import java.util.ArrayList;
import java.util.List;

/**
 * Parser CSV minimale: gestisce campi tra virgolette e virgolette escape,
 * evitando dipendenze runtime esterne.
 */
public final class CsvReader {

    private CsvReader() {
    }

    public static List<String> splitLine(String line) {
        List<String> fields = new ArrayList<String>();
        StringBuilder current = new StringBuilder();
        boolean insideQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);

            if (insideQuotes) {
                if (c == '"') {
                    if (i + 1 < line.length() && line.charAt(i + 1) == '"') {
                        current.append('"');
                        i++;
                    } else {
                        insideQuotes = false;
                    }
                } else {
                    current.append(c);
                }
            } else {
                if (c == '"') {
                    insideQuotes = true;
                } else if (c == ',') {
                    fields.add(current.toString());
                    current.setLength(0);
                } else {
                    current.append(c);
                }
            }
        }
        fields.add(current.toString());
        return fields;
    }
}
