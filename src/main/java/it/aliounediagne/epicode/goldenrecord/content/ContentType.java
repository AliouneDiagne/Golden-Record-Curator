package it.aliounediagne.epicode.goldenrecord.content;

import it.aliounediagne.epicode.goldenrecord.exceptions.UnsupportedContentTypeException;

/**
 * Nature possibili di un Content, con la regola di formato che le riguarda.
 *
 * La regola ("un'immagine dura al piu 30 secondi") vive sull'enum: foglie e
 * DiscValidator chiedono type.accepts(secondi), cosi non c'e un solo
 * "if type == X" sparso nel codice.
 */
public enum ContentType {

    /** Section: contenitore editoriale, non incidibile, senza limiti. */
    SECTION("none", 0, false),

    /** Greeting: audio mono, massimo un minuto. */
    GREETING("audio-mono", 60, true),

    /** MusicTrack: audio stereo, massimo dieci minuti. */
    MUSIC("audio-stereo", 600, true),

    /** EarthSound: audio mono, massimo due minuti. */
    EARTH_SOUND("audio-mono", 120, true),

    /** EncodedImage: video analogico, massimo trenta secondi. */
    IMAGE("video-analogue", 30, true);

    private final String encodingScheme;
    private final int maxSeconds;
    private final boolean engravable;

    ContentType(String encodingScheme, int maxSeconds, boolean engravable) {
        this.encodingScheme = encodingScheme;
        this.maxSeconds = maxSeconds;
        this.engravable = engravable;
    }

    public String getEncodingScheme() { return encodingScheme; }

    /** Distingue foglie (true) da contenitori (false). */
    public boolean isEngravable() { return engravable; }

    /** Regola di formato: la durata e accettabile per questo tipo? */
    public boolean accepts(int durationSeconds) {
        if (!engravable) {
            return true; // le Section non hanno vincolo di durata
        }
        return durationSeconds > 0 && durationSeconds <= maxSeconds;
    }

    /**
     * Legge la colonna "type" del CSV, case-insensitive.
     *
     * @throws UnsupportedContentTypeException se il valore non e riconosciuto
     */
    public static ContentType parse(String raw) {
        if (raw != null) {
            for (ContentType type : values()) {
                if (type.name().equalsIgnoreCase(raw.trim())) {
                    return type;
                }
            }
        }
        throw new UnsupportedContentTypeException(raw);
    }
}
