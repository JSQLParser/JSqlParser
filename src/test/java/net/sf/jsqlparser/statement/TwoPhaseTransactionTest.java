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

import static net.sf.jsqlparser.statement.TransactionStatementTest.assertSpans;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Stream;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.HexValue;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.parser.ASTNodeAccess;
import net.sf.jsqlparser.parser.AbstractJSqlParser.Dialect;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.parser.feature.Feature;
import net.sf.jsqlparser.parser.feature.FeatureConfiguration;
import net.sf.jsqlparser.statement.TransactionCompletionStatement.PreparedTransactionKeyword;
import net.sf.jsqlparser.util.TablesNamesFinder;
import net.sf.jsqlparser.util.deparser.StatementDeParser;
import net.sf.jsqlparser.util.validation.Validation;
import net.sf.jsqlparser.util.validation.ValidationCapability;
import net.sf.jsqlparser.util.validation.feature.FeaturesAllowed;
import net.sf.jsqlparser.util.validation.feature.MariaDbVersion;
import net.sf.jsqlparser.util.validation.feature.MySqlVersion;
import net.sf.jsqlparser.util.validation.feature.OracleVersion;
import net.sf.jsqlparser.util.validation.feature.PostgresqlVersion;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * PostgreSQL PREPARE TRANSACTION, COMMIT PREPARED and ROLLBACK PREPARED, and the MySQL XA
 * statements, which share {@link TransactionId}.
 */
class TwoPhaseTransactionTest {
    private static final Dialect[] MYSQL_FAMILY = {Dialect.MYSQL, Dialect.MARIADB, null};

    private static Statement parse(String sql, Dialect dialect) throws JSQLParserException {
        return dialect == null ? CCJSqlParserUtil.parse(sql)
                : CCJSqlParserUtil.parse(sql, p -> p.withDialect(dialect));
    }

    /**
     * Parses {@code sql}, which must print as written, and checks the statement span, the deparser,
     * the reparse, the feature analysis and the table finder.
     */
    private static Statement roundTrip(String sql, Dialect dialect, StmtFeature feature)
            throws Exception {
        Statement statement = parse(sql, dialect);
        assertEquals(sql, statement.toString());
        assertSpans(sql, sql, (ASTNodeAccess) statement);
        StringBuilder output = new StringBuilder();
        statement.accept(new StatementDeParser(output), null);
        assertEquals(sql, output.toString());
        Statement reparsed = parse(sql, dialect);
        assertEquals(statement.getClass(), reparsed.getClass());
        assertEquals(sql, reparsed.toString());
        assertEquals(Set.of(feature), statement.getFeatures().getCertain());
        assertTrue(statement.getFeatures().getUncertain().isEmpty());
        assertTrue(new TablesNamesFinder<>().getTables(statement).isEmpty());
        return statement;
    }

    private static Statement roundTrip(String sql, Dialect dialect) throws Exception {
        return roundTrip(sql, dialect, StmtFeature.MODIFIES_TRANSACTION);
    }

    private static StringValue string(Object value) {
        return assertInstanceOf(StringValue.class, value);
    }

    // ---- PostgreSQL ----------------------------------------------------------------------------

