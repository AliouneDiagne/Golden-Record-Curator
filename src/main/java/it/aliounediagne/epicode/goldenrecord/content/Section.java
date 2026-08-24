package it.aliounediagne.epicode.goldenrecord.content;

import it.aliounediagne.epicode.goldenrecord.exceptions.CyclicStructureException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Composite - ruolo "Composite": un contenitore di Content che a sua volta
 * E-UN Content, quindi puo contenere altre Section a qualsiasi profondita.
 *
 * Composizione e non ereditarieta: una Section HA-UNA lista di figli, cosi
 * espone solo i metodi che vogliamo (niente clear()/addAll() ereditati).
 * "children" e private e final e getChildren() ne restituisce una vista
 * read-only: l'unica porta di modifica resta addContent(), dove stanno i
 * controlli.
 */
public class Section implements Content {

    private final String id;
    private final String title;
    private final Integer capacitySeconds; // null se la sezione non ha budget proprio

    // ArrayList: l'ordine di incisione fa parte del dato, le letture sono
    // frequenti e in ordine, le scritture rare.
    private final List<Content> children = new ArrayList<>();

    // Usato solo dal guardiano di ciclo per risalire alla radice.
    Section parent;

    public Section(String id, String title, Integer capacitySeconds) {
        this.id = id;
        this.title = title;
        this.capacitySeconds = capacitySeconds;
    }

    // =======================================================================
    // LE TRE AGGREGAZIONI RICORSIVE - cuore del Composite
    // =======================================================================

    /**
     * Caso ricorsivo: il dynamic dispatch sceglie a runtime l'implementazione
     * del figlio, quindi non serve alcun "if" fra contenitore e foglia. La
     * condizione di terminazione e la forma del dato: le foglie.
     */
    @Override
    public int getPlaybackTime() {
        int total = 0;
        for (Content child : children) {
            total += child.getPlaybackTime();
        }
        return total;
    }

    /** Una Section non conta se stessa: e un raggruppamento, non si incide. */
    @Override
    public int getItemCount() {
        int count = 0;
        for (Content child : children) {
            count += child.getItemCount();
        }
        return count;
    }

    /**
     * TreeSet per unicita (la stessa lingua puo apparire in piu rami) e per
     * ordine stabile: il report di copertura deve essere deterministico.
     */
    @Override
    public Set<String> getLanguages() {
        Set<String> languages = new TreeSet<>();
        for (Content child : children) {
            languages.addAll(child.getLanguages());
        }
        return Collections.unmodifiableSet(languages);
    }

    // =======================================================================
    // IDENTITA
    // =======================================================================

    @Override public String getId()          { return id; }
    @Override public String getTitle()       { return title; }
    @Override public ContentType getType()   { return ContentType.SECTION; }

    /** L'unico posto in cui il default dell'interface viene sovrascritto. */
    @Override public boolean isContainer()   { return true; }

    /** @return il budget della sezione, o null se non ne ha uno proprio */
    public Integer getCapacitySeconds()      { return capacitySeconds; }

    /**
     * Rendering ricorsivo: la sezione rende se stessa, poi chiede ai figli di
     * rendersi piu indentati. StringBuilder perche il sottoalbero puo essere
     * grande.
     */
    @Override
    public String render(String indent) {
        StringBuilder out = new StringBuilder();
        out.append(indent).append("+ ").append(title)
                .append("  [").append(formatTime(getPlaybackTime()))
                .append(", ").append(getItemCount()).append(" items]");
        for (Content child : children) {
            out.append('\n').append(child.render(indent + "  "));
        }
        return out.toString();
    }

    // =======================================================================
    // MODIFICA CONTROLLATA - l'unica porta di ingresso alla lista
    // =======================================================================

    /**
     * L'unico ingresso a "children": qui vivono tutti i controlli.
     *
     * @throws CyclicStructureException se l'inserimento creerebbe un ciclo
     */
    public void addContent(Content child) {
        if (child == null) {
            throw new IllegalArgumentException("Cannot add a null content");
        }
        guardAgainstCycle(child);
        children.add(child);

        if (child instanceof Section) {
            ((Section) child).parent = this;
        }
    }

    public boolean removeContent(Content child) {
        boolean removed = children.remove(child);
        if (removed && child instanceof Section) {
            ((Section) child).parent = null;
        }
        return removed;
    }

    /** Vista read-only: ogni modifica deve passare da addContent(). */
    public List<Content> getChildren() {
        return Collections.unmodifiableList(children);
    }

    // =======================================================================
    // PROTEZIONE DAI CICLI - il punto debole di ogni Composite
    // =======================================================================

    /**
     * Il Composite assume un albero, ma nulla nel linguaggio lo garantisce:
     * un anello manderebbe le tre aggregazioni in ricorsione infinita fino a
     * StackOverflowError. Risaliamo la catena dei parent (costo: la profondita
     * dell'albero) invece di visitare tutto il sottoalbero.
     */
    private void guardAgainstCycle(Content candidate) {
        // '==' e non equals(): la domanda e "stesso oggetto?".
        if (candidate == this) {
            throw new CyclicStructureException(id, id);
        }
        // Solo una Section puo chiudere un anello: una foglia non ha figli.
        if (candidate instanceof Section) {
            Section incoming = (Section) candidate;
            Section ancestor = this;
            while (ancestor != null) {
                if (ancestor == incoming) {
                    throw new CyclicStructureException(id, incoming.getId());
                }
                ancestor = ancestor.parent;
            }
        }
    }

    /** mm:ss - ripetuto in AbstractContent per non accoppiare le classi. */
    protected static String formatTime(int seconds) {
        return seconds / 60 + ":" + String.format("%02d", seconds % 60);
    }
}
