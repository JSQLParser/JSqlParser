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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;
import java.util.stream.Stream;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.ASTNodeAccess;
import net.sf.jsqlparser.parser.AbstractJSqlParser.Dialect;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.parser.Node;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitor;
import net.sf.jsqlparser.statement.StatementVisitorAdapter;
import net.sf.jsqlparser.statement.Statements;
import net.sf.jsqlparser.statement.StmtFeature;
import net.sf.jsqlparser.statement.UnsupportedStatement;
import net.sf.jsqlparser.statement.alter.AlterInstance.Action;
import net.sf.jsqlparser.statement.lock.BackupLockStatement;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.util.TablesNamesFinder;
import net.sf.jsqlparser.util.deparser.StatementDeParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

/** MySQL ALTER INSTANCE. */
class AlterInstanceTest {
    private static final Dialect[] MYSQL_OR_NONE = {Dialect.MYSQL, null};

    private static Statement parse(String sql, Dialect dialect) throws JSQLParserException {
        return dialect == null ? CCJSqlParserUtil.parse(sql)
                : CCJSqlParserUtil.parse(sql, p -> p.withDialect(dialect));
    }

    private static void assertSpans(String expected, String source, ASTNodeAccess access) {
        Node node = access.getASTNode();
        assertNotNull(node, expected);
        assertEquals(expected, source.substring(node.jjtGetFirstToken().absoluteBegin - 1,
                node.jjtGetLastToken().absoluteEnd - 1));
    }

    /**
     * Parses {@code sql}, which must print as {@code expected}, and checks the statement span, the
     * deparser, the reparse, the feature analysis and the table finder.
     */
    private static AlterInstance roundTrip(String sql, String expected, Dialect dialect)
            throws Exception {
        AlterInstance statement = assertInstanceOf(AlterInstance.class, parse(sql, dialect));
        assertEquals(expected, statement.toString());
        assertSpans(sql, sql, statement);
        StringBuilder output = new StringBuilder();
        statement.accept(new StatementDeParser(output), null);
        assertEquals(expected, output.toString());
        Statement reparsed = parse(output.toString(), dialect);
        assertInstanceOf(AlterInstance.class, reparsed);
        assertEquals(expected, reparsed.toString());
        assertEquals(Set.of(StmtFeature.MODIFIES_SESSION), statement.getFeatures().getCertain());
        assertTrue(statement.getFeatures().getUncertain().isEmpty());
        assertTrue(new TablesNamesFinder<>().getTables(statement).isEmpty());
        return statement;
    }

    static Stream<Arguments> actions() {
        return Stream.of(
                Arguments.of("ALTER INSTANCE ENABLE INNODB REDO_LOG",
                        Action.ENABLE_INNODB_REDO_LOG, null, false),
                Arguments.of("ALTER INSTANCE DISABLE INNODB REDO_LOG",
                        Action.DISABLE_INNODB_REDO_LOG, null, false),
                Arguments.of("ALTER INSTANCE ROTATE INNODB MASTER KEY",
                        Action.ROTATE_INNODB_MASTER_KEY, null, false),
                Arguments.of("ALTER INSTANCE ROTATE BINLOG MASTER KEY",
                        Action.ROTATE_BINLOG_MASTER_KEY, null, false),
                Arguments.of("ALTER INSTANCE RELOAD TLS", Action.RELOAD_TLS, null, false),
                Arguments.of("ALTER INSTANCE RELOAD TLS NO ROLLBACK ON ERROR", Action.RELOAD_TLS,
                        null, true),
                Arguments.of("ALTER INSTANCE RELOAD TLS FOR CHANNEL mysql_main",
                        Action.RELOAD_TLS, "mysql_main", false),
                Arguments.of("ALTER INSTANCE RELOAD TLS FOR CHANNEL mysql_admin",
                        Action.RELOAD_TLS, "mysql_admin", false),
                Arguments.of(
                        "ALTER INSTANCE RELOAD TLS FOR CHANNEL mysql_admin NO ROLLBACK ON ERROR",
                        Action.RELOAD_TLS, "mysql_admin", true),
                Arguments.of("ALTER INSTANCE RELOAD TLS FOR CHANNEL `mysql_main`",
                        Action.RELOAD_TLS, "`mysql_main`", false),
                Arguments.of("ALTER INSTANCE RELOAD TLS FOR CHANNEL MYSQL_MAIN",
                        Action.RELOAD_TLS, "MYSQL_MAIN", false),
                // the grammar takes any identifier; the server checks the name when it runs
                Arguments.of("ALTER INSTANCE RELOAD TLS FOR CHANNEL other_channel",
                        Action.RELOAD_TLS, "other_channel", false),
                Arguments.of("ALTER INSTANCE RELOAD TLS FOR CHANNEL channel", Action.RELOAD_TLS,
                        "channel", false),
                Arguments.of("ALTER INSTANCE RELOAD TLS FOR CHANNEL tls NO ROLLBACK ON ERROR",
                        Action.RELOAD_TLS, "tls", true),
                Arguments.of("ALTER INSTANCE RELOAD KEYRING", Action.RELOAD_KEYRING, null, false));
    }