    @ParameterizedTest
    @ValueSource(strings = {"PREPARE TRANSACTION 'foobar'", "COMMIT PREPARED 'foobar'",
            "ROLLBACK PREPARED 'foobar'"})
    void postgresTwoPhaseStatementsNameTheTransaction(String sql) throws Exception {
        for (Dialect dialect : new Dialect[] {Dialect.POSTGRESQL, null}) {
            Statement statement = roundTrip(sql, dialect);
            TransactionId id;
            if (statement instanceof PrepareTransactionStatement) {
                id = ((PrepareTransactionStatement) statement).getTransactionId();
            } else {
                TransactionCompletionStatement completion =
                        assertInstanceOf(TransactionCompletionStatement.class, statement);
                assertEquals(PreparedTransactionKeyword.PREPARED,
                        completion.getPreparedTransactionKeyword());
                assertNull(completion.getKeyword());
                assertNull(completion.getChain());
                assertNull(completion.getRelease());
                assertFalse(completion.isPostgresSpelling());
                id = completion.getPreparedTransactionId();
            }
            assertEquals("foobar", string(id.getGlobalTransactionId()).getValue());
            assertNull(id.getBranchQualifier());
            assertNull(id.getFormatId());
            assertSpans("'foobar'", sql, id);
            assertSpans("'foobar'", sql, string(id.getGlobalTransactionId()));
        }
        assertInstanceOf(Commit.class, parse("COMMIT PREPARED 'foobar'", null));
        assertNull(((RollbackStatement) parse("ROLLBACK PREPARED 'foobar'", null))
                .getForceDistributedTransactionIdentifier());
    }

    @ParameterizedTest
    @ValueSource(strings = {"PREPARE TRANSACTION E'a\\'b'", "COMMIT PREPARED U&'\\0041'",
            "ROLLBACK PREPARED $$x$$", "COMMIT PREPARED $t$x$t$", "prepare transaction 'x'",
            "commit prepared 'x'", "rollback prepared 'x'"})
    void postgresTakesEveryStringConstant(String sql) throws Exception {
        Statement statement = parse(sql, Dialect.POSTGRESQL);
        assertEquals(sql.toUpperCase(Locale.ROOT).startsWith("PREPARE"),
                statement instanceof PrepareTransactionStatement);
        assertSpans(sql, sql, (ASTNodeAccess) statement);
        assertEquals(statement.toString(),
                parse(statement.toString(), Dialect.POSTGRESQL).toString());
    }

    @Test
    void escapeStringsKeepTheirValue() throws Exception {
        PrepareTransactionStatement statement = assertInstanceOf(
                PrepareTransactionStatement.class,
                parse("PREPARE TRANSACTION E'a\\'b'", Dialect.POSTGRESQL));
        StringValue value = string(statement.getTransactionId().getGlobalTransactionId());
        assertEquals("E", value.getPrefix());
        assertEquals("a'b", value.getNotExcapedValue());
    }

    @ParameterizedTest
    @ValueSource(strings = {"PREPARE TRANSACTION", "PREPARE TRANSACTION N'x'",
            "PREPARE TRANSACTION B'01'", "PREPARE TRANSACTION X'0A'",
            "PREPARE TRANSACTION 'x' 'y'", "PREPARE TRANSACTION 'x', 'y'",
            "COMMIT PREPARED", "COMMIT PREPARED x", "COMMIT PREPARED 1", "COMMIT PREPARED N'x'",
            "COMMIT PREPARED B'01'", "COMMIT PREPARED 'x' AND CHAIN",
            "COMMIT WORK PREPARED 'x'", "COMMIT TRANSACTION PREPARED 'x'",
            "COMMIT PREPARED 'x', 'y'", "COMMIT PREPARED 'x' 'y'", "ROLLBACK PREPARED",
            "ROLLBACK PREPARED x", "ROLLBACK PREPARED X'0A'", "ROLLBACK PREPARED 'x' AND NO CHAIN",
            "ROLLBACK WORK PREPARED 'x'", "ROLLBACK PREPARED 'x' TO SAVEPOINT sp",
            "ROLLBACK PREPARED 'x' FORCE 'y'", "END PREPARED 'x'", "ABORT PREPARED 'x'",
            "XA START 'x'", "XA RECOVER"})
    void postgresRejectsMalformedTwoPhaseStatements(String sql) {
        assertThrows(JSQLParserException.class, () -> parse(sql, Dialect.POSTGRESQL), sql);
    }

