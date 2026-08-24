package it.aliounediagne.epicode.goldenrecord.exceptions;

/**
 * Genera i trace id nella forma "ERR-000042": l'utente vede il codice accanto
 * al messaggio pulito, lo stesso codice compare nel log accanto al diagnostic.
 *
 * Un contatore intero basta per una sessione singola e single-threaded; in
 * ambiente concorrente servirebbe un AtomicInteger.
 */
public final class TraceId {

    private static int sequence = 0;

    /** Utility class: non instanziabile. */
    private TraceId() {
    }

    /** Restituisce il prossimo id (es. "ERR-000042"). */
    public static String next() {
        sequence++;
        return String.format("ERR-%06d", sequence);
    }
}
