/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.expression.operators.relational;

import static org.junit.jupiter.api.Assertions.*;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.test.TestUtils;
import org.junit.jupiter.api.Test;

/**
 *
 * @author Tobias Warneke (t.warneke@gmx.net)
 */
public class LikeExpressionTest {

    @Test
    public void testLikeNotIssue660() {
        LikeExpression instance = new LikeExpression();
        assertFalse(instance.isNot());
        assertTrue(instance.withNot(true).isNot());
    }

    @Test
    public void testSetEscapeAndGetStringExpression() throws JSQLParserException {
        LikeExpression instance =
                (LikeExpression) CCJSqlParserUtil.parseExpression("name LIKE 'J%$_%'");
        // escape character should be $
        Expression instance2 = new StringValue("$");
        instance.setEscape(instance2);

        // match all records with names that start with letter ’J’ and have the ’_’ character in
        // them
        assertEquals("name LIKE 'J%$_%' ESCAPE '$'", instance.toString());
    }

    @Test
    void testNotRLikeIssue1553() throws JSQLParserException {
        String sqlStr = "select * from test where id  not rlike '111'";
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);
    }

    @Test
    void testDuckDBSimuilarTo() throws JSQLParserException {
        String sqlStr = "SELECT v\n"
                + "    FROM strings\n"
                + "    WHERE v SIMILAR TO 'San* [fF].*'\n"
                + "    ORDER BY v;";
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);
    }

    @Test
    public void testMatchAny() throws JSQLParserException {
        TestUtils.assertSqlCanBeParsedAndDeparsed(
                "select * from dual where v MATCH_ANY 'keyword1 keyword2'", true);
        TestUtils.assertSqlCanBeParsedAndDeparsed(
                "select * from dual where v NOT MATCH_ANY 'keyword1 keyword2'", true);
    }

    @Test
    public void testMatchAll() throws JSQLParserException {
        TestUtils.assertSqlCanBeParsedAndDeparsed(
                "select * from dual where v MATCH_ALL 'keyword1 keyword2'", true);
        TestUtils.assertSqlCanBeParsedAndDeparsed(
                "select * from dual where v NOT MATCH_ALL 'keyword1 keyword2'", true);
    }

    @Test
    public void testMatchPhrase() throws JSQLParserException {
        TestUtils.assertSqlCanBeParsedAndDeparsed(
                "select * from dual where v MATCH_PHRASE 'keyword1 keyword2'", true);
        TestUtils.assertSqlCanBeParsedAndDeparsed(
                "select * from dual where v NOT MATCH_PHRASE 'keyword1 keyword2'", true);
    }

    @Test
    public void testMatchPhrasePrefix() throws JSQLParserException {
        TestUtils.assertSqlCanBeParsedAndDeparsed(
                "select * from dual where v MATCH_PHRASE_PREFIX 'keyword1 keyword2'", true);
        TestUtils.assertSqlCanBeParsedAndDeparsed(
                "select * from dual where v NOT MATCH_PHRASE_PREFIX 'keyword1 keyword2'", true);
    }

    @Test
    public void testMatchRegexp() throws JSQLParserException {
        TestUtils.assertSqlCanBeParsedAndDeparsed(
                "select * from dual where v MATCH_REGEXP 'keyword1 keyword2'", true);
        TestUtils.assertSqlCanBeParsedAndDeparsed(
                "select * from dual where v NOT MATCH_REGEXP 'keyword1 keyword2'", true);
    }
}
