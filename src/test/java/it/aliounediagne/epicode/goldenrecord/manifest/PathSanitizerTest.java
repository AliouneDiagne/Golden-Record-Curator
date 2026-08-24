package it.aliounediagne.epicode.goldenrecord.manifest;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Before;
import org.junit.Test;

import it.aliounediagne.epicode.goldenrecord.exceptions.InsecurePathException;


public class PathSanitizerTest {

    private PathSanitizer sanitizer;

    @Before
    public void setUp() {
        sanitizer = new PathSanitizer(Paths.get("data"));
    }

    @Test
    public void testAFileInsideTheBaseFolderIsAccepted() {
        Path resolved = sanitizer.sanitize("voyager-1977.csv");
        assertTrue(resolved.toString().endsWith("voyager-1977.csv"));
    }

    @Test
    public void testTraversalWithDotDotIsRefused() {
        try {
            sanitizer.sanitize("../../etc/passwd");
            fail("A path traversal attempt should have been refused");
        } catch (InsecurePathException expected) {
            
        }
    }

    @Test
    public void testAbsolutePathIsRefused() {
        try {
            sanitizer.sanitize("/etc/passwd");
            fail("An absolute path should have been refused");
        } catch (InsecurePathException expected) {
            
        }
    }

    @Test
    public void testTraversalHiddenInTheMiddleIsRefused() {
        try {
            sanitizer.sanitize("manifests/../../../etc/passwd");
            fail("A hidden traversal should have been refused");
        } catch (InsecurePathException expected) {
            
        }
    }
}