    @ParameterizedTest
    @MethodSource("actions")
    void everyActionIsStructured(String sql, Action action, String channel,
            boolean noRollbackOnError) throws Exception {
        for (Dialect dialect : MYSQL_OR_NONE) {
            AlterInstance statement = roundTrip(sql, sql, dialect);
            assertEquals(action, statement.getAction());
            assertEquals(channel, statement.getChannel());
            assertEquals(noRollbackOnError, statement.isNoRollbackOnError());
            assertTrue(sql.startsWith("ALTER INSTANCE " + action.getSql()));
        }
    }

    static Stream<Arguments> spellings() {
        return Stream.of(
                Arguments.of("alter instance rotate innodb master key",
                        "ALTER INSTANCE ROTATE INNODB MASTER KEY"),
                Arguments.of("ALTER INSTANCE ROTATE `INNODB` MASTER KEY",
                        "ALTER INSTANCE ROTATE INNODB MASTER KEY"),
                Arguments.of("ALTER INSTANCE ROTATE `binlog` MASTER KEY",
                        "ALTER INSTANCE ROTATE BINLOG MASTER KEY"),
                Arguments.of("ALTER INSTANCE ROTATE 'BINLOG' MASTER KEY",
                        "ALTER INSTANCE ROTATE BINLOG MASTER KEY"),
                Arguments.of("ALTER INSTANCE ROTATE \"binlog\" MASTER KEY",
                        "ALTER INSTANCE ROTATE BINLOG MASTER KEY"),
                Arguments.of("ALTER INSTANCE DISABLE innodb redo_log",
                        "ALTER INSTANCE DISABLE INNODB REDO_LOG"),
                Arguments.of("ALTER INSTANCE DISABLE `INNODB` `REDO_LOG`",
                        "ALTER INSTANCE DISABLE INNODB REDO_LOG"),
                Arguments.of("ALTER INSTANCE ENABLE `innodb` `redo_log`",
                        "ALTER INSTANCE ENABLE INNODB REDO_LOG"),
                Arguments.of("alter instance reload tls for channel mysql_main "
                        + "no rollback on error",
                        "ALTER INSTANCE RELOAD TLS FOR CHANNEL mysql_main NO ROLLBACK ON ERROR"),
                Arguments.of("alter instance reload keyring", "ALTER INSTANCE RELOAD KEYRING"));
    }

