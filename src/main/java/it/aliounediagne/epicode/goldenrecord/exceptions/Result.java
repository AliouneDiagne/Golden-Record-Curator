package it.aliounediagne.epicode.goldenrecord.exceptions;

/**
 * Exception Shielding - contenitore dell'esito di un'operazione.
 *
 * Il generico <T> evita i cast al call-site: il tipo del valore e verificato
 * dal compilatore. Tutti i campi sono final, quindi un Result e immutabile.
 * Si crea dai factory method statici (success/failure), piu leggibili di un
 * costruttore a tre parametri di cui due quasi sempre null.
 */
public final class Result<T> {

    private final T value;
    private final String userMessage;
    private final String traceId;

    private Result(T value, String userMessage, String traceId) {
        this.value = value;
        this.userMessage = userMessage;
        this.traceId = traceId;
    }

    /** Esito positivo: contiene solo il valore. */
    public static <T> Result<T> success(T value) {
        return new Result<T>(value, null, null);
    }

    /** Esito negativo: contiene messaggio utente e trace id. */
    public static <T> Result<T> failure(String message, String traceId) {
        return new Result<T>(null, message, traceId);
    }

    public boolean isSuccess()      { return userMessage == null; }
    public T getValue()             { return value; }
    public String getUserMessage()  { return userMessage; }
    public String getTraceId()      { return traceId; }
}
