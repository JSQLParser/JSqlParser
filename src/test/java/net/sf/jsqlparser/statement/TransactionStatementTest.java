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

import static org.junit.jupiter.api.Assertions.*;

import java.io.StringReader;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.ASTNodeAccess;
import net.sf.jsqlparser.parser.AbstractJSqlParser.Dialect;
import net.sf.jsqlparser.parser.CCJSqlParser;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.parser.Node;
import net.sf.jsqlparser.parser.StringProvider;
import net.sf.jsqlparser.parser.StreamProvider;
import net.sf.jsqlparser.parser.feature.Feature;
import net.sf.jsqlparser.parser.feature.FeatureConfiguration;
import net.sf.jsqlparser.statement.create.function.CreateFunction;
import net.sf.jsqlparser.statement.oracle.OracleBlock;
import net.sf.jsqlparser.util.TablesNamesFinder;
import net.sf.jsqlparser.util.deparser.StatementDeParser;
import net.sf.jsqlparser.util.validation.Validation;
import net.sf.jsqlparser.util.validation.feature.FeaturesAllowed;
import net.sf.jsqlparser.util.validation.feature.MySqlVersion;
import net.sf.jsqlparser.util.validation.feature.PostgresqlVersion;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

class TransactionStatementTest {
    private Statement parse(String sql, Dialect dialect) throws JSQLParserException {
        return dialect == null ? CCJSqlParserUtil.parse(sql)
                : CCJSqlParserUtil.parse(sql, p -> p.withDialect(dialect));
    }

    /** Asserts that the object's node spans {@code expected} within {@code source}. */
    static void assertSpans(String expected, String source, ASTNodeAccess access) {
        Node node = access.getASTNode();
        assertNotNull(node, expected);
        assertEquals(expected, source.substring(node.jjtGetFirstToken().absoluteBegin - 1,
                node.jjtGetLastToken().absoluteEnd - 1));
    }

    private Statement roundTrip(String sql, Dialect dialect) throws Exception {
        Statement statement = parse(sql, dialect);
        assertSpans(sql, sql, (ASTNodeAccess) statement);
        StringBuilder output = new StringBuilder();
        statement.accept(new StatementDeParser(output), null);
        assertEquals(statement.toString(), output.toString());
        Statement reparsed = parse(output.toString(), dialect);
        assertEquals(statement.getClass(), reparsed.getClass());
        assertEquals(statement.toString(), reparsed.toString());
        assertEquals(Set.of(StmtFeature.MODIFIES_TRANSACTION),
                statement.getFeatures().getCertain());
        assertTrue(statement.getFeatures().getUncertain().isEmpty());
        assertTrue(new TablesNamesFinder().getTables(statement).isEmpty());
        return statement;
    }

    @ParameterizedTest
    @ValueSource(strings = {"START TRANSACTION", "BEGIN", "BEGIN WORK", "BEGIN TRANSACTION",
            "START TRANSACTION ISOLATION LEVEL SERIALIZABLE, READ ONLY, DEFERRABLE",
            "BEGIN ISOLATION LEVEL REPEATABLE READ READ WRITE NOT DEFERRABLE",
            "BEGIN TRANSACTION ISOLATION LEVEL READ COMMITTED",
            "START TRANSACTION ISOLATION LEVEL READ UNCOMMITTED",
            "BEGIN NOT DEFERRABLE, READ ONLY, ISOLATION LEVEL SERIALIZABLE"})
    void postgresStartsAreStructured(String sql) throws Exception {
        StartTransaction statement = assertInstanceOf(StartTransaction.class,
                roundTrip(sql, Dialect.POSTGRESQL));
        assertEquals(sql.startsWith("BEGIN") ? StartTransaction.Command.BEGIN
                : StartTransaction.Command.START_TRANSACTION, statement.getCommand());
        assertEquals(sql.startsWith("BEGIN WORK") ? TransactionKeyword.WORK
                : sql.startsWith("BEGIN TRANSACTION") ? TransactionKeyword.TRANSACTION : null,
                statement.getKeyword());
    }

