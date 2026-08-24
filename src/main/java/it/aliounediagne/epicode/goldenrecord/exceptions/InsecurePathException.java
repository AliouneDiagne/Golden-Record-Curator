package it.aliounediagne.epicode.goldenrecord.exceptions;

/**
 * Difesa dal path traversal: dopo resolve + normalize il percorso uscirebbe
 * dalla directory base consentita. Il diagnostic nomina i path per il log,
 * il messaggio utente no.
 */
public class InsecurePathException extends CuratorException {

    private static final long serialVersionUID = 1L;

    public InsecurePathException(String requested, String resolved) {
        super("Path '" + requested + "' resolves to '" + resolved
                + "', which is outside the allowed base directory");
    }

    @Override
    public String getUserMessage() {
        return "The requested file path is not allowed.";
    }
}