    @Test
    void doubleQuotedIdentifiersAreNoTransactionIds() {
        assertThrows(JSQLParserException.class,
                () -> CCJSqlParserUtil.parse("COMMIT PREPARED \"x\"",
                        p -> p.withDialect(Dialect.POSTGRESQL)));
        assertThrows(JSQLParserException.class, () -> CCJSqlParserUtil
                .parse("COMMIT PREPARED \"x\"", p -> p.withDoubleQuotedStrings(true)));
    }

    @ParameterizedTest
    @ValueSource(strings = {"PREPARE TRANSACTION 'x'", "COMMIT PREPARED 'x'",
            "ROLLBACK PREPARED 'x'"})
    void otherDialectsKeepRejectingPostgresTwoPhaseStatements(String sql) {
        for (Dialect dialect : new Dialect[] {Dialect.MYSQL, Dialect.MARIADB, Dialect.ORACLE,
                Dialect.SQLSERVER}) {
            assertThrows(JSQLParserException.class, () -> parse(sql, dialect), sql);
        }
    }

    @Test
    void preparedStatementsKeepTheirReading() throws Exception {
        for (Dialect dialect : new Dialect[] {Dialect.POSTGRESQL, null}) {
            for (String sql : List.of("PREPARE transaction AS SELECT 1",
                    "PREPARE transaction (int) AS SELECT $1", "PREPARE p AS SELECT 1")) {
                PrepareStatement statement =
                        assertInstanceOf(PrepareStatement.class, parse(sql, dialect));
                assertEquals(sql.substring(8, sql.indexOf(' ', 8)), statement.getName());
            }
        }
        for (String sql : List.of("SELECT prepared FROM prepared", "CREATE TABLE prepared (a INT)",
                "SAVEPOINT prepared", "ROLLBACK TO prepared")) {
            assertEquals(sql, parse(sql, Dialect.POSTGRESQL).toString());
            assertEquals(sql, parse(sql, null).toString());
        }
    }

    @Test
    void oracleForceSharesTheTransactionId() throws Exception {
        RollbackStatement rollback = assertInstanceOf(RollbackStatement.class,
                roundTrip("ROLLBACK WORK FORCE '25.32.87'", Dialect.ORACLE));
        assertEquals(PreparedTransactionKeyword.FORCE, rollback.getPreparedTransactionKeyword());
        assertEquals("'25.32.87'", rollback.getForceDistributedTransactionIdentifier());
        assertEquals("25.32.87",
                string(rollback.getPreparedTransactionId().getGlobalTransactionId()).getValue());
        assertSpans("'25.32.87'", "ROLLBACK WORK FORCE '25.32.87'",
                rollback.getPreparedTransactionId());
        assertEquals("ROLLBACK FORCE 'x'", parse("ROLLBACK FORCE 'x'", null).toString());
        assertThrows(JSQLParserException.class,
                () -> parse("ROLLBACK FORCE 'x' AND CHAIN", Dialect.ORACLE));
    }

