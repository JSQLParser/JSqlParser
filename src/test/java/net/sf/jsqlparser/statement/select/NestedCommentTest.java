/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.select;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.AbstractJSqlParser.Dialect;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.*;

class NestedCommentTest {

    private void assertParses(String sql) {
        assertDoesNotThrow(() -> CCJSqlParserUtil.parse(sql),
                "Failed to parse: " + sql);
    }

    @Test
    void testFlatBlockComment() {
        assertParses("SELECT /* simple comment */ 1");
    }

    @Test
    void testNestedBlockComment() {
        assertParses("SELECT /* outer /* inner */ outer */ 1");
    }

    @Test
    void testDeeplyNestedBlockComment() {
        assertParses(
                "SELECT /* level 0 /* level 1 /* level 2 */ back to 1 */ back to 0 */ 1");
    }

    @Test
    void testNestedCommentInWhereClause() {
        assertParses(
                "SELECT * FROM t WHERE /* a /* nested */ comment */ x = 1");
    }

    @Test
    void testNestedCommentContainingStars() {
        assertParses("SELECT /* ** /* * */ ** */ 1");
    }

    @Test
    void testNestedCommentContainingSlashes() {
        assertParses("SELECT /* // /* -- */ // */ 1");
    }

    @Test
    void testMultipleNestedCommentsInSequence() {
        assertParses("SELECT /* /* a */ */ 1, /* /* b */ */ 2");
    }

    @Test
    @Disabled
    void testNestedCommentWithSQL() {
        // Common use case: commenting out code that already contains comments
        assertParses(
                "SELECT * FROM t WHERE 1 = 1\n"
                        + "/* commented out:\n"
                        + "   AND x = /* default */ 42\n"
                        + "   AND y = 0\n"
                        + "*/");
    }

    @Test
    void testEmptyNestedComment() {
        assertParses("SELECT /* /**/ */ 1");
    }

    @Test
    void testLineCommentStillWorks() {
        assertParses("SELECT 1 -- line comment");
    }

    @Test
    void testLineCommentInsideBlockComment() {
        assertParses("SELECT /* -- not a line comment */ 1");
    }

    @Test
    void testMultilineNestedComment() {
        assertParses(
                "SELECT *\n"
                        + "/*\n"
                        + "  /*\n"
                        + "    nested across lines\n"
                        + "  */\n"
                        + "*/\n"
                        + "FROM t");
    }

    @Test
    void testOracleHintPreserved() {
        assertParses("SELECT /*+ FULL(t) */ * FROM t");
    }

    private static String parse(String sql, Dialect dialect) throws JSQLParserException {
        return CCJSqlParserUtil.parse(sql, p -> p.withDialect(dialect)).toString();
    }

    @ParameterizedTest
    @EnumSource(value = Dialect.class,
            names = {"MYSQL", "MARIADB", "ORACLE", "SQLITE", "BIGQUERY", "SPANNER"})
    void testBlockCommentEndsAtFirstDelimiterWhereTheServerDoes(Dialect dialect)
            throws JSQLParserException {
        assertFalse(dialect.isNestedBlockComments());
        assertEquals("SELECT 1 + 2", parse("SELECT 1 /* a /* b */ + 2 -- */", dialect));
        assertEquals("SELECT 1 + 2", parse("SELECT 1 /* a /* b */ + 2", dialect));
        assertEquals("SELECT 1", parse("SELECT 1 /* a /* b */ /* c */", dialect));
        assertThrows(JSQLParserException.class, () -> parse("SELECT 1 /* a /* b */ */", dialect));
        assertThrows(JSQLParserException.class, () -> parse("SELECT 1 /* a /* b", dialect));
    }

    @ParameterizedTest
    @EnumSource(value = Dialect.class, names = {"ANSI_SQL", "POSTGRESQL", "SQLSERVER", "H2"})
    void testBlockCommentNestsWhereTheServerDoes(Dialect dialect) throws JSQLParserException {
        assertTrue(dialect.isNestedBlockComments());
        assertEquals("SELECT 1", parse("SELECT 1 /* a /* b */ + 2 -- */", dialect));
        assertEquals("SELECT 1 + 2", parse("SELECT 1 /* a /* b */ c */ + 2", dialect));
        assertThrows(JSQLParserException.class, () -> parse("SELECT 1 /* a /* b */", dialect));
    }

    @Test
    void testBlockCommentNestsWithoutDialect() throws JSQLParserException {
        assertEquals("SELECT 1",
                CCJSqlParserUtil.parse("SELECT 1 /* a /* b */ + 2 -- */").toString());
    }
}