    @ParameterizedTest
    @MethodSource("spellings")
    void quotedAndLowerCaseWordsPrintAsPlainKeywords(String sql, String expected)
            throws Exception {
        for (Dialect dialect : MYSQL_OR_NONE) {
            roundTrip(sql, expected, dialect);
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"ALTER INSTANCE ROTATE MASTER KEY",
            "ALTER INSTANCE ROTATE MYISAM MASTER KEY",
            "ALTER INSTANCE ROTATE REDO_LOG MASTER KEY",
            "ALTER INSTANCE ROTATE 'x' MASTER KEY",
            "ALTER INSTANCE ROTATE N'INNODB' MASTER KEY",
            "ALTER INSTANCE ROTATE X'494E4E4F4442' MASTER KEY",
            "ALTER INSTANCE ROTATE INNODB MASTER",
            "ALTER INSTANCE ROTATE INNODB KEY",
            "ALTER INSTANCE ROTATE INNODB MASTER KEYS",
            "ALTER INSTANCE ROTATE INNODB MASTER KEY x",
            "ALTER INSTANCE ROTATE INNODB, BINLOG MASTER KEY",
            "ALTER INSTANCE ROTATE INNODB MASTER KEY, ROTATE BINLOG MASTER KEY",
            "ALTER INSTANCE RELOAD",
            "ALTER INSTANCE RELOAD SSL",
            "ALTER INSTANCE RELOAD TLS ROLLBACK ON ERROR",
            "ALTER INSTANCE RELOAD TLS NO ROLLBACK",
            "ALTER INSTANCE RELOAD TLS NO ROLLBACK ON ERROR x",
            "ALTER INSTANCE RELOAD TLS NO ROLLBACK ON ERROR FOR CHANNEL mysql_main",
            "ALTER INSTANCE RELOAD TLS FOR CHANNEL",
            "ALTER INSTANCE RELOAD TLS FOR CHANNEL 'mysql_main'",
            "ALTER INSTANCE RELOAD TLS FOR CHANNEL \"mysql_main\"",
            "ALTER INSTANCE RELOAD TLS FOR CHANNEL 1",
            "ALTER INSTANCE RELOAD TLS FOR CHANNEL d.mysql_main",
            "ALTER INSTANCE RELOAD TLS FOR CHANNEL mysql_main, mysql_admin",
            "ALTER INSTANCE RELOAD TLS FOR mysql_main",
            "ALTER INSTANCE RELOAD TLS CHANNEL mysql_main",
            "ALTER INSTANCE RELOAD TLS, RELOAD KEYRING",
            "ALTER INSTANCE RELOAD KEYRING NO ROLLBACK ON ERROR",
            "ALTER INSTANCE RELOAD KEYRING x",
            "ALTER INSTANCE ENABLE INNODB",
            "ALTER INSTANCE ENABLE REDO_LOG",
            "ALTER INSTANCE ENABLE INNODB REDO LOG",
            "ALTER INSTANCE ENABLE MYISAM REDO_LOG",
            "ALTER INSTANCE ENABLE BINLOG REDO_LOG",
            "ALTER INSTANCE ENABLE INNODB UNDO_LOG",
            "ALTER INSTANCE ENABLE 'INNODB' REDO_LOG",
            "ALTER INSTANCE ENABLE \"INNODB\" REDO_LOG",
            "ALTER INSTANCE ENABLE INNODB 'REDO_LOG'",
            "ALTER INSTANCE ENABLE INNODB REDO_LOG NO ROLLBACK ON ERROR",
            "ALTER INSTANCE DISABLE INNODB REDO_LOG x",
            "ALTER INSTANCE ENABLE INNODB REDO_LOG, DISABLE INNODB REDO_LOG"})
    void rejectsWhatTheServerGrammarRejects(String sql) {
        for (Dialect dialect : MYSQL_OR_NONE) {
            assertThrows(JSQLParserException.class, () -> parse(sql, dialect), sql);
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"ALTER INSTANCE", "ALTER INSTANCE FLUSH TLS",
            "ALTER INSTANCE mysql ENABLE INNODB REDO_LOG"})
    void mySqlDialectRejectsAnUnknownAction(String sql) throws Exception {
        assertThrows(JSQLParserException.class, () -> parse(sql, Dialect.MYSQL), sql);
        // without a dialect only a known action selects the statement
        assertInstanceOf(UnsupportedStatement.class, parse(sql, null));
    }

    @ParameterizedTest
    @EnumSource(value = Dialect.class, names = {"MARIADB", "POSTGRESQL", "ORACLE", "SQLSERVER"})
    void otherDialectsKeepTheirReading(Dialect dialect) throws Exception {
        assertInstanceOf(UnsupportedStatement.class,
                parse("ALTER INSTANCE ROTATE INNODB MASTER KEY", dialect));
        assertInstanceOf(UnsupportedStatement.class, parse("ALTER INSTANCE RELOAD SSL", dialect));
    }