    @ParameterizedTest
    @ValueSource(strings = {"START TRANSACTION", "BEGIN", "BEGIN WORK",
            "START TRANSACTION READ ONLY", "START TRANSACTION READ WRITE",
            "START TRANSACTION WITH CONSISTENT SNAPSHOT, READ ONLY",
            "START TRANSACTION READ WRITE, WITH CONSISTENT SNAPSHOT"})
    void mysqlStartsAreStructured(String sql) throws Exception {
        assertInstanceOf(StartTransaction.class, roundTrip(sql, Dialect.MYSQL));
    }

    @ParameterizedTest
    @ValueSource(strings = {"START TRANSACTION READ ONLY, READ WRITE",
            "START TRANSACTION READ WRITE, READ ONLY"})
    void contradictoryAccessModesAreRejectedOnlyForMysql(String sql) throws Exception {
        JSQLParserException error = assertThrows(JSQLParserException.class,
                () -> parse(sql, Dialect.MYSQL));
        assertTrue(error.getCause().getMessage()
                .contains("cannot specify both READ ONLY and READ WRITE"));
        assertThrows(JSQLParserException.class,
                () -> parse(sql.replace(", ", ", WITH CONSISTENT SNAPSHOT, "), Dialect.MYSQL));
        StartTransaction postgres = assertInstanceOf(StartTransaction.class,
                roundTrip(sql, Dialect.POSTGRESQL));
        List<TransactionMode> expected = sql.endsWith("READ WRITE")
                ? List.of(TransactionMode.READ_ONLY, TransactionMode.READ_WRITE)
                : List.of(TransactionMode.READ_WRITE, TransactionMode.READ_ONLY);
        assertEquals(expected, postgres.getModes());
        assertEquals(sql, postgres.toString());
        StartTransaction permissive = assertInstanceOf(StartTransaction.class,
                CCJSqlParserUtil.parse(sql));
        assertEquals(expected, permissive.getModes());
        assertEquals(sql, permissive.toString());
    }

    @ParameterizedTest
    @ValueSource(strings = {"START TRANSACTION READ ONLY, READ ONLY",
            "START TRANSACTION READ WRITE, READ WRITE"})
    void mysqlRepeatedAccessModesArePreserved(String sql) throws Exception {
        StartTransaction statement = assertInstanceOf(StartTransaction.class,
                roundTrip(sql, Dialect.MYSQL));
        TransactionMode mode = sql.endsWith("READ ONLY")
                ? TransactionMode.READ_ONLY
                : TransactionMode.READ_WRITE;
        assertEquals(List.of(mode, mode), statement.getModes());
        assertEquals(sql, statement.toString());
    }

    @ParameterizedTest
    @ValueSource(strings = {"COMMIT", "COMMIT WORK", "COMMIT TRANSACTION", "COMMIT AND CHAIN",
            "COMMIT WORK AND NO CHAIN", "COMMIT TRANSACTION AND CHAIN", "ROLLBACK",
            "ROLLBACK WORK", "ROLLBACK TRANSACTION", "ROLLBACK AND CHAIN",
            "ROLLBACK WORK AND NO CHAIN", "ROLLBACK TRANSACTION AND CHAIN"})
    void postgresCompletionRetainsKeywordAndChain(String sql) throws Exception {
        Statement statement = roundTrip(sql, Dialect.POSTGRESQL);
        TransactionChain chain = sql.endsWith("NO CHAIN") ? TransactionChain.NO_CHAIN
                : sql.endsWith("CHAIN") ? TransactionChain.CHAIN : null;
        TransactionKeyword keyword = sql.contains("WORK") ? TransactionKeyword.WORK
                : sql.contains("TRANSACTION") ? TransactionKeyword.TRANSACTION : null;
        if (statement instanceof Commit) {
            assertEquals(chain, ((Commit) statement).getChain());
            assertEquals(keyword, ((Commit) statement).getKeyword());
        } else {
            RollbackStatement rollback = assertInstanceOf(RollbackStatement.class, statement);
            assertEquals(chain, rollback.getChain());
            assertEquals(keyword, rollback.getKeyword());
            assertEquals(keyword == TransactionKeyword.WORK, rollback.isUsingWorkKeyword());
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"COMMIT WORK AND CHAIN", "COMMIT AND NO CHAIN",
            "ROLLBACK WORK AND NO CHAIN", "ROLLBACK AND CHAIN"})
    void mysqlCompletionPreservesChain(String sql) throws Exception {
        roundTrip(sql, Dialect.MYSQL);
    }