    @Test
    void legacyForceAccessorsDeriveFromTheTransactionId() {
        RollbackStatement rollback =
                new RollbackStatement().withForceDistributedTransactionIdentifier("'1.2.3'");
        assertEquals("ROLLBACK FORCE '1.2.3'", rollback.toString());
        assertEquals(PreparedTransactionKeyword.FORCE, rollback.getPreparedTransactionKeyword());
        assertEquals("'1.2.3'", rollback.getForceDistributedTransactionIdentifier());
        assertEquals("1.2.3",
                string(rollback.getPreparedTransactionId().getGlobalTransactionId()).getValue());
        // text that is not a string literal is kept as written, as before
        rollback.setForceDistributedTransactionIdentifier("4.5.6");
        assertEquals("ROLLBACK FORCE 4.5.6", rollback.toString());
        assertEquals("4.5.6", rollback.getForceDistributedTransactionIdentifier());
        rollback.setForceDistributedTransactionIdentifier(" ");
        assertNull(rollback.getPreparedTransactionId());
        assertNull(rollback.getForceDistributedTransactionIdentifier());
        assertEquals("ROLLBACK", rollback.toString());

        TransactionId id = new TransactionId(new StringValue("'g'"));
        rollback.withPreparedTransaction(PreparedTransactionKeyword.PREPARED, id);
        assertNull(rollback.getForceDistributedTransactionIdentifier());
        rollback.setForceDistributedTransactionIdentifier(null);
        assertSame(id, rollback.getPreparedTransactionId());
        assertEquals("ROLLBACK PREPARED 'g'", rollback.toString());
        rollback.setPreparedTransaction(null, null);
        assertNull(rollback.getPreparedTransactionKeyword());
        assertThrows(NullPointerException.class, () -> rollback.setPreparedTransaction(null, id));

        Commit commit = new Commit().withPreparedTransaction(id);
        assertEquals("COMMIT PREPARED 'g'", commit.toString());
        StringBuilder output = new StringBuilder();
        commit.accept(new StatementDeParser(output), null);
        assertEquals("COMMIT PREPARED 'g'", output.toString());
        commit.withPreparedTransaction(null).withChain(TransactionChain.CHAIN);
        assertEquals("COMMIT AND CHAIN", commit.toString());
        PrepareTransactionStatement prepare = new PrepareTransactionStatement(id);
        assertEquals("PREPARE TRANSACTION 'g'", prepare.toString());
        assertThrows(NullPointerException.class, () -> prepare.setTransactionId(null));
    }

    // ---- MySQL XA ------------------------------------------------------------------------------

    static Stream<Arguments> xaStatements() {
        return Stream.of(
                Arguments.of("XA START 'x'", XaStatement.Action.START, null),
                Arguments.of("XA BEGIN 'x'", XaStatement.Action.BEGIN, null),
                Arguments.of("XA START 'x' JOIN", XaStatement.Action.START,
                        XaStatement.Option.JOIN),
                Arguments.of("XA BEGIN 'x' RESUME", XaStatement.Action.BEGIN,
                        XaStatement.Option.RESUME),
                Arguments.of("XA END 'x'", XaStatement.Action.END, null),
                Arguments.of("XA END 'x' SUSPEND", XaStatement.Action.END,
                        XaStatement.Option.SUSPEND),
                Arguments.of("XA END 'x' SUSPEND FOR MIGRATE", XaStatement.Action.END,
                        XaStatement.Option.SUSPEND_FOR_MIGRATE),
                Arguments.of("XA PREPARE 'x'", XaStatement.Action.PREPARE, null),
                Arguments.of("XA COMMIT 'x'", XaStatement.Action.COMMIT, null),
                Arguments.of("XA COMMIT 'x' ONE PHASE", XaStatement.Action.COMMIT,
                        XaStatement.Option.ONE_PHASE),
                Arguments.of("XA ROLLBACK 'x'", XaStatement.Action.ROLLBACK, null));
    }

    @ParameterizedTest
    @MethodSource("xaStatements")
    void xaStatementsKeepActionXidAndOption(String sql, XaStatement.Action action,
            XaStatement.Option option) throws Exception {
        for (Dialect dialect : MYSQL_FAMILY) {
            XaStatement statement =
                    assertInstanceOf(XaStatement.class, roundTrip(sql, dialect));
            assertEquals(action, statement.getAction());
            assertEquals(option, statement.getOption());
            TransactionId xid = statement.getTransactionId();
            assertEquals("x", string(xid.getGlobalTransactionId()).getValue());
            assertNull(xid.getBranchQualifier());
            assertSpans("'x'", sql, xid);
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"XA RECOVER", "XA RECOVER CONVERT XID"})
    void xaRecoverListsPreparedTransactions(String sql) throws Exception {
        for (Dialect dialect : new Dialect[] {Dialect.MYSQL, null}) {
            XaStatement statement = assertInstanceOf(XaStatement.class,
                    roundTrip(sql, dialect, StmtFeature.RETURNS_RESULT_SET));
            assertEquals(XaStatement.Action.RECOVER, statement.getAction());
            assertNull(statement.getTransactionId());
            assertEquals(sql.endsWith("XID") ? XaStatement.Option.CONVERT_XID : null,
                    statement.getOption());
        }
        assertInstanceOf(XaStatement.class, roundTrip("XA RECOVER", Dialect.MARIADB,
                StmtFeature.RETURNS_RESULT_SET));
        // MariaDB writes XA RECOVER FORMAT = 'SQL' instead
        assertThrows(JSQLParserException.class,
                () -> parse("XA RECOVER CONVERT XID", Dialect.MARIADB));
    }

