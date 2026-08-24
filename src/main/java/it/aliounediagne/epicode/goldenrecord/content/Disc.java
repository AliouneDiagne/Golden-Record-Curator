package it.aliounediagne.epicode.goldenrecord.content;

import it.aliounediagne.epicode.goldenrecord.iterator.PlaybackIterator;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Composite - radice dell'albero: E-UNA Section piu la capacita fisica del
 * disco e quella del singolo lato. I suoi figli sono le Section "Side 1" e
 * "Side 2".
 *
 * Iterator: implementando Iterable, un for-each visita l'intero albero
 * ({@code for (Content c : disc)}) senza che il chiamante scriva ricorsione.
 * Vedi EngravableContents per la variante "solo foglie".
 */
public class Disc extends Section implements Iterable<Content> {

    private final int totalCapacitySeconds;
    private final int sideCapacitySeconds;

    public Disc(String id, String title, int totalCapacitySeconds, int sideCapacitySeconds) {
        super(id, title, totalCapacitySeconds);
        this.totalCapacitySeconds = totalCapacitySeconds;
        this.sideCapacitySeconds = sideCapacitySeconds;
    }

    /** Secondi ancora disponibili sul groove (negativi se sforiamo). */
    public int getRemainingSeconds() {
        return totalCapacitySeconds - getPlaybackTime();
    }

    /** Vero quando la somma ricorsiva delle durate supera la capacita. */
    public boolean isOverBudget() {
        return getPlaybackTime() > totalCapacitySeconds;
    }

    /** Rapporto di riempimento, usato dai comandi budget/curate. */
    public double getFillRatio() {
        return (double) getPlaybackTime() / totalCapacitySeconds;
    }

    /** Le Section di primo livello, usate da 'budget', 'validate' e 'curate'. */
    public List<Section> getSides() {
        List<Section> sides = new ArrayList<>();
        for (Content child : getChildren()) {
            if (child instanceof Section) {
                sides.add((Section) child);
            }
        }
        return sides;
    }

    public int getTotalCapacitySeconds() { return totalCapacitySeconds; }
    public int getSideCapacitySeconds()  { return sideCapacitySeconds; }

    /**
     * Hook di Iterable: il for-each visita in ordine di incisione. La logica
     * di traversata sta in PlaybackIterator, non qui.
     */
    @Override
    public Iterator<Content> iterator() {
        return new PlaybackIterator(this);
    }
}
