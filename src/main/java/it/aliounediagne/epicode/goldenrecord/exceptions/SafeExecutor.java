package it.aliounediagne.epicode.goldenrecord.exceptions;

import it.aliounediagne.epicode.goldenrecord.logging.CuratorLogger;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Exception Shielding - il punto unico di cattura.
 *
 * Tutti i comandi utente passano da qui e cio che esce e sempre un Result,
 * mai un'eccezione: il command loop non puo essere rotto da un errore
 * lanciato piu in basso.
 *
 * I due canali di output non si incontrano: nel log file finisce il
 * diagnostic completo con stack trace, sul terminale solo getUserMessage()
 * piu il trace id che collega i due.
 */
public class SafeExecutor {

    private static final Logger LOG = CuratorLogger.get(SafeExecutor.class);

    /**
     * Esegue action.run() proteggendo il chiamante da qualsiasi eccezione.
     *
     * @param operation etichetta descrittiva (es. "COMMAND", "LOAD")
     * @param action    codice da eseguire, fornito dal chiamante
     * @return Result.success(...) se e andata bene, Result.failure(...) altrimenti
     */
    public <T> Result<T> execute(String operation, CuratorAction<T> action) {

        // Un trace id per ogni operazione, cosi log e messaggio utente sono
        // correlati anche quando l'errore non arriva mai.
        String traceId = TraceId.next();

        try {
            return Result.success(action.run());

        } catch (CuratorException ex) {
            // Errore di dominio: previsto, il messaggio utente lo porta con se.
            LOG.log(Level.WARNING, "[" + traceId + "] " + operation + " failed: "
                    + ex.getDiagnostic(), ex);
            return Result.failure(ex.getUserMessage(), traceId);

        } catch (Exception ex) {
            // Errore inatteso: SEVERE nel log, messaggio generico all'utente
            // perche non sappiamo cosa riveleremmo.
            LOG.log(Level.SEVERE, "[" + traceId + "] Unexpected failure during "
                    + operation, ex);
            return Result.failure("The operation could not be completed.", traceId);
        }
    }
}
