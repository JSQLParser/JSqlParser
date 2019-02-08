package net.sf.jsqlparser.parser;

import net.sf.jsqlparser.JSQLParserException;
import org.junit.After;
import org.junit.AfterClass;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author schwitters
 */
public class JSQLParserExceptionTest {

    public JSQLParserExceptionTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of parseExpression method, of class CCJSqlParserUtil.
     */
    @Test
    public void testExceptionWithCause() throws Exception {
        IllegalArgumentException arg1 = new IllegalArgumentException();
        JSQLParserException ex1=new JSQLParserException("", arg1);
        assertSame(arg1, ex1.getCause());
    }
    @Test
    public void testExceptionPrintStacktrace() throws Exception {
        IllegalArgumentException arg1 = new IllegalArgumentException("BRATKARTOFFEL");
        JSQLParserException ex1=new JSQLParserException("", arg1);
        StringWriter sw = new StringWriter();
        ex1.printStackTrace(new PrintWriter(sw, true));
        assertTrue(sw.toString().contains("BRATKARTOFFEL"));
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ex1.printStackTrace(new PrintStream(bos, true));
        assertTrue(new String(bos.toByteArray(), StandardCharsets.UTF_8).contains("BRATKARTOFFEL"));

    }

    @Test
    public void testExceptionPrintStacktraceNoCause() throws Exception {
        JSQLParserException ex1=new JSQLParserException("", null);
        StringWriter sw = new StringWriter();
        ex1.printStackTrace(new PrintWriter(sw, true));
        assertFalse(sw.toString().contains("BRATKARTOFFEL"));
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ex1.printStackTrace(new PrintStream(bos, true));
        assertFalse(new String(bos.toByteArray(), StandardCharsets.UTF_8).contains("BRATKARTOFFEL"));
    }
    @Test
    public void testExceptionDefaultContructorCauseInit() throws Exception {
        JSQLParserException ex1=new JSQLParserException();
        assertNull(ex1.getCause());
        ex1=new JSQLParserException((Throwable) null);
        assertNull(ex1.getCause());
    }

}
