package it.aliounediagne.epicode.goldenrecord.iterator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import org.junit.Before;
import org.junit.Test;

import it.aliounediagne.epicode.goldenrecord.content.Content;
import it.aliounediagne.epicode.goldenrecord.content.Section;
import it.aliounediagne.epicode.goldenrecord.content.leaves.Greeting;

public class PlaybackIteratorTest {

    private Section greetings;

    @Before
    public void setUp() {
        Section ancient = new Section("ancient", "Ancient", null);
        ancient.addContent(greeting("sux", "Sumerian", 3));
        ancient.addContent(greeting("akk", "Akkadian", 3));

        Section living = new Section("living", "Living", null);
        living.addContent(greeting("ita", "Italian", 3));

        greetings = new Section("greetings", "Greetings", null);
        greetings.addContent(ancient);
        greetings.addContent(living);
    }


    @Test
    public void testEngravingOrderIsRespected() {
        List<String> visited = new ArrayList<String>();
        for (Content content : new PlaybackIteratorTestIterable(greetings)) {
            visited.add(content.getTitle());
        }

        assertEquals(6, visited.size());
        assertEquals("Greetings", visited.get(0));
        assertEquals("Ancient", visited.get(1));
        assertEquals("Sumerian", visited.get(2));
        assertEquals("Akkadian", visited.get(3));
        assertEquals("Living", visited.get(4));
        assertEquals("Italian", visited.get(5));
    }

    @Test
    public void testEngravableIteratorReturnsOnlyLeaves() {
        List<String> visited = new ArrayList<String>();
        for (Content content : new EngravableContents(greetings)) {
            visited.add(content.getTitle());
        }

        assertEquals(3, visited.size());
        assertEquals("Sumerian", visited.get(0));
        assertEquals("Akkadian", visited.get(1));
        assertEquals("Italian", visited.get(2));
    }

    @Test
    public void testExhaustedIteratorThrows() {
        PlaybackIterator iterator = new PlaybackIterator(greeting("x", "Alone", 3));
        iterator.next(); 

        assertFalse(iterator.hasNext());
        try {
            iterator.next();
            fail("An exhausted iterator should throw");
        } catch (NoSuchElementException expected) {
            
        }
    }

    private Greeting greeting(String code, String name, int seconds) {
        return new Greeting("grt-" + code, name, seconds, code, "FAM", "LIVING", "text");
    }

    
    private static class PlaybackIteratorTestIterable implements Iterable<Content> {
        private final Content root;

        PlaybackIteratorTestIterable(Content root) {
            this.root = root;
        }

        @Override
        public java.util.Iterator<Content> iterator() {
            return new PlaybackIterator(root);
        }
    }
}
