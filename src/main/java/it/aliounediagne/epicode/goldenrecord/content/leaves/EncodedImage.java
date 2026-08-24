package it.aliounediagne.epicode.goldenrecord.content.leaves;

import it.aliounediagne.epicode.goldenrecord.content.ContentType;

/**
 * Composite - "Leaf": una delle 115 immagini.
 *
 * Nel 1977 un'immagine non era incidibile su disco analogico, quindi ognuna
 * e stata convertita in suono (tre frame R/G/B per quelle a colori). Per
 * questo anche le immagini si misurano in secondi, e getPlaybackTime() resta
 * una somma ricorsiva senza conversioni di unita.
 */
public class EncodedImage extends AbstractContent {

    private final String theme;
    private final String colourMode; // MONO (1 frame) oppure COLOUR (3 frame)

    public EncodedImage(String id, String title, int duration, String theme, String colourMode) {
        super(id, title, duration, ContentType.IMAGE);
        this.theme = requireText(theme, "theme");
        this.colourMode = colourMode == null ? "MONO" : colourMode.trim();
    }

    @Override
    public ContentType getType() {
        return ContentType.IMAGE;
    }

    /** 3 per le immagini a colori, 1 per le monocromatiche. */
    public int getFrames() {
        return "COLOUR".equalsIgnoreCase(colourMode) ? 3 : 1;
    }

    public String getTheme()      { return theme; }
    public String getColourMode() { return colourMode; }
}
