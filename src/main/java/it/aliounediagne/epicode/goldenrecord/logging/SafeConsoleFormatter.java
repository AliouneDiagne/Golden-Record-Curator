package it.aliounediagne.epicode.goldenrecord.logging;

import java.util.logging.Formatter;
import java.util.logging.LogRecord;


public class SafeConsoleFormatter extends Formatter {

    @Override
    public String format(LogRecord record) {
        StringBuilder sb = new StringBuilder();
        sb.append(record.getLevel()).append(": ");
        sb.append(formatMessage(record)).append("\n");
        
        return sb.toString();
    }
}
