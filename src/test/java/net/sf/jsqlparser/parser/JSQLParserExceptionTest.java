/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.parser;

import net.sf.jsqlparser.JSQLParserException;





import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;



import org.junit.jupiter.api.Test;

/**
 *
 * @author schwitters
 */
public class JSQLParserExceptionTest {
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
