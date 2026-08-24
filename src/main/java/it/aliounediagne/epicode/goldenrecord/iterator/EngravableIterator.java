package it.aliounediagne.epicode.goldenrecord.iterator;

import it.aliounediagne.epicode.goldenrecord.content.Content;
import it.aliounediagne.epicode.goldenrecord.content.Section;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Iterator - variante "solo foglie": stesso albero di PlaybackIterator ma
 * senza le sezioni. Serve a DiscValidator (regola di formato) e al comando
 * 'stats'. Due iteratori sullo stesso Composite, che non e cambiato di una
 * riga per ospitare il secondo.
 *
 * Il prossimo elemento e calcolato in anticipo (lookahead): senza, hasNext()
 * mentirebbe quando dopo un ramo di sole sezioni non resta nessuna foglia.
 */
public class EngravableIterator implements Iterator<Content> {

    private final List<Content> stack = new ArrayList<Content>();
    private Content lookahead;

    public EngravableIterator(Content root) {
        if (root == null) {
            throw new IllegalArgumentException("Cannot iterate over a null content");
        }
        stack.add(root);
        advance();
    }

    @Override
    public boolean hasNext() {
        return lookahead != null;
    }

    @Override
    public Content next() {
        if (lookahead == null) {
            throw new NoSuchElementException("No more engravable contents!");
        }
        Content result = lookahead;
        advance();
        return result;
    }

    /**
     * Consuma lo stack finche non trova una foglia (nuovo lookahead) o finche
     * lo stack e vuoto (lookahead null, fine).
     */
    private void advance() {
        lookahead = null;
        while (!stack.isEmpty()) {
            Content current = stack.remove(stack.size() - 1);

            if (current instanceof Section) {
                // Figli al contrario per preservare l'ordine, come in
                // PlaybackIterator.
                List<Content> children = ((Section) current).getChildren();
                for (int i = children.size() - 1; i >= 0; i--) {
                    stack.add(children.get(i));
                }
            } else {
                lookahead = current;
                return;
            }
        }
    }
}
