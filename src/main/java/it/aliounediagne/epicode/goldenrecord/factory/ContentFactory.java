package it.aliounediagne.epicode.goldenrecord.factory;

import it.aliounediagne.epicode.goldenrecord.content.Content;
import it.aliounediagne.epicode.goldenrecord.content.ContentType;
import it.aliounediagne.epicode.goldenrecord.content.Section;
import it.aliounediagne.epicode.goldenrecord.content.leaves.EarthSound;
import it.aliounediagne.epicode.goldenrecord.content.leaves.EncodedImage;
import it.aliounediagne.epicode.goldenrecord.content.leaves.Greeting;
import it.aliounediagne.epicode.goldenrecord.content.leaves.MusicTrack;
import it.aliounediagne.epicode.goldenrecord.manifest.ManifestRecord;
import it.aliounediagne.epicode.goldenrecord.exceptions.UnsupportedContentTypeException;

/**
 * Factory Method: la classe da istanziare dipende dalla colonna "type", nota
 * solo a runtime, mentre "new" pretende il nome della classe a compile time.
 *
 * Concentrando qui la scelta, il lettore CSV resta disaccoppiato dal dominio:
 * restituisce ManifestRecord grezzi e non conosce le quattro foglie concrete.
 * Aggiungere una quinta foglia costa un enum constant, una classe e un ramo
 * di questo switch: nessun metodo esistente viene riaperto (open/closed).
 */
public class ContentFactory {

    /**
     * Crea l'istanza Content corretta a partire da una riga del manifest.
     *
     * @param record dati grezzi letti dal CSV (mai null)
     * @return il Content concreto - Section, Greeting, MusicTrack, EarthSound
     *         oppure EncodedImage
     * @throws UnsupportedContentTypeException se il tipo non e riconosciuto
     */
    public Content createContent(ManifestRecord record) {

        // Il parsing e delegato all'enum: e li che vive la regola.
        ContentType type = ContentType.parse(record.getRawType());

        switch (type) {
            case SECTION:
                return new Section(record.getId(), record.getTitle(),
                        record.getCapacitySeconds());

            case GREETING:
                return new Greeting(
                        record.getId(),
                        record.getTitle(),
                        record.getDurationSeconds(),
                        record.getLanguageCode(),
                        record.getLanguageFamily(),
                        record.getEra(),
                        record.getDetail());

            case MUSIC:
                // Builder: il costruttore di MusicTrack e private, questa e
                // l'unica via di costruzione.
                return MusicTrack.builder()
                        .withId(record.getId())
                        .withTitle(record.getTitle())
                        .withDuration(record.getDurationSeconds())
                        .withComposer(record.getDetail())
                        .withPerformer(record.getExtra())
                        .withOriginRegion(record.getRegion())
                        .withTradition(record.getLanguageFamily())
                        .build();

            case EARTH_SOUND:
                return new EarthSound(record.getId(), record.getTitle(),
                        record.getDurationSeconds(), record.getRegion(), record.getDetail());

            case IMAGE:
                return new EncodedImage(record.getId(), record.getTitle(),
                        record.getDurationSeconds(), record.getRegion(), record.getExtra());

            default:
                // Rete di sicurezza se all'enum viene aggiunto un valore e
                // questo switch non viene aggiornato.
                throw new UnsupportedContentTypeException(type.name());
        }
    }
}
