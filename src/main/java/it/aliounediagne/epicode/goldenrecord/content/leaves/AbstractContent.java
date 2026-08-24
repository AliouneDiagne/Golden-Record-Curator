package it.aliounediagne.epicode.goldenrecord.content.leaves;

import it.aliounediagne.epicode.goldenrecord.content.Content;
import it.aliounediagne.epicode.goldenrecord.content.ContentType;
import it.aliounediagne.epicode.goldenrecord.exceptions.InvalidContentException;

import java.util.Collections;
import java.util.Set;

/**
 * Composite - base condivisa di tutte le "Leaf".
 *
 * Le quattro foglie sono tutte contenuti incidibili (relazione IS-A) e
 * condividono gli stessi tre campi, quindi stato e comportamento comuni
 * vivono qui una volta sola. E' abstract perche "un contenuto in generale"
 * non si incide: solo una forma concreta.
 *
 * I campi sono private e final, assegnati una volta sola dopo la validazione:
 * chi tiene una foglia sa che e valida.
 */
public abstract class AbstractContent implements Content {

    private final String id;
    private final String title;
    private final int durationSeconds;

    /**
     * Costruttore protected: si arriva qui solo via super(...) dalle
     * sottoclassi. La validazione avviene alla nascita, cosi un oggetto che
     * rompe le regole non esiste affatto.
     */
    protected AbstractContent(String id, String title, int durationSeconds, ContentType type) {
        this.id = requireText(id, "id");
        this.title = requireText(title, "title");

        // La regola di business vive sull'enum: ci limitiamo a chiedere.
        if (!type.accepts(durationSeconds)) {
            throw new InvalidContentException(id,
                    "duration " + durationSeconds + "s is not acceptable for type " + type.name());
        }
        this.durationSeconds = durationSeconds;
    }

    // ---- Metodi comuni a ogni foglia -------------------------------------
    // final perche esprimono invarianti del dominio: una foglia e per
    // definizione esattamente un elemento incidibile, e una sottoclasse che
    // restituisse 2 romperebbe l'aritmetica dell'intero albero.

    @Override public final String getId()    { return id; }
    @Override public final String getTitle() { return title; }

    /** Caso base della ricorsione: la foglia restituisce il proprio campo. */
    @Override
    public final int getPlaybackTime() {
        return durationSeconds;
    }

    /** Caso base: una foglia vale 1 elemento incidibile. */
    @Override
    public final int getItemCount() {
        return 1;
    }

    /** Non final: Greeting fa override per dichiarare la propria lingua. */
    @Override
    public Set<String> getLanguages() {
        return Collections.emptySet();
    }

    /** Rendering di una riga; Greeting e MusicTrack lo arricchiscono. */
    @Override
    public String render(String indent) {
        return indent + "- " + getTitle() + " (" + formatTime(durationSeconds) + ")";
    }

    /** Lasciato abstract: ogni foglia deve dichiarare la propria natura. */
    @Override
    public abstract ContentType getType();

    // ---- Helper riusati dalle sottoclassi --------------------------------

    /**
     * Rifiuta null, stringa vuota e stringa di soli spazi; normalizza.
     *
     * @throws InvalidContentException se manca
     */
    protected static String requireText(String value, String field) {
        // trim().isEmpty() invece di isBlank(): trim() serve comunque per il
        // valore normalizzato restituito sotto.
        if (value == null || value.trim().isEmpty()) {
            throw new InvalidContentException(field, "must not be empty");
        }
        return value.trim();
    }

    /** Formatta secondi come mm:ss. */
    protected static String formatTime(int seconds) {
        return seconds / 60 + ":" + String.format("%02d", seconds % 60);
    }
}
