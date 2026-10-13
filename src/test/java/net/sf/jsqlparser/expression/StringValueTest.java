/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.expression;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

import java.time.Duration;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.AbstractJSqlParser.Dialect;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.alter.Alter;
import net.sf.jsqlparser.statement.create.table.ColumnOption;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.test.TestUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 *
 * @author toben
 */
public class StringValueTest {

    @Test
    public void testGetValue() {
        StringValue instance = new StringValue("'*'");
        String expResult = "*";
        String result = instance.getValue();
        assertEquals(expResult, result);
    }

    @Test
    public void testGetValue2_issue329() {
        StringValue instance = new StringValue("*");
        String expResult = "*";
        String result = instance.getValue();
        assertEquals(expResult, result);
    }

    /**
     * Test of getNotExcapedValue method, of class StringValue.
     */
    @Test
    public void testGetNotExcapedValue() {
        StringValue instance = new StringValue("'*''*'");
        String expResult = "*'*";
        String result = instance.getNotExcapedValue();
        assertEquals(expResult, result);
    }

    @Test
    public void testGetNotExcapedValueLargeInputIsLinear() {
        // a string literal made of many escaped single quotes used to be unescaped with a
        // deleteCharAt() per pair, which is quadratic and lets a modest literal burn CPU
        int pairs = 1_000_000;
        StringBuilder sb = new StringBuilder(2 * pairs + 2);
        sb.append('\'');
        for (int i = 0; i < pairs; i++) {
            sb.append("''");
        }
        sb.append('\'');
        StringValue instance = new StringValue(sb.toString());

        String result = assertTimeoutPreemptively(Duration.ofSeconds(2),
                instance::getNotExcapedValue);

        assertEquals(pairs, result.length());
        for (int i = 0; i < result.length(); i++) {
            assertEquals('\'', result.charAt(i));
        }
    }

    private static StringValue literal(String sql, Dialect dialect) throws JSQLParserException {
        PlainSelect select = (PlainSelect) CCJSqlParserUtil.parse("SELECT " + sql,
                parser -> {
                    if (dialect != null) {
                        parser.withDialect(dialect);
                    }
                });
        return (StringValue) select.getSelectItem(0).getExpression();
    }

    @Test
    void testMySqlBackslashEscapes() throws JSQLParserException {
        for (Dialect dialect : new Dialect[] {Dialect.MYSQL, Dialect.MARIADB}) {
            StringValue value = literal("'it\\'s a\\\\b'", dialect);
            assertEquals(StringValue.Escaping.MYSQL_BACKSLASH, value.getEscaping());
            assertEquals("it\\'s a\\\\b", value.getValue());
            assertEquals("it's a\\b", value.getNotExcapedValue());
            assertEquals("it's", literal("'it''s'", dialect).getNotExcapedValue());
        }
        assertEquals("x\ny\tz\0\u001A\b\r",
                literal("'x\\ny\\tz\\0\\Z\\b\\r'", Dialect.MYSQL).getNotExcapedValue());
        // \% and \_ keep their backslash; other escaped characters stand for themselves
        assertEquals("a\\%b\\_cq\"", literal("'a\\%b\\_c\\q\\\"'", Dialect.MYSQL)
                .getNotExcapedValue());
        assertEquals("it's", literal("N'it\\'s'", Dialect.MYSQL).getNotExcapedValue());
    }

    @Test
    void testDoubledQuoteFollowsTheQuoteCharacter() throws JSQLParserException {
        assertEquals("it\"s", literal("\"it\"\"s\"", Dialect.MYSQL).getNotExcapedValue());
        assertEquals("it''s", literal("\"it''s\"", Dialect.MYSQL).getNotExcapedValue());
    }

    @Test
    void testBackslashEscapesWithoutDialect() throws JSQLParserException {
        PlainSelect select = (PlainSelect) CCJSqlParserUtil.parse("SELECT 'it\\'s'",
                parser -> parser.withBackslashEscapeCharacter(true));
        StringValue value = (StringValue) select.getSelectItem(0).getExpression();
        assertEquals(StringValue.Escaping.MYSQL_BACKSLASH, value.getEscaping());
        assertEquals("it's", value.getNotExcapedValue());

        StringValue standard = literal("'a\\nb'", null);
        assertEquals(StringValue.Escaping.STANDARD, standard.getEscaping());
        assertEquals("a\\nb", standard.getNotExcapedValue());
    }

    @Test
    void testPostgresEscapeStrings() throws JSQLParserException {
        StringValue standard = literal("'a\\nb'", Dialect.POSTGRESQL);
        assertEquals(StringValue.Escaping.STANDARD, standard.getEscaping());
        assertEquals("a\\nb", standard.getNotExcapedValue());

        StringValue escape = literal("E'it\\'s\\tx\\\\'", Dialect.POSTGRESQL);
        assertEquals(StringValue.Escaping.POSTGRESQL_ESCAPE_STRING, escape.getEscaping());
        assertEquals("it's\tx\\", escape.getNotExcapedValue());
        assertEquals("it's", literal("E'it''s'", Dialect.POSTGRESQL).getNotExcapedValue());
        // octal and hexadecimal escapes are bytes of the UTF-8 encoding
        assertEquals("AAéé café 😀 q", literal(
                "E'\\x41\\101\\303\\251\\xC3\\xA9 caf\\u00e9 \\U0001F600 \\q'",
                Dialect.POSTGRESQL).getNotExcapedValue());
        assertEquals("it''s\\n", literal("$$it''s\\n$$", Dialect.POSTGRESQL)
                .getNotExcapedValue());
    }

