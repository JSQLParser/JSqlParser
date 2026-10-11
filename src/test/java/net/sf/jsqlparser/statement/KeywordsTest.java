/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2021 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement;

import java.util.stream.Stream;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.TernaryExpression;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.statement.piped.FromQuery;
import net.sf.jsqlparser.statement.piped.SelectPipeOperator;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.parser.ParserKeywordsUtils;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.SampleClause;
import net.sf.jsqlparser.statement.select.WithItem;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import static net.sf.jsqlparser.test.TestUtils.assertSqlCanBeParsedAndDeparsed;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Verifies that all non-reserved keywords can be used as unquoted identifiers (schema, table,
 * column, alias, function names).
 *
 * @author <a href="mailto:andreas@manticore-projects.com">Andreas Reichel</a>
 */
public class KeywordsTest {

    public static Stream<String> nonReservedKeywords() {
        return ParserKeywordsUtils.getNonReservedKeywords().stream();
    }

    @ParameterizedTest(name = "Keyword {0}")
    @MethodSource("nonReservedKeywords")
    public void testRelObjectNameWithoutValue(String keyword) throws JSQLParserException {
        String sqlStr = String.format("SELECT %1$s.%1$s AS %1$s from %1$s.%1$s AS %1$s", keyword);
        assertSqlCanBeParsedAndDeparsed(sqlStr, true);
    }

    @ParameterizedTest(name = "Keyword {0}")
    @MethodSource("nonReservedKeywords")
    public void testRelObjectNameExt(String keyword) throws JSQLParserException {
        String sqlStr = String.format(
                "SELECT %1$s.%1$s.%1$s \"%1$s\" from %1$s \"%1$s\" ORDER BY %1$s ", keyword);
        assertSqlCanBeParsedAndDeparsed(sqlStr, true);
    }

    @Test
    public void testCombinedTokenKeywords() throws JSQLParserException {
        String sqlStr = "SELECT current_date(3)";
        assertSqlCanBeParsedAndDeparsed(sqlStr, true);
    }

