package it.aliounediagne.epicode.goldenrecord.validation;

public class ValidationIssue {

    private final String rule;
    private final String message;
    private final boolean blocking;

    public ValidationIssue(String rule, String message, boolean blocking) {
        this.rule = rule;
        this.message = message;
        this.blocking = blocking;
    }

    public String getRule() {
        return rule;
    }

    public String getMessage() {
        return message;
    }

    public boolean isBlocking() {
        return blocking;
    }

    @Override
    public String toString() {
        return (blocking ? "[FAIL] " : "[OK]   ") + rule + " - " + message;
    }
}
