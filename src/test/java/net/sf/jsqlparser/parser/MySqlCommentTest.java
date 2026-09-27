/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.parser;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayInputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.SignedExpression;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.operators.arithmetic.Addition;
import net.sf.jsqlparser.expression.operators.arithmetic.Subtraction;
import net.sf.jsqlparser.parser.AbstractJSqlParser.Dialect;
import net.sf.jsqlparser.parser.feature.Feature;
import net.sf.jsqlparser.parser.feature.FeatureConfiguration;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.util.deparser.StatementDeParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class MySqlCommentTest {
    private static PlainSelect parse(String sql) throws JSQLParserException {
        return (PlainSelect) CCJSqlParserUtil.parse(sql, p -> p.withDialect(Dialect.MYSQL));
    }

    private static PlainSelect parse(String sql, int version) throws JSQLParserException {
        return (PlainSelect) CCJSqlParserUtil.parse(sql,
                p -> p.withDialect(Dialect.MYSQL).withMySqlServerVersion(version));
    }

    private static void assertOutput(String expected, Statement statement) throws Exception {
        assertEquals(expected, statement.toString());
        StringBuilder output = new StringBuilder();
        statement.accept(new StatementDeParser(output));
        assertEquals(expected, output.toString());
        assertEquals(expected, parse(output.toString()).toString());
    }

    @ParameterizedTest
    @ValueSource(strings = {"SELECT 1--2", "SELECT 1 --2", "SELECT 1- -2",
            "SELECT 1--/*! 2 */"})
    void preservesMinusAndNegativeOperand(String sql) throws Exception {
        PlainSelect select = parse(sql);
        Subtraction subtraction =
                assertInstanceOf(Subtraction.class, select.getSelectItem(0).getExpression());
        assertEquals(1, ((LongValue) subtraction.getLeftExpression()).getValue());
        SignedExpression negative =
                assertInstanceOf(SignedExpression.class, subtraction.getRightExpression());
        assertEquals('-', negative.getSign());
        assertEquals(2, ((LongValue) negative.getExpression()).getValue());
        assertOutput("SELECT 1 - -2", select);
    }

    @ParameterizedTest
    @ValueSource(strings = {" ", "\t", "\r", "\n", "\f", "\u0007", "\u007f"})
    void acceptsWhitespaceOrControlAfterDoubleDash(String separator) throws Exception {
        String sql = "SELECT 1--" + separator;
        if (!separator.equals("\r") && !separator.equals("\n")) {
            sql += "discarded /*! +99 */";
        }
        assertOutput("SELECT 1", parse(sql));
    }

    @Test
    void keepsLineAndBlockCommentsSeparateFromExecutableSql() throws Exception {
        assertOutput("SELECT 1", parse("SELECT 1--"));
        assertOutput("SELECT 1 + 2", parse("SELECT 1 -- ignored\n + 2"));
        assertOutput("SELECT 1", parse("SELECT 1 /* +99 */"));
        assertOutput("SELECT 1", parse("SELECT 1 # /*! +99 */"));
        assertOutput("SELECT 1 + 2", parse("SELECT /*! 1 /* inner */ + 2 */"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"SELECT 1 /*! +2 */", "SELECT 1/*!+2*/",
            "/*!SELECT 1 + 2*/", "SELECT /*!1*/ /*!+*/ /*!2*/",
            "SELECT 1 /*! +\n2 */"})
    void parsesUnversionedCommentContentsAsExpressions(String sql) throws Exception {
        PlainSelect select = parse(sql);
        Addition addition =
                assertInstanceOf(Addition.class, select.getSelectItem(0).getExpression());
        assertEquals(1, ((LongValue) addition.getLeftExpression()).getValue());
        assertEquals(2, ((LongValue) addition.getRightExpression()).getValue());
        assertOutput("SELECT 1 + 2", select);
        addition.setRightExpression(new LongValue(3));
        assertOutput("SELECT 1 + 3", select);
    }

    @Test
    void parsesExecutableSelectModifier() throws Exception {
        PlainSelect select = parse("SELECT /*! STRAIGHT_JOIN */ a FROM t");
        assertTrue(select.getMySqlHintStraightJoin());
        assertEquals("a", select.getSelectItem(0).getExpression().toString());
        assertNull(select.getSelectItem(0).getAlias());
        assertOutput("SELECT STRAIGHT_JOIN a FROM t", select);
    }

    @Test
    void leavesQuotedContentsAndOptimizerHintsIntact() throws Exception {
        PlainSelect quoted = parse("SELECT '/*! +2 */ --x', `a--b`, \"/*! +3 */\"");
        assertEquals("/*! +2 */ --x",
                ((StringValue) quoted.getSelectItem(0).getExpression()).getValue());
        assertEquals("`a--b`", quoted.getSelectItem(1).getExpression().toString());
        assertEquals("/*! +3 */",
                ((StringValue) quoted.getSelectItem(2).getExpression()).getValue());
        assertOutput("SELECT '*/', 2", parse("SELECT /*! '*/' */, 2"));
        PlainSelect hint = parse("SELECT /*+ BKA(t) */ a FROM t");
        assertNotNull(hint.getOracleHint());
        assertOutput(hint.toString(), hint);
    }

    @Test
    void respectsExplicitQuoteAndEscapeOverrides() throws Exception {
        PlainSelect select = (PlainSelect) CCJSqlParserUtil.parse(
                "SELECT /*! \"--x\" */, '/*! text */'",
                p -> p.withDialect(Dialect.MYSQL).withDoubleQuotedStrings(false)
                        .withBackslashEscapeCharacter(false));
        assertInstanceOf(net.sf.jsqlparser.schema.Column.class,
                select.getSelectItem(0).getExpression());
        assertEquals("/*! text */",
                ((StringValue) select.getSelectItem(1).getExpression()).getValue());
        assertOutput("SELECT 'x\\\'/*! text */'", parse("SELECT /*! 'x\\\'/*! text */' */"));
    }

    @Test
    void resolvesConditionalCommentsForAnExplicitTarget() throws Exception {
        String sql = "SELECT 1 /*!90702 +2 */";
        assertOutput("SELECT 1", parse(sql, 90701));
        assertOutput("SELECT 1 + 2", parse(sql, 90702));
        assertOutput("SELECT 1 + 2", parse(sql, 260700));
        assertOutput("SELECT 1", parse("SELECT 1 /*!260700 +2 */", 90702));
        assertOutput("SELECT 1 + 2", parse("SELECT 1 /*!260700 +2 */", 260700));
        assertOutput("SELECT 1 + 2", parse("SELECT 1 /*!090702\t+2 */", 90702));
        assertOutput("SELECT 1 + 2", parse("SELECT 1 /*!90702+2 */", 90702));
        assertOutput("SELECT 1", parse("SELECT 1 /*!99999 not valid SQL */", 90702));
    }

    @Test
    void followsFiveDigitFallbackWhenSixthDigitHasNoFollowingWhitespace() throws Exception {
        assertOutput("SELECT 0 + 2", parse("SELECT /*!100000+2*/", 10000));
        assertOutput("SELECT 1234 + 2", parse("SELECT /*!1234+2*/"));
    }

    @Test
    void requiresVersionWithoutSilentlyFallingBackOrDroppingSql() {
        String sql = "SELECT 1 /*!90702 +2 */";
        JSQLParserException exception = assertThrows(JSQLParserException.class, () -> parse(sql));
        assertTrue(exception.getCause().getMessage().contains("withMySqlServerVersion"));
        assertThrows(JSQLParserException.class, () -> CCJSqlParserUtil.parse(sql,
                p -> p.withDialect(Dialect.MYSQL).withUnsupportedStatements()));
        assertThrows(TokenMgrException.class, () -> new CCJSqlParser(new StreamProvider(
                new StringReader(sql))).withDialect(Dialect.MYSQL).Statement());
        assertThrows(IllegalArgumentException.class,
                () -> CCJSqlParserUtil.newParser(sql).withMySqlServerVersion(-1));
    }

    @ParameterizedTest
    @ValueSource(strings = {"SELECT 1 /*! +2", "SELECT 1 /*!", "SELECT /*! 1",
            "SELECT /*! 1 -- closing marker hidden */"})
    void rejectsUnterminatedExecutableComments(String sql) {
        assertThrows(JSQLParserException.class, () -> parse(sql));
    }

    @Test
    void rejectsNestedExecutableComments() {
        assertThrows(JSQLParserException.class,
                () -> parse("SELECT /*! 1 /*! +99 */ + 2 */"));
    }

    @Test
    void sharesLexingAcrossStringReaderInputStreamAndFeatureConfiguration() throws Exception {
        String sql = "SELECT 1--2 /*!90702 +3 */";
        String expected = "SELECT 1 - -2 + 3";
        assertOutput(expected, parse(sql, 90702));
        assertOutput(expected, new CCJSqlParser(new StringProvider(sql)).withDialect(Dialect.MYSQL)
                .withMySqlServerVersion(90702).Statement());
        assertOutput(expected, new CCJSqlParser(new StreamProvider(new StringReader(sql)))
                .withDialect(Dialect.MYSQL).withMySqlServerVersion(90702).Statement());
        assertOutput(expected, CCJSqlParserUtil.newParser(new ByteArrayInputStream(
                sql.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8.name())
                .withDialect(Dialect.MYSQL).withMySqlServerVersion(90702).Statement());
        FeatureConfiguration configuration = new FeatureConfiguration()
                .setValue(Feature.dialect, Dialect.MYSQL.name())
                .setValue(Feature.mySqlServerVersion, 90702);
        assertOutput(expected,
                CCJSqlParserUtil.newParser(sql).withConfiguration(configuration).Statement());
        assertEquals("1 - -2 + 3", CCJSqlParserUtil.parseExpression("1--2 /*! +3 */", false,
                p -> p.withDialect(Dialect.MYSQL)).toString());
    }

    @Test
    void preservesStatementBoundariesAndOriginalTokenPositions() throws Exception {
        assertEquals(2, CCJSqlParserUtil.parseStatements("/*! SELECT 1 */; SELECT 2;",
                p -> p.withDialect(Dialect.MYSQL)).size());
        assertEquals(2, CCJSqlParserUtil.parseStatements("SELECT 1/*!+2*/; SELECT 3--4;",
                p -> p.withDialect(Dialect.MYSQL)).size());
        CCJSqlParser parser = CCJSqlParserUtil.newParser("/*!1*/--2").withDialect(Dialect.MYSQL);
        Token one = parser.getNextToken();
        assertEquals("1", one.image);
        assertEquals(4, one.beginColumn);
        assertEquals(4, one.absoluteBegin);
        Token minus = parser.getNextToken();
        assertEquals("-", minus.image);
        assertEquals(7, minus.beginColumn);
        assertEquals(7, minus.absoluteBegin);
        assertEquals("-", parser.getNextToken().image);
        assertEquals("2", parser.getNextToken().image);
    }

    @Test
    void handlesBufferExpansionAndStreamReinitialization() throws Exception {
        String sql = "SELECT " + " ".repeat(4090) + "1--2/*!+3*/";
        assertOutput("SELECT 1 - -2 + 3", parse(sql));
        assertOutput("SELECT '" + "x".repeat(9000) + "'",
                parse("SELECT /*! '" + "x".repeat(9000) + "' */"));
        CCJSqlParser parser = CCJSqlParserUtil.newParser("SELECT /*!1*/")
                .withDialect(Dialect.MYSQL);
        assertEquals("SELECT 1", parser.Statement().toString());
        parser.ReInit(new StringProvider("SELECT 2--3"));
        assertEquals("SELECT 2 - -3", parser.Statement().toString());
    }

    @Test
    void rejectsLiteralInternalMinusMarkerInEveryDialect() {
        String sql = "SELECT 1 " + (char) 2 + " 2";
        for (Dialect dialect : Dialect.values()) {
            assertThrows(JSQLParserException.class, () -> CCJSqlParserUtil.parse(sql,
                    p -> p.withDialect(dialect)), dialect.name());
            assertThrows(JSQLParserException.class, () -> CCJSqlParserUtil.parse(sql,
                    p -> p.withDialect(dialect).withUnsupportedStatements()), dialect.name());
        }
    }

    @Test
    void preservesOtherDialectCommentsAndQuotedBodies() throws Exception {
        assertEquals("SELECT 1", CCJSqlParserUtil.parse("SELECT 1--2").toString());
        for (Dialect dialect : Dialect.values()) {
            if (dialect != Dialect.MYSQL) {
                assertEquals("SELECT 1", CCJSqlParserUtil.parse("SELECT 1 /*!90702 +2 */ --3",
                        p -> p.withDialect(dialect)).toString(), dialect.name());
            }
        }
        assertEquals("SELECT $x$/*! --2 */$x$", CCJSqlParserUtil.parse(
                "SELECT $x$/*! --2 */$x$", p -> p.withDialect(Dialect.POSTGRESQL)).toString());
    }
}
