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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.ExpressionVisitorAdapter;
import net.sf.jsqlparser.parser.AbstractJSqlParser.Dialect;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.test.TestUtils;
import net.sf.jsqlparser.util.TablesNamesFinder;
import net.sf.jsqlparser.util.deparser.ExpressionDeParser;
import net.sf.jsqlparser.util.deparser.SelectDeParser;
import net.sf.jsqlparser.util.deparser.StatementDeParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class MySqlSelectModifierTest {
    private static PlainSelect parse(String sql, Dialect dialect) throws JSQLParserException {
        return (PlainSelect) CCJSqlParserUtil.parse(sql, parser -> parser.withDialect(dialect));
    }

    @ParameterizedTest
    @EnumSource(MySqlSelectModifier.class)
    void separatesModifiersFromColumnsAndAliases(MySqlSelectModifier modifier) throws Exception {
        String sql = "SELECT " + modifier + " a FROM t";
        for (Dialect dialect : List.of(Dialect.MYSQL, Dialect.MARIADB)) {
            PlainSelect select = parse(sql, dialect);
            assertEquals(Set.of(modifier), select.getMySqlSelectModifiers());
            SelectItem<?> item = select.getSelectItem(0);
            assertEquals("a", ((Column) item.getExpression()).getColumnName());
            assertNull(item.getAlias());
            assertEquals(Set.of("t"), new TablesNamesFinder<>().getTables((Statement) select));
            assertRoundTrip(select, sql, dialect);
        }
        PlainSelect generic = (PlainSelect) CCJSqlParserUtil.parse(sql);
        assertEquals(Set.of(modifier), generic.getMySqlSelectModifiers());
        assertNull(generic.getSelectItem(0).getAlias());
    }

    @Test
    void combinesDistinctAndDocumentedModifiers() throws Exception {
        String sql = "SELECT DISTINCT HIGH_PRIORITY STRAIGHT_JOIN SQL_SMALL_RESULT SQL_BIG_RESULT "
                + "SQL_BUFFER_RESULT SQL_NO_CACHE SQL_CALC_FOUND_ROWS a FROM t LIMIT 1";
        PlainSelect select = parse(sql, Dialect.MYSQL);
        EnumSet<MySqlSelectModifier> expected = EnumSet.allOf(MySqlSelectModifier.class);
        expected.remove(MySqlSelectModifier.SQL_CACHE);
        assertEquals(expected, select.getMySqlSelectModifiers());
        assertTrue(select.getMySqlHintStraightJoin());
        assertTrue(select.getMySqlSqlCalcFoundRows());
        assertEquals(MySqlSqlCacheFlags.SQL_NO_CACHE, select.getMySqlSqlCacheFlag());
        assertRoundTrip(select, sql, Dialect.MYSQL);
        assertRoundTrip(parse("SELECT ALL HIGH_PRIORITY SQL_BUFFER_RESULT a FROM t", Dialect.MYSQL),
                "SELECT HIGH_PRIORITY SQL_BUFFER_RESULT a FROM t", Dialect.MYSQL);
    }

    @Test
    void acceptsModifiersBeforeAndAfterDistinctOrAll() throws Exception {
        for (String sql : List.of(
                "SELECT HIGH_PRIORITY DISTINCT SQL_SMALL_RESULT SQL_BUFFER_RESULT SQL_NO_CACHE a FROM t",
                "SELECT SQL_SMALL_RESULT HIGH_PRIORITY DISTINCT SQL_BUFFER_RESULT SQL_NO_CACHE a FROM t",
                "SELECT DISTINCT HIGH_PRIORITY SQL_SMALL_RESULT SQL_BUFFER_RESULT SQL_NO_CACHE a FROM t")) {
            PlainSelect select = parse(sql, Dialect.MYSQL);
            assertTrue(select.getDistinct() != null);
            assertNull(select.getSelectItem(0).getAlias());
            assertRoundTrip(select,
                    "SELECT DISTINCT HIGH_PRIORITY SQL_SMALL_RESULT SQL_BUFFER_RESULT SQL_NO_CACHE a FROM t",
                    Dialect.MYSQL);
        }
        assertRoundTrip(parse("SELECT SQL_SMALL_RESULT ALL a FROM t", Dialect.MYSQL),
                "SELECT SQL_SMALL_RESULT a FROM t", Dialect.MYSQL);
    }

    @Test
    void preservesLegacyAccessorsAndBuilders() throws Exception {
        PlainSelect select = parse("SELECT STRAIGHT_JOIN SQL_CACHE SQL_CALC_FOUND_ROWS a FROM t",
                Dialect.MYSQL);
        select.setMySqlSqlCacheFlag(MySqlSqlCacheFlags.SQL_NO_CACHE);
        assertFalse(select.getMySqlSelectModifiers().contains(MySqlSelectModifier.SQL_CACHE));
        select.setMySqlHintStraightJoin(false);
        select.setMySqlSqlCalcFoundRows(false);
        assertEquals(Set.of(MySqlSelectModifier.SQL_NO_CACHE), select.getMySqlSelectModifiers());
        select.setMySqlSqlCacheFlag(null);
        assertTrue(select.getMySqlSelectModifiers().isEmpty());
        select.withMySqlHintStraightJoin(true).withMySqlSqlCalcFoundRows(true)
                .withMySqlSqlNoCache(MySqlSqlCacheFlags.SQL_CACHE)
                .addMySqlSelectModifiers(MySqlSelectModifier.HIGH_PRIORITY);
        assertRoundTrip(select,
                "SELECT HIGH_PRIORITY STRAIGHT_JOIN SQL_CACHE SQL_CALC_FOUND_ROWS a FROM t",
                Dialect.MYSQL);
        select.setMySqlSelectModifiers(select.getMySqlSelectModifiers());
        assertEquals(4, select.getMySqlSelectModifiers().size());
        select.withMySqlSelectModifiers(Set.of(MySqlSelectModifier.SQL_BIG_RESULT));
        assertFalse(select.getMySqlHintStraightJoin());
        assertFalse(select.getMySqlSqlCalcFoundRows());
        assertNull(select.getMySqlSqlCacheFlag());
        select.setMySqlSelectModifiers(null);
        assertTrue(select.getMySqlSelectModifiers().isEmpty());
        select.setMySqlSqlCacheFlag(MySqlSqlCacheFlags.SQL_CACHE);
        assertThrows(IllegalArgumentException.class,
                () -> select.addMySqlSelectModifiers(MySqlSelectModifier.SQL_NO_CACHE));
        assertEquals(Set.of(MySqlSelectModifier.SQL_CACHE), select.getMySqlSelectModifiers());
    }

    @Test
    void keepsOtherDialectAndQuotedOrQualifiedIdentifiers() throws Exception {
        for (String modifier : List.of("HIGH_PRIORITY", "SQL_SMALL_RESULT", "SQL_BIG_RESULT",
                "SQL_BUFFER_RESULT")) {
            for (Dialect dialect : List.of(Dialect.POSTGRESQL, Dialect.ORACLE, Dialect.SQLSERVER)) {
                PlainSelect select = parse("SELECT " + modifier + " a FROM t", dialect);
                assertTrue(select.getMySqlSelectModifiers().isEmpty());
                assertEquals(modifier,
                        ((Column) select.getSelectItem(0).getExpression()).getColumnName());
                assertEquals("a", select.getSelectItem(0).getAlias().getName());
            }
            for (String expression : List.of("`" + modifier + "`", "t." + modifier,
                    modifier + ".a")) {
                PlainSelect select = parse("SELECT " + expression + " FROM t", Dialect.MYSQL);
                assertTrue(select.getMySqlSelectModifiers().isEmpty());
                assertEquals(expression, select.getSelectItem(0).getExpression().toString());
            }
        }
        PlainSelect identifier = parse("SELECT SQL_BUFFER_RESULT FROM t", Dialect.MYSQL);
        assertTrue(identifier.getMySqlSelectModifiers().isEmpty());
        assertEquals("SQL_BUFFER_RESULT",
                ((Column) identifier.getSelectItem(0).getExpression()).getColumnName());
    }

    @Test
    void visitsAndRewritesActualProjectionWithoutTreatingModifiersAsColumns() throws Exception {
        PlainSelect select = parse("SELECT HIGH_PRIORITY SQL_SMALL_RESULT a FROM t", Dialect.MYSQL);
        List<String> columns = new ArrayList<>();
        select.accept(new SelectVisitorAdapter<Void>(new ExpressionVisitorAdapter<Void>() {
            @Override
            public <S> Void visit(Column column, S context) {
                columns.add(column.getColumnName());
                return null;
            }
        }), null);
        assertEquals(List.of("a"), columns);
        StringBuilder output = new StringBuilder();
        ExpressionDeParser expressions = new ExpressionDeParser() {
            @Override
            public <S> StringBuilder visit(Column column, S context) {
                return getBuilder().append("renamed_").append(column.getColumnName());
            }
        };
        SelectDeParser selects = new SelectDeParser(expressions, output);
        expressions.setSelectVisitor(selects);
        expressions.setBuilder(output);
        select.accept((SelectVisitor<StringBuilder>) selects, null);
        assertEquals("SELECT HIGH_PRIORITY SQL_SMALL_RESULT renamed_a FROM t", output.toString());
    }

    @Test
    void keepsNestedQueriesAndStatementBoundaries() throws Exception {
        String sql = "SELECT SQL_SMALL_RESULT a FROM "
                + "(SELECT SQL_BIG_RESULT a FROM t) s";
        PlainSelect outer = parse(sql, Dialect.MYSQL);
        PlainSelect inner = ((ParenthesedSelect) outer.getFromItem()).getPlainSelect();
        assertEquals(Set.of(MySqlSelectModifier.SQL_SMALL_RESULT), outer.getMySqlSelectModifiers());
        assertEquals(Set.of(MySqlSelectModifier.SQL_BIG_RESULT), inner.getMySqlSelectModifiers());
        assertRoundTrip(outer, sql, Dialect.MYSQL);
        assertEquals(2, CCJSqlParserUtil.parseStatements(sql + "; SELECT HIGH_PRIORITY b FROM u;",
                parser -> parser.withDialect(Dialect.MYSQL)).size());
    }

    @Test
    void rejectsConflictingCacheModesAndDistinctQualifiers() {
        for (String sql : List.of("SELECT SQL_CACHE SQL_NO_CACHE a FROM t",
                "SELECT SQL_NO_CACHE SQL_CACHE a FROM t",
                "SELECT DISTINCT ALL a FROM t", "SELECT ALL DISTINCT a FROM t",
                "SELECT ALL HIGH_PRIORITY DISTINCT a FROM t",
                "SELECT HIGH_PRIORITY DISTINCT SQL_SMALL_RESULT ALL a FROM t")) {
            assertThrows(JSQLParserException.class, () -> parse(sql, Dialect.MYSQL), sql);
        }
    }

    private static void assertRoundTrip(PlainSelect select, String expected, Dialect dialect)
            throws Exception {
        assertEquals(expected, select.toString());
        StringBuilder output = new StringBuilder();
        select.accept(new StatementDeParser(output), null);
        assertEquals(expected, output.toString());
        for (String rendered : List.of(select.toString(), output.toString())) {
            PlainSelect reparsed = parse(rendered, dialect);
            assertEquals(select.getMySqlSelectModifiers(), reparsed.getMySqlSelectModifiers());
            assertEquals(select.getSelectItems().toString(), reparsed.getSelectItems().toString());
        }
        TestUtils.assertDeparse(select, expected);
    }
}
