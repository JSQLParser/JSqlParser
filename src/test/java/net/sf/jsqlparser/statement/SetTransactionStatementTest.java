/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Stream;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.parser.AbstractJSqlParser.Dialect;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.parser.feature.Feature;
import net.sf.jsqlparser.parser.feature.FeatureConfiguration;
import net.sf.jsqlparser.util.TablesNamesFinder;
import net.sf.jsqlparser.util.deparser.StatementDeParser;
import net.sf.jsqlparser.util.validation.Validation;
import net.sf.jsqlparser.util.validation.feature.MySqlVersion;
import net.sf.jsqlparser.util.validation.feature.PostgresqlVersion;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

class SetTransactionStatementTest {
    private static Statement parse(String sql, Dialect dialect) throws JSQLParserException {
        return dialect == null ? CCJSqlParserUtil.parse(sql)
                : CCJSqlParserUtil.parse(sql, p -> p.withDialect(dialect));
    }

    /** Parses, renders through toString and the deparser, reparses and checks the analysis. */
    private static SetTransactionStatement roundTrip(String sql, Dialect dialect, String expected)
            throws Exception {
        SetTransactionStatement statement =
                assertInstanceOf(SetTransactionStatement.class, parse(sql, dialect));
        TransactionStatementTest.assertSpans(sql, sql, statement);
        assertEquals(expected, statement.toString());
        StringBuilder output = new StringBuilder();
        statement.accept(new StatementDeParser(output), null);
        assertEquals(expected, output.toString());
        assertEquals(expected, parse(expected, dialect).toString());
        assertEquals(Set.of(StmtFeature.MODIFIES_TRANSACTION),
                statement.getFeatures().getCertain());
        assertTrue(new TablesNamesFinder().getTables(statement).isEmpty());
        return statement;
    }

    static Stream<Arguments> mysqlForms() {
        return Stream.of(
                Arguments.of("SET TRANSACTION ISOLATION LEVEL READ COMMITTED", SetScope.NONE,
                        List.of(TransactionMode.ISOLATION_LEVEL_READ_COMMITTED)),
                Arguments.of("SET SESSION TRANSACTION ISOLATION LEVEL REPEATABLE READ, READ ONLY",
                        SetScope.SESSION,
                        List.of(TransactionMode.ISOLATION_LEVEL_REPEATABLE_READ,
                                TransactionMode.READ_ONLY)),
                Arguments.of("SET GLOBAL TRANSACTION READ WRITE", SetScope.GLOBAL,
                        List.of(TransactionMode.READ_WRITE)),
                Arguments.of("SET GLOBAL TRANSACTION READ ONLY, ISOLATION LEVEL SERIALIZABLE",
                        SetScope.GLOBAL,
                        List.of(TransactionMode.READ_ONLY,
                                TransactionMode.ISOLATION_LEVEL_SERIALIZABLE)),
                Arguments.of("SET TRANSACTION ISOLATION LEVEL READ UNCOMMITTED", SetScope.NONE,
                        List.of(TransactionMode.ISOLATION_LEVEL_READ_UNCOMMITTED)));
    }

    @ParameterizedTest
    @MethodSource("mysqlForms")
    void mysqlScopesAndCharacteristics(String sql, SetScope scope, List<TransactionMode> modes)
            throws Exception {
        for (Dialect dialect : new Dialect[] {Dialect.MYSQL, Dialect.MARIADB, null}) {
            SetTransactionStatement statement = roundTrip(sql, dialect, sql);
            assertEquals(scope, statement.getScope());
            assertEquals(modes, statement.getModes());
            assertFalse(statement.isSessionCharacteristics());
            assertNull(statement.getSnapshotId());
        }
    }