    @ParameterizedTest
    @ValueSource(strings = {"SAVEPOINT \"a b\"", "ROLLBACK TO \"a b\"",
            "ROLLBACK TRANSACTION TO SAVEPOINT \"a b\"", "RELEASE SAVEPOINT \"a b\"",
            "RELEASE \"a b\"", "RELEASE sp"})
    void postgresSavepointNamesAndOptionalKeyword(String sql) throws Exception {
        roundTrip(sql, Dialect.POSTGRESQL);
    }

    @Test
    void mysqlSavepointNamesAreQuotedAndMutable() throws Exception {
        roundTrip("SAVEPOINT `a b`", Dialect.MYSQL);
        roundTrip("ROLLBACK TO SAVEPOINT `a b`", Dialect.MYSQL);
        ReleaseSavepointStatement statement = assertInstanceOf(ReleaseSavepointStatement.class,
                roundTrip("RELEASE SAVEPOINT `a b`", Dialect.MYSQL));
        assertEquals("`a b`", statement.getSavepointName());
        assertTrue(statement.isUsingSavepointKeyword());
        statement.withSavepointName("changed").withUsingSavepointKeyword(false);
        assertEquals("RELEASE changed", statement.toString());
        assertEquals("RELEASE changed", statement.appendTo(new StringBuilder()).toString());
    }

    @Test
    void typedModesCanBeChangedWithoutKeepingAnOpaqueSource() throws Exception {
        StartTransaction statement = assertInstanceOf(StartTransaction.class, parse(
                "BEGIN TRANSACTION ISOLATION LEVEL REPEATABLE READ READ ONLY NOT DEFERRABLE",
                Dialect.POSTGRESQL));
        assertEquals(List.of(TransactionMode.ISOLATION_LEVEL_REPEATABLE_READ,
                TransactionMode.READ_ONLY, TransactionMode.NOT_DEFERRABLE),
                statement.getModes());
        statement.withCommand(StartTransaction.Command.START_TRANSACTION).withKeyword(null);
        statement.getModes().set(1, TransactionMode.READ_WRITE);
        statement.addMode(TransactionMode.DEFERRABLE);
        String sql =
                "START TRANSACTION ISOLATION LEVEL REPEATABLE READ, READ WRITE, NOT DEFERRABLE, DEFERRABLE";
        assertEquals(sql, statement.toString());
        assertEquals(sql, roundTrip(sql, Dialect.POSTGRESQL).toString());
        StringBuilder output = new StringBuilder();
        statement.accept(new StatementDeParser(output), null);
        assertEquals(sql, output.toString());
    }

    @Test
    void legacyRollbackMutatorsStillControlWorkAndSavepoint() throws Exception {
        RollbackStatement rollback = new RollbackStatement().withUsingWorkKeyword(true)
                .withUsingSavepointKeyword(true).withSavepointName("sp");
        assertEquals("ROLLBACK WORK TO SAVEPOINT sp", rollback.toString());
        rollback.setUsingWorkKeyword(false);
        assertNull(rollback.getKeyword());
        rollback.setKeyword(TransactionKeyword.TRANSACTION);
        assertFalse(rollback.isUsingWorkKeyword());
        rollback.setUsingWorkKeyword(true);
        assertEquals(TransactionKeyword.WORK, rollback.getKeyword());
        roundTrip("ROLLBACK WORK FORCE '25.32.87'", Dialect.ORACLE);
        rollback.withSavepointName(null).withChain(TransactionChain.NO_CHAIN);
        assertEquals("ROLLBACK WORK AND NO CHAIN", rollback.toString());
        Commit commit = new Commit().withKeyword(TransactionKeyword.WORK)
                .withChain(TransactionChain.CHAIN);
        assertEquals("COMMIT WORK AND CHAIN", commit.toString());
    }