    @Test
    void xidTakesBranchQualifierFormatIdAndEveryLiteralForm() throws Exception {
        String sql = "XA START X'0a', B'0101', 7 JOIN";
        XaStatement statement = assertInstanceOf(XaStatement.class, roundTrip(sql, Dialect.MYSQL));
        TransactionId xid = statement.getTransactionId();
        assertEquals("X'0a'", assertInstanceOf(HexValue.class, xid.getGlobalTransactionId())
                .getValue());
        StringValue bits = string(xid.getBranchQualifier());
        assertEquals("B", bits.getPrefix());
        assertEquals("0101", bits.getValue());
        assertEquals(7L, xid.getFormatId());
        assertSpans("X'0a', B'0101', 7", sql, xid);
        assertSpans("X'0a'", sql, (ASTNodeAccess) xid.getGlobalTransactionId());
        assertSpans("B'0101'", sql, bits);

        for (String form : List.of("XA PREPARE 0x6162, 'b'", "XA COMMIT 'a', '', 0",
                "XA ROLLBACK x'', X'00ff'", "XA END 'a', 'b', 9223372036854775807",
                "XA START \"a\", \"b\", 1")) {
            assertEquals(form, roundTrip(form, Dialect.MYSQL).toString());
        }
        assertEquals("XA START 'a', 'b', 2147483647",
                roundTrip("XA START 'a', 'b', 2147483647", Dialect.MARIADB).toString());
        assertEquals("XA START B'01'", parse("XA START b'01'", Dialect.MYSQL).toString());
        assertEquals("XA START 'a', 'b', 7",
                parse("XA START 'a', 'b', 007", Dialect.MYSQL).toString());
        assertEquals("XA START 'it''s'", roundTrip("XA START 'it''s'", null).toString());
        assertEquals("it's", string(((XaStatement) parse("XA START 'it\\'s'", Dialect.MYSQL))
                .getTransactionId().getGlobalTransactionId()).getNotExcapedValue());
    }

    @ParameterizedTest
    @ValueSource(strings = {"XA", "XA START", "XA START x", "XA START 1", "XA START NULL",
            "XA START @v", "XA START ?", "XA START N'x'", "XA START _utf8'x'", "XA START E'x'",
            "XA START $$x$$", "XA START X'0'", "XA START X'0 a'", "XA START X'0a' '0b'",
            "XA START 0X10", "XA START B'012'", "XA START 'a' 'b'", "XA START 'a',",
            "XA START 'a', 'b',", "XA START 'a', 'b', 'c'", "XA START 'a', 'b', -1",
            "XA START 'a', 'b', x", "XA START 'a', 'b', 1, 2", "XA START 'a', 1",
            "XA START 'a', 'b', 9223372036854775808", "XA START 'x' JOIN RESUME",
            "XA START 'x' SUSPEND", "XA START 'x' ONE PHASE", "XA BEGIN WORK",
            "XA END 'x' FOR MIGRATE", "XA END 'x' SUSPEND FOR", "XA END 'x' JOIN",
            "XA END 'x' SUSPEND MIGRATE", "XA PREPARE 'x' ONE PHASE", "XA PREPARE",
            "XA COMMIT 'x' ONE", "XA COMMIT 'x' PHASE", "XA COMMIT 'x' SUSPEND",
            "XA COMMIT 'x' AND CHAIN", "XA ROLLBACK 'x' JOIN", "XA ROLLBACK 'x' TO sp",
            "XA RECOVER 'x'", "XA RECOVER CONVERT", "XA RECOVER XID", "XA RECOVER CONVERT 'x'",
            "XA COMMIT", "XA ROLLBACK", "XA END"})
    void xaRejectsMalformedStatements(String sql) {
        for (Dialect dialect : MYSQL_FAMILY) {
            assertThrows(JSQLParserException.class, () -> parse(sql, dialect), sql);
        }
    }

