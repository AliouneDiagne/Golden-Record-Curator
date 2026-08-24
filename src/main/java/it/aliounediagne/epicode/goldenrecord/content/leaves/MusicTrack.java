package it.aliounediagne.epicode.goldenrecord.content.leaves;

import it.aliounediagne.epicode.goldenrecord.content.ContentType;

/**
 * Composite - "Leaf": una delle 27 selezioni musicali. Le durate sono quelle
 * pubblicate dalla NASA e sommano 87:16, la sanity check principale sui dati.
 *
 * Builder: sette campi, tre opzionali. Un costruttore posizionale sarebbe
 * illeggibile e scambiare due String compilerebbe lo stesso; i setter
 * romperebbero l'immutabilita. Il costruttore e private, quindi la Builder
 * fluente e l'unico modo di ottenere un MusicTrack ed e anche il solo posto
 * dove vivono i default. Factory e Builder si compongono: la Factory decide
 * QUALE classe, il Builder COME assemblarla.
 */
public class MusicTrack extends AbstractContent {

    private final String composer;
    private final String performer;
    private final String originRegion;
    private final String tradition; // CLASSICAL, TRADITIONAL o POPULAR

    /** Private: l'oggetto puo essere creato solo tramite la Builder. */
    private MusicTrack(Builder builder) {
        super(builder.id, builder.title, builder.duration, ContentType.MUSIC);
        this.composer = requireText(builder.composer, "composer");
        this.performer = builder.performer;
        this.originRegion = builder.originRegion;
        this.tradition = builder.tradition;
    }

    /** Punto di ingresso del Builder. */
    public static Builder builder() {
        return new Builder();
    }

    @Override
    public ContentType getType() {
        return ContentType.MUSIC;
    }

    /** Override: la traccia rende anche il compositore. */
    @Override
    public String render(String indent) {
        return indent + "- " + getTitle() + " / " + composer
                + " (" + formatTime(getPlaybackTime()) + ")";
    }

    public String getComposer()     { return composer; }
    public String getPerformer()    { return performer; }
    public String getOriginRegion() { return originRegion; }
    public String getTradition()    { return tradition; }

    /**
     * Builder fluente: ogni metodo restituisce "this" per concatenare le
     * chiamate. I campi opzionali sono inizializzati qui, cosi una colonna
     * mancante nel CSV non produce mai un null.
     */
    public static class Builder {
        private String id;
        private String title;
        private int duration;
        private String composer;
        private String performer = "Unknown";      // opzionale
        private String originRegion = "Unknown";   // opzionale
        private String tradition = "TRADITIONAL";  // opzionale

        public Builder withId(String id)              { this.id = id; return this; }
        public Builder withTitle(String title)        { this.title = title; return this; }
        public Builder withDuration(int seconds)      { this.duration = seconds; return this; }
        public Builder withComposer(String composer)  { this.composer = composer; return this; }
        public Builder withPerformer(String performer){ this.performer = performer; return this; }
        public Builder withOriginRegion(String r)     { this.originRegion = r; return this; }
        public Builder withTradition(String tradition){ this.tradition = tradition; return this; }

        /** La validazione avviene nel costruttore di MusicTrack, non qui. */
        public MusicTrack build() {
            return new MusicTrack(this);
        }
    }
}