    @Test
    void testOtherBackslashDialectsKeepTheirSource() throws JSQLParserException {
        for (Dialect dialect : new Dialect[] {Dialect.BIGQUERY, Dialect.DATABRICKS,
                Dialect.SNOWFLAKE}) {
            StringValue value = literal("'it\\'s'", dialect);
            assertEquals(StringValue.Escaping.STANDARD, value.getEscaping());
            assertEquals("it\\'s", value.getNotExcapedValue());
        }
    }

    @Test
    void testConstructedValuesDefaultToStandardEscaping() {
        StringValue value = new StringValue("'a\\n''b'");
        assertEquals(StringValue.Escaping.STANDARD, value.getEscaping());
        assertEquals("a\\n'b", value.getNotExcapedValue());
        assertEquals("a\n'b",
                value.withEscaping(StringValue.Escaping.MYSQL_BACKSLASH).getNotExcapedValue());
        value.setEscaping(null);
        assertEquals(StringValue.Escaping.STANDARD, value.getEscaping());
    }

    @Test
    void testColumnDefaultValueIsUnescaped() throws JSQLParserException {
        for (String literal : new String[] {"'it''s'", "'it\\'s'"}) {
            Alter alter = (Alter) CCJSqlParserUtil.parse(
                    "ALTER TABLE t ADD COLUMN s VARCHAR(10) DEFAULT " + literal,
                    parser -> parser.withDialect(Dialect.MYSQL));
            ColumnOption option = alter.getAlterExpressions().get(0).getColDataTypeList().get(0)
                    .getColumnOptions().get(0);
            assertEquals("it's",
                    ((StringValue) option.getDefaultExpression()).getNotExcapedValue());
            assertEquals("ALTER TABLE t ADD COLUMN s VARCHAR (10) DEFAULT " + literal,
                    alter.toString());
        }
    }

    @Test
    void testBackslashUnescapingIsLinear() {
        int pairs = 1_000_000;
        StringBuilder sb = new StringBuilder(2 * pairs + 2).append('\'');
        for (int i = 0; i < pairs; i++) {
            sb.append("\\'");
        }
        StringValue instance = new StringValue(sb.append('\'').toString())
                .withEscaping(StringValue.Escaping.MYSQL_BACKSLASH);

        String result = assertTimeoutPreemptively(Duration.ofSeconds(2),
                instance::getNotExcapedValue);

        assertEquals(pairs, result.length());
    }

    @Test
    public void testPrefixes() {
        checkStringValue("E'test'", "test", "E");
        checkStringValue("'test'", "test", null);

    }

    private void checkStringValue(String original, String expectedValue, String expectedPrefix) {
        StringValue v = new StringValue(original);
        assertEquals(expectedValue, v.getValue());
        assertEquals(expectedPrefix, v.getPrefix());
    }

    @Test
    public void testIssue1566EmptyStringValue() {
        StringValue v = new StringValue("'");
        assertEquals("'", v.getValue());
    }

    @Test
    public void testOracleAlternativeQuoting() throws JSQLParserException {
        String sqlStr = "COMMENT ON COLUMN EMP.NAME IS q'{Na'm\\e}'";
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);

        sqlStr = "COMMENT ON COLUMN EMP.NAME IS q'(Na'm\\e)'";
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);

        sqlStr = "COMMENT ON COLUMN EMP.NAME IS q'[Na'm\\e]'";
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);

        sqlStr = "COMMENT ON COLUMN EMP.NAME IS q''Na'm\\e]''";
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);

        sqlStr = "select q'{Its good!}' from dual";
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);

        sqlStr = "select q'{It's good!}' from dual";
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);
    }

    @Test
    public void testParseInput_BYTEA() throws Exception {
        String sqlStr = "VALUES (X'', X'01FF', X'01 bc 2a', X'01' '02')";
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);
    }

    @Test
    void testDollarQuotesIssue2267() throws JSQLParserException {
        String sqlStr = "SELECT $$this is a string$$, test, 'text' FROM tbl;";
        PlainSelect select = (PlainSelect) TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);

        Assertions.assertInstanceOf(StringValue.class, select.getSelectItem(0).getExpression());
    }

    @Test
    void testDollarQuotesWithDollarSignsInside() throws JSQLParserException {
        String sqlStr = "SELECT $$this references $1 and costs $5$$ FROM tbl;";
        PlainSelect select = (PlainSelect) TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);

        Assertions.assertInstanceOf(StringValue.class, select.getSelectItem(0).getExpression());
    }
}
