/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.notify;

import static net.sf.jsqlparser.test.TestUtils.assertDeparse;
import static net.sf.jsqlparser.test.TestUtils.assertSqlCanBeParsedAndDeparsed;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.function.Consumer;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.AbstractJSqlParser.Dialect;
import net.sf.jsqlparser.parser.CCJSqlParser;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitorAdapter;
import net.sf.jsqlparser.statement.Statements;
import net.sf.jsqlparser.statement.StmtFeature;
import net.sf.jsqlparser.statement.UnsupportedStatement;
import net.sf.jsqlparser.statement.notify.ListenStatement.Action;
import net.sf.jsqlparser.util.TablesNamesFinder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class ListenStatementTest {

    private static final Consumer<CCJSqlParser> POSTGRESQL =
            parser -> parser.withDialect(Dialect.POSTGRESQL);

    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {
            "LISTEN ch | LISTEN | ch",
            "LISTEN \"Ch 1\" | LISTEN | \"Ch 1\"",
            "UNLISTEN ch | UNLISTEN | ch",
            "UNLISTEN \"Ch 1\" | UNLISTEN | \"Ch 1\"",
            "UNLISTEN * | UNLISTEN | ",
            "LISTEN listen | LISTEN | listen",
            "LISTEN \"all\" | LISTEN | \"all\"",
            "LISTEN transaction | LISTEN | transaction",
            "UNLISTEN unlisten | UNLISTEN | unlisten"})
    void parsesWithPostgreSqlAndWithoutDialect(String sql, Action action, String channel)
            throws JSQLParserException {
        for (ListenStatement listen : new ListenStatement[] {
                (ListenStatement) assertSqlCanBeParsedAndDeparsed(sql, false, POSTGRESQL),
                (ListenStatement) assertSqlCanBeParsedAndDeparsed(sql, false)}) {
            assertEquals(action, listen.getAction());
            assertEquals(channel, listen.getChannel());
            assertEquals(channel == null, listen.isAllChannels());
        }
    }

    @Test
    void writesTheKeywordsInUpperCase() throws JSQLParserException {
        assertEquals("LISTEN ch", CCJSqlParserUtil.parse("listen ch", POSTGRESQL).toString());
        assertEquals("UNLISTEN *", CCJSqlParserUtil.parse("unlisten *", POSTGRESQL).toString());
    }

    @Test
    void buildsAndDeparses() {
        ListenStatement listen = new ListenStatement();
        assertEquals(Action.LISTEN, listen.getAction());
        assertDeparse(listen.withChannel("ch"), "LISTEN ch");
        assertDeparse(listen.withAction(Action.UNLISTEN), "UNLISTEN ch");
        ListenStatement all = new ListenStatement(Action.UNLISTEN, null);
        assertTrue(all.isAllChannels());
        assertDeparse(all, "UNLISTEN *");
        assertFalse(new ListenStatement(Action.LISTEN, "\"Ch\"").isAllChannels());
        assertThrows(NullPointerException.class, () -> listen.setAction(null));
    }

    @ParameterizedTest
    @ValueSource(strings = {"LISTEN", "LISTEN *", "LISTEN a.b", "LISTEN 'ch'", "LISTEN a b",
            "LISTEN a, b", "UNLISTEN", "UNLISTEN a, b", "UNLISTEN a.b", "UNLISTEN 'ch'",
            "UNLISTEN * *", "UNLISTEN ALL", "LISTEN all", "LISTEN freeze", "UNLISTEN binary",
            "LISTEN select"})
    void rejectsWhatPostgreSqlRejects(String sql) {
        assertThrows(JSQLParserException.class, () -> CCJSqlParserUtil.parse(sql, POSTGRESQL));
    }

    @Test
    void ruleActionsStayLimitedToNotify() {
        // PostgreSQL's RuleActionStmt takes NOTIFY but neither LISTEN nor UNLISTEN
        assertThrows(JSQLParserException.class, () -> CCJSqlParserUtil.parse(
                "CREATE RULE r AS ON INSERT TO t DO ALSO LISTEN ch", POSTGRESQL));
    }

    @Test
    void otherDialectsKeepTheirReading() throws JSQLParserException {
        for (Dialect dialect : new Dialect[] {Dialect.MYSQL, Dialect.ORACLE, Dialect.SQLSERVER}) {
            for (String sql : new String[] {"LISTEN ch", "UNLISTEN *"}) {
                assertThrows(JSQLParserException.class,
                        () -> CCJSqlParserUtil.parse(sql, parser -> parser.withDialect(dialect)),
                        dialect + " " + sql);
                assertInstanceOf(UnsupportedStatement.class, CCJSqlParserUtil.parse(sql,
                        parser -> parser.withDialect(dialect).withUnsupportedStatements(true)));
            }
        }
        assertInstanceOf(UnsupportedStatement.class, CCJSqlParserUtil.parse("LISTEN 'ch'",
                parser -> parser.withUnsupportedStatements(true)));
    }

    @Test
    void wordsRemainIdentifiers() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("SELECT listen, unlisten FROM listen AS unlisten", true,
                POSTGRESQL);
        assertSqlCanBeParsedAndDeparsed("SELECT listen.unlisten FROM unlisten.listen", true,
                parser -> parser.withDialect(Dialect.MYSQL));
        assertSqlCanBeParsedAndDeparsed("CREATE TABLE listen (unlisten INT)", true, POSTGRESQL);
        assertSqlCanBeParsedAndDeparsed("NOTIFY listen, 'x'", true, POSTGRESQL);
    }

    @Test
    void endsAtStatementBoundaries() throws JSQLParserException {
        Statements statements = CCJSqlParserUtil.parseStatements(
                "LISTEN a; UNLISTEN *\nNOTIFY a, 'x'\nUNLISTEN a;SELECT 1;\nLISTEN b", POSTGRESQL);
        assertEquals(6, statements.size());
        assertEquals("UNLISTEN *", statements.get(1).toString());
        assertEquals("NOTIFY a, 'x'", statements.get(2).toString());
        assertEquals("UNLISTEN a", statements.get(3).toString());
        assertEquals("LISTEN b", statements.get(5).toString());
    }

    @Test
    void reachesVisitorsAndModifiesTheSession() throws JSQLParserException {
        Statement statement = CCJSqlParserUtil.parse("UNLISTEN ch", POSTGRESQL);
        Action visited = statement.accept(new StatementVisitorAdapter<Action>() {
            @Override
            public <S> Action visit(ListenStatement listen, S context) {
                return listen.getAction();
            }
        }, null);
        assertEquals(Action.UNLISTEN, visited);
        assertNull(statement.accept(new StatementVisitorAdapter<Action>(), null));

        assertThat(new TablesNamesFinder<>().getTables(statement)).isEmpty();
        assertThat(statement.getFeatures().getCertain())
                .containsExactly(StmtFeature.MODIFIES_SESSION);
        assertThat(CCJSqlParserUtil.parse("LISTEN ch").getFeatures().getCertain())
                .containsExactly(StmtFeature.MODIFIES_SESSION);
    }
}
