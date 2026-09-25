/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.create;

import static org.junit.jupiter.api.Assertions.*;
import java.util.List;
import java.util.Set;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.AbstractJSqlParser.Dialect;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.alter.AlterStatistics;
import net.sf.jsqlparser.statement.create.statistics.CreateStatistics;
import net.sf.jsqlparser.util.TablesNamesFinder;
import net.sf.jsqlparser.util.deparser.StatementDeParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class PostgreSqlStatisticsDdlTest {
    @ParameterizedTest
    @ValueSource(strings = {
            "CREATE STATISTICS st ON id,label FROM t",
            "CREATE STATISTICS st ON (lower(label)) FROM t",
            "CREATE STATISTICS st ON (id+1),label FROM t",
            "CREATE STATISTICS IF NOT EXISTS st ON id,label FROM t",
            "CREATE STATISTICS IF NOT EXISTS st ON (lower(label)) FROM t",
            "CREATE STATISTICS IF NOT EXISTS st ON (id+1),label FROM t",
            "CREATE STATISTICS ON id,label FROM t", "CREATE STATISTICS ON (lower(label)) FROM t",
            "CREATE STATISTICS ON (id+1),label FROM t",
            "CREATE STATISTICS st(ndistinct) ON id,label FROM t",
            "CREATE STATISTICS st(dependencies) ON id,label FROM t",
            "CREATE STATISTICS st(mcv) ON id,label FROM t",
            "CREATE STATISTICS st(ndistinct,dependencies,mcv) ON id,label FROM t",
            "ALTER STATISTICS st OWNER TO CURRENT_USER", "ALTER STATISTICS st RENAME TO st2",
            "ALTER STATISTICS st SET SCHEMA ddl_aux", "ALTER STATISTICS st SET STATISTICS 500",
            "ALTER STATISTICS st SET STATISTICS DEFAULT", "DROP STATISTICS st",
            "DROP STATISTICS IF EXISTS st,st2 RESTRICT"})
    void definitionAndActionsRoundTrip(String sql) throws JSQLParserException {
        assertRoundTrip(parse(sql));
        assertEquals(2, CCJSqlParserUtil.parseStatements(sql + "; SELECT 1",
                p -> p.withDialect(Dialect.POSTGRESQL)).size());
    }

    @Test
    void namesKindsAndKeyExpressionsCanBeChanged() throws JSQLParserException {
        CreateStatistics statistics = (CreateStatistics) parse(
                "CREATE STATISTICS IF NOT EXISTS old_stats(ndistinct) ON id,label FROM t");
        assertTrue(statistics.isIfNotExists());
        assertEquals(List.of(CreateStatistics.Kind.NDISTINCT), statistics.getKinds());
        statistics.setName("audit.new_stats").setKinds(List.of(CreateStatistics.Kind.DEPENDENCIES))
                .setTable(new Table("other_table"));
        statistics.getExpressions().set(0, new Column("new_id"));
        assertEquals(
                "CREATE STATISTICS IF NOT EXISTS audit.new_stats (dependencies) ON new_id, label FROM other_table",
                statistics.toString());
        assertRoundTrip(statistics);
        assertEquals(Set.of("other_table"), TablesNamesFinder.findTables(statistics.toString()));
    }

    @Test
    void statisticsTargetDistinguishesDefault() throws JSQLParserException {
        AlterStatistics statistics =
                (AlterStatistics) parse("ALTER STATISTICS st SET STATISTICS DEFAULT");
        assertEquals(AlterStatistics.Action.SET_STATISTICS, statistics.getAction());
        assertNull(statistics.getStatistics());
        statistics.setName("new_st").setStatistics(200);
        assertEquals("ALTER STATISTICS new_st SET STATISTICS 200", statistics.toString());
        assertRoundTrip(statistics);
        assertTrue(TablesNamesFinder.findTables(statistics.toString()).isEmpty());
    }

    @Test
    void malformedDefinitionsAndUnknownKindsFail() {
        for (String sql : new String[] {"CREATE STATISTICS st ON id FROM t",
                "CREATE STATISTICS IF NOT EXISTS ON id,label FROM t",
                "CREATE STATISTICS st(unknown) ON id,label FROM t",
                "CREATE STATISTICS st(mcv,mcv) ON id,label FROM t",
                "CREATE STATISTICS st(mcv) ON (id+1) FROM t",
                "CREATE STATISTICS st ON id+1,label FROM t",
                "ALTER STATISTICS st SET STATISTICS 10001"}) {
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
