/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2024 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement;

import static net.sf.jsqlparser.test.TestUtils.assertSqlCanBeParsedAndDeparsed;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.ExpressionVisitorAdapter;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.operators.relational.ParenthesedExpressionList;
import net.sf.jsqlparser.parser.AbstractJSqlParser.Dialect;
import net.sf.jsqlparser.parser.CCJSqlParser;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.schema.TableReference;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.merge.Merge;
import net.sf.jsqlparser.statement.merge.MergeDelete;
import net.sf.jsqlparser.statement.merge.MergeDoNothing;
import net.sf.jsqlparser.statement.merge.MergeInsert;
import net.sf.jsqlparser.statement.merge.MergeOperation;
import net.sf.jsqlparser.statement.merge.MergeOperationVisitor;
import net.sf.jsqlparser.statement.merge.MergeOperationVisitorAdapter;
import net.sf.jsqlparser.statement.merge.MergeSide;
import net.sf.jsqlparser.statement.merge.MergeUpdate;
import net.sf.jsqlparser.statement.merge.MergeWhen;
import net.sf.jsqlparser.statement.select.ParenthesedSelect;
import net.sf.jsqlparser.statement.select.SelectVisitorAdapter;
import net.sf.jsqlparser.statement.update.Update;
import net.sf.jsqlparser.statement.update.UpdateSet;
import net.sf.jsqlparser.util.TablesNamesFinder;
import net.sf.jsqlparser.util.deparser.StatementDeParser;
import net.sf.jsqlparser.util.validation.ValidationTestAsserts;
import net.sf.jsqlparser.util.validation.feature.FeaturesAllowed;
import net.sf.jsqlparser.util.validation.feature.PostgresqlVersion;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * PostgreSQL's relation_expr_opt_alias targets, WHERE CURRENT OF, set_clause sources and
 * merge_when_clause actions of UPDATE, DELETE and MERGE.
 */
class PostgreSQLModifyFormsTest {

    private static final Consumer<CCJSqlParser> POSTGRESQL =
            parser -> parser.withDialect(Dialect.POSTGRESQL);
    private static final Consumer<CCJSqlParser> MYSQL = parser -> parser.withDialect(Dialect.MYSQL);
    private static final Consumer<CCJSqlParser> MARIADB =
            parser -> parser.withDialect(Dialect.MARIADB);
    private static final Consumer<CCJSqlParser> ORACLE =
            parser -> parser.withDialect(Dialect.ORACLE);

    private static final String MERGE = "MERGE INTO t USING s ON t.id = s.id ";

    /** Parses exactly with PostgreSQL and without a dialect; both render the statement again. */
    private static Statement parse(String sql) throws JSQLParserException {
        Statement statement = assertSqlCanBeParsedAndDeparsed(sql, false, POSTGRESQL);
        assertEquals(sql, statement.toString());
        assertEquals(sql, deparse(statement));
        assertEquals(sql, CCJSqlParserUtil.parse(statement.toString(), POSTGRESQL).toString());
        assertEquals(sql, assertSqlCanBeParsedAndDeparsed(sql, false).toString());
        return statement;
    }

    private static String deparse(Statement statement) {
        StringBuilder builder = new StringBuilder();
        statement.accept(new StatementDeParser(builder));
        return builder.toString();
    }

    private static void assertRejected(String sql) {
        assertThrows(JSQLParserException.class, () -> CCJSqlParserUtil.parse(sql, POSTGRESQL), sql);
        assertThrows(JSQLParserException.class, () -> CCJSqlParserUtil.parse(sql), sql);
    }

