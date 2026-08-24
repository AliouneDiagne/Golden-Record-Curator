package it.aliounediagne.epicode.goldenrecord.exceptions;

/**
 * E' stato chiesto un contenuto (tipicamente un greeting via 'play xxx') il
 * cui id non e presente nel manifest caricato.
 */
public class ContentNotFoundException extends CuratorException {

    private static final long serialVersionUID = 1L;

    public ContentNotFoundException(String requestedId) {
        super("No content with id '" + requestedId + "' in the loaded manifest");
    }

    @Override
    public String getUserMessage() {
        return "No content matches the identifier you provided.";
    }
}
