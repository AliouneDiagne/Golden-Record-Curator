package it.aliounediagne.epicode.goldenrecord.cli;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import it.aliounediagne.epicode.goldenrecord.exceptions.InsecurePathException;
import it.aliounediagne.epicode.goldenrecord.exceptions.Result;
import it.aliounediagne.epicode.goldenrecord.exceptions.SafeExecutor;

public class ExceptionShieldingTest {

    private SafeExecutor executor;

    @Before
    public void setUp() {
        executor = new SafeExecutor();
    }

    @Test
    public void testDomainFailureLeaksNothing() {

        Result<String> result = executor.execute("TEST", () -> {
            throw new InsecurePathException("../../etc/passwd", "/etc/passwd");
        });

        assertFalse("the operation should have failed", result.isSuccess());

        String shown = result.getUserMessage();
        assertNotNull(shown);

        
        assertFalse("leaks a class name", shown.contains("Exception"));
        assertFalse("leaks a package", shown.contains("it.aliounediagne"));
        assertFalse("leaks a file path", shown.contains("/"));
        assertFalse("leaks the offending value", shown.contains("passwd"));

        assertNotNull("no correlation code", result.getTraceId());
        assertTrue(result.getTraceId().startsWith("ERR-"));
    }


    @Test
    public void testUnexpectedFailureLeaksNothing() {

        Result<String> result = executor.execute("TEST", () -> {
            throw new NullPointerException("internal detail that must not escape");
        });

        assertFalse(result.isSuccess());

        String shown = result.getUserMessage();
        assertFalse(shown.contains("NullPointer"));
        assertFalse(shown.contains("internal detail"));
        assertNotNull(result.getTraceId());
    }


    @Test
    public void testSuccessCarriesTheValue() {
        Result<String> result = executor.execute("TEST", () -> "all good");

        assertTrue(result.isSuccess());
        assertTrue("all good".equals(result.getValue()));
    }

    @Test
    public void testTraceIdsAreUnique() {
        Result<String> first = executor.execute("A", () -> {
            throw new IllegalStateException("boom");
        });
        Result<String> second = executor.execute("B", () -> {
            throw new IllegalStateException("boom");
        });

        assertFalse(first.getTraceId().equals(second.getTraceId()));
    }
}
