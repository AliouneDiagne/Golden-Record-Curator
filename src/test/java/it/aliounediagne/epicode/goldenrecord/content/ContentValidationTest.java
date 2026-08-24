package it.aliounediagne.epicode.goldenrecord.content;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

import it.aliounediagne.epicode.goldenrecord.content.leaves.Greeting;
import it.aliounediagne.epicode.goldenrecord.content.leaves.MusicTrack;
import it.aliounediagne.epicode.goldenrecord.exceptions.InvalidContentException;
import it.aliounediagne.epicode.goldenrecord.exceptions.UnsupportedContentTypeException;

public class ContentValidationTest {


    @Test
    public void testNegativeDurationIsRefused() {
        try {
            new Greeting("grt-x", "Test", -30, "xxx", "FAM", "LIVING", "text");
            fail("A negative duration should have been refused");
        } catch (InvalidContentException expected) {
            assertTrue(expected.getDiagnostic().contains("-30"));
        }
    }


    @Test
    public void testBlankTitleIsRefused() {
        try {
            new Greeting("grt-x", "   ", 5, "xxx", "FAM", "LIVING", "text");
            fail("A blank title should have been refused");
        } catch (InvalidContentException expected) {
            
        }
    }

    @Test
    public void testEachTypeEnforcesItsOwnMaximum() {
        assertTrue(ContentType.GREETING.accepts(30));
        assertFalse(ContentType.GREETING.accepts(61));

        assertTrue(ContentType.MUSIC.accepts(440)); 
        assertFalse(ContentType.MUSIC.accepts(601));
    }

    @Test
    public void testUnknownTypeIsRefused() {
        try {
            ContentType.parse("HOLOGRAM");
            fail("An unknown type should have been refused");
        } catch (UnsupportedContentTypeException expected) {
            assertTrue(expected.getDiagnostic().contains("HOLOGRAM"));
        }
    }

    @Test
    public void testBuilderAppliesItsFallbackValues() {
        MusicTrack track = MusicTrack.builder()
                .withId("mus-01")
                .withTitle("Brandenburg Concerto No. 2")
                .withDuration(280)
                .withComposer("Johann Sebastian Bach")
                .build(); 

        assertEquals("Unknown", track.getPerformer());
        assertEquals(280, track.getPlaybackTime());
        assertEquals(1, track.getItemCount()); 
    }
}