    static Stream<Arguments> scopedForms() {
        List<Dialect> all = List.of(Dialect.MYSQL, Dialect.MARIADB, Dialect.POSTGRESQL);
        List<Dialect> mysqlOnly = List.of(Dialect.MYSQL);
        List<Dialect> mysqlAndMariaDb = List.of(Dialect.MYSQL, Dialect.MARIADB);
        return Stream.of(
                Arguments.of("SET LOCAL TRANSACTION READ ONLY", SetScope.LOCAL, all),
                Arguments.of("SET LOCAL TRANSACTION ISOLATION LEVEL SERIALIZABLE", SetScope.LOCAL,
                        all),
                Arguments.of("SET LOCAL TRANSACTION ISOLATION LEVEL SERIALIZABLE, READ ONLY",
                        SetScope.LOCAL, all),
                Arguments.of("SET SESSION TRANSACTION ISOLATION LEVEL READ COMMITTED",
                        SetScope.SESSION, all),
                Arguments.of("SET GLOBAL TRANSACTION READ ONLY", SetScope.GLOBAL, mysqlAndMariaDb),
                Arguments.of("SET PERSIST TRANSACTION READ ONLY", SetScope.PERSIST, mysqlOnly),
                Arguments.of("SET PERSIST_ONLY TRANSACTION ISOLATION LEVEL READ COMMITTED",
                        SetScope.PERSIST_ONLY, mysqlOnly),
                Arguments.of("set persist_only transaction read write", SetScope.PERSIST_ONLY,
                        mysqlOnly));
    }

    @ParameterizedTest
    @MethodSource("scopedForms")
    void scopesFollowEachServerGrammar(String sql, SetScope scope, List<Dialect> accepting)
            throws Exception {
        String expected = sql.toUpperCase(Locale.ROOT);
        for (Dialect dialect : new Dialect[] {Dialect.MYSQL, Dialect.MARIADB, Dialect.POSTGRESQL,
                null}) {
            if (dialect == null || accepting.contains(dialect)) {
                SetTransactionStatement statement = roundTrip(sql, dialect, expected);
                assertEquals(scope, statement.getScope());
                assertFalse(statement.isSessionCharacteristics());
            } else {
                assertThrows(JSQLParserException.class, () -> parse(sql, dialect),
                        sql + " with " + dialect);
            }
        }
    }

    @Test
    void postgresTransactionCharacteristicsAcceptSpacesAndCommas() throws Exception {
        SetTransactionStatement statement =
                roundTrip("SET TRANSACTION ISOLATION LEVEL SERIALIZABLE READ ONLY DEFERRABLE",
                        Dialect.POSTGRESQL,
                        "SET TRANSACTION ISOLATION LEVEL SERIALIZABLE, READ ONLY, DEFERRABLE");
        assertEquals(SetScope.NONE, statement.getScope());
        assertEquals(
                List.of(TransactionMode.ISOLATION_LEVEL_SERIALIZABLE, TransactionMode.READ_ONLY,
                        TransactionMode.DEFERRABLE),
                statement.getModes());
        roundTrip("SET TRANSACTION ISOLATION LEVEL READ COMMITTED", Dialect.POSTGRESQL,
                "SET TRANSACTION ISOLATION LEVEL READ COMMITTED");
        roundTrip("SET TRANSACTION READ WRITE, NOT DEFERRABLE", Dialect.POSTGRESQL,
                "SET TRANSACTION READ WRITE, NOT DEFERRABLE");
        assertEquals(SetScope.SESSION,
                roundTrip("SET SESSION TRANSACTION READ ONLY", Dialect.POSTGRESQL,
                        "SET SESSION TRANSACTION READ ONLY").getScope());
    }