    @Test
    void otherAlterStatementsAndInstanceNamesKeepTheirReading() throws Exception {
        for (Dialect dialect : MYSQL_OR_NONE) {
            assertInstanceOf(Alter.class, parse("ALTER TABLE instance ADD COLUMN c INT", dialect));
            assertInstanceOf(Alter.class,
                    parse("ALTER TABLE t ENABLE KEYS", dialect));
            assertInstanceOf(Select.class, parse("SELECT instance FROM instance", dialect));
            assertInstanceOf(BackupLockStatement.class,
                    parse("LOCK INSTANCE FOR BACKUP", dialect));
        }
    }

    @Test
    void statementsEndAtTheBoundary() throws Exception {
        String script = "ALTER INSTANCE RELOAD TLS;\n"
                + "ALTER INSTANCE RELOAD TLS FOR CHANNEL mysql_admin;\n"
                + "ALTER INSTANCE DISABLE INNODB REDO_LOG;\n"
                + "SELECT 1;\n"
                + "ALTER INSTANCE ROTATE BINLOG MASTER KEY";
        for (Dialect dialect : MYSQL_OR_NONE) {
            Statements statements = dialect == null ? CCJSqlParserUtil.parseStatements(script)
                    : CCJSqlParserUtil.parseStatements(script, p -> p.withDialect(dialect));
            assertEquals(5, statements.size());
            assertEquals(Action.RELOAD_TLS, ((AlterInstance) statements.get(0)).getAction());
            assertEquals("mysql_admin", ((AlterInstance) statements.get(1)).getChannel());
            assertEquals(Action.DISABLE_INNODB_REDO_LOG,
                    ((AlterInstance) statements.get(2)).getAction());
            assertInstanceOf(Select.class, statements.get(3));
            assertEquals(Action.ROTATE_BINLOG_MASTER_KEY,
                    ((AlterInstance) statements.get(4)).getAction());
            assertSpans("ALTER INSTANCE RELOAD TLS FOR CHANNEL mysql_admin", script,
                    (ASTNodeAccess) statements.get(1));
        }
        assertEquals("ALTER INSTANCE RELOAD KEYRING",
                parse("ALTER INSTANCE RELOAD KEYRING;", Dialect.MYSQL).toString());
    }

    @Test
    void canBeBuiltAndEdited() {
        AlterInstance statement = new AlterInstance(Action.RELOAD_TLS).withChannel("mysql_admin")
                .withNoRollbackOnError(true);
        assertEquals("ALTER INSTANCE RELOAD TLS FOR CHANNEL mysql_admin NO ROLLBACK ON ERROR",
                statement.toString());
        assertThrows(IllegalArgumentException.class,
                () -> statement.setAction(Action.RELOAD_KEYRING));
        statement.setChannel(null);
        assertThrows(IllegalArgumentException.class,
                () -> statement.setAction(Action.RELOAD_KEYRING));
        statement.setNoRollbackOnError(false);
        assertEquals("ALTER INSTANCE RELOAD KEYRING",
                statement.withAction(Action.RELOAD_KEYRING).toString());
        assertThrows(IllegalArgumentException.class, () -> statement.setChannel("mysql_main"));
        assertThrows(IllegalArgumentException.class, () -> statement.setNoRollbackOnError(true));
        statement.setChannel(null);
        statement.setNoRollbackOnError(false);
        assertNull(statement.getChannel());
        assertFalse(statement.isNoRollbackOnError());
        assertThrows(NullPointerException.class, () -> statement.setAction(null));
        assertThrows(NullPointerException.class, () -> new AlterInstance(null));
        assertEquals("ALTER INSTANCE ENABLE INNODB REDO_LOG",
                new AlterInstance(Action.ENABLE_INNODB_REDO_LOG).toString());
    }

    @Test
    void visitorsReachTheStatement() throws Exception {
        AlterInstance statement =
                (AlterInstance) parse("ALTER INSTANCE RELOAD TLS", Dialect.MYSQL);
        assertNull(statement.accept(new StatementVisitorAdapter<String>(), null));

        StringBuilder seen = new StringBuilder();
        StatementVisitor<String> visitor = new StatementVisitorAdapter<String>() {
            @Override
            public <S> String visit(AlterInstance visited, S context) {
                seen.append(visited.getAction()).append(context);
                return "visited";
            }
        };
        assertEquals("visited", statement.accept(visitor, "!"));
        visitor.visit(statement);
        assertEquals("RELOAD_TLS!RELOAD_TLSnull", seen.toString());
    }
}
