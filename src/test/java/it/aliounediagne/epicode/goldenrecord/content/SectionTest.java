package it.aliounediagne.epicode.goldenrecord.content;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Test;

import it.aliounediagne.epicode.goldenrecord.content.leaves.Greeting;
import it.aliounediagne.epicode.goldenrecord.exceptions.CyclicStructureException;


public class SectionTest {

    private Section ancient;

    @Before
    public void setUp() {
        ancient = new Section("grt-grp-ancient", "Ancient Tongues", null);
        ancient.addContent(greeting("grt-sux", "Sumerian", 3));
        ancient.addContent(greeting("grt-akk", "Akkadian", 3));
        ancient.addContent(greeting("grt-hit", "Hittite", 2));
        ancient.addContent(greeting("grt-arc", "Aramaic", 2));
        ancient.addContent(greeting("grt-lat", "Latin", 9));
        ancient.addContent(greeting("grt-grc", "Greek", 8));
    }

    @Test
    public void testPlaybackTimeIsTheRecursiveSum() {
        assertEquals(27, ancient.getPlaybackTime());
    }

    @Test
    public void testSectionDoesNotCountItself() {
        Section greetings = new Section("sec-greetings", "Greetings", null);
        greetings.addContent(ancient);

        assertEquals(6, greetings.getItemCount());
        assertEquals(27, greetings.getPlaybackTime());
    }


    @Test
    public void testLanguagesAreAggregatedWithoutDuplicates() {
        assertEquals(6, ancient.getLanguages().size());
        assertTrue(ancient.getLanguages().contains("sux"));
    }


    @Test
    public void testChildrenListCannotBeModifiedFromOutside() {
        try {
            ancient.getChildren().clear();
            fail("The children list should not be modifiable from outside");
        } catch (UnsupportedOperationException expected) {
            
        }
    }


    @Test
    public void testAddingAnAncestorIsRefused() {
        Section greetings = new Section("sec-greetings", "Greetings", null);
        greetings.addContent(ancient);

        try {
            ancient.addContent(greetings); 
            fail("Adding an ancestor should have been refused");
        } catch (CyclicStructureException expected) {
            
        }
    }


    @Test
    public void testAddingItselfIsRefused() {
        try {
            ancient.addContent(ancient);
            fail("Adding a section to itself should have been refused");
        } catch (CyclicStructureException expected) {
            
        }
    }

    
    private Greeting greeting(String id, String name, int seconds) {
        return new Greeting(id, name, seconds, id.substring(4),
                "TEST_FAMILY", "ANCIENT", "test text");
    }
}
