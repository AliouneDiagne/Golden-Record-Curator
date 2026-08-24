package it.aliounediagne.epicode.goldenrecord.exceptions;

/**
 * Il manifest CSV non e leggibile o e malformato (I/O oppure parsing).
 * Il costruttore con "cause" preserva la stack trace originale nel log,
 * senza mai mostrarla all'utente.
 */
public class ManifestException extends CuratorException {

    private static final long serialVersionUID = 1L;

    public ManifestException(String fileName, int line, Throwable cause) {
        super("Malformed manifest '" + fileName + "' at line " + line, cause);
    }

    public ManifestException(String detail) {
        super(detail);
    }

    @Override
    public String getUserMessage() {
        return "The manifest could not be read. Please check the file format.";
    }
}
