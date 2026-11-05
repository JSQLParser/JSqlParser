/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.maintenance;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.function.Consumer;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.ASTNodeAccess;
import net.sf.jsqlparser.parser.AbstractJSqlParser.Dialect;
import net.sf.jsqlparser.parser.CCJSqlParser;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.parser.Node;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.analyze.Analyze;
import org.junit.jupiter.api.Test;

/** The maintenance statements, their options and their targets map back to their source. */
class MaintenanceNodeTest {

    private static final Consumer<CCJSqlParser> POSTGRESQL =
            parser -> parser.withDialect(Dialect.POSTGRESQL);

    private static String source(String sql, ASTNodeAccess part) {
        Node node = part.getASTNode();
        return sql.substring(node.jjtGetFirstToken().absoluteBegin - 1,
                node.jjtGetLastToken().absoluteEnd - 1);
    }

    private static Statement parse(String sql) throws JSQLParserException {
        return CCJSqlParserUtil.parse(sql, POSTGRESQL);
    }

    @Test
    void vacuumOptionsTargetsAndColumns() throws JSQLParserException {
        String sql = "VACUUM (VERBOSE, PARALLEL 2) ONLY s.t (a, b), u *";
        VacuumStatement vacuum = (VacuumStatement) parse(sql);

        assertEquals(sql, source(sql, vacuum));
        assertEquals("VERBOSE", source(sql, vacuum.getOptions().get(0)));
        assertEquals("PARALLEL 2", source(sql, vacuum.getOptions().get(1)));
        MaintenanceTarget only = vacuum.getTargets().get(0);
        assertEquals("ONLY s.t (a, b)", source(sql, only));
        assertEquals("s.t", source(sql, only.getTable()));
        assertEquals("b", source(sql, only.getColumns().get(1)));
        assertEquals("u *", source(sql, vacuum.getTargets().get(1)));

        sql = "VACUUM FULL VERBOSE t";
        vacuum = (VacuumStatement) parse(sql);
        assertEquals("FULL", source(sql, vacuum.getOptions().get(0)));
        assertEquals("VERBOSE", source(sql, vacuum.getOptions().get(1)));
        assertEquals("t", source(sql, vacuum.getTargets().get(0)));
    }

    @Test
    void analyzeOfBothForms() throws JSQLParserException {
        String sql = "ANALYZE VERBOSE t (a)";
        Analyze analyze = (Analyze) parse(sql);

        assertEquals(sql, source(sql, analyze));
        assertEquals("VERBOSE", source(sql, analyze.getOptions().get(0)));
        assertEquals("t (a)", source(sql, analyze.getTargets().get(0)));
        assertEquals("a", source(sql, analyze.getTargets().get(0).getColumns().get(0)));

        sql = "ANALYZE (SKIP_LOCKED false) t";
        analyze = (Analyze) parse(sql);
        assertEquals("SKIP_LOCKED false", source(sql, analyze.getOptions().get(0)));

        sql = "ANALYSE (\"verbose\" on) ONLY t";
        analyze = (Analyze) parse(sql);
        assertEquals(sql, source(sql, analyze));
        assertEquals("\"verbose\" on", source(sql, analyze.getOptions().get(0)));
        assertEquals("ONLY t", source(sql, analyze.getTargets().get(0)));

        sql = "ANALYZE TABLE t1, t2 UPDATE HISTOGRAM ON c WITH 8 BUCKETS MANUAL UPDATE";
        analyze = (Analyze) CCJSqlParserUtil.parse(sql,
                parser -> parser.withDialect(Dialect.MYSQL));
        assertEquals(sql, source(sql, analyze));
        assertEquals("t2", source(sql, analyze.getTargets().get(1)));
    }

    @Test
    void clusterAndCheckpoint() throws JSQLParserException {
        String sql = "CLUSTER (VERBOSE) s.t USING i";
        ClusterStatement cluster = (ClusterStatement) parse(sql);

        assertEquals(sql, source(sql, cluster));
        assertEquals("VERBOSE", source(sql, cluster.getOptions().get(0)));
        assertEquals("s.t", source(sql, cluster.getTable()));

        sql = "CLUSTER VERBOSE";
        cluster = (ClusterStatement) parse(sql);
        assertEquals("VERBOSE", source(sql, cluster.getOptions().get(0)));

        sql = "CLUSTER VERBOSE i ON s.t";
        cluster = (ClusterStatement) parse(sql);
        assertEquals(sql, source(sql, cluster));
        assertEquals("s.t", source(sql, cluster.getTable()));

        sql = "CHECKPOINT";
        assertEquals(sql, source(sql, (CheckpointStatement) parse(sql)));
    }
}
