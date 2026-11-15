/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.replication;

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
import net.sf.jsqlparser.expression.DoubleValue;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.NullValue;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.UserVariable;
import net.sf.jsqlparser.expression.operators.relational.ParenthesedExpressionList;
import net.sf.jsqlparser.parser.ASTNodeAccess;
import net.sf.jsqlparser.parser.AbstractJSqlParser.Dialect;
import net.sf.jsqlparser.parser.CCJSqlParser;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.parser.Node;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.PurgeObjectType;
import net.sf.jsqlparser.statement.PurgeStatement;
import net.sf.jsqlparser.statement.ResetStatement;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.Statements;
import net.sf.jsqlparser.statement.StmtFeature;
import net.sf.jsqlparser.statement.UnsupportedStatement;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.sf.jsqlparser.statement.create.user.UserAccount;
import net.sf.jsqlparser.statement.replication.ReplicationSourceOption.GtidAssignment;
import net.sf.jsqlparser.statement.replication.ReplicationSourceOption.Kind;
import net.sf.jsqlparser.statement.replication.ReplicationSourceOption.PrimaryKeyCheck;
import net.sf.jsqlparser.statement.replication.ReplicationSourceOption.ValueType;
import net.sf.jsqlparser.util.TablesNamesFinder;
import net.sf.jsqlparser.util.deparser.StatementDeParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class MySqlReplicationStatementsTest {
    private static final Consumer<CCJSqlParser> MYSQL = parser -> parser.withDialect(Dialect.MYSQL);
    private static final Consumer<CCJSqlParser> MARIADB =
            parser -> parser.withDialect(Dialect.MARIADB);
    private static final Consumer<CCJSqlParser> POSTGRESQL =
            parser -> parser.withDialect(Dialect.POSTGRESQL);
    private static final Consumer<CCJSqlParser> ORACLE =
            parser -> parser.withDialect(Dialect.ORACLE);
    private static final Consumer<CCJSqlParser> NO_DIALECT = parser -> {
    };
    private static final String UUID = "3E11FA47-71CA-11E1-9E33-C80AA9429562";

    @ParameterizedTest
    @ValueSource(strings = {
            "RESET BINARY LOGS AND GTIDS",
            "RESET BINARY LOGS AND GTIDS TO 1234",
            "RESET REPLICA ALL",
            "RESET REPLICA FOR CHANNEL 'c1'",
            "RESET REPLICA ALL FOR CHANNEL 'c1'",
            "RESET REPLICA, BINARY LOGS AND GTIDS TO 5",
            "RESET BINARY LOGS AND GTIDS, REPLICA ALL FOR CHANNEL 'c1'",
            "PURGE BINARY LOGS TO 'b.000010'",
            "PURGE BINARY LOGS BEFORE '2026-01-01 00:00:00'",
            "PURGE BINARY LOGS BEFORE NOW() - INTERVAL 3 DAY",
            "PURGE BINARY LOGS BEFORE (NOW())",
            "PURGE BINARY LOGS BEFORE @cutoff",
            "CHANGE REPLICATION SOURCE TO SOURCE_HOST = 'h', SOURCE_PORT = 3306",
            "CHANGE REPLICATION SOURCE TO SOURCE_AUTO_POSITION = 1, GET_SOURCE_PUBLIC_KEY = 1"
                    + " FOR CHANNEL 'c1'",
            "CHANGE REPLICATION SOURCE TO SOURCE_LOG_FILE = 'b.000001', SOURCE_LOG_POS = 4,"
                    + " IGNORE_SERVER_IDS = (1, 2)",
            "CHANGE REPLICATION SOURCE TO PRIVILEGE_CHECKS_USER = NULL,"
                    + " REQUIRE_TABLE_PRIMARY_KEY_CHECK = OFF,"
                    + " ASSIGN_GTIDS_TO_ANONYMOUS_TRANSACTIONS = LOCAL",
            "CHANGE REPLICATION SOURCE TO SOURCE_BIND = '10.0.0.1', SOURCE_USER = 'repl',"
                    + " SOURCE_PASSWORD = 'secret', SOURCE_CONNECT_RETRY = 60,"
                    + " SOURCE_RETRY_COUNT = 10, SOURCE_DELAY = 0,"
                    + " SOURCE_HEARTBEAT_PERIOD = 2.5",
            "CHANGE REPLICATION SOURCE TO SOURCE_SSL = 1, SOURCE_SSL_CA = 'ca.pem',"
                    + " SOURCE_SSL_CAPATH = '/certs', SOURCE_SSL_CERT = 'cert.pem',"
                    + " SOURCE_SSL_CRL = 'crl.pem', SOURCE_SSL_CRLPATH = '/crl',"
                    + " SOURCE_SSL_KEY = 'key.pem', SOURCE_SSL_CIPHER = 'AES256-SHA',"
                    + " SOURCE_SSL_VERIFY_SERVER_CERT = 1",
            "CHANGE REPLICATION SOURCE TO SOURCE_TLS_VERSION = 'TLSv1.3',"
                    + " SOURCE_TLS_CIPHERSUITES = NULL, SOURCE_PUBLIC_KEY_PATH = 'key.pub',"
                    + " NETWORK_NAMESPACE = 'red'",
            "CHANGE REPLICATION SOURCE TO SOURCE_COMPRESSION_ALGORITHMS = 'zstd',"
                    + " SOURCE_ZSTD_COMPRESSION_LEVEL = 3, REQUIRE_ROW_FORMAT = 1,"
                    + " SOURCE_CONNECTION_AUTO_FAILOVER = 1, GTID_ONLY = 0",
            "CHANGE REPLICATION SOURCE TO RELAY_LOG_FILE = 'r.000001', RELAY_LOG_POS = 4,"
                    + " IGNORE_SERVER_IDS = ()",
            "CHANGE REPLICATION SOURCE TO PRIVILEGE_CHECKS_USER = 'priv'@'localhost',"
                    + " REQUIRE_TABLE_PRIMARY_KEY_CHECK = STREAM,"
                    + " ASSIGN_GTIDS_TO_ANONYMOUS_TRANSACTIONS = '" + UUID
                    + "' FOR CHANNEL 'c1'",
            "CHANGE REPLICATION SOURCE TO PRIVILEGE_CHECKS_USER = priv,"
                    + " REQUIRE_TABLE_PRIMARY_KEY_CHECK = GENERATE,"
                    + " SOURCE_TLS_CIPHERSUITES = 'TLS_AES_128_GCM_SHA256'",
            "change replication source to source_host = 'h',"
                    + " require_table_primary_key_check = on,"
                    + " assign_gtids_to_anonymous_transactions = off",
            "CHANGE REPLICATION FILTER REPLICATE_DO_DB = (d1)",
            "CHANGE REPLICATION FILTER REPLICATE_DO_DB = (d1, d2), REPLICATE_IGNORE_TABLE = (d1.t1),"
                    + " REPLICATE_WILD_DO_TABLE = ('d%.t%'), REPLICATE_REWRITE_DB = ((a, b))"
                    + " FOR CHANNEL 'c1'",
            "CHANGE REPLICATION FILTER REPLICATE_DO_DB = ()",
            "CHANGE REPLICATION FILTER REPLICATE_IGNORE_DB = (), REPLICATE_DO_TABLE = (),"
                    + " REPLICATE_WILD_IGNORE_TABLE = ('db.%'),"
                    + " REPLICATE_REWRITE_DB = ((a, b), (c, d))",
            "CHANGE REPLICATION FILTER REPLICATE_IGNORE_TABLE = (d1.t1, `d 2`.`t 2`),"
                    + " REPLICATE_REWRITE_DB = ()",
            "START REPLICA",
            "START REPLICA IO_THREAD, SQL_THREAD",
            "START REPLICA SQL_THREAD UNTIL SQL_BEFORE_GTIDS = '" + UUID + ":11-56'",
            "START REPLICA UNTIL SOURCE_LOG_FILE = 'b.000002', SOURCE_LOG_POS = 4 USER = 'u'"
                    + " PASSWORD = 'p' FOR CHANNEL 'c1'",
            "START REPLICA UNTIL SQL_AFTER_MTS_GAPS",
            "START REPLICA UNTIL RELAY_LOG_FILE = 'r.000001', RELAY_LOG_POS = 10",
            "START REPLICA SQL_THREAD UNTIL SQL_AFTER_GTIDS = '" + UUID + ":11-56'"
                    + " FOR CHANNEL 'c1'",
            "START REPLICA UNTIL SQL_BEFORE_GTIDS = '" + UUID + ":1-5', SOURCE_LOG_POS = 4",
            "START REPLICA IO_THREAD USER = 'u' PASSWORD = 'p'"
                    + " DEFAULT_AUTH = 'caching_sha2_password'"
                    + " PLUGIN_DIR = '/usr/lib/mysql/plugin'",
            "START REPLICA FOR CHANNEL 'c1'",
            "STOP REPLICA",
            "STOP REPLICA IO_THREAD",
            "STOP REPLICA IO_THREAD, SQL_THREAD",
            "STOP REPLICA SQL_THREAD FOR CHANNEL 'c1'",
            "STOP REPLICA FOR CHANNEL 'c1'"})
    void parsesMySqlFormsWithAndWithoutDialect(String sql) throws JSQLParserException {
        for (Consumer<CCJSqlParser> dialect : List.of(NO_DIALECT, MYSQL)) {
            Statement statement = assertSqlCanBeParsedAndDeparsed(sql, true, dialect);
            assertEquals(sql.toUpperCase(), deparse(statement).toUpperCase());
            assertEquals(statement.toString(),
                    CCJSqlParserUtil.parse(statement.toString(), dialect).toString());
        }
    }

    @Test
    void keepsTheSourceOptionsAndTheirValues() throws JSQLParserException {
        ChangeReplicationSourceStatement change = parse(
                "CHANGE REPLICATION SOURCE TO SOURCE_HOST = 'h', SOURCE_PORT = 3306,"
                        + " SOURCE_HEARTBEAT_PERIOD = 2.5, IGNORE_SERVER_IDS = (1, 2),"
                        + " PRIVILEGE_CHECKS_USER = 'priv'@'%',"
                        + " SOURCE_TLS_CIPHERSUITES = NULL,"
                        + " REQUIRE_TABLE_PRIMARY_KEY_CHECK = GENERATE,"
                        + " ASSIGN_GTIDS_TO_ANONYMOUS_TRANSACTIONS = '" + UUID + "'"
                        + " FOR CHANNEL 'c1'",
                MYSQL);
        List<ReplicationSourceOption> options = change.getOptions();
        assertEquals(List.of(Kind.SOURCE_HOST, Kind.SOURCE_PORT, Kind.SOURCE_HEARTBEAT_PERIOD,
                Kind.IGNORE_SERVER_IDS, Kind.PRIVILEGE_CHECKS_USER, Kind.SOURCE_TLS_CIPHERSUITES,
                Kind.REQUIRE_TABLE_PRIMARY_KEY_CHECK,
                Kind.ASSIGN_GTIDS_TO_ANONYMOUS_TRANSACTIONS),
                options.stream().map(ReplicationOption::getKind).collect(Collectors.toList()));
        assertEquals("h", ((StringValue) options.get(0).getValue()).getValue());
        assertEquals(3306L, ((LongValue) options.get(1).getValue()).getValue());
        assertEquals(2.5, ((DoubleValue) options.get(2).getValue()).getValue());
        ParenthesedExpressionList<?> ids =
                (ParenthesedExpressionList<?>) options.get(3).getValue();
        assertEquals("(1, 2)", ids.toString());

        UserAccount account = options.get(4).getAccount();
        assertEquals("priv", account.getUser().getValue());
        assertEquals("%", account.getHost().getValue());
        assertNull(options.get(4).getValue());
        assertEquals("'priv'@'%'", options.get(4).getValueText());

        assertInstanceOf(NullValue.class, options.get(5).getValue());
        assertNull(options.get(5).getAccount());
        assertEquals(PrimaryKeyCheck.GENERATE, options.get(6).getPrimaryKeyCheck());
        assertEquals(GtidAssignment.UUID, options.get(7).getGtidAssignment());
        assertEquals(UUID, options.get(7).getValueText());
        assertNull(options.get(0).getPrimaryKeyCheck());
        assertNull(options.get(0).getGtidAssignment());
        assertEquals("c1", change.getChannel().getValue());

        ChangeReplicationSourceStatement keywords = parse(
                "CHANGE REPLICATION SOURCE TO PRIVILEGE_CHECKS_USER = NULL,"
                        + " REQUIRE_TABLE_PRIMARY_KEY_CHECK = on,"
                        + " ASSIGN_GTIDS_TO_ANONYMOUS_TRANSACTIONS = LOCAL",
                MYSQL);
        assertInstanceOf(NullValue.class, keywords.getOptions().get(0).getValue());
        assertNull(keywords.getOptions().get(0).getAccount());
        assertInstanceOf(Column.class, keywords.getOptions().get(1).getValue());
        assertEquals(PrimaryKeyCheck.ON, keywords.getOptions().get(1).getPrimaryKeyCheck());
        assertEquals(GtidAssignment.LOCAL, keywords.getOptions().get(2).getGtidAssignment());
        assertNull(keywords.getChannel());
    }

    @Test
    void readsUnquotedAccountsOfPrivilegeChecksUser() throws JSQLParserException {
        for (String user : new String[] {"priv", "priv@localhost", "'priv'@localhost",
                "priv@'localhost'", "`priv`@`localhost`"}) {
            ChangeReplicationSourceStatement change = parse(
                    "CHANGE REPLICATION SOURCE TO PRIVILEGE_CHECKS_USER = " + user, MYSQL);
            UserAccount account = change.getOptions().get(0).getAccount();
            assertEquals("priv", account.getUser().getValue(), user);
            assertEquals(user.contains("@") ? "localhost" : null,
                    account.getHost() == null ? null : account.getHost().getValue());
        }
    }

    @Test
    void describesTheValueOfEveryOption() {
        assertEquals(ValueType.STRING, Kind.SOURCE_HOST.getValueType());
        assertEquals(ValueType.NUMBER, Kind.SOURCE_LOG_POS.getValueType());
        assertEquals(ValueType.ZERO_OR_ONE, Kind.GTID_ONLY.getValueType());
        assertEquals(ValueType.NUMBER_LIST, Kind.IGNORE_SERVER_IDS.getValueType());
        assertEquals(ValueType.ACCOUNT_OR_NULL, Kind.PRIVILEGE_CHECKS_USER.getValueType());
        assertEquals(ValueType.STRING_OR_NULL, Kind.SOURCE_TLS_CIPHERSUITES.getValueType());
        assertTrue(ValueType.ZERO_OR_ONE.accepts(new LongValue(0)));
        assertFalse(ValueType.ZERO_OR_ONE.accepts(new LongValue(2)));
        assertFalse(ValueType.STRING.accepts(new NullValue()));
        assertTrue(ValueType.NONE.accepts(null));
        assertFalse(ValueType.PRIMARY_KEY_CHECK.accepts(new StringValue("ON")));
        assertTrue(ValueType.GTID_ASSIGNMENT.accepts(new Column("off")));
    }

    @Test
    void keepsTheFiltersAndTheirLists() throws JSQLParserException {
        ChangeReplicationFilterStatement change = parse(
                "CHANGE REPLICATION FILTER REPLICATE_DO_DB = (d1, `d 2`),"
                        + " REPLICATE_IGNORE_TABLE = (d1.t1), REPLICATE_WILD_DO_TABLE = ('d%.t%'),"
                        + " REPLICATE_REWRITE_DB = ((a, b), (c, d)), REPLICATE_IGNORE_DB = ()"
                        + " FOR CHANNEL 'c1'",
                MYSQL);
        List<ReplicationFilter> filters = change.getFilters();
        assertEquals(5, filters.size());
        assertEquals(ReplicationFilter.Kind.REPLICATE_DO_DB, filters.get(0).getKind());
        assertEquals(List.of("d1", "`d 2`"), filters.get(0).getDatabases());
        assertEquals(filters.get(0).getDatabases(), filters.get(0).getValues());
        assertThat(filters.get(0).getTables()).isEmpty();

        Table table = filters.get(1).getTables().get(0);
        assertEquals("d1", table.getSchemaName());
        assertEquals("t1", table.getName());
        assertEquals(ReplicationFilter.ValueType.TABLES,
                filters.get(1).getKind().getValueType());

        assertEquals("d%.t%", filters.get(2).getPatterns().get(0).getValue());
        ReplicationFilter.DatabaseRewrite rewrite = filters.get(3).getRewrites().get(1);
        assertEquals("c", rewrite.getFrom());
        assertEquals("d", rewrite.getTo());
        assertThat(filters.get(4).getValues()).isEmpty();
        assertEquals("REPLICATE_IGNORE_DB = ()", filters.get(4).toString());
        assertEquals("c1", change.getChannel().getValue());
    }

    @Test
    void keepsTheThreadsConditionsAndConnectionOptions() throws JSQLParserException {
        StartReplicaStatement start = parse("START REPLICA IO_THREAD, SQL_THREAD"
                + " UNTIL SOURCE_LOG_FILE = 'b.000002', SOURCE_LOG_POS = 4 USER = 'u'"
                + " PASSWORD = 'p' DEFAULT_AUTH = 'a' PLUGIN_DIR = 'd' FOR CHANNEL 'c1'", MYSQL);
        assertEquals(List.of(ReplicaThread.IO_THREAD, ReplicaThread.SQL_THREAD),
                start.getThreads());
        assertEquals(ReplicaUntilOption.Kind.SOURCE_LOG_FILE,
                start.getUntilOptions().get(0).getKind());
        assertEquals("b.000002", start.getUntilOptions().get(0).getValueText());
        assertEquals(4L, ((LongValue) start.getUntilOptions().get(1).getValue()).getValue());
        assertEquals("u", start.getUser().getValue());
        assertEquals("p", start.getPassword().getValue());
        assertEquals("a", start.getDefaultAuth().getValue());
        assertEquals("d", start.getPluginDir().getValue());
        assertEquals("c1", start.getChannel().getValue());

        StartReplicaStatement gaps = parse("START REPLICA UNTIL SQL_AFTER_MTS_GAPS", MYSQL);
        ReplicaUntilOption condition = gaps.getUntilOptions().get(0);
        assertEquals(ReplicaUntilOption.Kind.SQL_AFTER_MTS_GAPS, condition.getKind());
        assertNull(condition.getValue());
        assertFalse(condition.getKind().isLogCoordinate());
        assertThat(gaps.getThreads()).isEmpty();
        assertNull(gaps.getUser());
        assertNull(gaps.getChannel());

        StopReplicaStatement stop = parse("STOP REPLICA SQL_THREAD FOR CHANNEL 'c1'", MYSQL);
        assertEquals(List.of(ReplicaThread.SQL_THREAD), stop.getThreads());
        assertEquals("c1", stop.getChannel().getValue());
        assertThat(((StopReplicaStatement) parse("STOP REPLICA", MYSQL)).getThreads()).isEmpty();
    }

    @Test
    void keepsTheResetOptions() throws JSQLParserException {
        ResetReplicationStatement reset = parse(
                "RESET REPLICA ALL FOR CHANNEL 'c1', BINARY LOGS AND GTIDS TO 1234", MYSQL);
        assertEquals(2, reset.getOptions().size());
        ResetReplicationStatement.Option replica = reset.getOptions().get(0);
        assertEquals(ResetReplicationStatement.Kind.REPLICA, replica.getKind());
        assertTrue(replica.isAll());
        assertEquals("c1", replica.getChannel().getValue());
        assertNull(replica.getBinaryLogIndex());
        ResetReplicationStatement.Option logs = reset.getOptions().get(1);
        assertEquals(ResetReplicationStatement.Kind.BINARY_LOGS_AND_GTIDS, logs.getKind());
        assertEquals(1234L, logs.getBinaryLogIndex().getValue());
        assertFalse(logs.isAll());
        assertNull(logs.getChannel());

        ResetReplicationStatement bare = parse("RESET REPLICA", MYSQL);
        assertFalse(bare.getOptions().get(0).isAll());
    }

    @Test
    void extendsPurgeWithBinaryLogs() throws JSQLParserException {
        PurgeStatement to = parse("PURGE BINARY LOGS TO 'b.000010'", MYSQL);
        assertEquals(PurgeObjectType.BINARY_LOGS, to.getPurgeObjectType());
        assertEquals(PurgeStatement.LogBound.TO, to.getLogBound());
        assertEquals("b.000010", ((StringValue) to.getLogExpression()).getValue());
        assertEquals(to.getLogExpression(), to.getObject());

        PurgeStatement before =
                parse("PURGE BINARY LOGS BEFORE NOW() - INTERVAL 3 DAY", NO_DIALECT);
        assertEquals(PurgeStatement.LogBound.BEFORE, before.getLogBound());
        assertEquals("NOW() - INTERVAL 3 DAY", before.getLogExpression().toString());
        assertInstanceOf(UserVariable.class,
                ((PurgeStatement) parse("PURGE BINARY LOGS BEFORE @cutoff", MYSQL))
                        .getLogExpression());

        for (String sql : new String[] {"PURGE BINARY LOGS TO 'b.000010'",
                "PURGE MASTER LOGS BEFORE '2026-01-01 00:00:00'"}) {
            PurgeStatement mariaDb = parse(sql, MARIADB);
            assertEquals(sql, deparse(mariaDb));
        }
        assertEquals(PurgeObjectType.MASTER_LOGS,
                ((PurgeStatement) parse("PURGE MASTER LOGS TO 'b.000010'", MARIADB))
                        .getPurgeObjectType());
        // MariaDB disallows subqueries in PURGE ... BEFORE as well
        assertThrows(JSQLParserException.class, () -> CCJSqlParserUtil
                .parse("PURGE MASTER LOGS BEFORE (SELECT NOW())", MARIADB));

        PurgeStatement table = parse("PURGE TABLE t", ORACLE);
        assertNull(table.getLogBound());
        assertNull(table.getLogExpression());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "RESET REPLICA ALL ALL",
            "RESET BINARY LOGS AND GTIDS TO 1.5",
            "RESET BINARY LOGS AND GTIDS FOR CHANNEL 'c1'",
            "RESET REPLICA TO 5",
            "RESET REPLICA FOR CHANNEL c1",
            "RESET REPLICA FOR CHANNEL N'c1'",
            "RESET BINARY LOGS",
            "RESET REPLICA ALL,",
            "PURGE BINARY LOGS",
            "PURGE BINARY LOGS TO b",
            "PURGE BINARY LOGS AFTER '2026-01-01'",
            "PURGE BINARY LOGS BEFORE (SELECT MAX(ts) FROM t)",
            "PURGE BINARY LOGS BEFORE (SELECT NOW())",
            "PURGE BINARY LOGS BEFORE NOW() + (SELECT 1)",
            "PURGE BINARY LOGS BEFORE DATE_SUB(NOW(), INTERVAL (SELECT 1) DAY)",
            "PURGE BINARY LOGS BEFORE CASE WHEN EXISTS (SELECT 1) THEN NOW() END",
            "PURGE MASTER LOGS TO 'b.000010'",
            "CHANGE MASTER TO MASTER_HOST = 'h'",
            "CHANGE REPLICATION SOURCE TO MASTER_HOST = 'h'",
            "CHANGE REPLICATION SOURCE TO SOURCE_PORT = '3306'",
            "CHANGE REPLICATION SOURCE TO SOURCE_HOST = 3306",
            "CHANGE REPLICATION SOURCE TO SOURCE_PORT = -1",
            "CHANGE REPLICATION SOURCE TO GTID_ONLY = 2",
            "CHANGE REPLICATION SOURCE TO GTID_ONLY = 1.0",
            "CHANGE REPLICATION SOURCE TO SOURCE_HOST = NULL",
            "CHANGE REPLICATION SOURCE TO SOURCE_HOST",
            "CHANGE REPLICATION SOURCE TO `SOURCE_HOST` = 'h'",
            "CHANGE REPLICATION SOURCE TO REQUIRE_TABLE_PRIMARY_KEY_CHECK = LOCAL",
            "CHANGE REPLICATION SOURCE TO ASSIGN_GTIDS_TO_ANONYMOUS_TRANSACTIONS = ON",
            "CHANGE REPLICATION SOURCE TO IGNORE_SERVER_IDS = 1",
            "CHANGE REPLICATION SOURCE TO IGNORE_SERVER_IDS = ('1')",
            "CHANGE REPLICATION SOURCE TO PRIVILEGE_CHECKS_USER = CURRENT_USER",
            "CHANGE REPLICATION SOURCE TO PRIVILEGE_CHECKS_USER = CURRENT_USER()",
            "CHANGE REPLICATION SOURCE TO",
            "CHANGE REPLICATION SOURCE TO SOURCE_HOST = 'h' FOR CHANNEL c1",
            "CHANGE REPLICATION SOURCE TO SOURCE_HOST = 'h',",
            "CHANGE REPLICATION SOURCE SOURCE_HOST = 'h'",
            "CHANGE REPLICATION FILTER REPLICATE_DO_DB = d1",
            "CHANGE REPLICATION FILTER REPLICATE_DO_DB = (d1.t1)",
            "CHANGE REPLICATION FILTER REPLICATE_DO_TABLE = (t1)",
            "CHANGE REPLICATION FILTER REPLICATE_DO_TABLE = (d1.t1, t2)",
            "CHANGE REPLICATION FILTER REPLICATE_WILD_DO_TABLE = (d1.t1)",
            "CHANGE REPLICATION FILTER REPLICATE_REWRITE_DB = (a, b)",
            "CHANGE REPLICATION FILTER REPLICATE_REWRITE_DB = ((a))",
            "CHANGE REPLICATION FILTER REPLICATE_DO_DB = ('d1')",
            "CHANGE REPLICATION FILTER REPLICATE_SAME_DB = (d1)",
            "CHANGE REPLICATION FILTER",
            "START REPLICA UNTIL SOURCE_LOG_POS = 4, SQL_AFTER_MTS_GAPS",
            "START REPLICA UNTIL SQL_AFTER_MTS_GAPS = 1",
            "START REPLICA UNTIL SQL_BEFORE_GTIDS",
            "START REPLICA UNTIL SOURCE_HOST = 'h'",
            "START REPLICA UNTIL",
            "START REPLICA PASSWORD = 'p' USER = 'u'",
            "START REPLICA USER = u",
            "START REPLICA IO_THREAD,",
            "STOP REPLICA UNTIL SQL_AFTER_MTS_GAPS",
            "STOP REPLICA USER = 'u'",
            "STOP REPLICA IO_THREAD SQL_THREAD",
            "START SLAVE",
            "STOP SLAVE"})
    void rejectsFormsMySqlRejects(String sql) {
        for (Consumer<CCJSqlParser> dialect : List.of(NO_DIALECT, MYSQL)) {
            assertThrows(JSQLParserException.class, () -> CCJSqlParserUtil.parse(sql, dialect),
                    sql);
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "START REPLICA",
            "STOP REPLICA",
            "CHANGE REPLICATION SOURCE TO SOURCE_HOST = 'h'",
            "CHANGE REPLICATION FILTER REPLICATE_DO_DB = (d1)",
            "RESET BINARY LOGS AND GTIDS",
            "RESET REPLICA ALL",
            "PURGE BINARY LOGS TO 'b.000010'"})
    void otherDialectsKeepRejectingMySqlForms(String sql) {
        assertThrows(JSQLParserException.class, () -> CCJSqlParserUtil.parse(sql, POSTGRESQL));
        assertThrows(JSQLParserException.class, () -> CCJSqlParserUtil.parse(sql, ORACLE));
        if (!sql.startsWith("PURGE")) {
            assertThrows(JSQLParserException.class, () -> CCJSqlParserUtil.parse(sql, MARIADB));
        }
    }

    @Test
    void keepsTheOtherReadingsOfResetPurgeAndStart() throws JSQLParserException {
        // without the MySQL dialect a bare RESET REPLICA stays a RESET of a parameter
        for (Consumer<CCJSqlParser> dialect : List.of(NO_DIALECT, POSTGRESQL, MARIADB)) {
            ResetStatement reset = (ResetStatement) assertSqlCanBeParsedAndDeparsed(
                    "RESET REPLICA", true, dialect);
            assertEquals("REPLICA", reset.getName());
        }
        assertInstanceOf(ResetReplicationStatement.class,
                CCJSqlParserUtil.parse("RESET REPLICA", MYSQL));
        for (Consumer<CCJSqlParser> dialect : List.of(NO_DIALECT, MYSQL, POSTGRESQL)) {
            assertInstanceOf(ResetStatement.class,
                    assertSqlCanBeParsedAndDeparsed("RESET binary", true, dialect));
            assertInstanceOf(ResetStatement.class,
                    assertSqlCanBeParsedAndDeparsed("RESET ALL", true, dialect));
            assertSqlCanBeParsedAndDeparsed("START TRANSACTION", true, dialect);
        }
        for (Consumer<CCJSqlParser> dialect : List.of(NO_DIALECT, ORACLE)) {
            for (String sql : new String[] {"PURGE TABLE t", "PURGE INDEX i", "PURGE RECYCLEBIN",
                    "PURGE DBA_RECYCLEBIN", "PURGE TABLESPACE ts USER u"}) {
                PurgeStatement purge =
                        (PurgeStatement) assertSqlCanBeParsedAndDeparsed(sql, true, dialect);
                assertNull(purge.getLogBound());
            }
        }
        assertThrows(JSQLParserException.class,
                () -> CCJSqlParserUtil.parse("PURGE MASTER LOGS TO 'b.000010'", NO_DIALECT));
    }

    @Test
    void wordsRemainUsableAsNames() throws JSQLParserException {
        for (Consumer<CCJSqlParser> dialect : List.of(NO_DIALECT, MYSQL, POSTGRESQL)) {
            assertSqlCanBeParsedAndDeparsed(
                    "SELECT stop, replica, replication, channel, source, logs FROM stop", true,
                    dialect);
            assertSqlCanBeParsedAndDeparsed("SELECT stop.stop FROM stop.stop AS stop", true,
                    dialect);
            assertSqlCanBeParsedAndDeparsed("SELECT * FROM t stop", true, dialect);
            assertInstanceOf(CreateTable.class, assertSqlCanBeParsedAndDeparsed(
                    "CREATE TABLE stop (stop INT, replica INT)", true, dialect));
        }
        for (Consumer<CCJSqlParser> dialect : List.of(NO_DIALECT, ORACLE)) {
            PurgeStatement purge = (PurgeStatement) assertSqlCanBeParsedAndDeparsed(
                    "PURGE TABLESPACE stop USER stop", true, dialect);
            assertEquals("stop", purge.getObject());
            assertEquals("stop", purge.getUserName());
        }
    }

    @Test
    void endsTheStatementsAroundIt() throws JSQLParserException {
        Statements statements = CCJSqlParserUtil.parseStatements(
                "STOP REPLICA; CHANGE REPLICATION SOURCE TO SOURCE_HOST = 'h';"
                        + " CHANGE REPLICATION FILTER REPLICATE_DO_DB = (d1);"
                        + " RESET REPLICA ALL; RESET BINARY LOGS AND GTIDS;"
                        + " PURGE BINARY LOGS TO 'b.000001'; START REPLICA; SELECT 1",
                MYSQL);
        assertEquals(List.of("StopReplicaStatement", "ChangeReplicationSourceStatement",
                "ChangeReplicationFilterStatement", "ResetReplicationStatement",
                "ResetReplicationStatement", "PurgeStatement", "StartReplicaStatement",
                "PlainSelect"), names(statements));
    }

    @Test
    void isAStatementStartForUnsupportedStatements() throws JSQLParserException {
        Statements statements = CCJSqlParserUtil.parseStatements(
                "FOO BAR; STOP REPLICA; BAZ; CHANGE REPLICATION FILTER REPLICATE_DO_DB = (d1)",
                parser -> parser.withUnsupportedStatements(true));
        assertEquals(List.of("UnsupportedStatement", "StopReplicaStatement",
                "UnsupportedStatement", "ChangeReplicationFilterStatement"), names(statements));

        Statements postgres = CCJSqlParserUtil.parseStatements("STOP REPLICA",
                parser -> parser.withDialect(Dialect.POSTGRESQL).withUnsupportedStatements(true));
        assertInstanceOf(UnsupportedStatement.class, postgres.get(0));
    }

    @Test
    void findsTheTablesFiltersName() throws JSQLParserException {
        Statement filter = CCJSqlParserUtil.parse("CHANGE REPLICATION FILTER"
                + " REPLICATE_DO_TABLE = (d1.t1), REPLICATE_IGNORE_TABLE = (d2.t2),"
                + " REPLICATE_WILD_DO_TABLE = ('d3.%'), REPLICATE_DO_DB = (d4)");
        assertThat(new TablesNamesFinder<>().getTables(filter))
                .containsExactlyInAnyOrder("d1.t1", "d2.t2");
        for (String sql : new String[] {"PURGE BINARY LOGS TO 'b.000001'",
                "PURGE BINARY LOGS BEFORE NOW()", "START REPLICA",
                "STOP REPLICA", "RESET REPLICA ALL",
                "CHANGE REPLICATION SOURCE TO SOURCE_HOST = 'h'"}) {
            assertThat(new TablesNamesFinder<>().getTables(CCJSqlParserUtil.parse(sql)))
                    .isEmpty();
        }
    }

    @Test
    void reportsSessionFeatures() throws JSQLParserException {
        for (String sql : new String[] {"START REPLICA", "STOP REPLICA", "RESET REPLICA ALL",
                "RESET BINARY LOGS AND GTIDS", "PURGE BINARY LOGS TO 'b.000001'",
                "CHANGE REPLICATION SOURCE TO SOURCE_HOST = 'h'",
                "CHANGE REPLICATION FILTER REPLICATE_DO_DB = (d1)"}) {
            assertThat(CCJSqlParserUtil.parse(sql).getFeatures().getCertain())
                    .containsExactly(StmtFeature.MODIFIES_SESSION);
        }
        assertThat(CCJSqlParserUtil.parse("PURGE TABLE t").getFeatures().getCertain())
                .containsExactlyInAnyOrder(StmtFeature.MODIFIES_SCHEMA,
                        StmtFeature.MODIFIES_DATA);
    }

    @Test
    void nodesSpanTheStatementsAndTheirParts() throws JSQLParserException {
        String sql = "CHANGE REPLICATION SOURCE TO SOURCE_HOST = 'h', IGNORE_SERVER_IDS = (1, 2)"
                + " FOR CHANNEL 'c1'";
        ChangeReplicationSourceStatement change = parse(sql, MYSQL);
        assertEquals(sql, text(sql, change));
        assertEquals("'h'", text(sql, (ASTNodeAccess) change.getOptions().get(0).getValue()));
        assertEquals("(1, 2)", text(sql, (ASTNodeAccess) change.getOptions().get(1).getValue()));
        assertEquals("'c1'", text(sql, change.getChannel()));

        sql = "CHANGE REPLICATION FILTER REPLICATE_DO_TABLE = (d1.t1)";
        ChangeReplicationFilterStatement filter = parse(sql, MYSQL);
        assertEquals("d1.t1", text(sql, filter.getFilters().get(0).getTables().get(0)));

        sql = "PURGE BINARY LOGS BEFORE NOW() - INTERVAL 3 DAY";
        PurgeStatement purge = parse(sql, MYSQL);
        assertEquals(sql, text(sql, purge));
        assertEquals("NOW() - INTERVAL 3 DAY", text(sql, (ASTNodeAccess) purge.getLogExpression()));

        sql = "RESET REPLICA ALL FOR CHANNEL 'c1', BINARY LOGS AND GTIDS TO 5";
        ResetReplicationStatement reset = parse(sql, MYSQL);
        assertEquals(sql, text(sql, reset));
        assertEquals("5", text(sql, reset.getOptions().get(1).getBinaryLogIndex()));

        for (String other : new String[] {"START REPLICA UNTIL SQL_AFTER_MTS_GAPS",
                "STOP REPLICA IO_THREAD FOR CHANNEL 'c1'"}) {
            assertEquals(other, text(other, (ASTNodeAccess) CCJSqlParserUtil.parse(other)));
        }
    }

    @Test
    void buildsStatementsInCode() {
        ChangeReplicationSourceStatement change = new ChangeReplicationSourceStatement();
        change.getOptions().add(new ReplicationSourceOption("SOURCE_HOST", Kind.SOURCE_HOST,
                new StringValue("h")));
        change.getOptions().add(new ReplicationSourceOption("PRIVILEGE_CHECKS_USER",
                new UserAccount().withUser(new StringValue("u")).withHost(new StringValue("%"))));
        change.setChannel(new StringValue("c1"));
        assertEquals("CHANGE REPLICATION SOURCE TO SOURCE_HOST = 'h',"
                + " PRIVILEGE_CHECKS_USER = 'u'@'%' FOR CHANNEL 'c1'", change.toString());
        assertEquals(change.toString(), deparse(change));

        ReplicationFilter filter =
                new ReplicationFilter(ReplicationFilter.Kind.REPLICATE_REWRITE_DB);
        filter.getRewrites().add(new ReplicationFilter.DatabaseRewrite("a", "b"));
        ChangeReplicationFilterStatement filters = new ChangeReplicationFilterStatement();
        filters.getFilters().add(filter);
        assertEquals("CHANGE REPLICATION FILTER REPLICATE_REWRITE_DB = ((a, b))",
                deparse(filters));

        StartReplicaStatement start = new StartReplicaStatement();
        start.getThreads().add(ReplicaThread.SQL_THREAD);
        start.getUntilOptions().add(new ReplicaUntilOption("SQL_AFTER_MTS_GAPS",
                ReplicaUntilOption.Kind.SQL_AFTER_MTS_GAPS, null));
        start.setUser(new StringValue("u"));
        assertEquals("START REPLICA SQL_THREAD UNTIL SQL_AFTER_MTS_GAPS USER = 'u'",
                deparse(start));

        StopReplicaStatement stop = new StopReplicaStatement();
        stop.setChannel(new StringValue("c1"));
        assertEquals("STOP REPLICA FOR CHANNEL 'c1'", stop.toString());

        ResetReplicationStatement reset = new ResetReplicationStatement();
        ResetReplicationStatement.Option logs =
                new ResetReplicationStatement.Option(
                        ResetReplicationStatement.Kind.BINARY_LOGS_AND_GTIDS);
        logs.setBinaryLogIndex(new LongValue(7));
        reset.getOptions().add(logs);
        assertEquals("RESET BINARY LOGS AND GTIDS TO 7", deparse(reset));

        Expression before = new StringValue("2026-01-01");
        PurgeStatement purge = new PurgeStatement(PurgeObjectType.BINARY_LOGS,
                PurgeStatement.LogBound.BEFORE, before);
        assertEquals("PURGE BINARY LOGS BEFORE '2026-01-01'", deparse(purge));
        assertThrows(IllegalArgumentException.class, () -> new PurgeStatement(
                PurgeObjectType.TABLE, PurgeStatement.LogBound.TO, before));
        assertThrows(NullPointerException.class,
                () -> new PurgeStatement(PurgeObjectType.MASTER_LOGS, null, before));
    }

    @SuppressWarnings("unchecked")
    private static <T extends Statement> T parse(String sql, Consumer<CCJSqlParser> dialect)
            throws JSQLParserException {
        return (T) assertSqlCanBeParsedAndDeparsed(sql, true, dialect);
    }

    private static String deparse(Statement statement) {
        StringBuilder builder = new StringBuilder();
        statement.accept(new StatementDeParser(builder), null);
        return builder.toString();
    }

    private static List<String> names(Statements statements) {
        return statements.stream().map(statement -> statement.getClass().getSimpleName())
                .collect(Collectors.toList());
    }

    private static String text(String sql, ASTNodeAccess access) {
        Node node = access.getASTNode();
        return sql.substring(node.jjtGetFirstToken().absoluteBegin - 1,
                node.jjtGetLastToken().absoluteEnd - 1);
    }
}
