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

import static net.sf.jsqlparser.statement.admin.MySqlFlushStatementTest.deparse;
import static net.sf.jsqlparser.statement.admin.MySqlFlushStatementTest.text;
import static net.sf.jsqlparser.test.TestUtils.assertSqlCanBeParsedAndDeparsed;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.UserVariable;
import net.sf.jsqlparser.expression.operators.arithmetic.Addition;
import net.sf.jsqlparser.parser.AbstractJSqlParser.Dialect;
import net.sf.jsqlparser.parser.CCJSqlParser;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.Statements;
import net.sf.jsqlparser.statement.StmtFeature;
import net.sf.jsqlparser.statement.UnsupportedStatement;
import net.sf.jsqlparser.statement.alter.AlterSystemStatement;
import net.sf.jsqlparser.util.TablesNamesFinder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class MySqlKillStatementTest {
    private static final Consumer<CCJSqlParser> MYSQL = parser -> parser.withDialect(Dialect.MYSQL);
    private static final Consumer<CCJSqlParser> NO_DIALECT = parser -> {
    };

    @ParameterizedTest
    @ValueSource(strings = {
            "KILL 123",
            "KILL QUERY 123",
            "KILL CONNECTION 123",
            "KILL CONNECTION 1 + 2",
            "KILL @x",
            "KILL QUERY @id",
            "KILL CONNECTION_ID()",
            "KILL ?",
            "KILL connection + 1"})
    void parsesMySqlFormsWithAndWithoutDialect(String sql) throws JSQLParserException {
        for (Consumer<CCJSqlParser> dialect : List.of(NO_DIALECT, MYSQL)) {
            Statement statement = assertSqlCanBeParsedAndDeparsed(sql, true, dialect);
            assertInstanceOf(KillStatement.class, statement);
            assertEquals(statement.toString(), deparse(statement));
            assertEquals(statement.toString(),
                    CCJSqlParserUtil.parse(statement.toString(), dialect).toString());
        }
    }

    @Test
    void keepsWhatItEndsAndTheIdentifierExpression() throws JSQLParserException {
        KillStatement kill = parse("KILL 123");
        assertNull(kill.getKind());
        assertEquals(123L, ((LongValue) kill.getProcesslistId()).getValue());

        assertEquals(KillStatement.Kind.QUERY, parse("kill query 123").getKind());
        assertEquals(KillStatement.Kind.CONNECTION, parse("KILL CONNECTION 123").getKind());
        assertInstanceOf(Addition.class, parse("KILL CONNECTION 1 + 2").getProcesslistId());
        assertInstanceOf(UserVariable.class, parse("KILL @x").getProcesslistId());

        // CONNECTION is the keyword only before the identifier, as in MySQL's grammar
        KillStatement function = parse("KILL CONNECTION_ID()");
        assertNull(function.getKind());
        assertInstanceOf(Function.class, function.getProcesslistId());
        KillStatement plus = parse("KILL connection + 1");
        assertEquals(KillStatement.Kind.CONNECTION, plus.getKind());
        assertEquals("KILL CONNECTION +1", plus.toString());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "KILL",
            "KILL QUERY",
            "KILL CONNECTION",
            "KILL QUERY CONNECTION 1",
            "KILL 1 2",
            "KILL 1, 2",
            "KILL USER u"})
    void rejectsFormsMySqlRejects(String sql) {
        for (Consumer<CCJSqlParser> dialect : List.of(NO_DIALECT, MYSQL)) {
            assertThrows(JSQLParserException.class, () -> CCJSqlParserUtil.parse(sql, dialect),
                    sql);
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"KILL 1", "KILL QUERY 1"})
    void otherDialectsKeepRejectingKill(String sql) {
        for (Dialect dialect : List.of(Dialect.POSTGRESQL, Dialect.MARIADB, Dialect.ORACLE,
                Dialect.SQLSERVER)) {
            assertThrows(JSQLParserException.class,
                    () -> CCJSqlParserUtil.parse(sql, parser -> parser.withDialect(dialect)),
                    sql + " " + dialect);
        }
    }

    @Test
    void oracleAlterSystemKillSessionIsUnchanged() throws JSQLParserException {
        assertInstanceOf(AlterSystemStatement.class, assertSqlCanBeParsedAndDeparsed(
                "ALTER SYSTEM KILL SESSION '13, 8'", true,
                parser -> parser.withDialect(Dialect.ORACLE)));
    }

    @Test
    void wordsRemainUsableAsNames() throws JSQLParserException {
        // MySQL 8.4 reserves KILL, but the parser keeps accepting it as a name, as before
        for (Consumer<CCJSqlParser> dialect : List.of(NO_DIALECT, MYSQL)) {
            assertSqlCanBeParsedAndDeparsed("SELECT kill, query, connection FROM kill", true,
                    dialect);
        }
    }

    @Test
    void endsTheStatementsAroundIt() throws JSQLParserException {
        Statements statements =
                CCJSqlParserUtil.parseStatements("SELECT 1; KILL QUERY 5; KILL 6", MYSQL);
        assertEquals(List.of("PlainSelect", "KillStatement", "KillStatement"),
                statements.stream().map(statement -> statement.getClass().getSimpleName())
                        .collect(Collectors.toList()));

        Statements unsupported = CCJSqlParserUtil.parseStatements("FOO; KILL 5",
                parser -> parser.withUnsupportedStatements(true));
        assertInstanceOf(UnsupportedStatement.class, unsupported.get(0));
        assertInstanceOf(KillStatement.class, unsupported.get(1));
    }

    @Test
    void findsTablesOnlyInASubquery() throws JSQLParserException {
        assertThat(new TablesNamesFinder<>().getTables(CCJSqlParserUtil.parse("KILL 5")))
                .isEmpty();
        assertThat(new TablesNamesFinder<>()
                .getTables(CCJSqlParserUtil.parse("KILL (SELECT MAX(id) FROM t)")))
                .containsExactly("t");
        assertThat(new TablesNamesFinder<>().getTables(new KillStatement())).isEmpty();
    }

    @Test
    void reportsSessionFeatures() throws JSQLParserException {
        assertThat(CCJSqlParserUtil.parse("KILL QUERY 5").getFeatures().getCertain())
                .containsExactly(StmtFeature.MODIFIES_SESSION);
    }

    @Test
    void nodesSpanTheStatementAndTheIdentifier() throws JSQLParserException {
        String sql = "KILL CONNECTION 1 + 2";
        KillStatement kill = parse(sql);
        assertEquals(sql, text(sql, kill));
        assertEquals("1 + 2", text(sql, kill.getProcesslistId()));
    }

    @Test
    void buildsStatementsInCode() {
        KillStatement kill = new KillStatement().withProcesslistId(new LongValue(5));
        assertEquals("KILL 5", kill.toString());
        kill.withKind(KillStatement.Kind.QUERY);
        assertEquals("KILL QUERY 5", deparse(kill));
        assertEquals("KILL CONNECTION @x", new KillStatement(KillStatement.Kind.CONNECTION,
                new UserVariable("x")).toString());
    }

    private static KillStatement parse(String sql) throws JSQLParserException {
        return (KillStatement) assertSqlCanBeParsedAndDeparsed(sql, true, MYSQL);
    }
}
