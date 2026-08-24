package it.aliounediagne.epicode.goldenrecord.exceptions;

/**
 * La colonna 'type' del CSV contiene un valore che ContentType.parse() non
 * riconosce (o e null).
 */
public class UnsupportedContentTypeException extends CuratorException {

    private static final long serialVersionUID = 1L;

    public UnsupportedContentTypeException(String rawType) {
        super("Unknown content type: '" + rawType + "'");
    }

    @Override
    public String getUserMessage() {
        return "The manifest contains a content type that is not supported.";
    }
}
