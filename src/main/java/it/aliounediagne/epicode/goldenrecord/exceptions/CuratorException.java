package it.aliounediagne.epicode.goldenrecord.exceptions;

/**
 * Exception Shielding - base di tutte le eccezioni di dominio.
 *
 * Ogni eccezione porta due messaggi separati: getDiagnostic() per il log
 * (id, path, dettagli tecnici) e getUserMessage() per il terminale (nessun
 * dettaglio interno). SafeExecutor cattura questo tipo base e lascia fare al
 * polimorfismo, senza conoscere le sottoclassi.
 *
 * Estende RuntimeException e non una checked per non costringere l'intera
 * catena di chiamata a dichiarare "throws": la cattura avviene in un punto
 * solo, SafeExecutor.
 */
public abstract class CuratorException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    protected CuratorException(String diagnostic) {
        super(diagnostic);
    }

    protected CuratorException(String diagnostic, Throwable cause) {
        super(diagnostic, cause);
    }

    /** Messaggio tecnico destinato al log file. */
    public String getDiagnostic() {
        return getMessage();
    }

    /**
     * Messaggio sicuro destinato all'utente: mai classi, path o dettagli di
     * implementazione (difesa dall'information leak).
     */
    public abstract String getUserMessage();
}