    @ParameterizedTest
    @ValueSource(strings = {"SET SESSION CHARACTERISTICS AS TRANSACTION READ ONLY",
            "SET SESSION CHARACTERISTICS AS TRANSACTION ISOLATION LEVEL REPEATABLE READ, READ WRITE",
            "set session characteristics as transaction read only"})
    void postgresSessionCharacteristics(String sql) throws Exception {
        String expected = sql.toUpperCase(Locale.ROOT);
        for (Dialect dialect : new Dialect[] {Dialect.POSTGRESQL, null}) {
            SetTransactionStatement statement = roundTrip(sql, dialect, expected);
            assertEquals(SetScope.NONE, statement.getScope());
            assertTrue(statement.isSessionCharacteristics());
            assertFalse(statement.getModes().isEmpty());
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"SET LOCAL SESSION CHARACTERISTICS AS TRANSACTION READ ONLY",
            "SET SESSION SESSION CHARACTERISTICS AS TRANSACTION ISOLATION LEVEL SERIALIZABLE"})
    void postgresScopeBeforeSessionCharacteristicsIsKept(String sql) throws Exception {
        for (Dialect dialect : new Dialect[] {Dialect.POSTGRESQL, null}) {
            SetTransactionStatement statement = roundTrip(sql, dialect, sql);
            assertEquals(sql.startsWith("SET LOCAL") ? SetScope.LOCAL : SetScope.SESSION,
                    statement.getScope());
            assertTrue(statement.isSessionCharacteristics());
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"SET TRANSACTION SNAPSHOT '00000003-0000001B-1'",
            "SET SESSION TRANSACTION SNAPSHOT '00000003-0000001B-1'",
            "SET LOCAL TRANSACTION SNAPSHOT '00000003-0000001B-1'"})
    void postgresSnapshotKeepsTheStringLiteral(String sql) throws Exception {
        SetScope scope = sql.startsWith("SET SESSION") ? SetScope.SESSION
                : sql.startsWith("SET LOCAL") ? SetScope.LOCAL : SetScope.NONE;
        for (Dialect dialect : new Dialect[] {Dialect.POSTGRESQL, null}) {
            SetTransactionStatement statement = roundTrip(sql, dialect, sql);
            StringValue snapshot = statement.getSnapshotId();
            assertEquals("00000003-0000001B-1", snapshot.getValue());
            TransactionStatementTest.assertSpans("'00000003-0000001B-1'", sql, snapshot);
            assertTrue(statement.getModes().isEmpty());
            assertEquals(scope, statement.getScope());
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"SET TRANSACTION ISOLATION LEVEL READ COMMITTED READ ONLY",
            "SET TRANSACTION READ ONLY, READ WRITE",
            "SET TRANSACTION ISOLATION LEVEL READ COMMITTED, ISOLATION LEVEL SERIALIZABLE",
            "SET TRANSACTION DEFERRABLE", "SET SESSION TRANSACTION NOT DEFERRABLE",
            "SET TRANSACTION WITH CONSISTENT SNAPSHOT",
            "SET TRANSACTION SNAPSHOT '00000003-0000001B-1'",
            "SET SESSION TRANSACTION SNAPSHOT '1'", "SET LOCAL TRANSACTION SNAPSHOT '1'",
            "SET SESSION CHARACTERISTICS AS TRANSACTION READ ONLY",
            "SET LOCAL SESSION CHARACTERISTICS AS TRANSACTION READ ONLY",
            "SET SESSION SESSION CHARACTERISTICS AS TRANSACTION READ ONLY"})
    void mysqlRejectsPostgresOnlyAndRepeatedCharacteristics(String sql) {
        assertThrows(JSQLParserException.class, () -> parse(sql, Dialect.MYSQL));
        assertThrows(JSQLParserException.class, () -> parse(sql, Dialect.MARIADB));
    }

    @ParameterizedTest
    @ValueSource(strings = {"SET GLOBAL TRANSACTION READ ONLY",
            "SET PERSIST TRANSACTION READ ONLY",
            "SET PERSIST_ONLY TRANSACTION ISOLATION LEVEL READ COMMITTED",
            "SET TRANSACTION WITH CONSISTENT SNAPSHOT"})
    void postgresRejectsMysqlOnlyForms(String sql) {
        assertThrows(JSQLParserException.class, () -> parse(sql, Dialect.POSTGRESQL));
    }

