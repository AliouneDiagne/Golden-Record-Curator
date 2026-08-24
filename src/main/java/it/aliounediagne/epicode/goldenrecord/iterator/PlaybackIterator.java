package it.aliounediagne.epicode.goldenrecord.iterator;

import it.aliounediagne.epicode.goldenrecord.content.Content;
import it.aliounediagne.epicode.goldenrecord.content.Section;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Iterator: attraversamento depth-first dell'albero in ordine di incisione,
 * sezioni comprese (usato dal comando 'tree' e dal for-each su Disc).
 *
 * Lo stack e esplicito e non ricorsivo perche un Iterator e pull-based: fra
 * due next() la traversata deve restare sospesa, e un metodo ricorsivo non
 * puo esserlo. Tenere la traversata fuori da Section evita di dare al
 * Composite una seconda responsabilita: aggiungere EngravableIterator non ha
 * richiesto di toccarlo.
 *
 * ArrayList usata come stack: add() e remove(size-1) sono entrambe O(1).
 */
public class PlaybackIterator implements Iterator<Content> {

    private final List<Content> stack = new ArrayList<Content>();

    public PlaybackIterator(Content root) {
        if (root == null) {
            throw new IllegalArgumentException("Cannot iterate over a null content");
        }
        stack.add(root);
    }

    @Override
    public boolean hasNext() {
        return !stack.isEmpty();
    }

    @Override
    public Content next() {
        if (!hasNext()) {
            throw new NoSuchElementException("No more contents in this tree!");
        }

        Content current = stack.remove(stack.size() - 1);

        if (current instanceof Section) {
            List<Content> children = ((Section) current).getChildren();

            // I figli entrano al contrario: cosi il primo finisce in cima allo
            // stack e l'ordine di visita resta quello del manifest.
            for (int i = children.size() - 1; i >= 0; i--) {
                stack.add(children.get(i));
            }
        }
        return current;
    }
}
