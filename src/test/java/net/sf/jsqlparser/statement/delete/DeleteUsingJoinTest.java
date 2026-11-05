/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.delete;

import static net.sf.jsqlparser.test.TestUtils.assertDeparse;
import static net.sf.jsqlparser.test.TestUtils.assertSqlCanBeParsedAndDeparsed;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.function.Consumer;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.expression.ExpressionVisitorAdapter;
import net.sf.jsqlparser.parser.AbstractJSqlParser.Dialect;
import net.sf.jsqlparser.parser.CCJSqlParser;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.StatementVisitorAdapter;
import net.sf.jsqlparser.statement.Statements;
import net.sf.jsqlparser.statement.select.FromItemVisitorAdapter;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.ParenthesedSelect;
import net.sf.jsqlparser.statement.select.SelectVisitorAdapter;
import net.sf.jsqlparser.util.TablesNamesFinder;
import net.sf.jsqlparser.util.validation.ValidationTestAsserts;
import net.sf.jsqlparser.util.validation.feature.MySqlVersion;
import net.sf.jsqlparser.util.validation.feature.PostgresqlVersion;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class DeleteUsingJoinTest {

    private static final Consumer<CCJSqlParser> MYSQL = parser -> parser.withDialect(Dialect.MYSQL);
    private static final Consumer<CCJSqlParser> POSTGRESQL =
            parser -> parser.withDialect(Dialect.POSTGRESQL);

    /** Parses exactly with and without the given dialect and returns the dialect's result. */
    private static Delete parse(String sql, Consumer<CCJSqlParser> dialect)
            throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed(sql, false);
        return (Delete) assertSqlCanBeParsedAndDeparsed(sql, false, dialect);
    }

    private static String name(Object fromItem) {
        return ((Table) fromItem).getFullyQualifiedName();
    }

    @Test
    void mySqlUsingJoin() throws JSQLParserException {
        Delete delete = parse("DELETE FROM t1 USING t1 JOIN t2 ON t1.id = t2.id", MYSQL);

        assertEquals("t1", delete.getTable().getName());
        assertTrue(delete.getTables().isEmpty());
        assertNull(delete.getJoins());
        assertEquals(1, delete.getUsingFromItemList().size());
        assertEquals("t1", name(delete.getUsingFromItemList().get(0)));
        Join join = delete.getUsingJoins().get(0);
        assertEquals("t2", name(join.getFromItem()));
        assertEquals("t1.id = t2.id", join.getOnExpressions().iterator().next().toString());
    }

    @Test
    void mySqlSeveralTargets() throws JSQLParserException {
        Delete delete = parse("DELETE FROM t1, t2 USING t1 INNER JOIN t2 INNER JOIN t3"
                + " WHERE t1.id = t2.id AND t2.id = t3.id", MYSQL);

        // the targets keep the existing FROM t1, t2 reading
        assertEquals("t1", delete.getTable().getName());
        assertTrue(delete.getJoins().get(0).isSimple());
        assertEquals("t2", name(delete.getJoins().get(0).getFromItem()));
        assertEquals("t1", name(delete.getUsingFromItemList().get(0)));
        assertEquals(2, delete.getUsingJoins().size());
        assertTrue(delete.getUsingJoins().get(1).isInner());
        assertEquals("t3", name(delete.getUsingJoins().get(1).getFromItem()));
    }

    @Test
    void targetsBeforeUsingKeepTheirPlace() throws JSQLParserException {
        // the target joins were rendered after USING before
        Delete delete = parse("DELETE FROM t1, t2 USING t1, t2 WHERE t1.id = t2.id", MYSQL);

        assertEquals(1, delete.getJoins().size());
        assertEquals(2, delete.getUsingFromItemList().size());
        assertNull(delete.getUsingJoins());
    }

    @Test
    void commaListFollowedByJoins() throws JSQLParserException {
        Delete delete = parse("DELETE FROM t1 USING t1, t2 LEFT JOIN t3 ON t2.a = t3.a, t4"
                + " WHERE t1.id = t2.id", MYSQL);

        assertEquals(2, delete.getUsingFromItemList().size());
        assertEquals(2, delete.getUsingJoins().size());
        assertTrue(delete.getUsingJoins().get(0).isLeft());
        assertTrue(delete.getUsingJoins().get(1).isSimple());
    }

    @Test
    void postgreSqlUsingJoinWithSubquery() throws JSQLParserException {
        Delete delete = parse("DELETE FROM t USING (SELECT id FROM s) AS x JOIN u ON x.id = u.id"
                + " WHERE t.id = x.id RETURNING t.id", POSTGRESQL);

        assertInstanceOf(ParenthesedSelect.class, delete.getUsingFromItemList().get(0));
        assertEquals("u", name(delete.getUsingJoins().get(0).getFromItem()));
        assertEquals(1, delete.getReturningClause().size());
    }

    @Test
    void plainUsingListIsUnchanged() throws JSQLParserException {
        Delete delete = parse("DELETE FROM t USING s, u WHERE t.id = s.id", POSTGRESQL);

        assertEquals(2, delete.getUsingFromItemList().size());
        assertNull(delete.getUsingJoins());
        assertNull(delete.getJoins());
    }

    @Test
    void existingMultiTableFormIsUnchanged() throws JSQLParserException {
        Delete delete = parse("DELETE t1 FROM t1 JOIN t2 ON t1.id = t2.id", MYSQL);

        assertEquals(1, delete.getTables().size());
        assertEquals(1, delete.getJoins().size());
        assertTrue(delete.getUsingFromItemList().isEmpty());
        assertNull(delete.getUsingJoins());
    }

    @Test
    void targetAliasesRenderInBothRenderers() throws JSQLParserException {
        // the deparser dropped the alias of a target table, which the grammar accepts although
        // MySQL takes aliases only after FROM
        Delete delete = (Delete) assertSqlCanBeParsedAndDeparsed(
                "DELETE t1 x FROM t1 x WHERE x.a = 1", false);

        assertEquals("x", delete.getTables().get(0).getAlias().getName());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "DELETE FROM t1 USING",
            "DELETE FROM t1 USING t1 JOIN",
            "DELETE FROM t1 USING t1 JOIN t2 ON",
            "DELETE FROM t1 USING , t1"
    })
    void rejectsIncompleteForms(String sql) {
        assertThrows(JSQLParserException.class, () -> CCJSqlParserUtil.parse(sql, MYSQL));
    }

    @Test
    void statementBoundary() throws JSQLParserException {
        Statements statements = CCJSqlParserUtil.parseStatements(
                "DELETE FROM t1 USING t1 JOIN t2 ON t1.id = t2.id; SELECT 1", MYSQL);

        assertEquals(2, statements.size());
        assertEquals("DELETE FROM t1 USING t1 JOIN t2 ON t1.id = t2.id",
                statements.get(0).toString());
    }

    @Test
    void tablesNamesFinderSeesUsingJoins() throws JSQLParserException {
        assertEquals(new HashSet<>(Arrays.asList("t1", "t2", "t3")), TablesNamesFinder.findTables(
                "DELETE FROM t1 USING t1 JOIN t2 ON t1.id = t2.id JOIN t3 ON t2.id = t3.id"));
        assertEquals(new HashSet<>(Arrays.asList("t", "s", "u")), TablesNamesFinder.findTables(
                "DELETE FROM t USING (SELECT id FROM s) x JOIN u ON x.id = u.id"));
    }

    @Test
    void statementVisitorAdapterVisitsUsingItemsAndJoins() throws JSQLParserException {
        Delete delete = (Delete) CCJSqlParserUtil.parse(
                "DELETE FROM t USING (SELECT id FROM s) x JOIN u ON x.id = u.id");
        List<String> tables = new ArrayList<>();
        ExpressionVisitorAdapter<Void> expressions = new ExpressionVisitorAdapter<>();
        FromItemVisitorAdapter<Void> fromItems = new FromItemVisitorAdapter<Void>() {
            @Override
            public <S> Void visit(Table table, S context) {
                tables.add(table.getName());
                return null;
            }
        };
        SelectVisitorAdapter<Void> selects = new SelectVisitorAdapter<>(expressions, fromItems);
        expressions.setSelectVisitor(selects);
        fromItems.setSelectVisitor(selects).setExpressionVisitor(expressions);

        // a subquery in USING no longer fails the cast in getUsingList()
        delete.accept(new StatementVisitorAdapter<Void>(selects), null);

        assertEquals(Arrays.asList("t", "s", "u"), tables);
    }

    @Test
    void validatesForMySqlAndPostgreSql() throws JSQLParserException {
        ValidationTestAsserts.validateNoErrors("DELETE FROM t1 USING t1 JOIN t2 ON t1.id = t2.id",
                1, MySqlVersion.V8_0);
        ValidationTestAsserts.validateNoErrors(
                "DELETE FROM t USING s JOIN u ON s.id = u.id WHERE t.id = s.id", 1,
                PostgresqlVersion.V14);
    }

    @Test
    void buildUsingJoinsOnTheModel() {
        Delete delete = new Delete().withTable(new Table("t1"))
                .addUsingFromItemList(new Table("t1"));
        delete.addUsingJoins(new Join().withInner(true)
                .setFromItem(new Table("t2").withAlias(new Alias("b", false))));

        assertEquals("DELETE FROM t1 USING t1 INNER JOIN t2 b", delete.toString());
        assertDeparse(delete, "DELETE FROM t1 USING t1 INNER JOIN t2 b");

        // the joins of USING are rendered only with USING items
        delete.setUsingFromItemList(null);
        assertEquals("DELETE FROM t1", delete.toString());
        assertDeparse(delete, "DELETE FROM t1");
    }
}
