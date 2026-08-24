package it.aliounediagne.epicode.goldenrecord.content;

import java.util.Set;

/**
 * Composite - ruolo "Component": contratto comune a contenitori (Section) e
 * foglie (Greeting, MusicTrack, EarthSound, EncodedImage).
 *
 * I client (CuratorConsole, DiscValidator, gli Iterator) parlano solo con
 * Content: le aggregazioni ricorsive attraversano l'albero senza "instanceof".
 * E' un'interface e non una classe astratta perche contenitori e foglie non
 * condividono implementazione, e lo slot di single-inheritance resta libero
 * per AbstractContent.
 */
public interface Content {

    /** Identificatore univoco proveniente dal manifest (mai null). */
    String getId();

    /** Etichetta leggibile del contenuto (mai null). */
    String getTitle();

    /** Natura del contenuto - vedi enum ContentType. */
    ContentType getType();

    /**
     * Durata in secondi: una foglia restituisce il proprio campo (caso base),
     * una Section somma i figli (caso ricorsivo).
     */
    int getPlaybackTime();

    /**
     * Quanti elementi INCIDIBILI rappresenta questo contenuto: 1 per una
     * foglia, la somma dei figli per una Section (che non conta se stessa).
     */
    int getItemCount();

    /**
     * Lingue coperte (ISO 639-3). Solo Greeting ne porta una; l'aggregazione
     * ricorsiva vive in Section.getLanguages().
     */
    Set<String> getLanguages();

    /** Default difensivo: solo Section fa override. */
    default boolean isContainer() {
        return false;
    }

    /**
     * Rappresentazione testuale (multi-riga per i contenitori). Ritorna una
     * String e non stampa: l'I/O resta confinato in ConsoleWriter.
     */
    String render(String indent);
}