    @Test
    void mariaDbFormatIdsAreSigned32Bit() {
        assertThrows(JSQLParserException.class,
                () -> parse("XA START 'a', 'b', 2147483648", Dialect.MARIADB));
    }

    @ParameterizedTest
    @ValueSource(strings = {"XA START 'x'", "XA END 'x'", "XA COMMIT 'x' ONE PHASE",
            "XA RECOVER"})
    void otherDialectsKeepRejectingXa(String sql) {
        for (Dialect dialect : new Dialect[] {Dialect.POSTGRESQL, Dialect.ORACLE,
                Dialect.SQLSERVER, Dialect.H2}) {
            assertThrows(JSQLParserException.class, () -> parse(sql, dialect), sql);
        }
    }

    @Test
    void xaRemainsAnIdentifier() throws Exception {
        for (Dialect dialect : MYSQL_FAMILY) {
            for (String sql : List.of("SELECT xa FROM xa", "CREATE TABLE xa (xa INT)",
                    "SELECT recover, migrate, phase, xid FROM t",
                    "ALTER TABLE t ADD COLUMN xa INT", "INSERT INTO xa (xa) VALUES (1)")) {
                assertEquals(sql, parse(sql, dialect).toString());
            }
        }
    }

    // ---- statement boundaries, visitors and validation -----------------------------------------

    static Stream<Arguments> newStatementStarts() {
        return Stream.of(
                Arguments.of(Dialect.POSTGRESQL, "PREPARE TRANSACTION 'x'",
                        PrepareTransactionStatement.class),
                Arguments.of(Dialect.POSTGRESQL, "COMMIT PREPARED 'x'", Commit.class),
                Arguments.of(null, "ROLLBACK PREPARED 'x'", RollbackStatement.class),
                Arguments.of(Dialect.MYSQL, "XA START 'x', 'b', 1 JOIN", XaStatement.class),
                Arguments.of(Dialect.MYSQL, "XA END 'x' SUSPEND FOR MIGRATE", XaStatement.class),
                Arguments.of(Dialect.MARIADB, "XA COMMIT 'x' ONE PHASE", XaStatement.class),
                Arguments.of(null, "XA RECOVER", XaStatement.class),
                Arguments.of(Dialect.MYSQL, "XA RECOVER CONVERT XID", XaStatement.class));
    }

    @ParameterizedTest
    @MethodSource("newStatementStarts")
    void newStatementStartsEndThePreviousStatement(Dialect dialect, String sql,
            Class<? extends Statement> type) throws Exception {
        // COMMIT on its own line is closed by the next statement start rather than a semicolon
        String script = "SELECT 1;\nCOMMIT\n" + sql + "\n;SAVEPOINT sp";
        for (boolean opaque : new boolean[] {false, true}) {
            Statements statements = CCJSqlParserUtil.parseStatements(script, p -> {
                if (dialect != null) {
                    p.withDialect(dialect);
                }
                p.withUnsupportedStatements(opaque);
            });
            assertEquals(4, statements.size(), script);
            assertEquals("COMMIT", statements.get(1).toString());
            assertInstanceOf(type, statements.get(2));
            assertEquals(sql, statements.get(2).toString());
            assertSpans(sql, script, (ASTNodeAccess) statements.get(2));
            assertInstanceOf(SavepointStatement.class, statements.get(3));
        }
    }

