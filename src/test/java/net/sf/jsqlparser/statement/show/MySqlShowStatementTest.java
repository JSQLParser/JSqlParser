/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.show;

import static net.sf.jsqlparser.test.TestUtils.assertDeparse;
import static net.sf.jsqlparser.test.TestUtils.assertSqlCanBeParsedAndDeparsed;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.function.Consumer;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.DoubleValue;
import net.sf.jsqlparser.expression.JdbcParameter;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.parser.ASTNodeAccess;
import net.sf.jsqlparser.parser.AbstractJSqlParser.Dialect;
import net.sf.jsqlparser.parser.CCJSqlParser;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.IfElseStatement;
import net.sf.jsqlparser.statement.ShowStatement;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementFeatures;
import net.sf.jsqlparser.statement.StatementVisitorAdapter;
import net.sf.jsqlparser.statement.Statements;
import net.sf.jsqlparser.statement.StmtFeature;
import net.sf.jsqlparser.statement.UnsupportedStatement;
import net.sf.jsqlparser.statement.create.user.UserAccount;
import net.sf.jsqlparser.statement.select.Limit;
import net.sf.jsqlparser.statement.select.Offset;
import net.sf.jsqlparser.statement.show.MySqlShowStatement.EngineInfo;
import net.sf.jsqlparser.statement.show.MySqlShowStatement.Kind;
import net.sf.jsqlparser.statement.show.MySqlShowStatement.Part;
import net.sf.jsqlparser.statement.show.MySqlShowStatement.ProfileType;
import net.sf.jsqlparser.statement.show.MySqlShowStatement.Scope;
import net.sf.jsqlparser.statement.show.ShowTablesStatement.SelectionMode;
import net.sf.jsqlparser.util.TablesNamesFinder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class MySqlShowStatementTest {

    private static final Consumer<CCJSqlParser> MYSQL = parser -> parser.withDialect(Dialect.MYSQL);

    private static final List<Consumer<CCJSqlParser>> DIALECTS = List.of(parser -> {
    }, MYSQL, parser -> parser.withDialect(Dialect.MARIADB));

    /** Parses without a dialect and with MySQL and MariaDB, then deparses and reparses. */
    private static MySqlShowStatement roundTrip(String sql) throws JSQLParserException {
        return roundTrip(sql, DIALECTS);
    }

    private static MySqlShowStatement roundTrip(String sql, List<Consumer<CCJSqlParser>> dialects)
            throws JSQLParserException {
        MySqlShowStatement result = null;
        for (Consumer<CCJSqlParser> dialect : dialects) {
            Statement statement = assertSqlCanBeParsedAndDeparsed(sql, false, dialect);
            assertInstanceOf(MySqlShowStatement.class, statement);
            assertDeparse(statement, sql);
            assertEquals(sql, CCJSqlParserUtil.parse(statement.toString(), dialect).toString());
            result = (MySqlShowStatement) statement;
        }
        return result;
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {
            "SHOW GLOBAL VARIABLES LIKE 'a%' | VARIABLES | VARIABLES",
            "SHOW SESSION VARIABLES WHERE Variable_name = 'a' | VARIABLES | VARIABLES",
            "SHOW LOCAL VARIABLES | VARIABLES | VARIABLES",
            "SHOW SESSION STATUS | STATUS | STATUS",
            "SHOW GLOBAL STATUS LIKE 'Com%' | STATUS | STATUS",
            "SHOW LOCAL STATUS WHERE Value > 1 | STATUS | STATUS",
            "SHOW STATUS WHERE Variable_name LIKE 'a%' AND Value > 0 | STATUS | STATUS",
            "SHOW FULL PROCESSLIST | PROCESSLIST | PROCESSLIST",
            "SHOW BINARY LOG STATUS | BINARY_LOG_STATUS | BINARY LOG STATUS",
            "SHOW BINARY LOGS | BINARY_LOGS | BINARY LOGS",
            "SHOW BINLOG EVENTS IN 'f' | BINLOG_EVENTS | BINLOG EVENTS",
            "SHOW BINLOG EVENTS FROM 4 | BINLOG_EVENTS | BINLOG EVENTS",
            "SHOW BINLOG EVENTS LIMIT 2 | BINLOG_EVENTS | BINLOG EVENTS",
            "SHOW BINLOG EVENTS IN 'f' FROM 4 LIMIT 2, 3 | BINLOG_EVENTS | BINLOG EVENTS",
            "SHOW BINLOG EVENTS IN 'f' FROM 4 LIMIT 3 OFFSET 2 | BINLOG_EVENTS | BINLOG EVENTS",
            "SHOW BINLOG EVENTS IN \"f\" | BINLOG_EVENTS | BINLOG EVENTS",
            "SHOW BINLOG EVENTS FROM 1.5 | BINLOG_EVENTS | BINLOG EVENTS",
            "SHOW BINLOG EVENTS FROM 18446744073709551615 | BINLOG_EVENTS | BINLOG EVENTS",
            "SHOW BINLOG EVENTS LIMIT a | BINLOG_EVENTS | BINLOG EVENTS",
            "SHOW RELAYLOG EVENTS IN 'f' FROM 4 LIMIT 2, 3 FOR CHANNEL 'c' | RELAYLOG_EVENTS | RELAYLOG EVENTS",
            "SHOW RELAYLOG EVENTS FOR CHANNEL 'c' | RELAYLOG_EVENTS | RELAYLOG EVENTS",
            "SHOW RELAYLOG EVENTS FOR CHANNEL \"c\" | RELAYLOG_EVENTS | RELAYLOG EVENTS",
            "SHOW RELAYLOG EVENTS LIMIT 1 FOR CHANNEL 'c' | RELAYLOG_EVENTS | RELAYLOG EVENTS",
            "SHOW GRANTS FOR 'u'@'h' | GRANTS | GRANTS",
            "SHOW GRANTS FOR u@h | GRANTS | GRANTS",
            "SHOW GRANTS FOR u | GRANTS | GRANTS",
            "SHOW GRANTS FOR `u`@`h` | GRANTS | GRANTS",
            "SHOW GRANTS FOR CURRENT_USER | GRANTS | GRANTS",
            "SHOW GRANTS FOR CURRENT_USER() | GRANTS | GRANTS",
            "SHOW GRANTS FOR 'u'@'h' USING 'r1' | GRANTS | GRANTS",
            "SHOW GRANTS FOR 'u'@'h' USING 'r1', 'r2'@'h' | GRANTS | GRANTS",
            "SHOW GRANTS FOR CURRENT_USER() USING r1, r2@h | GRANTS | GRANTS",
            "SHOW WARNINGS LIMIT 5 | WARNINGS | WARNINGS",
            "SHOW WARNINGS LIMIT 2, 5 | WARNINGS | WARNINGS",
            "SHOW WARNINGS LIMIT 5 OFFSET 2 | WARNINGS | WARNINGS",
            "SHOW ERRORS LIMIT 5 | ERRORS | ERRORS",
            "SHOW ERRORS LIMIT 2, 5 | ERRORS | ERRORS",
            "SHOW ERRORS LIMIT 5 OFFSET 2 | ERRORS | ERRORS",
            "SHOW COUNT(*) WARNINGS | WARNINGS | WARNINGS",
            "SHOW COUNT(*) ERRORS | ERRORS | ERRORS",
            "SHOW ENGINE INNODB STATUS | ENGINE | ENGINE",
            "SHOW ENGINE INNODB MUTEX | ENGINE | ENGINE",
            "SHOW ENGINE INNODB LOGS | ENGINE | ENGINE",
            "SHOW ENGINE PERFORMANCE_SCHEMA STATUS | ENGINE | ENGINE",
            "SHOW ENGINE ALL STATUS | ENGINE | ENGINE",
            "SHOW ENGINE 'innodb' STATUS | ENGINE | ENGINE",
            "SHOW ENGINE `innodb` MUTEX | ENGINE | ENGINE",
            "SHOW STORAGE ENGINES | ENGINES | STORAGE ENGINES",
            "SHOW TRIGGERS FROM d | TRIGGERS | TRIGGERS",
            "SHOW TRIGGERS IN d | TRIGGERS | TRIGGERS",
            "SHOW TRIGGERS LIKE 't%' | TRIGGERS | TRIGGERS",
            "SHOW TRIGGERS FROM d LIKE 't%' | TRIGGERS | TRIGGERS",
            "SHOW TRIGGERS IN d WHERE `Trigger` = 'x' | TRIGGERS | TRIGGERS",
            "SHOW FULL TRIGGERS | TRIGGERS | TRIGGERS",
            "SHOW FULL TRIGGERS FROM d | TRIGGERS | TRIGGERS",
            "SHOW EVENTS FROM d | EVENTS | EVENTS",
            "SHOW EVENTS IN d | EVENTS | EVENTS",
            "SHOW EVENTS LIKE 'e%' | EVENTS | EVENTS",
            "SHOW EVENTS FROM d WHERE Name = 'x' | EVENTS | EVENTS",
            "SHOW OPEN TABLES | OPEN_TABLES | OPEN TABLES",
            "SHOW OPEN TABLES FROM d | OPEN_TABLES | OPEN TABLES",
            "SHOW OPEN TABLES IN d | OPEN_TABLES | OPEN TABLES",
            "SHOW OPEN TABLES LIKE 't%' | OPEN_TABLES | OPEN TABLES",
            "SHOW OPEN TABLES FROM d WHERE In_use > 0 | OPEN_TABLES | OPEN TABLES",
            "SHOW PROCEDURE STATUS | PROCEDURE_STATUS | PROCEDURE STATUS",
            "SHOW PROCEDURE STATUS LIKE 'p%' | PROCEDURE_STATUS | PROCEDURE STATUS",
            "SHOW PROCEDURE STATUS WHERE Db = 'x' | PROCEDURE_STATUS | PROCEDURE STATUS",
            "SHOW FUNCTION STATUS | FUNCTION_STATUS | FUNCTION STATUS",
            "SHOW FUNCTION STATUS LIKE 'f%' | FUNCTION_STATUS | FUNCTION STATUS",
            "SHOW FUNCTION STATUS WHERE Db = 'x' | FUNCTION_STATUS | FUNCTION STATUS",
            "SHOW PROCEDURE CODE p | PROCEDURE_CODE | PROCEDURE CODE",
            "SHOW PROCEDURE CODE d.p | PROCEDURE_CODE | PROCEDURE CODE",
            "SHOW FUNCTION CODE f | FUNCTION_CODE | FUNCTION CODE",
            "SHOW FUNCTION CODE d.`f` | FUNCTION_CODE | FUNCTION CODE",
            "SHOW REPLICA STATUS | REPLICA_STATUS | REPLICA STATUS",
            "SHOW REPLICA STATUS FOR CHANNEL 'c' | REPLICA_STATUS | REPLICA STATUS",
            "SHOW REPLICA STATUS FOR CHANNEL '' | REPLICA_STATUS | REPLICA STATUS",
            "SHOW CHARACTER SET | CHARACTER_SET | CHARACTER SET",
            "SHOW CHARACTER SET LIKE 'utf8%' | CHARACTER_SET | CHARACTER SET",
            "SHOW CHAR SET | CHARACTER_SET | CHAR SET",
            "SHOW CHAR SET LIKE 'a' | CHARACTER_SET | CHAR SET",
            "SHOW CHARSET WHERE Maxlen > 1 | CHARACTER_SET | CHARSET",
            "SHOW COLLATION LIKE 'utf8%' | COLLATION | COLLATION",
            "SHOW COLLATION WHERE Charset = 'utf8mb4' | COLLATION | COLLATION",
            "SHOW DATABASES LIKE 'd%' | DATABASES | DATABASES",
            "SHOW SCHEMAS WHERE `Database` = 'd' | DATABASES | SCHEMAS",
            "SHOW PROFILE ALL | PROFILE | PROFILE",
            "SHOW PROFILE BLOCK IO | PROFILE | PROFILE",
            "SHOW PROFILE CONTEXT SWITCHES | PROFILE | PROFILE",
            "SHOW PROFILE CPU | PROFILE | PROFILE",
            "SHOW PROFILE IPC | PROFILE | PROFILE",
            "SHOW PROFILE MEMORY | PROFILE | PROFILE",
            "SHOW PROFILE PAGE FAULTS | PROFILE | PROFILE",
            "SHOW PROFILE SOURCE | PROFILE | PROFILE",
            "SHOW PROFILE SWAPS | PROFILE | PROFILE",
            "SHOW PROFILE CPU, BLOCK IO | PROFILE | PROFILE",
            "SHOW PROFILE CPU, CPU | PROFILE | PROFILE",
            "SHOW PROFILE CPU, BLOCK IO FOR QUERY 2 | PROFILE | PROFILE",
            "SHOW PROFILE FOR QUERY 2 | PROFILE | PROFILE",
            "SHOW PROFILE FOR QUERY 2 LIMIT 1 | PROFILE | PROFILE",
            "SHOW PROFILE LIMIT 1, 2 | PROFILE | PROFILE",
            "SHOW PROFILE ALL, CPU FOR QUERY 2 LIMIT 3 OFFSET 1 | PROFILE | PROFILE"
    })
    void mySqlForms(String sql, Kind kind, String keyword) throws JSQLParserException {
        MySqlShowStatement show = roundTrip(sql);

        assertEquals(kind, show.getKind());
        assertEquals(keyword, show.getKeyword());
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {
            "SHOW MASTER STATUS | BINARY_LOG_STATUS | MASTER STATUS",
            "SHOW MASTER LOGS | BINARY_LOGS | MASTER LOGS",
            "SHOW SLAVE HOSTS | REPLICAS | SLAVE HOSTS",
            "SHOW SLAVE STATUS | REPLICA_STATUS | SLAVE STATUS",
            "SHOW SLAVE STATUS FOR CHANNEL 'c1' | REPLICA_STATUS | SLAVE STATUS"
    })
    void spellingsRemovedInMySql84(String sql, Kind kind, String keyword)
            throws JSQLParserException {
        MySqlShowStatement show = roundTrip(sql, List.of(DIALECTS.get(0), DIALECTS.get(2)));

        assertEquals(kind, show.getKind());
        assertEquals(keyword, show.getKeyword());
        assertInstanceOf(UnsupportedStatement.class,
                assertSqlCanBeParsedAndDeparsed(sql, true, MYSQL));
    }

    @Test
    void scopeAndFilter() throws JSQLParserException {
        MySqlShowStatement show = roundTrip("SHOW GLOBAL STATUS WHERE Variable_name = 'Uptime'");

        assertEquals(Scope.GLOBAL, show.getScope());
        assertEquals("Variable_name = 'Uptime'", show.getWhereCondition().toString());
        assertNull(show.getLikeExpression());
        assertEquals(ShowFilter.Kind.WHERE, show.getFilter().getKind());

        show = roundTrip("SHOW VARIABLES LIKE 'sql_mode'");
        assertNull(show.getScope());
        assertEquals("sql_mode", ((StringValue) show.getLikeExpression()).getValue());

        show = roundTrip("SHOW LOCAL VARIABLES LIKE \"a%\"", List.of(MYSQL));
        assertEquals(Scope.LOCAL, show.getScope());
        assertEquals("a%", ((StringValue) show.getLikeExpression()).getValue());
    }

    @Test
    void whereTakesAWholeExpression() throws JSQLParserException {
        String sql = "SHOW VARIABLES WHERE Variable_name LIKE 'innodb%' AND Value > 0 "
                + "OR Variable_name IN ('a', 'b')";
        MySqlShowStatement show = roundTrip(sql);

        assertInstanceOf(OrExpression.class, show.getWhereCondition());
        // the statements of #2824 read the same filter
        for (String other : Arrays.asList("SHOW COLUMNS FROM t WHERE a = 1 AND b = 2",
                "SHOW TABLE STATUS WHERE a = 1 OR b = 2",
                "SHOW INDEX FROM t WHERE Key_name = 'a' AND Seq_in_index > 1")) {
            for (Consumer<CCJSqlParser> dialect : DIALECTS) {
                assertFalse(assertSqlCanBeParsedAndDeparsed(other, false,
                        dialect) instanceof UnsupportedStatement, other);
            }
        }
    }

    @Test
    void fullProcessListAndTriggers() throws JSQLParserException {
        assertTrue(roundTrip("SHOW FULL PROCESSLIST").isFull());
        assertFalse(roundTrip("SHOW PROCESSLIST", List.of(MYSQL)).isFull());
        assertTrue(roundTrip("SHOW FULL TRIGGERS IN d").isFull());
        assertFalse(roundTrip("SHOW TRIGGERS IN d").isFull());
    }

    @Test
    void databaseSelection() throws JSQLParserException {
        MySqlShowStatement show = roundTrip("SHOW TRIGGERS FROM v LIKE 't%'");

        assertEquals(SelectionMode.FROM, show.getDbSelection().getSelectionMode());
        assertEquals("v", show.getDbName());
        assertEquals("t%", ((StringValue) show.getLikeExpression()).getValue());

        show = roundTrip("SHOW OPEN TABLES IN v LIKE 't%'");
        assertEquals(SelectionMode.IN, show.getDbSelection().getSelectionMode());
    }

    @Test
    void binaryLogEvents() throws JSQLParserException {
        MySqlShowStatement show =
                roundTrip("SHOW BINLOG EVENTS IN 'binlog.000001' FROM 4 LIMIT 10");

        assertEquals("binlog.000001", show.getLogName().getValue());
        assertEquals(4L, ((LongValue) show.getPosition()).getValue());
        assertNull(show.getLimit().getOffset());
        assertEquals("10", show.getLimit().getRowCount().toString());
        assertNull(show.getOffset());
        assertNull(show.getChannel());

        show = roundTrip("SHOW BINLOG EVENTS IN \"f\" FROM 1.5 LIMIT 3 OFFSET 2");
        assertEquals("f", show.getLogName().getValue());
        assertEquals("\"", show.getLogName().getQuoteStr());
        assertInstanceOf(DoubleValue.class, show.getPosition());
        assertEquals("3", show.getLimit().getRowCount().toString());
        assertEquals("2", show.getOffset().getOffset().toString());
    }

    @Test
    void relayLogEvents() throws JSQLParserException {
        MySqlShowStatement show = roundTrip(
                "SHOW RELAYLOG EVENTS IN 'relay.000002' FROM 4 LIMIT 2, 10 FOR CHANNEL 'c1'");

        assertEquals("2", show.getLimit().getOffset().toString());
        assertEquals("10", show.getLimit().getRowCount().toString());
        assertEquals("c1", show.getChannel().getValue());
    }

    @Test
    void limitValues() throws JSQLParserException {
        MySqlShowStatement show = roundTrip("SHOW WARNINGS LIMIT a, 18446744073709551615");

        assertInstanceOf(Column.class, show.getLimit().getOffset());
        assertEquals("18446744073709551615",
                ((LongValue) show.getLimit().getRowCount()).getStringValue());

        // a ? is a syntax error in the text protocol, but PREPARE reads it
        show = roundTrip("SHOW ERRORS LIMIT ? OFFSET ?");
        assertInstanceOf(JdbcParameter.class, show.getLimit().getRowCount());
        assertInstanceOf(JdbcParameter.class, show.getOffset().getOffset());
    }

    @Test
    void grantsForAccount() throws JSQLParserException {
        MySqlShowStatement show = roundTrip("SHOW GRANTS FOR 'app'@'%'");

        assertEquals("app", show.getAccount().getUser().getValue());
        assertEquals("%", show.getAccount().getHost().getValue());
        assertTrue(show.getUsingRoles().isEmpty());

        show = roundTrip("SHOW GRANTS FOR app@localhost USING r1, 'r2'@'%'");
        assertEquals("app", show.getAccount().getUser().getValue());
        assertEquals("localhost", show.getAccount().getHost().getValue());
        assertEquals(2, show.getUsingRoles().size());
        assertEquals("'r2'@'%'", show.getUsingRoles().get(1).toString());
    }

    @Test
    void grantsForCurrentUser() throws JSQLParserException {
        MySqlShowStatement show = roundTrip("SHOW GRANTS FOR CURRENT_USER");

        assertEquals("CURRENT_USER", show.getAccount().getUser().getValue());
        assertFalse(show.getAccount().isCurrentUserParentheses());

        show = roundTrip("SHOW GRANTS FOR CURRENT_USER()");
        assertTrue(show.getAccount().isCurrentUserParentheses());

        assertNull(roundTrip("SHOW GRANTS", List.of(MYSQL)).getAccount());
    }

    @Test
    void diagnostics() throws JSQLParserException {
        MySqlShowStatement show = roundTrip("SHOW WARNINGS LIMIT 5");

        assertFalse(show.isCount());
        assertEquals("5", show.getLimit().getRowCount().toString());

        show = roundTrip("SHOW COUNT(*) ERRORS");
        assertEquals(Kind.ERRORS, show.getKind());
        assertTrue(show.isCount());
        assertNull(show.getLimit());

        assertEquals("SHOW COUNT(*) WARNINGS",
                CCJSqlParserUtil.parse("SHOW COUNT( * )WARNINGS", MYSQL).toString());
    }

    @Test
    void engineAndRoutineCode() throws JSQLParserException {
        MySqlShowStatement show = roundTrip("SHOW ENGINE INNODB STATUS");

        assertEquals("INNODB", show.getEngineName());
        assertEquals(EngineInfo.STATUS, show.getEngineInfo());

        show = roundTrip("SHOW ENGINE ALL MUTEX");
        assertEquals("ALL", show.getEngineName());
        assertEquals(EngineInfo.MUTEX, show.getEngineInfo());

        show = roundTrip("SHOW ENGINE 'innodb' LOGS");
        assertEquals("'innodb'", show.getEngineName());
        assertEquals(EngineInfo.LOGS, show.getEngineInfo());

        show = roundTrip("SHOW PROCEDURE CODE db.p");
        assertEquals("db", show.getRoutineName().getSchemaName());
        assertEquals("p", show.getRoutineName().getName());
        assertNull(roundTrip("SHOW FUNCTION CODE f").getRoutineName().getSchemaName());
    }

    @Test
    void profile() throws JSQLParserException {
        MySqlShowStatement show =
                roundTrip("SHOW PROFILE BLOCK IO, PAGE FAULTS, CPU FOR QUERY 2 LIMIT 3 OFFSET 1");

        assertEquals(List.of(ProfileType.BLOCK_IO, ProfileType.PAGE_FAULTS, ProfileType.CPU),
                show.getProfileTypes());
        assertEquals("2", show.getProfileQuery().toString());
        assertEquals("3", show.getLimit().getRowCount().toString());
        assertEquals("1", show.getOffset().getOffset().toString());

        show = roundTrip("SHOW PROFILE", List.of(MYSQL));
        assertTrue(show.getProfileTypes().isEmpty());
        assertNull(show.getProfileQuery());
        assertEquals(ProfileType.CONTEXT_SWITCHES, ProfileType.fromKeyword("context  switches"));
    }

    @Test
    void keywordsAreCaseInsensitive() throws JSQLParserException {
        assertEquals("SHOW GLOBAL VARIABLES LIKE 'x%'",
                CCJSqlParserUtil.parse("show global variables like 'x%'", MYSQL).toString());
        assertEquals("SHOW SLAVE STATUS FOR CHANNEL 'c'",
                CCJSqlParserUtil.parse("show slave status for channel 'c'").toString());
        assertEquals("SHOW CHARSET", CCJSqlParserUtil.parse("show charset", MYSQL).toString());
        assertEquals("SHOW PROFILE BLOCK IO, CONTEXT SWITCHES", CCJSqlParserUtil
                .parse("show profile block io, Context Switches", MYSQL).toString());
        assertEquals("SHOW ENGINE innodb STATUS",
                CCJSqlParserUtil.parse("show engine innodb status", MYSQL).toString());
    }

    @ParameterizedTest
    @ValueSource(strings = {"SHOW VARIABLES", "SHOW STATUS", "SHOW PROCESSLIST",
            "SHOW DATABASES", "SHOW SCHEMAS", "SHOW TRIGGERS", "SHOW EVENTS", "SHOW CHARSET",
            "SHOW COLLATION", "SHOW ENGINES", "SHOW PLUGINS", "SHOW PRIVILEGES", "SHOW PROFILES",
            "SHOW PROFILE", "SHOW REPLICAS", "SHOW GRANTS", "SHOW WARNINGS", "SHOW ERRORS"})
    void singleWordsAreTypedForMySqlOnly(String sql) throws JSQLParserException {
        roundTrip(sql, List.of(MYSQL, parser -> parser.withDialect(Dialect.MARIADB)));

        // without a dialect and in other dialects, a single word stays a ShowStatement
        for (Consumer<CCJSqlParser> dialect : List.<Consumer<CCJSqlParser>>of(parser -> {
        }, parser -> parser.withDialect(Dialect.SNOWFLAKE),
                parser -> parser.withDialect(Dialect.POSTGRESQL))) {
            Statement statement = CCJSqlParserUtil.parse(sql, dialect);
            assertInstanceOf(ShowStatement.class, statement);
            assertEquals(sql, statement.toString());
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"SHOW VARIABLES LIKE 'sql_mode'", "SHOW DATABASES LIKE 'v%'",
            "SHOW GRANTS FOR 'app'@'%'", "SHOW FULL PROCESSLIST", "SHOW WARNINGS LIMIT 5",
            "SHOW BINARY LOGS", "SHOW ENGINE INNODB STATUS", "SHOW PROFILE CPU",
            "SHOW CHARACTER SET", "SHOW REPLICA STATUS"})
    void otherDialectsKeepUnsupportedStatement(String sql) throws JSQLParserException {
        for (Dialect dialect : Arrays.asList(Dialect.SNOWFLAKE, Dialect.POSTGRESQL,
                Dialect.ORACLE, Dialect.DATABRICKS)) {
            Statement statement =
                    CCJSqlParserUtil.parse(sql, parser -> parser.withDialect(dialect));
            assertInstanceOf(UnsupportedStatement.class, statement, dialect.name());
        }
    }

    @Test
    void postgreSqlShowIsUnchanged() throws JSQLParserException {
        Consumer<CCJSqlParser> postgreSql = parser -> parser.withDialect(Dialect.POSTGRESQL);
        for (String sql : Arrays.asList("SHOW TIME ZONE", "SHOW TRANSACTION ISOLATION LEVEL",
                "SHOW SESSION AUTHORIZATION", "SHOW search_path", "SHOW ALL", "SHOW status")) {
            assertInstanceOf(ShowStatement.class, assertSqlCanBeParsedAndDeparsed(sql, true,
                    postgreSql), sql);
        }
        for (String sql : Arrays.asList("SHOW TIME ZONE", "SHOW TRANSACTION ISOLATION LEVEL",
                "SHOW SESSION AUTHORIZATION")) {
            assertInstanceOf(ShowStatement.class, assertSqlCanBeParsedAndDeparsed(sql, true));
        }
    }

    /** MySQL 8.4 reports a syntax error for each; the parser keeps them unsupported statements. */
    @ParameterizedTest
    @ValueSource(strings = {
            "SHOW PERSIST VARIABLES",
            "SHOW GLOBAL SESSION VARIABLES",
            "SHOW VARIABLES LIKE a",
            "SHOW VARIABLES LIKE 1",
            "SHOW VARIABLES LIKE N'a%'",
            "SHOW VARIABLES LIKE _utf8mb4'a%'",
            "SHOW VARIABLES LIKE CONCAT('a', '%')",
            "SHOW VARIABLES LIKE ?",
            "SHOW VARIABLES LIKE @v",
            "SHOW VARIABLES LIKE 'a%' WHERE Value = 1",
            "SHOW VARIABLES FROM d",
            "SHOW VARIABLES LIMIT 1",
            "SHOW FULL VARIABLES",
            "SHOW FULL STATUS",
            "SHOW PROCESSLIST LIKE 'a'",
            "SHOW EXTENDED PROCESSLIST",
            "SHOW SESSION PROCESSLIST",
            "SHOW BINARY LOG",
            "SHOW BINARY LOGS LIKE 'a'",
            "SHOW BINLOG EVENTS IN f",
            "SHOW BINLOG EVENTS FROM 'x'",
            "SHOW BINLOG EVENTS FROM -1",
            "SHOW BINLOG EVENTS FROM 0x10",
            "SHOW BINLOG EVENTS FROM 4 IN 'f'",
            "SHOW BINLOG EVENTS LIMIT 2 FROM 4",
            "SHOW BINLOG EVENTS FOR CHANNEL 'c'",
            "SHOW BINLOG EVENTS LIMIT @v",
            "SHOW BINLOG EVENTS LIMIT 1 + 1",
            "SHOW BINLOG EVENTS LIMIT -1",
            "SHOW BINLOG EVENTS LIMIT 1, 2, 3",
            "SHOW BINLOG EVENTS LIMIT 'a'",
            "SHOW RELAYLOG EVENTS FOR CHANNEL c",
            "SHOW RELAYLOG EVENTS FOR CHANNEL 'c' LIMIT 1",
            "SHOW RELAYLOG EVENTS FOR CHANNEL",
            "SHOW GRANTS USING 'r1'",
            "SHOW GRANTS FOR",
            "SHOW GRANTS FOR 'u'@'h' USING",
            "SHOW GRANTS FOR 'u'@'h', 'u2'@'h'",
            "SHOW GRANTS ON d.t",
            "SHOW COUNT (*) ERRORS",
            "SHOW COUNT(*) WARNINGS LIMIT 1",
            "SHOW COUNT(1) WARNINGS",
            "SHOW COUNT(*) VARIABLES",
            "SHOW COUNT(*)",
            "SHOW WARNINGS LIKE 'a'",
            "SHOW WARNINGS WHERE Code = 1",
            "SHOW WARNINGS LIMIT",
            "SHOW WARNINGS OFFSET 2",
            "SHOW WARNINGS LIMIT \"a\"",
            "SHOW WARNINGS LIMIT 1.5",
            "SHOW WARNINGS LIMIT 0x10",
            "SHOW WARNINGS LIMIT a.b",
            "SHOW ENGINE INNODB",
            "SHOW ENGINE STATUS",
            "SHOW ENGINE INNODB STATUS LIKE 'a'",
            "SHOW ENGINE d.innodb STATUS",
            "SHOW ENGINE N'innodb' STATUS",
            "SHOW ENGINES LIKE 'a'",
            "SHOW STORAGE ENGINE INNODB STATUS",
            "SHOW EXTENDED TRIGGERS",
            "SHOW TRIGGERS FROM d.t",
            "SHOW TRIGGERS LIKE 't%' FROM d",
            "SHOW TRIGGERS FROM 'd'",
            "SHOW FULL EVENTS",
            "SHOW EVENTS FROM d.e",
            "SHOW FULL OPEN TABLES",
            "SHOW OPEN FULL TABLES",
            "SHOW OPEN TABLE",
            "SHOW PROCEDURE STATUS FROM d",
            "SHOW PROCEDURE CODE",
            "SHOW PROCEDURE CODE p LIKE 'a'",
            "SHOW PROCEDURE CODE a.b.c",
            "SHOW PROCEDURE CODE 'p'",
            "SHOW PROCEDURE p",
            "SHOW MASTER STATUS",
            "SHOW MASTER LOGS",
            "SHOW MASTER HOSTS",
            "SHOW SLAVE STATUS",
            "SHOW SLAVE HOSTS",
            "SHOW REPLICAS LIKE 'a'",
            "SHOW REPLICA STATUS LIKE 'a'",
            "SHOW REPLICA STATUS FOR 'c'",
            "SHOW REPLICA STATUS FOR CHANNEL c",
            "SHOW REPLICA STATUS FOR CHANNEL N'c'",
            "SHOW REPLICA STATUS FOR CHANNEL 'c' LIMIT 1",
            "SHOW REPLICA HOSTS",
            "SHOW CHARACTER SET FROM d",
            "SHOW CHARACTER SETS",
            "SHOW CHARSET LIKE 'a' LIKE 'b'",
            "SHOW COLLATION FROM d",
            "SHOW DATABASES FROM d",
            "SHOW GLOBAL DATABASES",
            "SHOW FULL DATABASES",
            "SHOW PLUGINS LIKE 'a'",
            "SHOW PLUGINS WHERE Name = 'a'",
            "SHOW PRIVILEGES LIKE 'a'",
            "SHOW PROFILES LIMIT 1",
            "SHOW PROFILE CPU,",
            "SHOW PROFILE BLOCK",
            "SHOW PROFILE FOR QUERY",
            "SHOW PROFILE FOR QUERY 'a'",
            "SHOW PROFILE FOR QUERY ?",
            "SHOW PROFILE LIMIT 1 FOR QUERY 2",
            "SHOW PROFILE FOO",
            "SHOW PROFILE CPU CPU",
            "SHOW INNODB STATUS",
            "SHOW MUTEX STATUS",
            "SHOW TABLE TYPES"
    })
    void formsMySqlRejectsRemainUnsupported(String sql) throws JSQLParserException {
        assertInstanceOf(UnsupportedStatement.class, CCJSqlParserUtil.parse(sql, MYSQL));
    }

    @Test
    void textAfterAWhereConditionIsAnError() {
        for (String sql : Arrays.asList("SHOW VARIABLES WHERE a = 1 LIMIT 1",
                "SHOW EVENTS FROM d WHERE a = 1 LIKE", "SHOW STATUS WHERE a = 1 FROM d")) {
            assertThrows(JSQLParserException.class, () -> CCJSqlParserUtil.parse(sql, MYSQL), sql);
        }
    }

    @Test
    void withoutADialectLikeTakesAnExpressionAsBefore() throws JSQLParserException {
        Statement statement = assertSqlCanBeParsedAndDeparsed("SHOW VARIABLES LIKE a", true);

        assertInstanceOf(Column.class, ((MySqlShowStatement) statement).getLikeExpression());
        assertInstanceOf(ShowTablesStatement.class,
                assertSqlCanBeParsedAndDeparsed("SHOW TABLES LIKE 'a' || 'b'", true));
        assertInstanceOf(ShowTablesStatement.class,
                assertSqlCanBeParsedAndDeparsed("SHOW TABLES LIKE 'a%'", true, MYSQL));
    }

    @ParameterizedTest
    @ValueSource(strings = {"SHOW VARIABLES LIKE 'a%'", "SHOW GLOBAL STATUS",
            "SHOW FULL PROCESSLIST", "SHOW SCHEMAS", "SHOW TRIGGERS FROM v",
            "SHOW OPEN TABLES", "SHOW PROCEDURE STATUS", "SHOW FUNCTION CODE f",
            "SHOW CHARACTER SET", "SHOW STORAGE ENGINES", "SHOW PLUGINS", "SHOW BINARY LOGS",
            "SHOW BINARY LOG STATUS", "SHOW REPLICAS",
            "SHOW REPLICA STATUS FOR CHANNEL 'c'", "SHOW GRANTS FOR CURRENT_USER()",
            "SHOW WARNINGS LIMIT 2, 5", "SHOW ERRORS LIMIT 5 OFFSET a", "SHOW COUNT(*) ERRORS",
            "SHOW ENGINE INNODB MUTEX", "SHOW BINLOG EVENTS FROM 4",
            "SHOW RELAYLOG EVENTS FOR CHANNEL 'c'", "SHOW PROFILE CPU FOR QUERY 2",
            "SHOW STATUS WHERE a = 1 AND b = 2", "SHOW INDEX FROM t FROM v", "SHOW TABLES IN v"})
    void endsAtTheStatementBoundary(String sql) throws JSQLParserException {
        for (String script : Arrays.asList(sql + ";\nSELECT 1", sql + ";\nSHOW TABLES",
                "SHOW TABLES;\n" + sql)) {
            Statements statements = CCJSqlParserUtil.parseStatements(script, MYSQL);

            assertEquals(2, statements.size(), script);
            Statement show = statements.get(script.startsWith(sql) ? 0 : 1);
            assertEquals(sql, show.toString());
            assertFalse(show instanceof UnsupportedStatement, script);
        }
        IfElseStatement ifElse = (IfElseStatement) CCJSqlParserUtil
                .parse("IF a = 1 " + sql + " ELSE " + sql, MYSQL);
        assertEquals(sql, ifElse.getIfStatement().toString());
        assertEquals(sql, ifElse.getElseStatement().toString());
    }

    @Test
    void namesNoTable() throws JSQLParserException {
        assertThat(TablesNamesFinder.findTables("SHOW OPEN TABLES FROM db LIKE 't%'")).isEmpty();
        assertThat(TablesNamesFinder.findTables("SHOW PROCEDURE CODE db.p")).isEmpty();
        assertThat(TablesNamesFinder.findTables("SHOW GRANTS FOR u")).isEmpty();
        assertThat(new TablesNamesFinder<Void>().getTables(
                CCJSqlParserUtil.parse("SHOW EVENTS FROM db WHERE Name = 'x'", MYSQL))).isEmpty();
    }

    @Test
    void reportsMetadataFeatures() throws JSQLParserException {
        StatementFeatures features =
                CCJSqlParserUtil.parse("SHOW GLOBAL VARIABLES LIKE 'a%'").getFeatures();

        assertThat(features.getCertain()).containsExactly(StmtFeature.RETURNS_RESULT_SET);
    }

    @Test
    void visitorsReceiveTheStatement() throws JSQLParserException {
        Statement statement = CCJSqlParserUtil.parse("SHOW BINARY LOGS");
        MySqlShowStatement[] visited = new MySqlShowStatement[1];
        statement.accept(new StatementVisitorAdapter<Void>() {
            @Override
            public <S> Void visit(MySqlShowStatement mySqlShow, S context) {
                visited[0] = mySqlShow;
                return null;
            }
        });

        assertSame(statement, visited[0]);
        assertNull(statement.accept(new StatementVisitorAdapter<Void>(), null));
    }

    @Test
    void buildsAndDeparses() {
        MySqlShowStatement show = new MySqlShowStatement(Kind.VARIABLES).withScope(Scope.SESSION)
                .withFilter(ShowFilter.like(new StringValue("max%")));
        assertDeparse(show, "SHOW SESSION VARIABLES LIKE 'max%'");

        show = new MySqlShowStatement().withKeyword("slave status")
                .withChannel(new StringValue("c1"));
        assertEquals(Kind.REPLICA_STATUS, show.getKind());
        assertDeparse(show, "SHOW SLAVE STATUS FOR CHANNEL 'c1'");

        UserAccount account = new UserAccount().withUser(new StringValue("app"))
                .withHost(new StringValue("%"));
        show = new MySqlShowStatement(Kind.GRANTS).withAccount(account)
                .withUsingRoles(List.of(new UserAccount().withUser(new StringValue("r"))));
        assertDeparse(show, "SHOW GRANTS FOR 'app'@'%' USING 'r'");

        show = new MySqlShowStatement(Kind.BINLOG_EVENTS)
                .withLogName(new StringValue("binlog.000002")).withPosition(new LongValue(4))
                .withLimit(new Limit().withOffset(new LongValue(1)).withRowCount(new LongValue(2)));
        assertDeparse(show, "SHOW BINLOG EVENTS IN 'binlog.000002' FROM 4 LIMIT 1, 2");

        show = new MySqlShowStatement(Kind.ENGINE).withEngineName("INNODB")
                .withEngineInfo(EngineInfo.MUTEX);
        assertDeparse(show, "SHOW ENGINE INNODB MUTEX");

        show = new MySqlShowStatement(Kind.PROCEDURE_CODE).withRoutineName(new Table("p"));
        assertDeparse(show, "SHOW PROCEDURE CODE p");

        show = new MySqlShowStatement(Kind.TRIGGERS).withFull(true)
                .withDbSelection(new ShowDbSelection(SelectionMode.IN, "db"));
        assertDeparse(show, "SHOW FULL TRIGGERS IN db");

        show = new MySqlShowStatement(Kind.PROFILE)
                .withProfileTypes(List.of(ProfileType.CPU, ProfileType.BLOCK_IO))
                .withProfileQuery(new LongValue(2))
                .withLimit(new Limit().withRowCount(new LongValue(3)))
                .withOffset(new Offset().withOffset(new LongValue(1)));
        assertDeparse(show, "SHOW PROFILE CPU, BLOCK IO FOR QUERY 2 LIMIT 3 OFFSET 1");
    }

    @Test
    void kindsAllowTheirParts() {
        assertTrue(Kind.VARIABLES.allows(Part.SCOPE));
        assertFalse(Kind.DATABASES.allows(Part.SCOPE));
        assertEquals(List.of("BINARY LOG STATUS", "MASTER STATUS"),
                Kind.BINARY_LOG_STATUS.getKeywords());
        assertEquals(List.of("CHARACTER SET", "CHARSET", "CHAR SET"),
                Kind.CHARACTER_SET.getKeywords());
        assertEquals(EnumSet.of(Part.PROFILE, Part.LIMIT), Kind.PROFILE.getParts());
        assertEquals(Kind.CHARACTER_SET, Kind.fromKeyword(" charset "));
        assertThrows(IllegalArgumentException.class, () -> Kind.fromKeyword("FUNCTIONS"));

        MySqlShowStatement show = new MySqlShowStatement(Kind.DATABASES);
        assertThrows(IllegalArgumentException.class, () -> show.setScope(Scope.GLOBAL));
        assertThrows(IllegalArgumentException.class, () -> show.setFull(true));
        assertThrows(IllegalArgumentException.class,
                () -> show.setDbSelection(new ShowDbSelection("db")));
        assertThrows(IllegalArgumentException.class,
                () -> show.setChannel(new StringValue("c")));
        assertThrows(IllegalArgumentException.class, () -> show.setLimit(new Limit()));
        assertThrows(IllegalArgumentException.class,
                () -> show.setProfileTypes(List.of(ProfileType.CPU)));
        assertThrows(IllegalArgumentException.class,
                () -> show.setProfileQuery(new LongValue(1)));

        MySqlShowStatement binlog = new MySqlShowStatement(Kind.BINLOG_EVENTS);
        assertThrows(IllegalArgumentException.class,
                () -> binlog.setChannel(new StringValue("c")));
        // OFFSET needs LIMIT row_count before it
        assertThrows(IllegalArgumentException.class,
                () -> binlog.setOffset(new Offset().withOffset(new LongValue(1))));
        binlog.setLimit(new Limit().withOffset(new LongValue(1)).withRowCount(new LongValue(2)));
        assertThrows(IllegalArgumentException.class,
                () -> binlog.setOffset(new Offset().withOffset(new LongValue(1))));
    }

    @Test
    void changingTheKindDropsPartsItDoesNotAllow() {
        MySqlShowStatement show = new MySqlShowStatement(Kind.TRIGGERS)
                .withDbSelection(new ShowDbSelection("db"))
                .withFilter(ShowFilter.like(new StringValue("t%")));

        show.setKind(Kind.DATABASES);
        assertNull(show.getDbSelection());
        assertEquals("SHOW DATABASES LIKE 't%'", show.toString());

        show.setKeyword("SCHEMAS");
        assertEquals(Kind.DATABASES, show.getKind());
        assertEquals("SHOW SCHEMAS LIKE 't%'", show.toString());

        show.setKeyword("PLUGINS");
        assertEquals(Kind.PLUGINS, show.getKind());
        assertNull(show.getFilter());
        assertEquals("SHOW PLUGINS", show.toString());

        show.setKind(Kind.REPLICAS);
        assertEquals("REPLICAS", show.getKeyword());
        show.setKeyword(null);
        assertEquals("REPLICAS", show.getKeyword());
    }

    @Test
    void countAndLimitReplaceEachOther() {
        MySqlShowStatement show = new MySqlShowStatement(Kind.WARNINGS)
                .withLimit(new Limit().withRowCount(new LongValue(3)));

        show.setCount(true);
        assertNull(show.getLimit());
        assertEquals("SHOW COUNT(*) WARNINGS", show.toString());

        show.setLimit(new Limit().withRowCount(new LongValue(3)));
        show.setOffset(new Offset().withOffset(new LongValue(1)));
        assertFalse(show.isCount());
        assertEquals("SHOW WARNINGS LIMIT 3 OFFSET 1", show.toString());

        show.setLimit(null);
        assertNull(show.getOffset());
        assertEquals("SHOW WARNINGS", show.toString());
    }

    @Test
    void removingTheAccountRemovesTheRoles() {
        MySqlShowStatement show = new MySqlShowStatement(Kind.GRANTS)
                .withAccount(new UserAccount().withUser(new StringValue("u")))
                .withUsingRoles(List.of(new UserAccount().withUser(new StringValue("r"))));

        show.setAccount(null);
        assertTrue(show.getUsingRoles().isEmpty());
        assertEquals("SHOW GRANTS", show.toString());
        assertThrows(UnsupportedOperationException.class,
                () -> show.getUsingRoles().add(new UserAccount()));
    }

    @Test
    void statementDeParserPrintsExpressionsWithTheExpressionDeParser()
            throws JSQLParserException {
        assertEquals("SHOW VARIABLES LIKE <sql_mode>",
                ShowExpressionDeParsing.deparse("SHOW VARIABLES LIKE 'sql_mode'"));
        assertEquals("SHOW BINLOG EVENTS IN <b.000001> FROM #4 LIMIT #2, #10",
                ShowExpressionDeParsing.deparse(
                        "SHOW BINLOG EVENTS IN 'b.000001' FROM 4 LIMIT 2, 10"));
        assertEquals("SHOW RELAYLOG EVENTS LIMIT #10 OFFSET #2 FOR CHANNEL <c>",
                ShowExpressionDeParsing.deparse(
                        "SHOW RELAYLOG EVENTS LIMIT 10 OFFSET 2 FOR CHANNEL 'c'"));
        assertEquals("SHOW PROFILE CPU FOR QUERY #2",
                ShowExpressionDeParsing.deparse("SHOW PROFILE CPU FOR QUERY 2"));
    }

    @Test
    void parsedObjectsHaveNodes() throws JSQLParserException {
        String sql = "SHOW BINLOG EVENTS IN 'f' FROM 4 LIMIT 2, 3";
        MySqlShowStatement show = (MySqlShowStatement) CCJSqlParserUtil.parse(sql, MYSQL);

        assertEquals(sql, span(sql, show));
        assertEquals("'f'", span(sql, show.getLogName()));
        assertEquals("4", span(sql, (ASTNodeAccess) show.getPosition()));
        assertEquals("LIMIT 2, 3", span(sql, show.getLimit()));

        sql = "SHOW PROCEDURE CODE d.p";
        show = (MySqlShowStatement) CCJSqlParserUtil.parse(sql, MYSQL);
        assertEquals("d.p", span(sql, show.getRoutineName()));

        sql = "SHOW WARNINGS LIMIT 5 OFFSET a";
        show = (MySqlShowStatement) CCJSqlParserUtil.parse(sql, MYSQL);
        assertEquals("LIMIT 5", span(sql, show.getLimit()));
        assertEquals("a", span(sql, (ASTNodeAccess) show.getOffset().getOffset()));
    }

    private static String span(String sql, ASTNodeAccess access) {
        return sql.substring(access.getASTNode().jjtGetFirstToken().absoluteBegin - 1,
                access.getASTNode().jjtGetLastToken().absoluteEnd - 1);
    }
}