    @Test
    void statementBoundariesAndProceduralBlocksRemainSeparate() throws Exception {
        for (Dialect dialect : List.of(Dialect.POSTGRESQL, Dialect.MYSQL)) {
            Statements script = CCJSqlParserUtil.parseStatements(
                    "BEGIN; SELECT 1; SAVEPOINT sp; ROLLBACK TO SAVEPOINT sp; RELEASE SAVEPOINT sp; COMMIT AND NO CHAIN; SELECT 2",
                    p -> p.withDialect(dialect));
            assertEquals(7, script.size());
            assertInstanceOf(StartTransaction.class, script.get(0));
            assertInstanceOf(ReleaseSavepointStatement.class, script.get(4));
            assertInstanceOf(Commit.class, script.get(5));
            assertEquals("SELECT 2", script.get(6).toString());
            assertEquals(7, CCJSqlParserUtil.parseStatements(script.toString(),
                    p -> p.withDialect(dialect)).size());
            assertInstanceOf(Block.class, parse("BEGIN SELECT 1; END", dialect));
        }
        assertInstanceOf(Block.class, CCJSqlParserUtil.parse("BEGIN SELECT 1; END"));
        assertInstanceOf(OracleBlock.class, parse("BEGIN NULL; END;", Dialect.ORACLE));
        assertInstanceOf(StartTransaction.class, CCJSqlParserUtil.parse("START TRANSACTION"));
        assertInstanceOf(ReleaseSavepointStatement.class, CCJSqlParserUtil.parse("RELEASE sp"));
    }

    @Test
    void directAndReaderEntryPointsUseTheSameDialectDisambiguation() throws Exception {
        assertInstanceOf(StartTransaction.class, new CCJSqlParser(new StringProvider("BEGIN"))
                .withDialect(Dialect.POSTGRESQL).Statement());
        assertInstanceOf(StartTransaction.class,
                new CCJSqlParser(new StreamProvider(new StringReader("BEGIN")))
                        .withDialect(Dialect.MYSQL).Statement());
        assertInstanceOf(StartTransaction.class, CCJSqlParserUtil.newParser("BEGIN")
                .withDialect(Dialect.POSTGRESQL).Statement());
    }

    @Test
    void releaseRemainsAvailableAsAnIdentifier() throws Exception {
        for (String sql : List.of("SELECT release FROM release", "SELECT release(1) AS release",
                "CREATE TABLE release (release INT)", "SAVEPOINT release")) {
            assertEquals(sql, CCJSqlParserUtil.parse(sql).toString());
        }
    }

    @Test
    void visitorDispatchPassesContextAndResult() throws Exception {
        StatementVisitorAdapter<String> visitor = new StatementVisitorAdapter<>() {
            @Override
            public <S> String visit(StartTransaction statement, S context) {
                return context + ":" + statement.getCommand();
            }

            @Override
            public <S> String visit(ReleaseSavepointStatement statement, S context) {
                return context + ":" + statement.getSavepointName();
            }
        };
        assertEquals("ctx:BEGIN", parse("BEGIN", Dialect.POSTGRESQL).accept(visitor, "ctx"));
        assertEquals("ctx:sp", parse("RELEASE sp", Dialect.POSTGRESQL).accept(visitor, "ctx"));
        assertNull(
                parse("BEGIN", Dialect.POSTGRESQL).accept(new StatementVisitorAdapter<>(), null));
    }

    @ParameterizedTest
    @ValueSource(strings = {"START", "START TRANSACTION READ",
            "START TRANSACTION ISOLATION LEVEL BAD",
            "BEGIN ISOLATION READ COMMITTED", "BEGIN READ ONLY,", "BEGIN , READ ONLY",
            "COMMIT AND", "COMMIT AND NO", "COMMIT AND CHAIN AND CHAIN",
            "ROLLBACK AND", "ROLLBACK TO SAVEPOINT sp AND CHAIN", "ROLLBACK AND CHAIN TO sp",
            "RELEASE", "RELEASE SAVEPOINT", "RELEASE SAVEPOINT sp garbage",
            "COMMIT PREPARED 'id'", "ROLLBACK PREPARED 'id'", "XA START 'id'",
            "START TRANSACTION WITH CONSISTENT SNAPSHOT"})
    void malformedAndDeferredPostgresSyntaxIsRejected(String sql) {
        assertThrows(JSQLParserException.class, () -> parse(sql, Dialect.POSTGRESQL));
    }

