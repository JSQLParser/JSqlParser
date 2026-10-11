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
import net.sf.jsqlparser.expression.NotExpression;
import net.sf.jsqlparser.expression.MapExpression;
import net.sf.jsqlparser.expression.JdbcNamedParameter;
import net.sf.jsqlparser.expression.TernaryExpression;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.InExpression;
import net.sf.jsqlparser.expression.operators.relational.ExistsExpression;
import net.sf.jsqlparser.expression.operators.relational.SupportsOldOracleJoinSyntax;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.parser.ParserKeywordsUtils;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.PlainSelect;
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

    // exists is a non-reserved keyword in the common dialects and reaches the
    // parser as an ordinary column name in downstream rewriting tools.
    @Test
    public void testExistsAsIdentifier() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT exists FROM mytable", true);
        assertSqlCanBeParsedAndDeparsed("SELECT mytable.exists FROM mytable", true);
        assertSqlCanBeParsedAndDeparsed("SELECT * FROM mytable WHERE exists = 1", true);
        assertSqlCanBeParsedAndDeparsed("SELECT * FROM mytable ORDER BY exists", true);
        assertSqlCanBeParsedAndDeparsed("UPDATE mytable SET exists = 1 WHERE id = 1", true);
        assertSqlCanBeParsedAndDeparsed("INSERT INTO mytable (exists) VALUES (1)", true);
        assertSqlCanBeParsedAndDeparsed("SELECT a AS exists FROM mytable", true);
    }

    // the guarded keyword form keeps its operator meaning
    @Test
    public void testExistsPredicateStaysPredicate() throws JSQLParserException {
        PlainSelect select = (PlainSelect) assertSqlCanBeParsedAndDeparsed(
                "SELECT EXISTS (SELECT 1 FROM x) FROM mytable", true);
        assertInstanceOf(ExistsExpression.class, select.getSelectItem(0).getExpression());
        PlainSelect negated = (PlainSelect) assertSqlCanBeParsedAndDeparsed(
                "SELECT * FROM mytable WHERE NOT EXISTS (SELECT 1 FROM x)", true);
        assertInstanceOf(ExistsExpression.class,
                assertInstanceOf(NotExpression.class, negated.getWhere()).getExpression());
        assertSqlCanBeParsedAndDeparsed("SELECT * FROM mytable WHERE EXISTS (SELECT 1 FROM x)",
                true);
    }

    @ParameterizedTest
    @ValueSource(strings = {"exists", "ExIsTs"})
    void testExistsImplicitAliases(String keyword) throws JSQLParserException {
        PlainSelect select = (PlainSelect) assertSqlCanBeParsedAndDeparsed(
                "SELECT a " + keyword + " FROM mytable " + keyword, true);
        assertEquals(keyword, select.getSelectItem(0).getAlias().getName());
        assertEquals(keyword, select.getFromItem().getAlias().getName());
        assertEquals("a", assertInstanceOf(Column.class,
                select.getSelectItem(0).getExpression()).getColumnName());
    }

    @ParameterizedTest
    @ValueSource(strings = {"exists", "ExIsTs"})
    void testExistsCte(String keyword) throws JSQLParserException {
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
    @ValueSource(strings = {"exists", "ExIsTs"})
    void testExistsExpressionContexts(String keyword) throws JSQLParserException {
        PlainSelect select = (PlainSelect) CCJSqlParserUtil.parse(
                "SELECT ALL " + keyword + " FROM mytable");
        assertEquals(keyword, assertInstanceOf(Column.class,
                select.getSelectItem(0).getExpression()).getColumnName());
        assertSqlCanBeParsedAndDeparsed("SELECT CASE " + keyword
                + " WHEN 1 THEN " + keyword + " ELSE 0 END FROM mytable", true);
        assertSqlCanBeParsedAndDeparsed("UPDATE " + keyword + " SET a = 1", true);
    }

    @ParameterizedTest
    @ValueSource(strings = {"exists", "ExIsTs"})
    void testExistsTernaryOperand(String keyword) throws JSQLParserException {
        PlainSelect select = (PlainSelect) assertSqlCanBeParsedAndDeparsed(
                "SELECT flag ? " + keyword + " : 0 FROM mytable", true);
        TernaryExpression ternary = assertInstanceOf(TernaryExpression.class,
                select.getSelectItem(0).getExpression());
        assertEquals(keyword, assertInstanceOf(Column.class, ternary.getThenExpression())
                .getColumnName());
    }

    @Test
    void testExistsColumnWithOracleJoin() throws JSQLParserException {
        PlainSelect select = (PlainSelect) assertSqlCanBeParsedAndDeparsed(
                "SELECT * FROM mytable WHERE exists(+) = 1", true);
        EqualsTo equals = assertInstanceOf(EqualsTo.class, select.getWhere());
        assertEquals("exists", assertInstanceOf(Column.class, equals.getLeftExpression())
                .getColumnName());
        assertEquals(SupportsOldOracleJoinSyntax.ORACLE_JOIN_RIGHT,
                ((Column) equals.getLeftExpression()).getOldOracleJoinSyntax());
    }

    @ParameterizedTest
    @ValueSource(strings = {"SELECT 1", "VALUES (1)"})
    void testExistsUnparenthesizedSubSelect(String query) throws JSQLParserException {
        PlainSelect select = (PlainSelect) assertSqlCanBeParsedAndDeparsed(
                "SELECT EXISTS " + query, true, p -> p.withUnparenthesizedSubSelects(true));
        assertInstanceOf(ExistsExpression.class, select.getSelectItem(0).getExpression());
        PlainSelect where = (PlainSelect) assertSqlCanBeParsedAndDeparsed(
                "SELECT * FROM mytable WHERE EXISTS " + query, true,
                p -> p.withUnparenthesizedSubSelects(true));
        assertInstanceOf(ExistsExpression.class, where.getWhere());
    }

    @ParameterizedTest
    @ValueSource(strings = {"a", "t.a", "[1]", "+1", "(1)", "(+1)", ":arr", "limit", "offset"})
    void testExistsBareOperandStaysPredicate(String operand) throws JSQLParserException {
        PlainSelect select = (PlainSelect) assertSqlCanBeParsedAndDeparsed(
                "SELECT EXISTS " + operand + " FROM t", true);
        ExistsExpression exists = assertInstanceOf(ExistsExpression.class,
                select.getSelectItem(0).getExpression());
        assertEquals(operand, exists.getRightExpression().toString());
        PlainSelect where = (PlainSelect) assertSqlCanBeParsedAndDeparsed(
                "SELECT * FROM t WHERE EXISTS " + operand, true);
        assertInstanceOf(ExistsExpression.class, where.getWhere());
    }

    @Test
    void testExistsNamedParameterInTernary() throws JSQLParserException {
        PlainSelect select = (PlainSelect) assertSqlCanBeParsedAndDeparsed(
                "SELECT flag ? EXISTS :arr : 0 FROM t", true);
        TernaryExpression ternary = assertInstanceOf(TernaryExpression.class,
                select.getSelectItem(0).getExpression());
        ExistsExpression exists = assertInstanceOf(ExistsExpression.class,
                ternary.getThenExpression());
        assertEquals("arr", assertInstanceOf(JdbcNamedParameter.class,
                exists.getRightExpression()).getName());
    }

    @Test
    void testExistsMapKey() throws JSQLParserException {
        PlainSelect select = (PlainSelect) assertSqlCanBeParsedAndDeparsed(
                "SELECT MAP {exists: 1}", true);
        MapExpression map = assertInstanceOf(MapExpression.class,
                select.getSelectItem(0).getExpression());
        assertEquals("exists", assertInstanceOf(Column.class, map.getEntries().get(0).getKey())
                .getColumnName());
    }

    @ParameterizedTest
    @ValueSource(strings = {"IN (1, 2)", "NOT IN (1, 2)"})
    void testExistsColumnInCondition(String suffix) throws JSQLParserException {
        PlainSelect select = (PlainSelect) assertSqlCanBeParsedAndDeparsed(
                "SELECT * FROM t WHERE exists " + suffix, true);
        InExpression in = assertInstanceOf(InExpression.class, select.getWhere());
        assertEquals("exists", assertInstanceOf(Column.class, in.getLeftExpression())
                .getColumnName());
    }

    @ParameterizedTest
    @ValueSource(strings = {"ORDER BY id", "ORDER SIBLINGS BY id", "GROUP BY id", "LIMIT 1",
            "LIMIT 1.0", "LIMIT ALL", "LIMIT NULL", "LIMIT ?", "LIMIT $1", "LIMIT :n", "LIMIT &n",
            "OFFSET 1", "OFFSET 1.0", "OFFSET ?", "OFFSET $1", "OFFSET :n", "OFFSET &n"})
    void testExistsColumnBeforeClause(String suffix) throws JSQLParserException {
        PlainSelect select = (PlainSelect) assertSqlCanBeParsedAndDeparsed(
                "SELECT * FROM t WHERE exists " + suffix, true);
        assertEquals("exists", assertInstanceOf(Column.class, select.getWhere()).getColumnName());
        if (suffix.startsWith("LIMIT")) {
            assertNotNull(select.getLimit());
            assertEquals(suffix, select.getLimit().toString().trim());
        } else if (suffix.startsWith("OFFSET")) {
            assertNotNull(select.getOffset());
            assertEquals(suffix, select.getOffset().toString().trim());
        }
    }

    @Test
    void testExistsBeforeNestedTernary() throws JSQLParserException {
        PlainSelect select = (PlainSelect) assertSqlCanBeParsedAndDeparsed(
                "SELECT flag ? exists : b ? c : d FROM t", true);
        TernaryExpression ternary = assertInstanceOf(TernaryExpression.class,
                select.getSelectItem(0).getExpression());
        assertEquals("exists", assertInstanceOf(Column.class, ternary.getThenExpression())
                .getColumnName());
        assertInstanceOf(TernaryExpression.class, ternary.getElseExpression());
        PlainSelect mapSelect = (PlainSelect) assertSqlCanBeParsedAndDeparsed(
                "SELECT MAP {exists: b ? c : d}", true);
        MapExpression map = assertInstanceOf(MapExpression.class,
                mapSelect.getSelectItem(0).getExpression());
        assertEquals("exists", assertInstanceOf(Column.class, map.getEntries().get(0).getKey())
                .getColumnName());
        assertInstanceOf(TernaryExpression.class, map.getEntries().get(0).getValue());
    }
}
