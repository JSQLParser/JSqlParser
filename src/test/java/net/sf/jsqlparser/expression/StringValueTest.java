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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

import java.time.Duration;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.AbstractJSqlParser.Dialect;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.alter.Alter;
import net.sf.jsqlparser.statement.comment.Comment;
import net.sf.jsqlparser.statement.create.table.ColumnOption;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.test.TestUtils;
import net.sf.jsqlparser.util.deparser.ExpressionDeParser;
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
    void testPostgresUnicodeEscapeStrings() throws JSQLParserException {
        StringValue value = literal("U&'\\0041BC \\00e9'", Dialect.POSTGRESQL);
        assertEquals(StringValue.Escaping.POSTGRESQL_UNICODE_ESCAPE, value.getEscaping());
        assertEquals("U&", value.getPrefix());
        assertEquals("\\0041BC \\00e9", value.getValue());
        assertNull(value.getUnicodeEscapeCharacter());
        assertEquals("ABC \u00e9", value.getNotExcapedValue());

        StringValue custom = literal("U&'d!0061t!+000061' UESCAPE '!'", Dialect.POSTGRESQL);
        assertEquals("d!0061t!+000061", custom.getValue());
        assertEquals(Character.valueOf('!'), custom.getUnicodeEscapeCharacter());
        assertEquals("data", custom.getNotExcapedValue());
        assertEquals("U&'d!0061t!+000061' UESCAPE '!'", custom.toString());

        String[][] cases = {
                {"U&'\\+01F600'", "\uD83D\uDE00"},
                {"U&'a\\\\b'", "a\\b"},
                {"U&'it''s'", "it's"},
                // a surrogate pair as two escapes, in either form
                {"u&'\\D83D\\DE00'", "\uD83D\uDE00"},
                {"U&'\\d83d\\+00de00'", "\uD83D\uDE00"},
                {"U&'caf\\00E9 \\+00AC00 \uD55C\uAE00'", "caf\u00e9 \uAC00 \uD55C\uAE00"},
                // a doubled escape character stands for itself; other backslashes stay
                {"U&'a!!b\\n!0021' UESCAPE '!'", "a!b\\n!"},
                {"U&'\\' UESCAPE '!'", "\\"},
                {"U&'#0041' uescape '#'", "A"},
                {"U&'x' UESCAPE '\\'", "x"},
                {"U&''", ""}};
        for (String[] c : cases) {
            assertEquals(c[1], literal(c[0], Dialect.POSTGRESQL).getNotExcapedValue(), c[0]);
        }
    }

    @Test
    void testPostgresUnicodeEscapeStringsRoundTrip() throws JSQLParserException {
        for (String sql : new String[] {
                "COMMENT ON TABLE t IS U&'\\0041BC \\00e9'",
                "COMMENT ON TABLE t IS U&'d!0061t!+000061' UESCAPE '!'",
                "CREATE TABLE t (a text DEFAULT U&'\\+01F600', b text DEFAULT U&'it''s')",
                "ALTER TABLE t ALTER COLUMN a SET DEFAULT U&'a\\\\b'",
                "INSERT INTO t VALUES (U&'#0041' UESCAPE '#', U&'x')",
                "SELECT * FROM t WHERE a = U&'\\00e9' AND b LIKE U&'%!0041' UESCAPE '!'"}) {
            Statement statement = TestUtils.assertSqlCanBeParsedAndDeparsed(sql, false,
                    parser -> parser.withDialect(Dialect.POSTGRESQL));
            Statement reparsed = CCJSqlParserUtil.parse(statement.toString(),
                    parser -> parser.withDialect(Dialect.POSTGRESQL));
            assertEquals(sql, reparsed.toString());
        }
        Comment comment = (Comment) CCJSqlParserUtil.parse(
                "COMMENT ON TABLE t IS U&'d!0061t!+000061' UESCAPE '!'",
                parser -> parser.withDialect(Dialect.POSTGRESQL));
        assertEquals("data", comment.getComment().getNotExcapedValue());
        // a continuation line belongs to the same Unicode escape string, as on the server
        StringValue continued = literal("U&'\\0041'\n'\\0042'", Dialect.POSTGRESQL);
        assertEquals("AB", continued.getNotExcapedValue());
        assertEquals("U&'\\0041\\0042'", continued.toString());
    }

    @Test
    void testConstructedUnicodeEscapeStrings() {
        StringValue value = new StringValue("U&'!0041' UESCAPE '!'");
        assertEquals("U&", value.getPrefix());
        assertEquals("!0041", value.getValue());
        assertEquals(StringValue.Escaping.POSTGRESQL_UNICODE_ESCAPE, value.getEscaping());
        assertEquals("A", value.getNotExcapedValue());
        assertEquals("U&'!0041' UESCAPE '!'", value.toString());
        assertEquals("U&'!0041'", value.withUnicodeEscapeCharacter(null).toString());
        assertEquals("!0041", value.getNotExcapedValue());

        StringValue built = new StringValue("'#00e9'").withPrefix("U&")
                .withEscaping(StringValue.Escaping.POSTGRESQL_UNICODE_ESCAPE)
                .withUnicodeEscapeCharacter('#');
        assertEquals("U&'#00e9' UESCAPE '#'", built.toString());
        assertEquals("\u00e9", built.getNotExcapedValue());
        ExpressionDeParser deParser = new ExpressionDeParser();
        built.accept(deParser, null);
        assertEquals("U&'#00e9' UESCAPE '#'", deParser.getBuilder().toString());

        assertThrows(IllegalArgumentException.class,
                () -> new StringValue("U&'\\00'").getNotExcapedValue());
        assertThrows(IllegalArgumentException.class,
                () -> new StringValue("U&'x' UESCAPE '+'").getNotExcapedValue());
        // without the escaping a U& prefix only renders
        assertEquals("\\0041", new StringValue("'\\0041'").withPrefix("U&").getNotExcapedValue());
    }

    @Test
    void testUnicodeUnescapingIsLinear() {
        int escapes = 500_000;
        StringBuilder sb = new StringBuilder(13 * escapes + 4).append("U&'");
        for (int i = 0; i < escapes; i++) {
            sb.append("\\0041\\+01F600");
        }
        StringValue instance = new StringValue(sb.append('\'').toString());

        String result = assertTimeoutPreemptively(Duration.ofSeconds(2),
                instance::getNotExcapedValue);

        assertEquals(3 * escapes, result.length());
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
