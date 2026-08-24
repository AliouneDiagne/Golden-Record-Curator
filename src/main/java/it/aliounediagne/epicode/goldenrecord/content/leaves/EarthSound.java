package it.aliounediagne.epicode.goldenrecord.content.leaves;

import it.aliounediagne.epicode.goldenrecord.content.ContentType;

/**
 * Composite - "Leaf": uno dei 21 segmenti del montaggio "Sounds of Earth"
 * (tuono, canto della balena, un battito cardiaco, un Saturn V).
 *
 * Aggiunge solo categoria e nota: validazione, render e item count arrivano
 * da AbstractContent.
 */
public class EarthSound extends AbstractContent {

    private final String category; // COSMIC, GEOPHYSICAL, ANIMAL, HUMAN, TECHNOLOGICAL
    private final String note;

    public EarthSound(String id, String title, int duration, String category, String note) {
        super(id, title, duration, ContentType.EARTH_SOUND);
        this.category = requireText(category, "category");
        this.note = note == null ? "" : note.trim();
    }

    @Override
    public ContentType getType() {
        return ContentType.EARTH_SOUND;
    }

    public String getCategory() { return category; }
    public String getNote()     { return note; }
}
