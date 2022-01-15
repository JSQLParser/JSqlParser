/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2022 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.expression;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.test.TestUtils;
import org.junit.jupiter.api.Test;

/**
 * @author Mathieu Goeminne
 */
public class CaseExpressionTest {
    @Test
    public void testSimpleCase() throws JSQLParserException {
        TestUtils.assertExpressionCanBeParsedAndDeparsed("CASE true WHEN true THEN 1 ELSE 2 END", true);
    }

    @Test
    public void testCaseBinaryAndWhen() throws JSQLParserException {
        TestUtils.assertExpressionCanBeParsedAndDeparsed("CASE true WHEN true & false THEN 1 ELSE 2 END", true);
    }

    @Test
    public void testCaseBinaryOrWhen() throws JSQLParserException {
        TestUtils.assertExpressionCanBeParsedAndDeparsed("CASE true WHEN true | false THEN 1 ELSE 2 END", true);
    }

    @Test
    public void testCaseAndWhen() throws JSQLParserException {
        TestUtils.assertExpressionCanBeParsedAndDeparsed("CASE true WHEN true AND false THEN 1 ELSE 2 END", true);
    }

    @Test
    public void testCaseOrWhen() throws JSQLParserException {
        TestUtils.assertExpressionCanBeParsedAndDeparsed("CASE true WHEN true OR false THEN 1 ELSE 2 END", true);
    }

    @Test
    public void testCaseBinaryAndSwitch() throws JSQLParserException {
        TestUtils.assertExpressionCanBeParsedAndDeparsed("CASE true & false WHEN true THEN 1 ELSE 2 END", true);
    }

    @Test
    public void testCaseBinaryOrSwitch() throws JSQLParserException {
        TestUtils.assertExpressionCanBeParsedAndDeparsed("CASE true | false WHEN true THEN 1 ELSE 2 END", true);
    }

    @Test
    public void testCaseAndSwitch() throws JSQLParserException {
        TestUtils.assertExpressionCanBeParsedAndDeparsed("CASE true AND false WHEN true THEN 1 ELSE 2 END", true);
    }

    @Test
    public void testCaseOrSwitch() throws JSQLParserException {
        TestUtils.assertExpressionCanBeParsedAndDeparsed("CASE true OR false WHEN true THEN 1 ELSE 2 END", true);
    }
}