    @ParameterizedTest
    @ValueSource(strings = {"BEGIN TRANSACTION", "BEGIN READ ONLY", "START TRANSACTION DEFERRABLE",
            "START TRANSACTION ISOLATION LEVEL READ COMMITTED", "RELEASE sp",
            "START TRANSACTION READ ONLY WITH CONSISTENT SNAPSHOT", "COMMIT TRANSACTION",
            "ROLLBACK TRANSACTION"})
    void explicitMysqlDialectRejectsPostgresOnlySyntax(String sql) {
        assertThrows(JSQLParserException.class, () -> parse(sql, Dialect.MYSQL));
    }

    @Test
    void validatesNewCapabilitiesAndPostgresChainVersion() {
        FeatureConfiguration pg = new FeatureConfiguration().setValue(Feature.dialect,
                Dialect.POSTGRESQL.name());
        assertTrue(new Validation(pg, List.of(PostgresqlVersion.V10),
                "BEGIN ISOLATION LEVEL SERIALIZABLE READ ONLY DEFERRABLE; RELEASE sp")
                .validate().isEmpty());
        assertFalse(new Validation(pg, List.of(PostgresqlVersion.V11), "COMMIT AND CHAIN")
                .validate().isEmpty());
        assertTrue(new Validation(pg, List.of(PostgresqlVersion.V12),
                "COMMIT AND CHAIN; ROLLBACK AND NO CHAIN").validate().isEmpty());
        assertFalse(new Validation(pg, List.of(new FeaturesAllowed(Feature.commit)),
                "COMMIT AND CHAIN").validate().isEmpty());
        assertFalse(new Validation(pg, List.of(new FeaturesAllowed(Feature.select)),
                "BEGIN; RELEASE sp").validate().isEmpty());
        FeatureConfiguration mysql = new FeatureConfiguration().setValue(Feature.dialect,
                Dialect.MYSQL.name());
        assertTrue(new Validation(mysql, List.of(MySqlVersion.V8_0),
                "START TRANSACTION WITH CONSISTENT SNAPSHOT, READ ONLY; RELEASE SAVEPOINT sp; COMMIT AND CHAIN")
                .validate().isEmpty());
        assertFalse(new Validation(pg, List.of(MySqlVersion.V8_0),
                "START TRANSACTION ISOLATION LEVEL SERIALIZABLE DEFERRABLE")
                .validate().isEmpty());
    }

    static Stream<Arguments> mysqlReleaseForms() {
        return Stream.of(
                Arguments.of("COMMIT AND NO CHAIN NO RELEASE", false,
                        null, TransactionChain.NO_CHAIN, TransactionRelease.NO_RELEASE),
                Arguments.of("COMMIT RELEASE", false, null, null, TransactionRelease.RELEASE),
                Arguments.of("COMMIT WORK NO RELEASE", false, TransactionKeyword.WORK, null,
                        TransactionRelease.NO_RELEASE),
                Arguments.of("COMMIT AND NO CHAIN RELEASE", false, null,
                        TransactionChain.NO_CHAIN, TransactionRelease.RELEASE),
                Arguments.of("ROLLBACK WORK AND CHAIN NO RELEASE", true, TransactionKeyword.WORK,
                        TransactionChain.CHAIN, TransactionRelease.NO_RELEASE),
                Arguments.of("ROLLBACK RELEASE", true, null, null, TransactionRelease.RELEASE),
                Arguments.of("ROLLBACK AND NO CHAIN RELEASE", true, null,
                        TransactionChain.NO_CHAIN, TransactionRelease.RELEASE));
    }