    private static void assertTarget(TableReference target, String name, boolean only,
            boolean descendants, boolean parenthesized, String alias) {
        assertEquals(name, target.getTable().getFullyQualifiedName());
        assertEquals(only, target.isOnly());
        assertEquals(descendants, target.isIncludeDescendants());
        assertEquals(parenthesized, target.isParenthesized());
        assertEquals(only || descendants, target.hasInheritanceScope());
        Alias tableAlias = target.getTable().getAlias();
        assertEquals(alias, tableAlias == null ? null : tableAlias.getName());
        assertNotNull(target.getTable().getASTNode());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "UPDATE ONLY t SET a = 1",
            "UPDATE ONLY t AS x SET a = 1",
            "UPDATE ONLY t x SET a = 1",
            "UPDATE t * SET a = 1",
            "UPDATE t * AS x SET a = 1",
            "UPDATE t * x SET a = 1",
            "UPDATE ONLY (t) SET a = 1",
            "UPDATE ONLY (t) AS x SET a = 1",
            "UPDATE ONLY (sc.t) x SET a = 1",
            "UPDATE ONLY sc.t AS x SET a = 1 FROM u WHERE x.a = u.a RETURNING *",
            "UPDATE sc.t * AS x SET a = 1 FROM u WHERE x.a = u.a RETURNING x.a",
            "WITH w AS (SELECT 1 AS a) UPDATE ONLY t AS x SET a = w.a FROM w",
            "DELETE FROM ONLY t",
            "DELETE FROM ONLY t AS x WHERE x.a = 1",
            "DELETE FROM ONLY t x WHERE x.a = 1",
            "DELETE FROM t * WHERE a = 1",
            "DELETE FROM t * AS x WHERE x.a = 1",
            "DELETE FROM t * x WHERE x.a = 1",
            "DELETE FROM t *",
            "DELETE FROM ONLY (t)",
            "DELETE FROM ONLY (t) AS x WHERE x.a = 1",
            "DELETE FROM ONLY t USING u WHERE t.a = u.a",
            "DELETE FROM ONLY t AS x USING u WHERE x.a = u.a RETURNING x.a",
            "DELETE FROM sc.t * AS x USING u, s WHERE x.a = u.a RETURNING *",
            "WITH w AS (SELECT 1 AS a) DELETE FROM ONLY t AS x USING w WHERE x.a = w.a",
            "MERGE INTO ONLY t USING s ON t.id = s.id WHEN MATCHED THEN DELETE",
            "MERGE INTO ONLY t AS x USING s ON x.id = s.id WHEN MATCHED THEN DELETE",
            "MERGE INTO ONLY (t) x USING s ON x.id = s.id WHEN MATCHED THEN DELETE",
            "MERGE INTO t * USING s ON t.id = s.id WHEN MATCHED THEN DELETE",
            "MERGE INTO t * x USING s ON x.id = s.id WHEN MATCHED THEN UPDATE SET (a, b) = ROW(1, 2)",
            "MERGE INTO t * AS x USING s ON x.id = s.id WHEN MATCHED THEN DO NOTHING"
    })
    void inheritanceScopeOfTheTarget(String sql) throws JSQLParserException {
        parse(sql);
    }

    @Test
    void updateTargetModel() throws JSQLParserException {
        Update update = (Update) parse("UPDATE ONLY t AS x SET a = 1");
        assertTarget(update.getTableReference(), "t", true, false, false, "x");
        assertTrue(update.getTable().getAlias().isUseAs());
        assertEquals(update.getTableReference().getTable(), update.getTable());

        update = (Update) parse("UPDATE ONLY (sc.t) x SET a = 1");
        assertTarget(update.getTableReference(), "sc.t", true, false, true, "x");
        assertFalse(update.getTable().getAlias().isUseAs());

        update = (Update) parse("UPDATE t * SET a = 1");
        assertTarget(update.getTableReference(), "t", false, true, false, null);

        update = (Update) parse("UPDATE t AS x SET a = 1");
        assertTarget(update.getTableReference(), "t", false, false, false, "x");

        // the star needs no space
        update = (Update) CCJSqlParserUtil.parse("UPDATE t* SET a = 1", POSTGRESQL);
        assertEquals("UPDATE t * SET a = 1", update.toString());
    }

    @Test
    void deleteAndMergeTargetModel() throws JSQLParserException {
        Delete delete = (Delete) parse("DELETE FROM ONLY t AS x WHERE x.a = 1");
        assertTarget(delete.getTableReference(), "t", true, false, false, "x");
        assertTrue(delete.isHasFrom());
        assertEquals("x.a = 1", delete.getWhere().toString());

        delete = (Delete) parse("DELETE FROM sc.t * x USING u WHERE x.a = u.a");
        assertTarget(delete.getTableReference(), "sc.t", false, true, false, "x");
        assertEquals(1, delete.getUsingFromItemList().size());

        delete = (Delete) parse("DELETE FROM t WHERE a = 1");
        assertTarget(delete.getTableReference(), "t", false, false, false, null);

        Merge merge = (Merge) parse(
                "MERGE INTO ONLY (t) x USING s ON x.id = s.id WHEN MATCHED THEN DELETE");
        assertTarget(merge.getTableReference(), "t", true, false, true, "x");
        assertEquals("s", merge.getFromItem().toString());

        merge = (Merge) parse("MERGE INTO t * USING s ON t.id = s.id WHEN MATCHED THEN DELETE");
        assertTarget(merge.getTableReference(), "t", false, true, false, null);
    }

    @Test
    void targetsAreBuiltThroughTheModel() {
        Update update = new Update().withTable(new Table("t"))
                .addUpdateSet(new Column("a"), new LongValue(1));
        assertEquals("UPDATE t SET a = 1", update.toString());
        update.getTableReference().setOnly(true);
        assertEquals("UPDATE ONLY t SET a = 1", update.toString());
        assertEquals(update.toString(), deparse(update));

        // replacing the table keeps the scope
        update.setTable(new Table("u").withAlias(new Alias("x", true)));
        assertEquals("UPDATE ONLY u AS x SET a = 1", update.toString());
        TableReference descendants = new TableReference(new Table("t"));
        descendants.setIncludeDescendants(true);
        assertEquals("UPDATE t * SET a = 1", update.withTableReference(descendants).toString());
        assertThrows(NullPointerException.class, () -> update.setTableReference(null));

        Delete delete = new Delete().withTable(new Table("t")).withHasFrom(true);
        assertEquals("DELETE FROM t", delete.toString());
        delete.getTableReference().setParenthesized(true);
        assertEquals("DELETE FROM ONLY (t)", delete.toString());
        assertEquals(delete.toString(), deparse(delete));
        assertEquals("DELETE FROM t *", delete.withTableReference(descendants).toString());
        assertThrows(NullPointerException.class, () -> delete.setTableReference(null));

        Merge merge = new Merge().withTableReference(descendants);
        assertEquals("t", merge.getTable().getName());
        assertThrows(NullPointerException.class, () -> merge.setTableReference(null));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "UPDATE ONLY t * SET a = 1",
            "UPDATE (t) SET a = 1",
            "UPDATE ONLY (t *) SET a = 1",
            "UPDATE ONLY (t AS x) SET a = 1",
            "UPDATE ONLY ONLY t SET a = 1",
            "UPDATE ONLY SET a = 1",
            "UPDATE t * * SET a = 1",
            "UPDATE t AS x * SET a = 1",
            "DELETE FROM ONLY t * WHERE a = 1",
            "DELETE FROM (t) WHERE a = 1",
            "DELETE FROM ONLY (t AS x)",
            "DELETE ONLY t",
            "DELETE FROM t AS x *",
            "MERGE INTO ONLY t * USING s ON t.id = s.id WHEN MATCHED THEN DELETE",
            "MERGE ONLY t USING s ON t.id = s.id WHEN MATCHED THEN DELETE"
    })
    void rejectedTargets(String sql) {
        assertRejected(sql);
    }

    @Test
    void mySqlHasNoOnlyTargets() throws JSQLParserException {
        // MySQL's ONLY is no keyword of UPDATE or DELETE; the forms stay as they were
        for (Consumer<CCJSqlParser> dialect : Arrays.asList(MYSQL, MARIADB)) {
            assertThrows(JSQLParserException.class,
                    () -> CCJSqlParserUtil.parse("UPDATE ONLY t SET a = 1", dialect));
            assertThrows(JSQLParserException.class,
                    () -> CCJSqlParserUtil.parse("DELETE FROM ONLY t WHERE a = 1", dialect));
            Delete delete = (Delete) assertSqlCanBeParsedAndDeparsed("DELETE FROM only WHERE a = 1",
                    false, dialect);
            assertEquals("only", delete.getTable().getName());
            assertFalse(delete.getTableReference().isOnly());
        }
        Delete delete = (Delete) assertSqlCanBeParsedAndDeparsed("DELETE FROM only", false);
        assertEquals("only", delete.getTable().getName());

        // index hints and multi-table forms keep their readings
        Update update = (Update) assertSqlCanBeParsedAndDeparsed(
                "UPDATE t AS x USE INDEX (i) SET a = 1", false, MYSQL);
        assertEquals(1, update.getTable().getIndexHints().size());
        assertFalse(update.getTableReference().hasInheritanceScope());
        assertSqlCanBeParsedAndDeparsed("UPDATE LOW_PRIORITY IGNORE t, u SET t.a = u.a", false,
                MYSQL);
        assertSqlCanBeParsedAndDeparsed("DELETE t, u FROM t JOIN u ON t.id = u.id", false, MYSQL);
        assertSqlCanBeParsedAndDeparsed("UPDATE OR REPLACE t SET a = 1", false,
                parser -> parser.withDialect(Dialect.SQLITE));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "UPDATE t SET a = 1 WHERE CURRENT OF c",
            "UPDATE t SET a = 1 WHERE CURRENT OF c RETURNING a",
            "UPDATE ONLY t AS x SET a = 1 WHERE CURRENT OF c",
            "UPDATE t SET a = 1 FROM u WHERE CURRENT OF c",
            "UPDATE t SET a = 1 WHERE CURRENT OF \"My Cursor\"",
            "UPDATE t SET a = 1 WHERE CURRENT OF current",
            "UPDATE t SET (a, b) = (1, 2) WHERE CURRENT OF c",
            "DELETE FROM t WHERE CURRENT OF c",
            "DELETE FROM t WHERE CURRENT OF c RETURNING *",
            "DELETE FROM ONLY t x WHERE CURRENT OF c",
            "DELETE FROM t USING u WHERE CURRENT OF c"
    })
    void whereCurrentOf(String sql) throws JSQLParserException {
        parse(sql);
        // the positioned forms are standard SQL
        assertSqlCanBeParsedAndDeparsed(sql, false, ORACLE);
    }

    @Test
    void whereCurrentOfModel() throws JSQLParserException {
        Update update = (Update) parse("UPDATE t SET a = 1 WHERE CURRENT OF c RETURNING a");
        assertEquals("c", update.getCursorName());
        assertNull(update.getWhere());
        assertNotNull(update.getReturningClause());

        Delete delete = (Delete) parse("DELETE FROM t WHERE CURRENT OF \"My Cursor\"");
        assertEquals("\"My Cursor\"", delete.getCursorName());
        assertNull(delete.getWhere());

        // a column named current is still a condition
        update = (Update) parse("UPDATE t SET a = 1 WHERE current = 1");
        assertNull(update.getCursorName());
        assertEquals("current = 1", update.getWhere().toString());
        delete = (Delete) parse("DELETE FROM t WHERE current");
        assertNull(delete.getCursorName());

        update = new Update().withTable(new Table("t"))
                .addUpdateSet(new Column("a"), new LongValue(1)).withCursorName("c");
        assertEquals("UPDATE t SET a = 1 WHERE CURRENT OF c", update.toString());
        assertEquals(update.toString(), deparse(update));
        delete = new Delete().withTable(new Table("t")).withHasFrom(true).withCursorName("c");
        assertEquals("DELETE FROM t WHERE CURRENT OF c", delete.toString());
        assertEquals(delete.toString(), deparse(delete));
        delete.setCursorName(null);
        assertEquals("DELETE FROM t", delete.toString());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "UPDATE t SET a = 1 WHERE CURRENT OF c AND a = 1",
            "UPDATE t SET a = 1 WHERE CURRENT OF sc.c",
            "UPDATE t SET a = 1 WHERE CURRENT OF",
            "UPDATE t SET a = 1 WHERE CURRENT c",
            "UPDATE t SET a = 1 WHERE CURRENT OF 'c'",
            "UPDATE t SET a = 1 WHERE a = 1 AND CURRENT OF c",
            "UPDATE t SET a = 1 WHERE CURRENT OF c WHERE a = 1",
            "DELETE FROM t WHERE CURRENT OF c OR a = 1",
            "DELETE FROM t WHERE CURRENT OF sc.c",
            "DELETE FROM t WHERE CURRENT OF",
            "SELECT * FROM t WHERE CURRENT OF c",
            "MERGE INTO t USING s ON t.id = s.id WHEN MATCHED THEN DELETE WHERE CURRENT OF c"
    })
    void rejectedCursorForms(String sql) {
        assertRejected(sql);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "UPDATE t SET a = DEFAULT",
            "UPDATE t SET (a, b) = (1, 2)",
            "UPDATE t SET (a, b) = ROW(1, 2)",
            "UPDATE t SET (a, b) = ROW(1, DEFAULT)",
            "UPDATE t SET (a, b) = (DEFAULT, DEFAULT)",
            "UPDATE t SET (a, b) = (SELECT 1, 2)",
            "UPDATE t SET (a, b) = (SELECT s.a, s.b FROM s WHERE s.id = t.id)",
            "UPDATE t SET (a) = (1)",
            "UPDATE t SET (a) = ROW(1)",
            "UPDATE t SET (a) = 1",
            "UPDATE t SET (a) = (SELECT 1)",
            "UPDATE t SET (a) = DEFAULT",
            "UPDATE t SET a = 1, (b, c) = ROW(2, 3), d = DEFAULT",
            "UPDATE t SET (a, b) = ROW(1, 2), (c, d) = (3, 4)",
            "UPDATE t SET (a, b) = (SELECT 1, 2), c = 3",
            "UPDATE t SET (arr[1], a) = ROW(1, 2)",
            "UPDATE t SET (arr[1][2], arr[1:2]) = ROW(1, 2)",
            "UPDATE t SET (a, b) = ((SELECT 1, 2))",
            "UPDATE t SET (a, b) = 1",
            "UPDATE t SET (a, b) = f(1)",
            "UPDATE t SET (a, b) = DEFAULT",
            "UPDATE t SET (a, b) = ROW(1, 2) || ROW(3, 4)",
            "UPDATE t SET a = ROW(1, 2)",
            "MERGE INTO t USING s ON t.id = s.id WHEN MATCHED THEN UPDATE SET (a, b) = ROW(s.a, DEFAULT), c = DEFAULT",
            "MERGE INTO t USING s ON t.id = s.id WHEN MATCHED THEN UPDATE SET (a, b) = (SELECT 1, 2)",
            "INSERT INTO t VALUES (1, 2) ON CONFLICT (id) DO UPDATE SET (a, b) = ROW(1, 2)"
    })
    void setClauseSources(String sql) throws JSQLParserException {
        parse(sql);
    }

    @Test
    void setClauseModel() throws JSQLParserException {
        Update update = (Update) parse("UPDATE t SET a = 1, (b, c) = ROW(2, DEFAULT), d = DEFAULT");
        List<UpdateSet> sets = update.getUpdateSets();
        assertEquals(3, sets.size());
        assertEquals("a", sets.get(0).getColumn(0).getColumnName());

        // ROW(...) is one source for the column list, the function it is in other expressions
        UpdateSet row = sets.get(1);
        assertInstanceOf(ParenthesedExpressionList.class, row.getColumns());
        assertEquals(2, row.getColumns().size());
        assertEquals(1, row.getValues().size());
        assertFalse(row.getValues() instanceof ParenthesedExpressionList);
        Function constructor = assertInstanceOf(Function.class, row.getValue(0));
        assertEquals("ROW", constructor.getName());
        assertEquals(2, constructor.getParameters().size());
        assertNotNull(row.getColumn(0).getASTNode());
        assertEquals("d = DEFAULT", sets.get(2).toString());

        // a parenthesized list keeps one value per column, a sub-select is one value
        update = (Update) parse("UPDATE t SET (a, b) = (1, 2), (c, d) = (SELECT 3, 4)");
        assertInstanceOf(ParenthesedExpressionList.class,
                update.getUpdateSets().get(0).getValues());
        assertEquals(2, update.getUpdateSets().get(0).getValues().size());
        assertInstanceOf(ParenthesedSelect.class, update.getUpdateSets().get(1).getValue(0));

        // other dialects read ROW(...) as well
        assertSqlCanBeParsedAndDeparsed("UPDATE t SET (a, b) = ROW(1, 2)", false, ORACLE);
        assertSqlCanBeParsedAndDeparsed("UPDATE t SET (a, b) = ROW(1, 2)", false, MYSQL);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "UPDATE t SET (a, b) = ROW 1, 2",
            "UPDATE t SET () = ROW(1, 2)",
            "UPDATE t SET (a, b) ROW(1, 2)",
            "UPDATE t SET (a, b) = ROW(1, 2),",
            "UPDATE t SET (a, b) = ",
            "UPDATE t SET ROW(a, b) = ROW(1, 2)",
            "UPDATE t SET (a, (b)) = ROW(1, 2)",
            "UPDATE t SET (a + 1) = ROW(1)",
            "UPDATE t SET (a, 1) = DEFAULT"
    })
    void rejectedSetClauses(String sql) {
        assertRejected(sql);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "WHEN MATCHED THEN DO NOTHING",
            "WHEN MATCHED AND t.a > 1 THEN DO NOTHING",
            "WHEN NOT MATCHED THEN DO NOTHING",
            "WHEN NOT MATCHED AND s.a > 1 THEN DO NOTHING",
            "WHEN NOT MATCHED BY SOURCE THEN DO NOTHING",
            "WHEN NOT MATCHED BY SOURCE AND t.a > 1 THEN DO NOTHING",
            "WHEN NOT MATCHED BY TARGET THEN DO NOTHING",
            "WHEN NOT MATCHED BY SOURCE THEN DELETE",
            "WHEN NOT MATCHED BY SOURCE THEN UPDATE SET a = 1",
            "WHEN NOT MATCHED BY TARGET THEN INSERT (id) VALUES (s.id)",
            "WHEN MATCHED THEN DO NOTHING WHEN NOT MATCHED THEN INSERT DEFAULT VALUES"
                    + " WHEN NOT MATCHED BY SOURCE THEN DELETE",
            "WHEN NOT MATCHED THEN INSERT DEFAULT VALUES",
            "WHEN NOT MATCHED BY TARGET AND s.a > 1 THEN INSERT DEFAULT VALUES",
            "WHEN NOT MATCHED THEN INSERT OVERRIDING SYSTEM VALUE VALUES (1, 2)",
            "WHEN NOT MATCHED THEN INSERT OVERRIDING USER VALUE VALUES (1, 2)",
            "WHEN NOT MATCHED THEN INSERT (id, a) OVERRIDING SYSTEM VALUE VALUES (1, 2)",
            "WHEN NOT MATCHED THEN INSERT (id, a) OVERRIDING USER VALUE VALUES (1, 2)",
            "WHEN MATCHED THEN DO NOTHING RETURNING merge_action()"
    })
    void mergeWhenClauses(String clauses) throws JSQLParserException {
        parse(MERGE + clauses);
    }

    @Test
    void mergeDoNothingModel() throws JSQLParserException {
        Merge merge = (Merge) parse(MERGE + "WHEN MATCHED AND t.a > 1 THEN DO NOTHING"
                + " WHEN NOT MATCHED AND s.a > 2 THEN DO NOTHING"
                + " WHEN NOT MATCHED BY SOURCE AND t.a > 3 THEN DO NOTHING"
                + " WHEN NOT MATCHED BY TARGET AND s.a > 1 THEN DO NOTHING"
                + " WHEN NOT MATCHED BY SOURCE AND t.a > 4 THEN DELETE"
                + " WHEN NOT MATCHED BY SOURCE THEN UPDATE SET a = 1"
                + " WHEN MATCHED THEN DELETE"
                + " WHEN NOT MATCHED BY TARGET AND s.a > 5 THEN INSERT (id) VALUES (s.id)"
                + " WHEN NOT MATCHED THEN INSERT (id) VALUES (s.id)");
        List<MergeOperation> operations = merge.getOperations();
        assertEquals(9, operations.size());

        MergeDoNothing matched = assertInstanceOf(MergeDoNothing.class, operations.get(0));
        assertEquals(MergeWhen.MATCHED, matched.getWhen());
        assertEquals("t.a > 1", matched.getAndPredicate().toString());
        assertEquals(" WHEN MATCHED AND t.a > 1 THEN DO NOTHING", matched.toString());
        assertEquals(MergeWhen.NOT_MATCHED, ((MergeDoNothing) operations.get(1)).getWhen());
        assertEquals("s.a > 2", ((MergeDoNothing) operations.get(1)).getAndPredicate().toString());
        assertEquals(MergeWhen.NOT_MATCHED_BY_SOURCE,
                ((MergeDoNothing) operations.get(2)).getWhen());
        assertEquals(MergeWhen.NOT_MATCHED_BY_TARGET,
                ((MergeDoNothing) operations.get(3)).getWhen());

        // the other actions derive the condition from their side
        assertEquals(MergeWhen.NOT_MATCHED_BY_SOURCE, ((MergeDelete) operations.get(4)).getWhen());
        assertEquals(MergeWhen.NOT_MATCHED_BY_SOURCE, ((MergeUpdate) operations.get(5)).getWhen());
        assertEquals(MergeWhen.MATCHED, ((MergeDelete) operations.get(6)).getWhen());
        assertEquals(MergeWhen.NOT_MATCHED_BY_TARGET, ((MergeInsert) operations.get(7)).getWhen());
        assertEquals(MergeWhen.NOT_MATCHED, ((MergeInsert) operations.get(8)).getWhen());
        assertEquals(MergeWhen.MATCHED, new MergeUpdate().getWhen());

        assertEquals(MergeSide.SOURCE, MergeWhen.NOT_MATCHED_BY_SOURCE.getSide());
        assertEquals(MergeSide.TARGET, MergeWhen.NOT_MATCHED_BY_TARGET.getSide());
        assertNull(MergeWhen.MATCHED.getSide());
        assertNull(MergeWhen.NOT_MATCHED.getSide());
        assertEquals("WHEN NOT MATCHED BY SOURCE", MergeWhen.NOT_MATCHED_BY_SOURCE.toString());

        MergeDoNothing built = new MergeDoNothing().withWhen(MergeWhen.NOT_MATCHED)
                .withAndPredicate(new Column("a"));
        assertEquals(" WHEN NOT MATCHED AND a THEN DO NOTHING", built.toString());
        assertEquals(MergeWhen.MATCHED, new MergeDoNothing().getWhen());
        assertThrows(NullPointerException.class, () -> built.setWhen(null));

        // DO NOTHING is neither the standard update nor the insert clause
        merge = (Merge) parse(MERGE + "WHEN MATCHED THEN DO NOTHING");
        assertNull(merge.getMergeUpdate());
        assertNull(merge.getMergeInsert());
    }

    @Test
    void mergeInsertModel() throws JSQLParserException {
        Merge merge = (Merge) parse(MERGE
                + "WHEN NOT MATCHED AND s.a > 1 THEN INSERT DEFAULT VALUES"
                + " WHEN NOT MATCHED AND s.a > 2 THEN INSERT (id, a) OVERRIDING USER VALUE VALUES (1, 2)"
                + " WHEN NOT MATCHED THEN INSERT OVERRIDING SYSTEM VALUE VALUES (1, 2)");
        MergeInsert defaults = (MergeInsert) merge.getOperations().get(0);
        assertTrue(defaults.isDefaultValues());
        assertNull(defaults.getValues());
        assertNull(defaults.getColumns());
        assertEquals(Insert.OverridingMode.NONE, defaults.getOverridingMode());

        MergeInsert user = (MergeInsert) merge.getOperations().get(1);
        assertFalse(user.isDefaultValues());
        assertEquals(Insert.OverridingMode.USER, user.getOverridingMode());
        assertEquals(2, user.getColumns().size());
        assertEquals(2, user.getValues().size());

        MergeInsert system = (MergeInsert) merge.getOperations().get(2);
        assertEquals(Insert.OverridingMode.SYSTEM, system.getOverridingMode());
        assertNull(system.getColumns());

        MergeInsert built = new MergeInsert();
        assertEquals(" WHEN NOT MATCHED THEN INSERT DEFAULT VALUES", built.toString());
        built.withOverridingMode(Insert.OverridingMode.SYSTEM).addColumns(new Column("id"))
                .withValues(new ParenthesedExpressionList<Expression>(new LongValue(1)));
        assertThrows(NullPointerException.class, () -> built.setOverridingMode(null));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "WHEN MATCHED THEN DO",
            "WHEN MATCHED THEN NOTHING",
            "WHEN MATCHED THEN DO NOTHING WHERE t.a = 1",
            "WHEN MATCHED THEN DO UPDATE SET a = 1",
            "WHEN MATCHED THEN INSERT DEFAULT VALUES",
            "WHEN MATCHED THEN INSERT VALUES (1)",
            "WHEN NOT MATCHED THEN UPDATE SET a = 1",
            "WHEN NOT MATCHED THEN DELETE",
            "WHEN NOT MATCHED BY TARGET THEN DELETE",
            "WHEN NOT MATCHED BY SOURCE THEN INSERT VALUES (1)",
            "WHEN NOT MATCHED BY SOURCE THEN INSERT DEFAULT VALUES",
            "WHEN MATCHED BY SOURCE THEN DELETE",
            "WHEN NOT MATCHED BY foo THEN DO NOTHING",
            "WHEN NOT MATCHED THEN INSERT (id) DEFAULT VALUES",
            "WHEN NOT MATCHED THEN INSERT OVERRIDING SYSTEM VALUE DEFAULT VALUES",
            "WHEN NOT MATCHED THEN INSERT OVERRIDING VALUE VALUES (1)",
            "WHEN NOT MATCHED THEN INSERT OVERRIDING SYSTEM VALUES (1)",
            "WHEN NOT MATCHED THEN INSERT OVERRIDING SYSTEM VALUE (id) VALUES (1)",
            "WHEN NOT MATCHED THEN INSERT DEFAULT VALUES (1)",
            "WHEN NOT MATCHED THEN INSERT DEFAULT VALUES WHERE s.a = 1",
            "WHEN NOT MATCHED THEN INSERT VALUES (1), (2)",
            "WHEN NOT MATCHED THEN INSERT (id) SELECT 1"
    })
    void rejectedMergeClauses(String clauses) {
        assertThrows(Exception.class, () -> CCJSqlParserUtil.parse(MERGE + clauses, POSTGRESQL),
                clauses);
        assertThrows(Exception.class, () -> CCJSqlParserUtil.parse(MERGE + clauses), clauses);
    }

    @Test
    void otherMergeDialectsKeepTheirClauses() throws JSQLParserException {
        Merge merge = (Merge) assertSqlCanBeParsedAndDeparsed(
                "MERGE INTO t USING s ON (t.id = s.id) WHEN MATCHED THEN UPDATE SET a = 1"
                        + " WHERE t.a = 2 DELETE WHERE t.a = 3"
                        + " WHEN NOT MATCHED THEN INSERT (id) VALUES (s.id) WHERE s.a = 2",
                true, ORACLE);
        assertNotNull(merge.getMergeUpdate().getDeleteWhereCondition());
        assertNotNull(merge.getMergeInsert().getWhereCondition());
        assertFalse(merge.getMergeInsert().isDefaultValues());
        assertFalse(merge.getTableReference().hasInheritanceScope());

        assertSqlCanBeParsedAndDeparsed(
                "MERGE INTO t AS x WITH (HOLDLOCK) USING s ON x.id = s.id"
                        + " WHEN NOT MATCHED BY TARGET THEN INSERT (id) VALUES (s.id)"
                        + " WHEN NOT MATCHED BY SOURCE THEN DELETE OUTPUT deleted.id",
                true, parser -> parser.withDialect(Dialect.SQLSERVER));
    }

    @Test
    void visitorsReachTheDoNothingPredicate() throws JSQLParserException {
        Merge merge = (Merge) CCJSqlParserUtil.parse(MERGE
                + "WHEN MATCHED AND t.a IN (SELECT a FROM u) THEN DO NOTHING"
                + " WHEN NOT MATCHED THEN INSERT DEFAULT VALUES", POSTGRESQL);

        List<String> columns = new ArrayList<>();
        ExpressionVisitorAdapter<Void> expressions = new ExpressionVisitorAdapter<Void>() {
            @Override
            public <S> Void visit(Column column, S context) {
                columns.add(column.getFullyQualifiedName());
                return super.visit(column, context);
            }
        };
        merge.getOperations().get(0).accept(new MergeOperationVisitorAdapter<>(expressions), null);
        assertEquals(Arrays.asList("t.a"), columns);
        merge.getOperations().get(1).accept(new MergeOperationVisitorAdapter<>(expressions), null);
        merge.accept(new StatementVisitorAdapter<Void>(new SelectVisitorAdapter<>(expressions)),
                null);

        // a visitor written before DO NOTHING existed skips the action
        MergeOperationVisitor<String> legacy = new MergeOperationVisitor<String>() {
            @Override
            public <S> String visit(MergeDelete mergeDelete, S context) {
                return "delete";
            }

            @Override
            public <S> String visit(MergeUpdate mergeUpdate, S context) {
                return "update";
            }

            @Override
            public <S> String visit(MergeInsert mergeInsert, S context) {
                return "insert";
            }
        };
        assertNull(merge.getOperations().get(0).accept(legacy, null));
        assertEquals("insert", merge.getOperations().get(1).accept(legacy, null));

        assertEquals(Arrays.asList("s", "t", "u"),
                sorted(new TablesNamesFinder<Void>().getTables((Statement) merge)));
    }

    private static List<String> sorted(Collection<String> names) {
        List<String> list = new ArrayList<>(names);
        Collections.sort(list);
        return list;
    }

    @Test
    void tablesNamesFinder() throws JSQLParserException {
        assertEquals(Arrays.asList("sc.t", "u"), sorted(TablesNamesFinder.findTables(
                "UPDATE ONLY sc.t AS x SET (a, b) = ROW(1, (SELECT max(a) FROM u))"
                        + " WHERE CURRENT OF c")));
        assertEquals(Arrays.asList("t", "u"), sorted(TablesNamesFinder.findTables(
                "DELETE FROM t * AS x USING u WHERE CURRENT OF c")));
        assertEquals(Arrays.asList("s", "t"), sorted(TablesNamesFinder.findTables(
                "MERGE INTO ONLY (t) x USING s ON x.id = s.id WHEN NOT MATCHED THEN DO NOTHING")));
    }

    @Test
    void statementBoundaries() throws JSQLParserException {
        Statements statements = CCJSqlParserUtil.parseStatements(
                MERGE + "WHEN MATCHED THEN DO NOTHING; "
                        + "UPDATE t SET (a, b) = ROW(1, 2) WHERE CURRENT OF c; "
                        + "DELETE FROM ONLY t x WHERE CURRENT OF c; "
                        + MERGE + "WHEN NOT MATCHED THEN INSERT DEFAULT VALUES; "
                        + "DO $$ BEGIN NULL; END $$; "
                        + "UPDATE t * SET a = DEFAULT",
                POSTGRESQL);
        assertEquals(6, statements.size());
        assertInstanceOf(Merge.class, statements.get(0));
        assertEquals("c", ((Update) statements.get(1)).getCursorName());
        assertEquals("c", ((Delete) statements.get(2)).getCursorName());
        assertTrue(((MergeInsert) ((Merge) statements.get(3)).getOperations().get(0))
                .isDefaultValues());
        assertTrue(((Update) statements.get(5)).getTableReference().isIncludeDescendants());
    }

    @Test
    void validation() {
        ValidationTestAsserts.validateNoErrors("UPDATE ONLY t AS x SET (a, b) = ROW(1, 2)", 1,
                PostgresqlVersion.V14);
        ValidationTestAsserts.validateNoErrors("DELETE FROM ONLY t x WHERE CURRENT OF c", 1,
                PostgresqlVersion.V14);
        ValidationTestAsserts.validateNoErrors(
                MERGE + "WHEN MATCHED AND t.a > 1 THEN DO NOTHING"
                        + " WHEN NOT MATCHED THEN INSERT DEFAULT VALUES",
                1, FeaturesAllowed.DML);
    }
}
