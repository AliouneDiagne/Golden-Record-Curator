package it.aliounediagne.epicode.goldenrecord.exceptions;

/**
 * Interfaccia funzionale (SAM) che rappresenta un'azione da eseguire dentro
 * SafeExecutor; il generico <T> lascia libero il tipo di ritorno (Boolean per
 * il dispatch di un comando, Disc per il load...).
 *
 * Non usiamo Callable<T> per non legarci a java.util.concurrent e per dire
 * nel nome che si tratta di un'azione del curatore.
 */
public interface CuratorAction<T> {

    /** Esegue l'azione. Puo lanciare qualsiasi eccezione: la cattura SafeExecutor. */
    T run() throws Exception;
}
