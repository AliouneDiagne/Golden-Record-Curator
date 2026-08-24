package it.aliounediagne.epicode.goldenrecord.content.leaves;

import it.aliounediagne.epicode.goldenrecord.content.ContentType;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Composite - "Leaf": uno dei 55 saluti incisi sul disco (quello italiano
 * dice "Tanti auguri e saluti.").
 *
 * E' l'unico tipo di contenuto che porta una lingua, quindi l'unico a fare
 * override di getLanguages(): una Section che aggrega i figli non sa - ne
 * deve sapere - quale di loro rispondera con qualcosa.
 */
public class Greeting extends AbstractContent {

    private final String languageCode;   // ISO 639-3, es. "ita"
    private final String languageFamily; // es. "INDO_EUROPEAN"
    private final String era;            // "ANCIENT" o "LIVING"
    private final String spokenText;     // frase pronunciata sul disco

    public Greeting(String id, String title, int duration,
            String languageCode, String languageFamily, String era, String spokenText) {

        // super(...) per primo: la base valida i campi ereditati.
        super(id, title, duration, ContentType.GREETING);

        this.languageCode = requireText(languageCode, "languageCode");
        this.languageFamily = requireText(languageFamily, "languageFamily");
        this.era = era == null ? "LIVING" : era.trim();
        this.spokenText = spokenText == null ? "" : spokenText.trim();
    }

    @Override
    public ContentType getType() {
        return ContentType.GREETING;
    }

    /**
     * Solo i Greeting contribuiscono alla copertura linguistica. Il Set esce
     * unmodifiable per non esporre lo stato interno.
     */
    @Override
    public Set<String> getLanguages() {
        Set<String> single = new HashSet<>();
        single.add(languageCode);
        return Collections.unmodifiableSet(single);
    }

    /** Override: il saluto rende anche il testo parlato (comando 'play'). */
    @Override
    public String render(String indent) {
        return indent + "- " + getTitle() + " [" + languageCode + "] ("
                + formatTime(getPlaybackTime()) + ")  \"" + spokenText + "\"";
    }

    public String getLanguageCode()   { return languageCode; }
    public String getLanguageFamily() { return languageFamily; }
    public String getEra()            { return era; }
    public String getSpokenText()     { return spokenText; }
}