    @ParameterizedTest
    @MethodSource("mysqlReleaseForms")
    void mysqlCompletionRetainsRelease(String sql, boolean rollback, TransactionKeyword keyword,
            TransactionChain chain, TransactionRelease release) throws Exception {
        for (Dialect dialect : new Dialect[] {Dialect.MYSQL, Dialect.MARIADB}) {
            Class<? extends TransactionCompletionStatement> type =
                    rollback ? RollbackStatement.class : Commit.class;
            TransactionCompletionStatement statement =
                    assertInstanceOf(type, roundTrip(sql, dialect));
            assertEquals(sql, statement.toString());
            assertEquals(keyword, statement.getKeyword());
            assertEquals(chain, statement.getChain());
            assertEquals(release, statement.getRelease());
            assertFalse(statement.isPostgresSpelling());
        }
        assertEquals(sql, CCJSqlParserUtil.parse(sql).toString());
    }

    @ParameterizedTest
    @ValueSource(strings = {"COMMIT AND CHAIN RELEASE", "ROLLBACK WORK AND CHAIN RELEASE",
            "COMMIT RELEASE RELEASE", "COMMIT NO", "ROLLBACK TO SAVEPOINT sp RELEASE",
            "ROLLBACK RELEASE AND CHAIN"})
    void contradictoryOrMisplacedReleaseIsRejected(String sql) {
        for (Dialect dialect : new Dialect[] {Dialect.MYSQL, null}) {
            assertThrows(JSQLParserException.class, () -> parse(sql, dialect), sql);
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"COMMIT RELEASE", "COMMIT AND NO CHAIN NO RELEASE",
            "ROLLBACK NO RELEASE", "END RELEASE", "ABORT NO RELEASE"})
    void postgresRejectsRelease(String sql) {
        assertThrows(JSQLParserException.class, () -> parse(sql, Dialect.POSTGRESQL));
    }

    @ParameterizedTest
    @ValueSource(strings = {"END", "END WORK", "END TRANSACTION", "END AND CHAIN",
            "END WORK AND NO CHAIN", "ABORT", "ABORT WORK", "ABORT TRANSACTION AND CHAIN",
            "ABORT AND NO CHAIN"})
    void postgresEndAndAbortAreCommitAndRollback(String sql) throws Exception {
        TransactionCompletionStatement statement =
                assertInstanceOf(TransactionCompletionStatement.class,
                        roundTrip(sql, Dialect.POSTGRESQL));
        assertTrue(statement.isPostgresSpelling());
        if (sql.startsWith("END")) {
            assertEquals(Commit.Command.END,
                    assertInstanceOf(Commit.class, statement).getCommand());
        } else {
            RollbackStatement rollback = assertInstanceOf(RollbackStatement.class, statement);
            assertEquals(RollbackStatement.Command.ABORT, rollback.getCommand());
            assertNull(rollback.getSavepointName());
        }
        assertEquals(sql.contains("WORK") ? TransactionKeyword.WORK
                : sql.contains("TRANSACTION") ? TransactionKeyword.TRANSACTION : null,
                statement.getKeyword());
        assertEquals(sql.endsWith("NO CHAIN") ? TransactionChain.NO_CHAIN
                : sql.endsWith("CHAIN") ? TransactionChain.CHAIN : null, statement.getChain());
    }

    @Test
    void abortIsUnambiguousWithoutADialectButEndIsNot() throws Exception {
        assertInstanceOf(RollbackStatement.class, CCJSqlParserUtil.parse("ABORT WORK"));
        assertThrows(JSQLParserException.class, () -> CCJSqlParserUtil.parse("END"));
        for (Dialect dialect : new Dialect[] {Dialect.MYSQL, Dialect.MARIADB, Dialect.ORACLE,
                Dialect.SQLSERVER}) {
            assertThrows(JSQLParserException.class, () -> parse("ABORT", dialect));
            assertThrows(JSQLParserException.class, () -> parse("END", dialect));
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"ABORT TO SAVEPOINT sp", "ABORT TO sp", "ABORT FORCE '1.2.3'",
            "END TO SAVEPOINT sp", "END PREPARED 'x'", "END AND", "ABORT AND",
            "END garbage", "ABORT garbage"})
    void postgresSpellingsTakeOnlyTheCompletionClauses(String sql) {
        assertThrows(JSQLParserException.class, () -> parse(sql, Dialect.POSTGRESQL));
    }

