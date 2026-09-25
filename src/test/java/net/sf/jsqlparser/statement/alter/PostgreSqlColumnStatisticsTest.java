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
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.parser.AbstractJSqlParser.Dialect;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.util.deparser.StatementDeParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class PostgreSqlColumnStatisticsTest {
    @ParameterizedTest
    @ValueSource(strings = {"SET STATISTICS DEFAULT", "SET STATISTICS -1", "SET STATISTICS 10000",
            "SET (n_distinct = -0.5)", "SET (n_distinct = -1, n_distinct_inherited = 20)",
            "RESET (n_distinct)", "RESET (n_distinct, n_distinct_inherited)"})
    void tablesAndMaterializedViewsShareColumnActions(String action) throws JSQLParserException {
        for (String object : List.of("TABLE t", "MATERIALIZED VIEW mv")) {
            String sql = "ALTER " + object + " ALTER COLUMN id " + action;
            Statement statement = parse(sql);
            assertRoundTrip(statement);
            assertEquals(2, CCJSqlParserUtil.parseStatements(sql + "; SELECT 1",
                    p -> p.withDialect(Dialect.POSTGRESQL)).size());
        }
    }

    @Test
    void defaultTargetsAndAttributeValuesAreMutable() throws JSQLParserException {
        AlterRelation index =
                (AlterRelation) parse("ALTER INDEX ix ALTER COLUMN 1 SET STATISTICS DEFAULT");
        RelationAlterAction action = index.getActions().get(0);
        assertTrue(action.isStatisticsDefault());
        assertNull(action.getStatistics());
        action.setStatistics(200L);
        assertFalse(action.isStatisticsDefault());
        assertEquals("ALTER INDEX ix ALTER COLUMN 1 SET STATISTICS 200", index.toString());
        assertRoundTrip(index);
        action.setStatisticsDefault(true);
        assertRoundTrip(index);
        Alter table = (Alter) parse("ALTER TABLE t ALTER COLUMN id SET (n_distinct=10)");
        action = (RelationAlterAction) table.getAlterExpressions().get(0);
        action.getOptions().get(0).setValue(new LongValue(20));
        List<Expression> visited = new ArrayList<>();
        action.visitExpressions(visited::add);
        assertEquals(List.of(new LongValue(20)), visited);
        assertEquals("ALTER TABLE t ALTER COLUMN id SET (n_distinct = 20)", table.toString());
        assertRoundTrip(table);
    }

    @Test
    void invalidTargetsAndCrossObjectActionsFail() {
        for (String sql : new String[] {"ALTER VIEW v ALTER COLUMN id SET STATISTICS DEFAULT",
                "ALTER INDEX ix ALTER COLUMN 1 SET (n_distinct=10)",
                "ALTER TABLE t ALTER COLUMN id SET (n_distinct)",
                "ALTER TABLE t ALTER COLUMN id SET STATISTICS 10001"}) {
            assertThrows(JSQLParserException.class, () -> parse(sql), sql);
        }
    }

    private static Statement parse(String sql) throws JSQLParserException {
        return CCJSqlParserUtil.parse(sql, p -> p.withDialect(Dialect.POSTGRESQL));
    }

    private static void assertRoundTrip(Statement statement) throws JSQLParserException {
        StringBuilder sql = new StringBuilder();
        statement.accept(new StatementDeParser(sql), null);
        assertEquals(statement.toString(), sql.toString());
        assertEquals(statement.toString(), parse(sql.toString()).toString());
    }
}
