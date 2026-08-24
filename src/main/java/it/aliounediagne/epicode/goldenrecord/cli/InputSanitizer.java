package it.aliounediagne.epicode.goldenrecord.cli;

import it.aliounediagne.epicode.goldenrecord.exceptions.InvalidInputException;

import java.util.regex.Pattern;

/**
 * Validazione difensiva dell'input: accetta solo forme note invece di provare
 * a bloccare singoli caratteri pericolosi.
 */
public class InputSanitizer {

    private static final int MAX_COMMAND_LENGTH = 256;

    // Whitelist: solo lettere, numeri, trattino e underscore per gli identificatori.
    private static final Pattern SAFE_ID = Pattern.compile("^[a-zA-Z0-9_-]{1,64}$");


    public String sanitizeCommand(String raw) {
        if (raw == null) {
            return "";
        }
        String trimmed = raw.trim();

        if (trimmed.length() > MAX_COMMAND_LENGTH) {
            throw new InvalidInputException("command exceeds "
                    + MAX_COMMAND_LENGTH + " characters");
        }
        return trimmed.replaceAll("\\p{Cntrl}", "");
    }


    public String sanitizeId(String raw) {
        String cleaned = sanitizeCommand(raw);
        if (!SAFE_ID.matcher(cleaned).matches()) {
            throw new InvalidInputException("malformed identifier");
        }
        return cleaned;
    }


    public String sanitizeFileName(String raw) {
        String cleaned = sanitizeCommand(raw);
        if (cleaned.isEmpty() || cleaned.contains("..")) {
            throw new InvalidInputException("file name not allowed");
        }
        return cleaned;
    }
}