    @Test
    void endStillClosesBlocksAndCaseExpressions() throws Exception {
        for (Dialect dialect : new Dialect[] {Dialect.POSTGRESQL, Dialect.MYSQL, null}) {
            assertInstanceOf(Block.class,
                    dialect == null ? CCJSqlParserUtil.parse("BEGIN SELECT 1; SELECT 2; END")
                            : parse("BEGIN SELECT 1; SELECT 2; END", dialect));
        }
        Statements script = CCJSqlParserUtil.parseStatements(
                "BEGIN; SELECT CASE WHEN a THEN 1 END AS \"end\" FROM t; END; ABORT",
                p -> p.withDialect(Dialect.POSTGRESQL));
        assertEquals(4, script.size());
        assertInstanceOf(StartTransaction.class, script.get(0));
        assertEquals(Commit.Command.END, ((Commit) script.get(2)).getCommand());
        assertEquals(RollbackStatement.Command.ABORT,
                ((RollbackStatement) script.get(3)).getCommand());
        assertInstanceOf(IfElseStatement.class,
                CCJSqlParserUtil.parse("IF 1 = 1 BEGIN SELECT 1; END ELSE SELECT 2"));
        assertInstanceOf(CreateFunction.class,
                parse("CREATE FUNCTION f() RETURNS int LANGUAGE sql BEGIN ATOMIC SELECT 1; END",
                        Dialect.POSTGRESQL));
        for (String sql : List.of("SELECT abort FROM abort", "CREATE TABLE abort (abort INT)",
                "SELECT 1 AS \"end\"")) {
            assertEquals(sql, parse(sql, Dialect.POSTGRESQL).toString());
            assertEquals(sql, CCJSqlParserUtil.parse(sql).toString());
        }
    }

