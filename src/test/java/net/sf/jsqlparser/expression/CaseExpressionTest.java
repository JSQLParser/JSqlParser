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
    public void testCaseExclamationWhen() throws JSQLParserException {
        TestUtils.assertExpressionCanBeParsedAndDeparsed("CASE true WHEN !true THEN 1 ELSE 2 END", true);
    }

    @Test
    public void testCaseNotWhen() throws JSQLParserException {
        TestUtils.assertExpressionCanBeParsedAndDeparsed("CASE true WHEN NOT true THEN 1 ELSE 2 END", true);
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
    public void testCaseExclamationSwitch() throws JSQLParserException {
        TestUtils.assertExpressionCanBeParsedAndDeparsed("CASE !true WHEN true THEN 1 ELSE 2 END", true);
    }

    @Test
    public void testCaseNotSwitch() throws JSQLParserException {
        TestUtils.assertExpressionCanBeParsedAndDeparsed("CASE NOT true WHEN true THEN 1 ELSE 2 END", true);
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

    @Test
    public void testCaseInsideBrackets() throws JSQLParserException {
        String sqlStr = "SELECT ( CASE\n"
                        + "            WHEN something\n"
                        + "                THEN CASE\n"
                        + "                     WHEN something2\n"
                        + "                         THEN 1\n"
                        + "                     ELSE 0\n"
                        + "                     END + 1\n"
                        + "            ELSE 0\n"
                        + "        END ) + 1 \n"
                        + "FROM test";

        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);

        sqlStr = "SELECT\n"
                 + "(CASE WHEN FIELD_A=0 THEN FIELD_B\n"
                 + "WHEN FIELD_C >FIELD_D  THEN (CASE WHEN FIELD_A>0 THEN\n"
                 + "(FIELD_B)/(FIELD_A/(DATEDIFF(DAY,:started,:end)+1))\n"
                 + "ELSE 0 END)-FIELD_D ELSE 0 END)*FIELD_A/(DATEDIFF(DAY,:started,:end)+1)  AS UNNECESSARY_COMPLEX_EXPRESSION\n"
                 + "FROM TEST";

        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);
    }
}