    // #2473: GROUPS is a non-reserved keyword in PostgreSQL and must stay usable as an
    // identifier, although it also introduces the GROUPS window frame unit.
    @Test
    public void testGroupsAsIdentifier() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT groups FROM mytable", true);
        assertSqlCanBeParsedAndDeparsed("SELECT * FROM groups", true);
        assertSqlCanBeParsedAndDeparsed("SELECT a AS groups FROM mytable", true);
        assertSqlCanBeParsedAndDeparsed("SELECT a groups FROM mytable", true);
        assertSqlCanBeParsedAndDeparsed("CREATE TABLE mytable (groups INT)", true);
    }

    // sample and extend are non-reserved in the common dialects and reach the
    // parser as ordinary table and column names in downstream rewriting tools.
    @Test
    public void testSampleExtendAsIdentifier() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT sample, extend FROM mytable", true);
        assertSqlCanBeParsedAndDeparsed("SELECT mytable.extend FROM mytable", true);
        assertSqlCanBeParsedAndDeparsed("SELECT * FROM mytable WHERE extend = 'x'", true);
        assertSqlCanBeParsedAndDeparsed("UPDATE mytable SET sample = 1, extend = 'x' WHERE id = 1",
                true);
        assertSqlCanBeParsedAndDeparsed("INSERT INTO mytable (sample, extend) VALUES (1, 'x')",
                true);
        assertSqlCanBeParsedAndDeparsed("SELECT * FROM sample", true);
        assertSqlCanBeParsedAndDeparsed("DELETE FROM sample WHERE extend = 'x'", true);
        assertSqlCanBeParsedAndDeparsed("INSERT INTO sample (extend) VALUES ('x')", true);
        assertSqlCanBeParsedAndDeparsed("SELECT a AS sample, b AS extend FROM mytable", true);
    }

    @ParameterizedTest
    @ValueSource(strings = {"sample", "extend", "SaMpLe", "ExTeNd"})
    void testSampleExtendImplicitAliases(String keyword) throws JSQLParserException {
        PlainSelect select = (PlainSelect) assertSqlCanBeParsedAndDeparsed(
                "SELECT a " + keyword + " FROM mytable " + keyword, true);
        assertEquals(keyword, select.getSelectItem(0).getAlias().getName());
        assertEquals(keyword, select.getFromItem().getAlias().getName());
        assertEquals("a", assertInstanceOf(Column.class,
                select.getSelectItem(0).getExpression()).getColumnName());
    }

    @ParameterizedTest
    @ValueSource(strings = {"sample", "extend", "SaMpLe", "ExTeNd"})
    void testSampleExtendCte(String keyword) throws JSQLParserException {
        PlainSelect select = (PlainSelect) assertSqlCanBeParsedAndDeparsed(
                "WITH " + keyword + "(x) AS (SELECT 1) SELECT x FROM " + keyword, true);
        WithItem<?> withItem = select.getWithItemsList().get(0);
        assertEquals(keyword, withItem.getAliasName());
        assertNotNull(withItem.getParenthesedStatement());
        assertNull(withItem.getExpression());
        assertEquals(keyword, assertInstanceOf(Table.class, select.getFromItem()).getName());
        assertSqlCanBeParsedAndDeparsed(
                "WITH RECURSIVE " + keyword + "(x) AS (SELECT 1) SELECT * FROM " + keyword, true);
    }

    @ParameterizedTest
    @ValueSource(strings = {"sample", "extend", "SaMpLe", "ExTeNd"})
    void testSampleExtendExpressionContexts(String keyword) throws JSQLParserException {
        PlainSelect select = (PlainSelect) CCJSqlParserUtil.parse(
                "SELECT ALL " + keyword + " FROM mytable");
        assertEquals(keyword, assertInstanceOf(Column.class,
                select.getSelectItem(0).getExpression()).getColumnName());
        assertSqlCanBeParsedAndDeparsed("SELECT CASE " + keyword
                + " WHEN 1 THEN " + keyword + " ELSE 0 END FROM mytable", true);
        assertSqlCanBeParsedAndDeparsed("UPDATE " + keyword + " SET a = 1", true);
    }

    @ParameterizedTest
    @ValueSource(strings = {"sample", "extend", "SaMpLe", "ExTeNd"})
    void testSampleExtendTernaryOperand(String keyword) throws JSQLParserException {
        PlainSelect select = (PlainSelect) assertSqlCanBeParsedAndDeparsed(
                "SELECT flag ? " + keyword + " : 0 FROM mytable", true);
        TernaryExpression ternary = assertInstanceOf(TernaryExpression.class,
                select.getSelectItem(0).getExpression());
        assertEquals(keyword, assertInstanceOf(Column.class, ternary.getThenExpression())
                .getColumnName());
    }

    @ParameterizedTest
    @ValueSource(strings = {"SAMPLE (10)", "SAMPLE BLOCK (10) SEED (1)",
            "SAMPLE 0.1 OFFSET 1000", "USING SAMPLE 10 PERCENT (BERNOULLI)",
            "TABLESAMPLE SYSTEM (10)"})
    void testSampleClauseStaysClause(String clause) throws JSQLParserException {
        PlainSelect select = (PlainSelect) assertSqlCanBeParsedAndDeparsed(
                "SELECT * FROM mytable " + clause, true);
        Table table = assertInstanceOf(Table.class, select.getFromItem());
        assertNull(table.getAlias());
        SampleClause sample = table.getSampleClause();
        assertNotNull(sample);
        assertEquals(" " + clause, sample.toString());
    }

    @ParameterizedTest
    @ValueSource(strings = {"sample", "extend", "SaMpLe", "ExTeNd"})
    void testSampleExtendPipeExpression(String keyword) throws JSQLParserException {
        FromQuery query = (FromQuery) assertSqlCanBeParsedAndDeparsed(
                "FROM " + keyword + " |> EXTEND " + keyword + " AS x", true);
        SelectPipeOperator operator = assertInstanceOf(SelectPipeOperator.class,
                query.getPipeOperators().get(0));
        assertEquals("EXTEND", operator.getOperatorName());
        assertEquals(keyword, assertInstanceOf(Column.class,
                operator.getSelectItems().get(0).getExpression()).getColumnName());
        assertSqlCanBeParsedAndDeparsed(
                "FROM mytable |> EXTEND " + keyword + "(x) AS x", true);
        PlainSelect select = (PlainSelect) CCJSqlParserUtil.parse(
                "SELECT ALL " + keyword + "(x) FROM mytable");
        assertEquals(keyword, assertInstanceOf(Function.class,
                select.getSelectItem(0).getExpression()).getName());
    }
}
