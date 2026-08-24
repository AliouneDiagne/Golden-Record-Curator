package it.aliounediagne.epicode.goldenrecord.iterator;

import it.aliounediagne.epicode.goldenrecord.content.Content;

import java.util.Iterator;

/**
 * Wrapper Iterable che rende EngravableIterator usabile in un for-each
 * ({@code for (Content c : new EngravableContents(disc))}).
 *
 * iterator() crea una nuova istanza a ogni chiamata, cosi due for-each
 * annidati non interferiscono.
 */
public class EngravableContents implements Iterable<Content> {

    private final Content root;

    public EngravableContents(Content root) {
        this.root = root;
    }

    @Override
    public Iterator<Content> iterator() {
        return new EngravableIterator(root);
    }
}
