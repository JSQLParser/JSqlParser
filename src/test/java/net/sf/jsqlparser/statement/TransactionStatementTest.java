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
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.AbstractJSqlParser.Dialect;
import net.sf.jsqlparser.parser.CCJSqlParser;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.parser.StringProvider;
import net.sf.jsqlparser.parser.StreamProvider;
import net.sf.jsqlparser.parser.feature.Feature;
import net.sf.jsqlparser.parser.feature.FeatureConfiguration;
import net.sf.jsqlparser.statement.oracle.OracleBlock;
import net.sf.jsqlparser.util.TablesNamesFinder;
import net.sf.jsqlparser.util.deparser.StatementDeParser;
import net.sf.jsqlparser.util.validation.Validation;
import net.sf.jsqlparser.util.validation.feature.FeaturesAllowed;
import net.sf.jsqlparser.util.validation.feature.MySqlVersion;
import net.sf.jsqlparser.util.validation.feature.PostgresqlVersion;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class TransactionStatementTest {
    private Statement parse(String sql, Dialect dialect) throws JSQLParserException {
        return CCJSqlParserUtil.parse(sql, p -> p.withDialect(dialect));
    }

    private Statement roundTrip(String sql, Dialect dialect) throws Exception {
        Statement statement = parse(sql, dialect);
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
        assertEquals(List.of(StartTransaction.Mode.ISOLATION_LEVEL_REPEATABLE_READ,
                StartTransaction.Mode.READ_ONLY, StartTransaction.Mode.NOT_DEFERRABLE),
                statement.getModes());
        statement.withCommand(StartTransaction.Command.START_TRANSACTION).withKeyword(null);
        statement.getModes().set(1, StartTransaction.Mode.READ_WRITE);
        statement.addMode(StartTransaction.Mode.DEFERRABLE);
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
}
