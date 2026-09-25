/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.alter;

import static org.junit.jupiter.api.Assertions.*;
import java.util.ArrayList;
import java.util.List;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.parser.AbstractJSqlParser.Dialect;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.sf.jsqlparser.statement.create.table.TableOption;
import net.sf.jsqlparser.util.TableDefinitionTraversal;
import net.sf.jsqlparser.util.deparser.StatementDeParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class MySqlAlterOrderingOptionsTest {
    @ParameterizedTest
    @ValueSource(strings = {"ORDER BY id DESC, label ASC", "ORDER BY id, label",
            "ENGINE = InnoDB, ROW_FORMAT = DYNAMIC, STATS_PERSISTENT = DEFAULT",
            "STATS_AUTO_RECALC = 1, STATS_SAMPLE_PAGES = 16",
            "ORDER BY id DESC, label ASC, ENGINE = InnoDB", "ROW_FORMAT COMPACT, ORDER BY id DESC"})
    void actionAndOrderListBoundariesRemainSeparate(String actions) throws JSQLParserException {
        Alter alter = parse("ALTER TABLE t " + actions);
        assertEquals("ALTER TABLE t " + actions, alter.toString());
        alter.getAlterExpressions().forEach(
                action -> assertNotEquals(AlterOperation.UNSPECIFIC, action.getOperation()));
        assertRoundTrip(alter);
        assertEquals(2, CCJSqlParserUtil
                .parseStatements(alter + "; SELECT 1", p -> p.withDialect(Dialect.MYSQL)).size());
    }

    @Test
    void orderElementsAreMutableAndVisited() throws JSQLParserException {
        Alter alter = parse("ALTER TABLE t ORDER BY id DESC, label ASC");
        AlterExpressionOrderBy order = (AlterExpressionOrderBy) alter.getAlterExpressions().get(0);
        assertEquals(2, order.getOrderByElements().size());
        order.getOrderByElements().get(0).setExpression(new Column("new_id"));
        order.getOrderByElements().get(0).setAsc(true);
        List<Expression> visited = new ArrayList<>();
        TableDefinitionTraversal.visit(order, visited::add, ignored -> {
        });
        assertEquals(2, visited.size());
        assertEquals("ALTER TABLE t ORDER BY new_id ASC, label ASC", alter.toString());
        assertRoundTrip(alter);
    }

    @Test
    void rowFormatAndStatisticsShareCreateOptions() throws JSQLParserException {
        Alter alter = parse("ALTER TABLE t ROW_FORMAT = DYNAMIC, STATS_PERSISTENT = DEFAULT");
        TableOption rowFormat = ((AlterExpressionTableOption) alter.getAlterExpressions().get(0))
                .getStructuredTableOption();
        TableOption statistics = ((AlterExpressionTableOption) alter.getAlterExpressions().get(1))
                .getStructuredTableOption();
        assertEquals(TableOption.Kind.ROW_FORMAT, rowFormat.getKind());
        assertEquals(TableOption.Kind.STATS_PERSISTENT, statistics.getKind());
        rowFormat.setValue("COMPACT");
        statistics.setValue("1");
        assertEquals("ALTER TABLE t ROW_FORMAT = COMPACT, STATS_PERSISTENT = 1", alter.toString());
        CreateTable create =
                (CreateTable) CCJSqlParserUtil.parse("CREATE TABLE t (id INT) ROW_FORMAT = COMPACT",
                        p -> p.withDialect(Dialect.MYSQL));
        assertEquals(rowFormat.toString(),
                create.getTableOption(TableOption.Kind.ROW_FORMAT).orElseThrow().toString());
        assertRoundTrip(alter);
    }

    @Test
    void invalidOptionsAndOrderExpressionsFail() {
        for (String actions : new String[] {"ROW_FORMAT = invalid", "STATS_PERSISTENT = 2",
                "ORDER BY id + 1"}) {
            assertThrows(JSQLParserException.class, () -> parse("ALTER TABLE t " + actions));
        }
    }

    private static Alter parse(String sql) throws JSQLParserException {
        return (Alter) CCJSqlParserUtil.parse(sql, p -> p.withDialect(Dialect.MYSQL));
    }

    private static void assertRoundTrip(Alter alter) throws JSQLParserException {
        StringBuilder sql = new StringBuilder();
        alter.accept(new StatementDeParser(sql), null);
        assertEquals(alter.toString(), sql.toString());
        assertEquals(alter.toString(), parse(sql.toString()).toString());
    }
}