    @ParameterizedTest
    @ValueSource(strings = {"SET GLOBAL TRANSACTION SNAPSHOT 'x'",
            "SET PERSIST TRANSACTION SNAPSHOT 'x'",
            "SET PERSIST SESSION CHARACTERISTICS AS TRANSACTION READ ONLY",
            "SET GLOBAL SESSION CHARACTERISTICS AS TRANSACTION READ ONLY",
            "SET PERSIST_ONLY SESSION CHARACTERISTICS AS TRANSACTION READ ONLY",
            "SET SESSION CHARACTERISTICS AS TRANSACTION SNAPSHOT '1'",
            "SET LOCAL SESSION CHARACTERISTICS AS TRANSACTION SNAPSHOT '1'"})
    void scopesThatNoServerAcceptsAreRejected(String sql) {
        for (Dialect dialect : new Dialect[] {Dialect.POSTGRESQL, Dialect.MYSQL, Dialect.MARIADB,
                null}) {
            assertThrows(JSQLParserException.class, () -> parse(sql, dialect), sql);
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"SET TRANSACTION ISOLATION LEVEL", "SET TRANSACTION READ",
            "SET TRANSACTION READ ONLY,", "SET TRANSACTION , READ ONLY",
            "SET SESSION CHARACTERISTICS AS TRANSACTION",
            "SET SESSION CHARACTERISTICS TRANSACTION READ ONLY",
            "SET TRANSACTION ISOLATION LEVEL READ COMMITTED garbage"})
    void malformedFormsAreRejected(String sql) {
        for (Dialect dialect : new Dialect[] {Dialect.POSTGRESQL, Dialect.MYSQL, null}) {
            assertThrows(JSQLParserException.class, () -> parse(sql, dialect), sql);
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"SET SESSION work_mem TO DEFAULT", "SET LOCAL statement_timeout = '5s'",
            "SET @@SESSION.sql_mode = 'STRICT_TRANS_TABLES'",
            "SET transaction_isolation = 'READ-COMMITTED'",
            "SET SESSION transaction_isolation = 'READ-COMMITTED'", "SET transaction = 1",
            "SET LOCAL transaction = 1",
            "SET LOCAL transaction_isolation = 'READ-COMMITTED'",
            "SET GLOBAL transaction_isolation = 'READ-COMMITTED'",
            "SET PERSIST transaction_isolation = 'READ-COMMITTED'",
            "SET PERSIST_ONLY transaction_isolation = 'READ-COMMITTED'",
            "SET SESSION AUTHORIZATION DEFAULT"})
    void variableAssignmentsRemainSetStatements(String sql) throws Exception {
        for (Dialect dialect : new Dialect[] {Dialect.POSTGRESQL, Dialect.MYSQL, null}) {
            if (sql.contains(" TO ") && dialect == Dialect.MYSQL
                    || sql.contains("AUTHORIZATION") && dialect == Dialect.MYSQL) {
                continue;
            }
            Statement statement = parse(sql, dialect);
            assertInstanceOf(SetStatement.class, statement, sql);
            assertEquals(sql, statement.toString());
        }
    }

    @Test
    void modelCanBeBuiltAndChanged() throws Exception {
        SetTransactionStatement statement = new SetTransactionStatement()
                .withScope(SetScope.GLOBAL).addMode(TransactionMode.READ_ONLY);
        assertEquals("SET GLOBAL TRANSACTION READ ONLY", statement.toString());
        statement.setScope(SetScope.PERSIST_ONLY);
        assertEquals("SET PERSIST_ONLY TRANSACTION READ ONLY", statement.toString());
        statement.withScope(null).withSessionCharacteristics(true).getModes().set(0,
                TransactionMode.READ_WRITE);
        assertEquals(SetScope.NONE, statement.getScope());
        assertEquals("SET SESSION CHARACTERISTICS AS TRANSACTION READ WRITE", statement.toString());
        statement.setScope(SetScope.LOCAL);
        assertTrue(statement.isSessionCharacteristics());
        assertEquals("SET LOCAL SESSION CHARACTERISTICS AS TRANSACTION READ WRITE",
                statement.toString());
        statement.withSessionCharacteristics(false).setScope(null);
        assertEquals(SetScope.NONE, statement.getScope());
        assertFalse(statement.isSessionCharacteristics());
        statement.getModes().clear();
        statement.withSnapshotId(new StringValue("snap"));
        assertEquals("SET TRANSACTION SNAPSHOT 'snap'", statement.toString());
        assertEquals(statement.toString(),
                parse(statement.toString(), Dialect.POSTGRESQL).toString());
        assertThrows(NullPointerException.class, () -> statement.addMode(null));
    }

