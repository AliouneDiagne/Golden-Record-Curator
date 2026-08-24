package it.aliounediagne.epicode.goldenrecord.exceptions;

/**
 * Un contenuto del manifest non rispetta le regole del proprio tipo (durata
 * fuori limite, titolo vuoto, id mancante).
 */
public class InvalidContentException extends CuratorException {

    private static final long serialVersionUID = 1L;

    public InvalidContentException(String contentId, String reason) {
        super("Invalid content '" + contentId + "': " + reason);
    }

    @Override
    public String getUserMessage() {
        return "One of the contents in the manifest is not valid and was rejected.";
    }
}
