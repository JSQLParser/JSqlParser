/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.insert;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.parser.AbstractJSqlParser.Dialect;
import net.sf.jsqlparser.parser.CCJSqlParser;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Partition;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.update.UpdateSet;
import net.sf.jsqlparser.util.deparser.ExpressionDeParser;
import net.sf.jsqlparser.util.deparser.InsertDeParser;
import net.sf.jsqlparser.util.deparser.SelectDeParser;
import net.sf.jsqlparser.util.deparser.StatementDeParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/** toString() and the deparsers share Insert.appendTo, so both render the same text. */
class InsertRenderingTest {

    private static final Consumer<CCJSqlParser> MYSQL = parser -> parser.withDialect(Dialect.MYSQL);

    private static String deparse(Statement statement) {
        StringBuilder builder = new StringBuilder();
        statement.accept(new StatementDeParser(builder));
        return builder.toString();
    }

    /** Asserts that toString(), the deparser and a reparse give back exactly {@code sql}. */
    private static Statement assertExact(String sql, Consumer<CCJSqlParser> consumer)
            throws JSQLParserException {
        Statement statement = CCJSqlParserUtil.parse(sql, consumer);
        assertEquals(sql, statement.toString(), "toString()");
        assertEquals(sql, deparse(statement), "StatementDeParser");
        assertEquals(sql, CCJSqlParserUtil.parse(statement.toString(), consumer).toString(),
                "reparse");
        return statement;
    }