    @Test
    void visitorsAndStatementBoundaries() throws Exception {
        Statement statement = parse("SET TRANSACTION READ ONLY", Dialect.POSTGRESQL);
        StatementVisitorAdapter<String> visitor = new StatementVisitorAdapter<>() {
            @Override
            public <S> String visit(SetTransactionStatement set, S context) {
                return context + ":" + set.getModes();
            }
        };
        assertEquals("ctx:[READ ONLY]", statement.accept(visitor, "ctx"));
        assertNull(statement.accept(new StatementVisitorAdapter<>(), null));
        Statements script = CCJSqlParserUtil.parseStatements(
                "SET TRANSACTION READ ONLY; SELECT 1; SET SESSION CHARACTERISTICS AS TRANSACTION READ WRITE",
                p -> p.withDialect(Dialect.POSTGRESQL));
        assertEquals(3, script.size());
        assertInstanceOf(SetTransactionStatement.class, script.get(0));
        assertInstanceOf(SetTransactionStatement.class, script.get(2));
    }

    @Test
    void validationSeparatesMysqlAndPostgresForms() {
        FeatureConfiguration none = new FeatureConfiguration();
        assertTrue(new Validation(none, List.of(MySqlVersion.V8_0),
                "SET GLOBAL TRANSACTION ISOLATION LEVEL SERIALIZABLE, READ ONLY; "
                        + "SET SESSION TRANSACTION READ WRITE")
                .validate().isEmpty());
        assertFalse(new Validation(none, List.of(MySqlVersion.V8_0),
                "SET SESSION CHARACTERISTICS AS TRANSACTION READ ONLY").validate().isEmpty());
        assertFalse(new Validation(none, List.of(MySqlVersion.V8_0),
                "SET TRANSACTION SNAPSHOT 'x'").validate().isEmpty());
        assertTrue(new Validation(none, List.of(PostgresqlVersion.V10),
                "SET TRANSACTION ISOLATION LEVEL READ COMMITTED DEFERRABLE; "
                        + "SET TRANSACTION SNAPSHOT 'x'; "
                        + "SET SESSION CHARACTERISTICS AS TRANSACTION READ ONLY")
                .validate().isEmpty());
        assertFalse(new Validation(none, List.of(PostgresqlVersion.V10),
                "SET GLOBAL TRANSACTION READ ONLY").validate().isEmpty());
        assertFalse(new Validation(new FeatureConfiguration().setValue(Feature.dialect,
                Dialect.MYSQL.name()), List.of(MySqlVersion.V8_0),
                "SET TRANSACTION READ ONLY, READ WRITE").validate().isEmpty());
        assertTrue(new Validation(none, List.of(MySqlVersion.V8_0),
                "SET PERSIST TRANSACTION READ ONLY; SET PERSIST_ONLY TRANSACTION READ WRITE; "
                        + "SET LOCAL TRANSACTION ISOLATION LEVEL SERIALIZABLE")
                .validate().isEmpty());
        assertTrue(new Validation(none, List.of(PostgresqlVersion.V10),
                "SET LOCAL TRANSACTION READ ONLY; SET SESSION TRANSACTION SNAPSHOT 'x'; "
                        + "SET LOCAL SESSION CHARACTERISTICS AS TRANSACTION READ ONLY")
                .validate().isEmpty());
        assertFalse(new Validation(none, List.of(PostgresqlVersion.V10),
                "SET PERSIST TRANSACTION READ ONLY").validate().isEmpty());
    }
}