    @Test
    void scriptsKeepEachStatement() throws Exception {
        String mysql = "XA START 'x'; INSERT INTO t VALUES (1); XA END 'x'; XA PREPARE 'x';"
                + " XA RECOVER; XA COMMIT 'x'";
        Statements statements =
                CCJSqlParserUtil.parseStatements(mysql, p -> p.withDialect(Dialect.MYSQL));
        assertEquals(6, statements.size());
        assertEquals(Set.of("t"), new TablesNamesFinder<>().getTables(statements.get(1)));
        assertEquals(XaStatement.Action.RECOVER, ((XaStatement) statements.get(4)).getAction());
        String postgres = "BEGIN; UPDATE t SET a = 1; PREPARE TRANSACTION 'x'; COMMIT PREPARED 'x';"
                + " ROLLBACK PREPARED 'y'; END";
        statements = CCJSqlParserUtil.parseStatements(postgres,
                p -> p.withDialect(Dialect.POSTGRESQL));
        assertEquals(6, statements.size());
        assertInstanceOf(PrepareTransactionStatement.class, statements.get(2));
        assertEquals(Commit.Command.END, ((Commit) statements.get(5)).getCommand());
        assertEquals(statements.toString(), CCJSqlParserUtil.parseStatements(
                statements.toString(), p -> p.withDialect(Dialect.POSTGRESQL)).toString());
    }

    @Test
    void visitorDispatchPassesContextAndResult() throws Exception {
        StatementVisitorAdapter<String> visitor = new StatementVisitorAdapter<>() {
            @Override
            public <S> String visit(PrepareTransactionStatement statement, S context) {
                return context + ":" + statement.getTransactionId();
            }

            @Override
            public <S> String visit(XaStatement statement, S context) {
                return context + ":" + statement.getAction();
            }
        };
        assertEquals("ctx:'x'", parse("PREPARE TRANSACTION 'x'", null).accept(visitor, "ctx"));
        assertEquals("ctx:END", parse("XA END 'x'", null).accept(visitor, "ctx"));
        assertNull(parse("XA END 'x'", null).accept(new StatementVisitorAdapter<>(), null));
        assertNull(parse("PREPARE TRANSACTION 'x'", null)
                .accept(new StatementVisitorAdapter<>(), null));
    }

    private static boolean isValid(Dialect dialect, String sql,
            ValidationCapability... capabilities) {
        FeatureConfiguration configuration = new FeatureConfiguration();
        if (dialect != null) {
            configuration.setValue(Feature.dialect, dialect.name());
        }
        return new Validation(configuration, Arrays.asList(capabilities), sql).validate()
                .isEmpty();
    }

    @Test
    void validationKnowsWhichServersTakeWhichForm() {
        String postgres = "PREPARE TRANSACTION 'x'; COMMIT PREPARED 'x'; ROLLBACK PREPARED 'x'";
        assertTrue(isValid(Dialect.POSTGRESQL, postgres, PostgresqlVersion.V10));
        assertTrue(isValid(null, postgres, PostgresqlVersion.V14));
        for (String sql : postgres.split("; ")) {
            assertFalse(isValid(null, sql, MySqlVersion.V8_0), sql);
            assertFalse(isValid(null, sql, OracleVersion.V19C), sql);
        }
        assertFalse(isValid(Dialect.POSTGRESQL, "COMMIT PREPARED 'x'",
                new FeaturesAllowed(Feature.commit)));
        assertTrue(isValid(Dialect.POSTGRESQL, "COMMIT PREPARED 'x'",
                new FeaturesAllowed(Feature.commit, Feature.twoPhaseCommit)));
        assertTrue(isValid(Dialect.POSTGRESQL, "ROLLBACK PREPARED 'x'",
                new FeaturesAllowed(Feature.rollback, Feature.twoPhaseCommit)));
        assertTrue(isValid(Dialect.ORACLE, "ROLLBACK FORCE 'x'", OracleVersion.V19C));

        String xa = "XA START 'x'; XA END 'x'; XA PREPARE 'x'; XA COMMIT 'x'; XA RECOVER";
        assertTrue(isValid(Dialect.MYSQL, xa, MySqlVersion.V8_0));
        assertTrue(isValid(Dialect.MARIADB, xa, MariaDbVersion.V10_5_4));
        assertTrue(isValid(null, xa, new FeaturesAllowed(Feature.xa)));
        assertFalse(isValid(null, "XA RECOVER", PostgresqlVersion.V14));
        assertFalse(isValid(null, "XA ROLLBACK 'x'", new FeaturesAllowed(Feature.rollback)));
    }

