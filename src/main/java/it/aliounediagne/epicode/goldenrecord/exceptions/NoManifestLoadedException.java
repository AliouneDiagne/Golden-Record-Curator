package it.aliounediagne.epicode.goldenrecord.exceptions;

/**
 * E' stato invocato un comando che lavora sull'albero in memoria (tree,
 * budget, play, validate, stats, curate) prima di aver eseguito 'load'.
 */
public class NoManifestLoadedException extends CuratorException {

    private static final long serialVersionUID = 1L;

    public NoManifestLoadedException(String command) {
        super("Command '" + command + "' requires a loaded manifest");
    }

    @Override
    public String getUserMessage() {
        return "No manifest is loaded yet. Run 'load' first.";
    }
}