    /** Renders column names in brackets, to see which parts the deparser visits. */
    private static String deparseWithMarkedColumns(Insert insert) {
        StringBuilder builder = new StringBuilder();
        SelectDeParser selects = new SelectDeParser(builder);
        ExpressionDeParser expressions = new ExpressionDeParser(selects, builder) {
            @Override
            public <S> StringBuilder visit(Column column, S context) {
                return builder.append('[').append(column.getColumnName()).append(']');
            }
        };
        selects.setExpressionVisitor(expressions);
        new InsertDeParser(expressions, selects, builder).deParse(insert);
        return builder.toString();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            // MySQL and Hive write PARTITION before the column list
            "INSERT INTO t PARTITION (p0) (a) VALUES (1)",
            "INSERT INTO t PARTITION (p0, p1) VALUES (1), (2)",
            "INSERT IGNORE INTO t PARTITION (p0) (a, b) SELECT a, b FROM s",
            "INSERT OVERWRITE TABLE t PARTITION (pt1 = 'pt1', pt2) SELECT * FROM a",
            "INSERT INTO TABLE t PARTITION (dt = 1) SELECT * FROM a",
            // row aliases
            "INSERT INTO t (a, b) VALUES (1, 2) AS new ON DUPLICATE KEY UPDATE b = new.b",
            "INSERT INTO t (a, b) VALUES (1, 2) AS new(x, y) ON DUPLICATE KEY UPDATE b = y",
            "INSERT INTO t SET a = 1, b = 2 AS new ON DUPLICATE KEY UPDATE b = new.b",
            // ON DUPLICATE KEY UPDATE keeps its WHERE in both renderers
            "INSERT INTO t (a) VALUES (1) ON DUPLICATE KEY UPDATE a = 2 WHERE a > 1",
            "INSERT INTO t (a) VALUES (1) ON DUPLICATE KEY UPDATE NOTHING",
            "INSERT LOW_PRIORITY IGNORE INTO t VALUES (1)",
            "INSERT INTO t DEFAULT VALUES",
            "INSERT INTO t1 OUTPUT inserted.* INTO t2 VALUES (1)",
            "INSERT INTO t1 OUTPUT inserted.* DEFAULT VALUES",
            "INSERT INTO t AS x (a) OVERRIDING SYSTEM VALUE VALUES (1)"
                    + " ON CONFLICT ON CONSTRAINT c DO NOTHING RETURNING *",
            "INSERT INTO t (a) VALUES (1) ON CONFLICT (a) WHERE a > 0"
                    + " DO UPDATE SET a = excluded.a WHERE t.a < 5 RETURNING a, b AS c",
            "INSERT INTO t BY NAME SELECT * FROM s",
            "WITH w AS (SELECT 1 AS a) INSERT INTO t SELECT a FROM w",
            "INSERT ALL INTO t1 (a) VALUES (x) INTO t2 VALUES (y) SELECT x, y FROM s",
            "INSERT FIRST WHEN x > 1 THEN INTO t1 VALUES (x) ELSE INTO t2 VALUES (y)"
                    + " SELECT x, y FROM s"
    })
    void insertRendersIdentically(String sql) throws JSQLParserException {
        assertExact(sql, null);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "REPLACE INTO t (a, b) VALUES (1, 2)",
            "REPLACE t (a) SELECT a FROM s",
            "REPLACE INTO t SET a = 1",
            "UPSERT INTO t (a) VALUES (1) ON DUPLICATE KEY UPDATE NOTHING",
            "UPSERT INTO t (a) VALUES (1) ON DUPLICATE KEY UPDATE a = 2 WHERE a > 1"
    })
    void replaceRendersIdentically(String sql) throws JSQLParserException {
        assertExact(sql, MYSQL);
    }

    @Test
    void partitionFollowsTheTable() throws JSQLParserException {
        Insert insert = (Insert) assertExact("INSERT INTO t PARTITION (p0) (a) VALUES (1)", MYSQL);

        assertEquals("p0", insert.getPartitions().get(0).getColumn().getColumnName());
        assertEquals("a", insert.getColumns().get(0).getColumnName());
    }

    @Test
    void optimizerHintComesBeforeThePriority() throws JSQLParserException {
        Insert insert = (Insert) CCJSqlParserUtil
                .parse("INSERT /*+ APPEND */ LOW_PRIORITY IGNORE INTO t VALUES (1)");

        assertEquals("INSERT /*+ APPEND */ LOW_PRIORITY IGNORE INTO t VALUES (1)",
                insert.toString());
        assertEquals(insert.toString(), deparse(insert));
    }

    @Test
    void deparserVisitsConflictAndDuplicateActions() throws JSQLParserException {
        Insert conflict = (Insert) CCJSqlParserUtil.parse("INSERT INTO t (a) VALUES (1)"
                + " ON CONFLICT (a) WHERE a > 0 DO UPDATE SET a = excluded.a WHERE t.a < 5"
                + " RETURNING a");
        // the conflict target names index columns; its predicate and the action are visited
        assertEquals("INSERT INTO t (a) VALUES (1) ON CONFLICT (a) WHERE [a] > 0"
                + " DO UPDATE SET [a] = [a] WHERE [a] < 5 RETURNING [a]",
                deparseWithMarkedColumns(conflict));

        Insert duplicate = (Insert) CCJSqlParserUtil.parse(
                "INSERT INTO t (a) VALUES (b) ON DUPLICATE KEY UPDATE a = c WHERE d > 1");
        assertEquals("INSERT INTO t (a) VALUES ([b]) ON DUPLICATE KEY UPDATE [a] = [c]"
                + " WHERE [d] > 1", deparseWithMarkedColumns(duplicate));

        Insert set = (Insert) CCJSqlParserUtil.parse("INSERT INTO t SET a = b, (c, d) = (1, e)");
        assertEquals("INSERT INTO t SET [a] = [b], ([c], [d]) = (1, [e])",
                deparseWithMarkedColumns(set));
    }

    @Test
    void appendToTakesRenderers() throws JSQLParserException {
        Insert insert = (Insert) CCJSqlParserUtil.parse(
                "WITH w AS (SELECT 1) INSERT INTO t SELECT * FROM w ON DUPLICATE KEY UPDATE a = 1"
                        + " RETURNING a");
        StringBuilder builder = new StringBuilder();

        insert.appendTo(builder, item -> builder.append("<with>"),
                select -> builder.append("<query>"), expression -> builder.append("<e>"),
                item -> builder.append("<item>"));

        assertEquals("WITH <with> INSERT INTO t <query> ON DUPLICATE KEY UPDATE <e> = <e>"
                + " RETURNING <item>", builder.toString());
    }

    @Test
    void duplicateUpdateSetsComeFromTheAction() throws JSQLParserException {
        Insert insert = (Insert) CCJSqlParserUtil.parse("INSERT INTO t (a) VALUES (1)");
        assertNull(insert.getDuplicateUpdateSets());

        List<UpdateSet> sets =
                Collections.singletonList(new UpdateSet(new Column("a"), new LongValue(2)));
        insert.withDuplicateUpdateSets(sets);
        assertSame(sets, insert.getDuplicateUpdateSets());
        assertSame(sets, insert.getDuplicateAction().getUpdateSets());
        assertEquals("INSERT INTO t (a) VALUES (1) ON DUPLICATE KEY UPDATE a = 2",
                insert.toString());
        assertEquals(insert.toString(), deparse(insert));
    }

    @Test
    void builtInsertRendersPartitionsFirst() {
        Insert insert = new Insert().withTable(new Table("t")).addColumns(new Column("a"));
        insert.setPartitions(Collections.singletonList(new Partition(new Column("p0"), null)));
        insert.setOnlyDefaultValues(true);

        assertEquals("INSERT INTO t PARTITION (p0) (a) DEFAULT VALUES", insert.toString());
        assertEquals(insert.toString(), deparse(insert));
    }

    @Test
    void actionsRenderWithoutStraySpaces() {
        InsertDuplicateAction nothing = new InsertDuplicateAction(ConflictActionType.NOTHING);
        assertEquals("NOTHING", nothing.toString());

        InsertConflictAction doNothing = new InsertConflictAction(ConflictActionType.DO_NOTHING);
        assertEquals(" DO NOTHING", doNothing.toString());
    }
}
