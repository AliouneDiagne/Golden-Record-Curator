package it.aliounediagne.epicode.goldenrecord.exceptions;

/**
 * InputSanitizer ha rifiutato l'input dell'utente: caratteri fuori whitelist,
 * lunghezza eccessiva o caratteri di controllo.
 */
public class InvalidInputException extends CuratorException {

    private static final long serialVersionUID = 1L;

    public InvalidInputException(String reason) {
        super("Rejected user input: " + reason);
    }

    @Override
    public String getUserMessage() {
        return "The command could not be understood and was ignored.";
    }
}