    static Stream<Arguments> newStatementStarts() {
        return Stream.of(
                Arguments.of(Dialect.POSTGRESQL, "END", Commit.class),
                Arguments.of(Dialect.POSTGRESQL, "END WORK AND CHAIN", Commit.class),
                Arguments.of(Dialect.POSTGRESQL, "ABORT", RollbackStatement.class),
                Arguments.of(null, "ABORT TRANSACTION", RollbackStatement.class),
                Arguments.of(Dialect.MYSQL, "COMMIT AND NO CHAIN NO RELEASE", Commit.class),
                Arguments.of(Dialect.MYSQL, "ROLLBACK RELEASE", RollbackStatement.class),
                Arguments.of(Dialect.MYSQL, "SET GLOBAL TRANSACTION READ WRITE",
                        SetTransactionStatement.class),
                Arguments.of(Dialect.POSTGRESQL, "SET TRANSACTION SNAPSHOT '1'",
                        SetTransactionStatement.class),
                Arguments.of(Dialect.POSTGRESQL,
                        "SET SESSION CHARACTERISTICS AS TRANSACTION READ ONLY",
                        SetTransactionStatement.class),
                Arguments.of(Dialect.MYSQL,
                        "SET PERSIST_ONLY TRANSACTION ISOLATION LEVEL READ COMMITTED",
                        SetTransactionStatement.class),
                Arguments.of(Dialect.POSTGRESQL, "SET LOCAL TRANSACTION SNAPSHOT '1'",
                        SetTransactionStatement.class),
                Arguments.of(Dialect.POSTGRESQL,
                        "SET LOCAL SESSION CHARACTERISTICS AS TRANSACTION READ ONLY",
                        SetTransactionStatement.class),
                Arguments.of(Dialect.POSTGRESQL, "SET CONSTRAINTS ALL DEFERRED",
                        SetConstraintsStatement.class));
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
            assertSpans("COMMIT", script, (ASTNodeAccess) statements.get(1));
            assertSpans(sql, script, (ASTNodeAccess) statements.get(2));
            assertInstanceOf(SavepointStatement.class, statements.get(3));
        }
    }

    @Test
    void completionModelIsSharedAndMutable() {
        Commit commit = new Commit().withCommand(Commit.Command.END)
                .withKeyword(TransactionKeyword.TRANSACTION).withChain(TransactionChain.CHAIN);
        assertEquals("END TRANSACTION AND CHAIN", commit.toString());
        commit.withCommand(Commit.Command.COMMIT).withKeyword(null).withChain(null)
                .withRelease(TransactionRelease.RELEASE);
        assertEquals("COMMIT RELEASE", commit.toString());
        RollbackStatement rollback = new RollbackStatement()
                .withCommand(RollbackStatement.Command.ABORT).withUsingWorkKeyword(true);
        assertEquals("ABORT WORK", rollback.toString());
        rollback.withCommand(RollbackStatement.Command.ROLLBACK)
                .withChain(TransactionChain.NO_CHAIN).withRelease(TransactionRelease.NO_RELEASE);
        assertEquals("ROLLBACK WORK AND NO CHAIN NO RELEASE", rollback.toString());
        StringBuilder output = new StringBuilder("x: ");
        rollback.accept(new StatementDeParser(output), null);
        assertEquals("x: ROLLBACK WORK AND NO CHAIN NO RELEASE", output.toString());
        assertThrows(NullPointerException.class, () -> commit.setCommand(null));
        assertThrows(NullPointerException.class, () -> rollback.setCommand(null));
    }

    @Test
    void validatesRollbackSavepointReleaseAndPostgresSpellings() {
        FeatureConfiguration pg = new FeatureConfiguration().setValue(Feature.dialect,
                Dialect.POSTGRESQL.name());
        FeatureConfiguration mysql = new FeatureConfiguration().setValue(Feature.dialect,
                Dialect.MYSQL.name());
        assertTrue(new Validation(pg, List.of(PostgresqlVersion.V12),
                "SAVEPOINT sp; ROLLBACK TO SAVEPOINT sp; ROLLBACK; END AND CHAIN; ABORT")
                .validate().isEmpty());
        assertTrue(new Validation(mysql, List.of(MySqlVersion.V8_0),
                "SAVEPOINT sp; ROLLBACK TO sp; COMMIT AND NO CHAIN NO RELEASE; ROLLBACK RELEASE")
                .validate().isEmpty());
        assertFalse(new Validation(new FeatureConfiguration(), List.of(MySqlVersion.V8_0),
                "ABORT").validate().isEmpty());
        assertFalse(new Validation(new FeatureConfiguration(), List.of(PostgresqlVersion.V12),
                "COMMIT RELEASE").validate().isEmpty());
        assertFalse(new Validation(pg, List.of(new FeaturesAllowed(Feature.rollback)),
                "ROLLBACK TO sp").validate().isEmpty());
        assertTrue(new Validation(pg,
                List.of(new FeaturesAllowed(Feature.rollback, Feature.savepoint)),
                "SAVEPOINT sp; ROLLBACK TO sp").validate().isEmpty());
        assertFalse(new Validation(pg, List.of(new FeaturesAllowed(Feature.commit)),
                "ROLLBACK").validate().isEmpty());
        assertTrue(new Validation(new FeatureConfiguration(),
                List.of(net.sf.jsqlparser.util.validation.feature.SqlServerVersion.V2019),
                "COMMIT; ROLLBACK").validate().isEmpty());
    }

    @Test
    void visitorDispatchCoversCompletionSpellings() throws Exception {
        StatementVisitorAdapter<String> visitor = new StatementVisitorAdapter<>() {
            @Override
            public <S> String visit(Commit commit, S context) {
                return context + ":" + commit.getCommand();
            }

            @Override
            public <S> String visit(RollbackStatement rollback, S context) {
                return context + ":" + rollback.getCommand();
            }
        };
        assertEquals("ctx:END", parse("END", Dialect.POSTGRESQL).accept(visitor, "ctx"));
        assertEquals("ctx:ABORT", parse("ABORT", Dialect.POSTGRESQL).accept(visitor, "ctx"));
    }
}