    // ---- the shared model ----------------------------------------------------------------------

    @Test
    void transactionIdKeepsTheXidShape() {
        TransactionId id = new TransactionId(new StringValue("'g'"));
        assertEquals("'g'", id.toString());
        id.withBranchQualifier(new HexValue("X'0a'")).withFormatId(3L);
        assertEquals("'g', X'0a', 3", id.toString());
        assertThrows(IllegalStateException.class, () -> id.setBranchQualifier(null));
        assertThrows(IllegalArgumentException.class, () -> id.setFormatId(-1L));
        id.withFormatId(null).withBranchQualifier(null);
        assertEquals("'g'", id.toString());
        assertThrows(IllegalStateException.class, () -> id.setFormatId(1L));
        assertThrows(NullPointerException.class, () -> id.setGlobalTransactionId(null));
        assertEquals("'h', 'b', 1",
                new TransactionId(new StringValue("'h'"), new StringValue("'b'"), 1L).toString());
        assertThrows(IllegalStateException.class,
                () -> new TransactionId(new StringValue("'h'"), null, 1L));
        id.withGlobalTransactionId(new HexValue("0x01"));
        assertEquals("0x01", id.appendTo(new StringBuilder()).toString());
    }

    @Test
    void xaStatementKeepsOptionsWithTheirAction() {
        TransactionId xid = new TransactionId(new StringValue("'x'"));
        XaStatement statement = new XaStatement(XaStatement.Action.END, xid)
                .withOption(XaStatement.Option.SUSPEND_FOR_MIGRATE);
        assertEquals("XA END 'x' SUSPEND FOR MIGRATE", statement.toString());
        assertThrows(IllegalArgumentException.class,
                () -> statement.setAction(XaStatement.Action.COMMIT));
        assertThrows(IllegalArgumentException.class,
                () -> statement.setOption(XaStatement.Option.ONE_PHASE));
        statement.withOption(null).withAction(XaStatement.Action.COMMIT)
                .withOption(XaStatement.Option.ONE_PHASE);
        assertEquals("XA COMMIT 'x' ONE PHASE", statement.toString());
        StringBuilder output = new StringBuilder();
        statement.accept(new StatementDeParser(output), null);
        assertEquals("XA COMMIT 'x' ONE PHASE", output.toString());
        assertSame(xid, statement.getTransactionId());
        XaStatement recover = new XaStatement(XaStatement.Action.RECOVER, null)
                .withOption(XaStatement.Option.CONVERT_XID);
        assertEquals("XA RECOVER CONVERT XID", recover.toString());
        recover.withOption(null).withAction(XaStatement.Action.BEGIN).withTransactionId(xid)
                .withOption(XaStatement.Option.RESUME);
        assertEquals("XA BEGIN 'x' RESUME", recover.toString());
        assertThrows(NullPointerException.class, () -> recover.setAction(null));
        assertThrows(NullPointerException.class, () -> new XaStatement(null, xid));
        for (XaStatement.Option option : XaStatement.Option.values()) {
            long actions = Arrays.stream(XaStatement.Action.values()).filter(option::appliesTo)
                    .count();
            assertEquals(option == XaStatement.Option.JOIN
                    || option == XaStatement.Option.RESUME ? 2 : 1, actions, option.name());
        }
    }
}
