package it.aliounediagne.epicode.goldenrecord.exceptions;

/**
 * Section ha rifiutato un inserimento che avrebbe creato un anello
 * nell'albero: le aggregazioni ricorsive andrebbero in ricorsione infinita.
 */
public class CyclicStructureException extends CuratorException {

    private static final long serialVersionUID = 1L;

    public CyclicStructureException(String parentId, String candidateId) {
        super("Adding '" + candidateId + "' into '" + parentId
                + "' would create a cycle in the content tree");
    }

    @Override
    public String getUserMessage() {
        return "That operation would create an invalid structure and was refused.";
    }
}
