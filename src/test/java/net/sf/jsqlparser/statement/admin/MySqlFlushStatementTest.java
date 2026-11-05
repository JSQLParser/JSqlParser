/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.admin;

import static net.sf.jsqlparser.test.TestUtils.assertSqlCanBeParsedAndDeparsed;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.parser.ASTNodeAccess;
import net.sf.jsqlparser.parser.AbstractJSqlParser.Dialect;
import net.sf.jsqlparser.parser.CCJSqlParser;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.parser.Node;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementFeatures;
import net.sf.jsqlparser.statement.Statements;
import net.sf.jsqlparser.statement.StmtFeature;
import net.sf.jsqlparser.statement.TableMaintenanceStatement.Modifier;
import net.sf.jsqlparser.statement.UnsupportedStatement;
import net.sf.jsqlparser.statement.alter.AlterSystemStatement;
import net.sf.jsqlparser.util.TablesNamesFinder;
import net.sf.jsqlparser.util.deparser.StatementDeParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class MySqlFlushStatementTest {
    private static final Consumer<CCJSqlParser> MYSQL = parser -> parser.withDialect(Dialect.MYSQL);
    private static final Consumer<CCJSqlParser> NO_DIALECT = parser -> {
    };

    @ParameterizedTest
    @ValueSource(strings = {
            "FLUSH TABLES",
            "FLUSH TABLE",
            "FLUSH TABLES WITH READ LOCK",
            "FLUSH TABLES t, s WITH READ LOCK",
            "FLUSH TABLES t FOR EXPORT",
            "FLUSH TABLE t",
            "FLUSH TABLES db1.t, `u`",
            "FLUSH LOCAL TABLES t FOR EXPORT",
            "FLUSH NO_WRITE_TO_BINLOG TABLE t WITH READ LOCK",
            "FLUSH PRIVILEGES",
            "FLUSH LOCAL BINARY LOGS",
            "FLUSH NO_WRITE_TO_BINLOG LOGS",
            "FLUSH ERROR LOGS, SLOW LOGS, STATUS",
            "FLUSH RELAY LOGS",
            "FLUSH RELAY LOGS FOR CHANNEL 'c1'",
            "FLUSH LOGS, RELAY LOGS FOR CHANNEL 'c1'",
            "FLUSH OPTIMIZER_COSTS",
            "FLUSH USER_RESOURCES",
            "FLUSH ENGINE LOGS, GENERAL LOGS",
            "FLUSH BINARY LOGS, ENGINE LOGS, ERROR LOGS, GENERAL LOGS, LOGS, OPTIMIZER_COSTS, "
                    + "PRIVILEGES, RELAY LOGS, SLOW LOGS, STATUS, USER_RESOURCES"})
    void parsesMySqlFormsWithAndWithoutDialect(String sql) throws JSQLParserException {
        for (Consumer<CCJSqlParser> dialect : List.of(NO_DIALECT, MYSQL)) {
            Statement statement = assertSqlCanBeParsedAndDeparsed(sql, false, dialect);
            assertInstanceOf(FlushStatement.class, statement);
            assertEquals(sql, deparse(statement));
            assertEquals(sql, CCJSqlParserUtil.parse(statement.toString(), dialect).toString());
        }
    }

    @Test
    void keepsTheOptionsInSourceOrder() throws JSQLParserException {
        FlushStatement flush =
                parse("flush local binary logs, relay logs for channel 'c1', status");
        assertEquals(Modifier.LOCAL, flush.getModifier());
        assertFalse(flush.isFlushTables());
        assertEquals(List.of(FlushOption.Kind.BINARY_LOGS, FlushOption.Kind.RELAY_LOGS,
                FlushOption.Kind.STATUS),
                flush.getOptions().stream().map(FlushOption::getKind)
                        .collect(Collectors.toList()));
        assertNull(flush.getOptions().get(0).getChannel());
        assertEquals("c1", flush.getOptions().get(1).getChannel().getValue());
        assertTrue(flush.getTables().isEmpty());
        assertNull(flush.getTableLock());
        assertEquals("FLUSH LOCAL BINARY LOGS, RELAY LOGS FOR CHANNEL 'c1', STATUS",
                flush.toString());
    }

    @Test
    void keepsTheTablesAndTheirLock() throws JSQLParserException {
        FlushStatement flush = parse("FLUSH NO_WRITE_TO_BINLOG TABLES db1.t, s WITH READ LOCK");
        assertTrue(flush.isFlushTables());
        assertTrue(flush.isTablesKeyword());
        assertEquals(Modifier.NO_WRITE_TO_BINLOG, flush.getModifier());
        assertEquals(List.of("db1.t", "s"), flush.getTables().stream()
                .map(Table::getFullyQualifiedName).collect(Collectors.toList()));
        assertEquals(FlushStatement.TableLock.WITH_READ_LOCK, flush.getTableLock());

        FlushStatement export = parse("FLUSH TABLE t FOR EXPORT");
        assertFalse(export.isTablesKeyword());
        assertNull(export.getModifier());
        assertEquals(FlushStatement.TableLock.FOR_EXPORT, export.getTableLock());

        FlushStatement all = parse("FLUSH TABLES");
        assertTrue(all.getTables().isEmpty());
        assertNull(all.getTableLock());
        assertTrue(all.getOptions().isEmpty());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "FLUSH",
            "FLUSH LOCAL",
            "FLUSH HOSTS",
            "FLUSH QUERY CACHE",
            "FLUSH DES_KEY_FILE",
            "FLUSH LOGS,",
            "FLUSH BINARY",
            "FLUSH STATUS LOGS",
            "FLUSH LOCAL NO_WRITE_TO_BINLOG LOGS",
            "FLUSH TABLES FOR EXPORT",
            "FLUSH TABLES t WITH READ LOCK, PRIVILEGES",
            "FLUSH PRIVILEGES, TABLES",
            "FLUSH TABLES t WITH READ",
            "FLUSH TABLES t FOR UPDATE",
            "FLUSH TABLES t WITH READ LOCK FOR EXPORT",
            "FLUSH RELAY LOGS FOR CHANNEL c1",
            "FLUSH RELAY LOGS FOR CHANNEL `c1`",
            "FLUSH RELAY LOGS FOR CHANNEL",
            "FLUSH BINARY LOGS FOR CHANNEL 'c1'",
            "FLUSH TABLES t,"})
    void rejectsFormsMySqlRejects(String sql) {
        for (Consumer<CCJSqlParser> dialect : List.of(NO_DIALECT, MYSQL)) {
            assertThrows(JSQLParserException.class, () -> CCJSqlParserUtil.parse(sql, dialect),
                    sql);
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"FLUSH TABLES", "FLUSH PRIVILEGES", "FLUSH LOCAL LOGS"})
    void otherDialectsKeepRejectingFlush(String sql) {
        for (Dialect dialect : List.of(Dialect.POSTGRESQL, Dialect.MARIADB, Dialect.ORACLE,
                Dialect.SQLSERVER)) {
            assertThrows(JSQLParserException.class,
                    () -> CCJSqlParserUtil.parse(sql, parser -> parser.withDialect(dialect)),
                    sql + " " + dialect);
        }
    }

    @Test
    void oracleAlterSystemFlushIsUnchanged() throws JSQLParserException {
        Statement statement = assertSqlCanBeParsedAndDeparsed("ALTER SYSTEM FLUSH SHARED_POOL",
                true, parser -> parser.withDialect(Dialect.ORACLE));
        assertInstanceOf(AlterSystemStatement.class, statement);
    }

    @Test
    void wordsRemainUsableAsNames() throws JSQLParserException {
        for (Consumer<CCJSqlParser> dialect : List.of(NO_DIALECT, MYSQL)) {
            assertSqlCanBeParsedAndDeparsed(
                    "SELECT flush, logs, relay, slow, general, privileges, status, channel FROM flush",
                    true, dialect);
            assertSqlCanBeParsedAndDeparsed("SELECT * FROM t AS flush", true, dialect);
        }
    }

    @Test
    void endsTheStatementsAroundIt() throws JSQLParserException {
        Statements statements = CCJSqlParserUtil.parseStatements(
                "SELECT 1; FLUSH TABLES WITH READ LOCK; FLUSH LOGS; UNLOCK TABLES", MYSQL);
        assertEquals(List.of("PlainSelect", "FlushStatement", "FlushStatement",
                "UnlockStatement"), names(statements));
        assertEquals("FLUSH TABLES WITH READ LOCK", statements.get(1).toString());

        // WITH without READ LOCK starts the next statement
        Statements next = CCJSqlParserUtil.parseStatements(
                "SELECT 1; FLUSH TABLES t\nWITH x AS (SELECT 1) SELECT * FROM x", MYSQL);
        assertEquals(List.of("PlainSelect", "FlushStatement", "PlainSelect"), names(next));
        assertEquals("FLUSH TABLES t", next.get(1).toString());
    }

    @Test
    void isAStatementStartForUnsupportedStatements() throws JSQLParserException {
        Statements statements = CCJSqlParserUtil.parseStatements(
                "FOO BAR; FLUSH TABLES; BAZ; FLUSH HOSTS",
                parser -> parser.withUnsupportedStatements(true));
        assertEquals(List.of("UnsupportedStatement", "FlushStatement", "UnsupportedStatement",
                "UnsupportedStatement"), names(statements));
        assertEquals("FLUSH HOSTS", statements.get(3).toString());

        Statements postgres = CCJSqlParserUtil.parseStatements("FLUSH TABLES",
                parser -> parser.withDialect(Dialect.POSTGRESQL).withUnsupportedStatements(true));
        assertInstanceOf(UnsupportedStatement.class, postgres.get(0));
    }

    @Test
    void findsTheFlushedTables() throws JSQLParserException {
        assertThat(new TablesNamesFinder<>()
                .getTables(CCJSqlParserUtil.parse("FLUSH TABLES db1.t, s WITH READ LOCK")))
                .containsExactlyInAnyOrder("db1.t", "s");
        assertThat(new TablesNamesFinder<>().getTables(CCJSqlParserUtil.parse("FLUSH TABLES")))
                .isEmpty();
        assertThat(new TablesNamesFinder<>()
                .getTables(CCJSqlParserUtil.parse("FLUSH PRIVILEGES, LOGS"))).isEmpty();
    }

    @Test
    void reportsSessionFeaturesAndTheLock() throws JSQLParserException {
        StatementFeatures logs = CCJSqlParserUtil.parse("FLUSH LOGS").getFeatures();
        assertThat(logs.getCertain()).containsExactly(StmtFeature.MODIFIES_SESSION);

        for (String sql : new String[] {"FLUSH TABLES WITH READ LOCK",
                "FLUSH TABLE t FOR EXPORT"}) {
            assertThat(CCJSqlParserUtil.parse(sql).getFeatures().getCertain())
                    .containsExactlyInAnyOrder(StmtFeature.MODIFIES_SESSION,
                            StmtFeature.MODIFIES_TRANSACTION);
        }
    }

    @Test
    void nodesSpanTheStatementTablesAndOptions() throws JSQLParserException {
        String sql = "FLUSH LOCAL TABLES db1.t, s WITH READ LOCK";
        FlushStatement flush = parse(sql);
        assertEquals(sql, text(sql, flush));
        assertEquals("db1.t", text(sql, flush.getTables().get(0)));
        assertEquals("s", text(sql, flush.getTables().get(1)));

        sql = "FLUSH ERROR LOGS, RELAY LOGS FOR CHANNEL 'c1', STATUS";
        flush = parse(sql);
        assertEquals(sql, text(sql, flush));
        assertEquals("ERROR LOGS", text(sql, flush.getOptions().get(0)));
        assertEquals("RELAY LOGS FOR CHANNEL 'c1'", text(sql, flush.getOptions().get(1)));
        assertEquals("'c1'", text(sql, flush.getOptions().get(1).getChannel()));
        assertEquals("STATUS", text(sql, flush.getOptions().get(2)));
    }

    @Test
    void buildsStatementsInCode() {
        FlushStatement flush = new FlushStatement();
        assertTrue(flush.isFlushTables());
        assertEquals("FLUSH TABLES", flush.toString());
        flush.withTablesKeyword(false).addTables(new Table("db", "t"))
                .withTableLock(FlushStatement.TableLock.FOR_EXPORT)
                .withModifier(Modifier.NO_WRITE_TO_BINLOG);
        assertEquals("FLUSH NO_WRITE_TO_BINLOG TABLE db.t FOR EXPORT", flush.toString());
        assertEquals(flush.toString(), deparse(flush));

        FlushOption relay = new FlushOption(FlushOption.Kind.RELAY_LOGS)
                .withChannel(new StringValue("c1"));
        FlushStatement options = new FlushStatement()
                .withOptions(List.of(new FlushOption(FlushOption.Kind.OPTIMIZER_COSTS)))
                .addOptions(relay);
        assertFalse(options.isFlushTables());
        assertEquals("FLUSH OPTIMIZER_COSTS, RELAY LOGS FOR CHANNEL 'c1'", options.toString());
        assertEquals(options.toString(), deparse(options));
        options.setOptions(null);
        options.setTables(List.of(new Table("t")));
        assertEquals("FLUSH TABLES t", options.toString());
        options.setTables(null);
        assertEquals("FLUSH TABLES", options.toString());

        assertEquals(FlushOption.Kind.SLOW_LOGS, FlushOption.Kind.fromKeyword("slow logs"));
        assertNull(FlushOption.Kind.fromKeyword("HOSTS"));
        assertNull(FlushOption.Kind.fromKeyword(null));
        assertThrows(NullPointerException.class, () -> relay.setKind(null));
        assertEquals("ENGINE LOGS", new FlushOption(FlushOption.Kind.ENGINE_LOGS).toString());
    }

    private static FlushStatement parse(String sql) throws JSQLParserException {
        return (FlushStatement) assertSqlCanBeParsedAndDeparsed(sql, true, MYSQL);
    }

    private static List<String> names(Statements statements) {
        return statements.stream().map(statement -> statement.getClass().getSimpleName())
                .collect(Collectors.toList());
    }

    static String deparse(Statement statement) {
        StringBuilder builder = new StringBuilder();
        statement.accept(new StatementDeParser(builder), null);
        return builder.toString();
    }

    static String text(String sql, ASTNodeAccess access) {
        Node node = access.getASTNode();
        return sql.substring(node.jjtGetFirstToken().absoluteBegin - 1,
                node.jjtGetLastToken().absoluteEnd - 1);
    }
}
