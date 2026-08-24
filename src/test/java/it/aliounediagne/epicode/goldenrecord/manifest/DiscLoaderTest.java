package it.aliounediagne.epicode.goldenrecord.manifest;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import it.aliounediagne.epicode.goldenrecord.content.Disc;
import it.aliounediagne.epicode.goldenrecord.factory.ContentFactory;
import it.aliounediagne.epicode.goldenrecord.manifest.provider.ManifestProvider;


public class DiscLoaderTest {

    private ManifestProvider provider; 
    private DiscLoader loader;

    @Before
    public void setUp() {
        provider = mock(ManifestProvider.class);
        loader = new DiscLoader(provider, new ContentFactory());
    }

    @Test
    public void testFlatRowsBecomeATree() {

        List<ManifestRecord> rows = new ArrayList<ManifestRecord>();
        rows.add(row("root", "", "SECTION", "Golden Record", "", "6600"));
        rows.add(row("side-1", "root", "SECTION", "Side 1", "", "3300"));
        rows.add(greetingRow("grt-ita", "side-1", "Italian", "3", "ita"));
        rows.add(greetingRow("grt-jpn", "side-1", "Japanese", "5", "jpn"));

        when(provider.loadRecords("test.csv")).thenReturn(rows);

        Disc disc = loader.load("test.csv", 6600, 3300);

        assertEquals(2, disc.getItemCount()); 
        assertEquals(8, disc.getPlaybackTime()); 
        assertEquals(2, disc.getLanguages().size());
        assertEquals(1, disc.getSides().size());

        verify(provider).loadRecords("test.csv");
    }

    @Test
    public void testTheIndexFindsContentsById() {
        List<ManifestRecord> rows = new ArrayList<ManifestRecord>();
        rows.add(row("root", "", "SECTION", "Golden Record", "", "6600"));
        rows.add(greetingRow("grt-ita", "root", "Italian", "3", "ita"));

        when(provider.loadRecords("test.csv")).thenReturn(rows);
        loader.load("test.csv", 6600, 3300);

        assertEquals("Italian", loader.getIndex().get("grt-ita").getTitle());
    }

    private ManifestRecord row(String id, String parent, String type,
            String title, String duration, String capacity) {
        return new ManifestRecord(id, parent, type, title, duration, capacity,
                "", "", "", "", "", "");
    }

    private ManifestRecord greetingRow(String id, String parent, String title,
            String duration, String language) {
        return new ManifestRecord(id, parent, "GREETING", title, duration, "",
                language, "TEST_FAMILY", "LIVING", "EUROPE", "spoken text", "");
    }
}
