/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.cursor;

import static net.sf.jsqlparser.test.TestUtils.assertSqlCanBeParsedAndDeparsed;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.ASTNodeAccess;
import net.sf.jsqlparser.parser.AbstractJSqlParser.Dialect;
import net.sf.jsqlparser.parser.CCJSqlParser;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.parser.Node;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.DeclareStatement;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementFeatures;
import net.sf.jsqlparser.statement.Statements;
import net.sf.jsqlparser.statement.StmtFeature;
import net.sf.jsqlparser.statement.UnsupportedStatement;
import net.sf.jsqlparser.statement.alter.AlterSession;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.TableStatement;
import net.sf.jsqlparser.statement.select.Values;
import net.sf.jsqlparser.util.TablesNamesFinder;
import net.sf.jsqlparser.util.deparser.StatementDeParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class PostgreSqlCursorTest {
    private static final Consumer<CCJSqlParser> POSTGRESQL =
            parser -> parser.withDialect(Dialect.POSTGRESQL);
    private static final Consumer<CCJSqlParser> NO_DIALECT = parser -> {
    };
    private static final Consumer<CCJSqlParser> UNSUPPORTED =
            parser -> parser.withUnsupportedStatements(true);

    @ParameterizedTest
    @ValueSource(strings = {
            "DECLARE c CURSOR FOR SELECT * FROM t",
            "DECLARE c BINARY INSENSITIVE NO SCROLL CURSOR WITH HOLD FOR SELECT 1",
            "DECLARE c ASENSITIVE SCROLL CURSOR WITHOUT HOLD FOR SELECT a FROM t WHERE a > 0",
            "DECLARE c CURSOR FOR VALUES (1), (2)",
            "DECLARE c CURSOR FOR TABLE t",
            "DECLARE c SCROLL BINARY CURSOR FOR SELECT 1",
            "DECLARE c SCROLL NO SCROLL CURSOR FOR SELECT 1",
            "DECLARE c BINARY BINARY CURSOR FOR SELECT 1",
            "DECLARE c CURSOR WITH HOLD FOR WITH x AS (SELECT a FROM t) SELECT a FROM x",
            "DECLARE c CURSOR FOR SELECT a FROM t UNION SELECT a FROM u ORDER BY a",
            "DECLARE c CURSOR FOR SELECT a FROM t FOR UPDATE",
            "DECLARE c CURSOR FOR (SELECT 1)",
            "DECLARE \"Cur\" CURSOR FOR SELECT 1",
            "DECLARE scroll SCROLL CURSOR FOR SELECT 1",
            "DECLARE cursor CURSOR FOR SELECT 1",
            "FETCH c",
            "FETCH NEXT FROM c",
            "FETCH PRIOR IN c",
            "FETCH FIRST FROM c",
            "FETCH LAST c",
            "FETCH ABSOLUTE -1 FROM c",
            "FETCH RELATIVE 3 IN c",
            "FETCH RELATIVE 0 c",
            "FETCH 5 FROM c",
            "FETCH -2 c",
            "FETCH ALL c",
            "FETCH ALL IN c",
            "FETCH FORWARD c",
            "FETCH FORWARD 10 IN c",
            "FETCH FORWARD ALL FROM c",
            "FETCH BACKWARD FROM c",
            "FETCH BACKWARD 2 FROM c",
            "FETCH BACKWARD ALL c",
            "FETCH FROM c",
            "FETCH IN c",
            "FETCH ABSOLUTE 2147483647 FROM c",
            "FETCH ABSOLUTE -2147483647 FROM c",
            "FETCH next",
            "FETCH NEXT next",
            "FETCH FROM next",
            "FETCH forward",
            "FETCH ABSOLUTE 1 relative",
            "FETCH \"c\"",
            "MOVE ABSOLUTE 2 IN c",
            "MOVE c",
            "MOVE BACKWARD ALL FROM c",
            "MOVE NEXT c",
            "MOVE -1 IN c",
            "MOVE LAST FROM c",
            "MOVE absolute",
            "CLOSE c",
            "CLOSE ALL",
            "CLOSE \"all\"",
            "CLOSE next",
            "CLOSE move",
            "CLOSE cursor"})
    void parsesWithPostgreSqlAndWithoutDialect(String sql) throws JSQLParserException {
        for (Consumer<CCJSqlParser> dialect : List.of(POSTGRESQL, NO_DIALECT)) {
            Statement statement = assertSqlCanBeParsedAndDeparsed(sql, false, dialect);
            assertEquals(sql, deparse(statement));
            assertEquals(sql, CCJSqlParserUtil.parse(statement.toString(), dialect).toString());
        }
    }

    @Test
    void normalizesSignsLeadingZerosAndKeywordCase() throws JSQLParserException {
        assertNormalized("FETCH 5 c", "FETCH +5 c");
        assertNormalized("FETCH -3 c", "FETCH - 3 c");
        assertNormalized("FETCH ABSOLUTE 7 c", "FETCH ABSOLUTE 007 c");
        assertNormalized("MOVE FORWARD ALL IN c", "move forward all in c");
        assertNormalized("DECLARE c NO SCROLL CURSOR WITH HOLD FOR SELECT 1",
                "declare c no scroll cursor with hold for select 1");
        assertNormalized("CLOSE ALL", "close all");
        // PostgreSQL 16 and later take digit separators
        assertEquals("FETCH 1000 c",
                CCJSqlParserUtil.parse("FETCH 1_000 c", POSTGRESQL).toString());
    }

    @Test
    void keepsTheDeclareOptionsAndTheQuery() throws JSQLParserException {
        DeclareCursorStatement declare = (DeclareCursorStatement) parse(
                "DECLARE c BINARY INSENSITIVE NO SCROLL CURSOR WITH HOLD FOR SELECT a FROM t");
        assertEquals("c", declare.getCursorName());
        assertEquals(Arrays.asList(CursorOption.BINARY, CursorOption.INSENSITIVE,
                CursorOption.NO_SCROLL), declare.getOptions());
        assertEquals(DeclareCursorStatement.Holdability.WITH_HOLD, declare.getHoldability());
        assertInstanceOf(PlainSelect.class, declare.getQuery());
        assertEquals("SELECT a FROM t", declare.getQuery().toString());

        DeclareCursorStatement plain = (DeclareCursorStatement) parse(
                "DECLARE c SCROLL SCROLL CURSOR WITHOUT HOLD FOR VALUES (1)");
        assertEquals(Arrays.asList(CursorOption.SCROLL, CursorOption.SCROLL), plain.getOptions());
        assertEquals(DeclareCursorStatement.Holdability.WITHOUT_HOLD, plain.getHoldability());
        assertInstanceOf(Values.class, plain.getQuery());

        DeclareCursorStatement table =
                (DeclareCursorStatement) parse("DECLARE \"C\" CURSOR FOR TABLE t");
        assertEquals("\"C\"", table.getCursorName());
        assertThat(table.getOptions()).isEmpty();
        assertNull(table.getHoldability());
        assertInstanceOf(TableStatement.class, table.getQuery());
    }

    @Test
    void keepsTheFetchDirection() throws JSQLParserException {
        assertFetch("FETCH c", FetchCursorStatement.Kind.FETCH, null, null, false, null, "c");
        assertFetch("FETCH NEXT FROM c", FetchCursorStatement.Kind.FETCH, FetchDirection.NEXT,
                null, false, FetchCursorStatement.FromKeyword.FROM, "c");
        assertFetch("FETCH ABSOLUTE -1 IN c", FetchCursorStatement.Kind.FETCH,
                FetchDirection.ABSOLUTE, -1L, false, FetchCursorStatement.FromKeyword.IN, "c");
        assertFetch("FETCH RELATIVE 3 c", FetchCursorStatement.Kind.FETCH,
                FetchDirection.RELATIVE, 3L, false, null, "c");
        assertFetch("FETCH 5 FROM c", FetchCursorStatement.Kind.FETCH, null, 5L, false,
                FetchCursorStatement.FromKeyword.FROM, "c");
        assertFetch("FETCH ALL c", FetchCursorStatement.Kind.FETCH, null, null, true, null, "c");
        assertFetch("MOVE FORWARD ALL FROM c", FetchCursorStatement.Kind.MOVE,
                FetchDirection.FORWARD, null, true, FetchCursorStatement.FromKeyword.FROM, "c");
        assertFetch("MOVE BACKWARD 2 c", FetchCursorStatement.Kind.MOVE, FetchDirection.BACKWARD,
                2L, false, null, "c");
        assertFetch("MOVE BACKWARD c", FetchCursorStatement.Kind.MOVE, FetchDirection.BACKWARD,
                null, false, null, "c");
        assertFetch("FETCH PRIOR prior", FetchCursorStatement.Kind.FETCH, FetchDirection.PRIOR,
                null, false, null, "prior");
        // a direction word that ends the statement names the cursor, as in PostgreSQL
        assertFetch("FETCH last", FetchCursorStatement.Kind.FETCH, null, null, false, null,
                "last");
        assertFetch("MOVE backward", FetchCursorStatement.Kind.MOVE, null, null, false, null,
                "backward");

        assertTrue(FetchDirection.ABSOLUTE.isCountRequired());
        assertFalse(FetchDirection.FORWARD.isCountRequired());
        assertTrue(FetchDirection.BACKWARD.isCountOptional());
        assertFalse(FetchDirection.NEXT.isCountOptional());
    }

    @Test
    void keepsTheClosedCursor() throws JSQLParserException {
        CloseCursorStatement close = (CloseCursorStatement) parse("CLOSE c");
        assertEquals("c", close.getCursorName());
        assertFalse(close.isAll());

        CloseCursorStatement all = (CloseCursorStatement) parse("CLOSE ALL");
        assertNull(all.getCursorName());
        assertTrue(all.isAll());

        CloseCursorStatement quoted = (CloseCursorStatement) parse("CLOSE \"all\"");
        assertEquals("\"all\"", quoted.getCursorName());
        assertFalse(quoted.isAll());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "DECLARE c",
            "DECLARE c CURSOR",
            "DECLARE c CURSOR FOR",
            "DECLARE c FOR SELECT 1",
            "DECLARE c NO CURSOR FOR SELECT 1",
            "DECLARE c SENSITIVE CURSOR FOR SELECT 1",
            "DECLARE c HOLD CURSOR FOR SELECT 1",
            "DECLARE c CURSOR WITH FOR SELECT 1",
            "DECLARE c CURSOR HOLD FOR SELECT 1",
            "DECLARE c CURSOR WITHOUT HOLD WITH HOLD FOR SELECT 1",
            "DECLARE c CURSOR SCROLL FOR SELECT 1",
            "DECLARE c CURSOR FOR INSERT INTO t VALUES (1)",
            "DECLARE c CURSOR FOR UPDATE t SET a = 1",
            "DECLARE c CURSOR FOR DELETE FROM t",
            "DECLARE c CURSOR FOR SELECT 1 FETCH",
            "DECLARE s.c CURSOR FOR SELECT 1",
            "DECLARE binary CURSOR FOR SELECT 1",
            "DECLARE table CURSOR FOR SELECT 1",
            "DECLARE verbose CURSOR FOR SELECT 1",
            "FETCH",
            "FETCH FROM",
            "FETCH ALL",
            "FETCH 5",
            "FETCH FORWARD 5",
            "FETCH FORWARD ALL",
            "FETCH NEXT 5 FROM c",
            "FETCH PRIOR ALL c",
            "FETCH ABSOLUTE FROM c",
            "FETCH ABSOLUTE c",
            "FETCH RELATIVE ALL c",
            "FETCH 1.5 c",
            "FETCH ABSOLUTE 2147483648 c",
            "FETCH ABSOLUTE -2147483648 c",
            "FETCH c INTO x",
            "FETCH NEXT FROM c, d",
            "FETCH NEXT FROM s.c",
            "FETCH NEXT FROM table",
            "FETCH NEXT FROM all",
            "FETCH FROM IN c",
            "MOVE",
            "MOVE ALL",
            "MOVE c INTO x",
            "CLOSE",
            "CLOSE c, d",
            "CLOSE ALL c",
            "CLOSE s.c",
            "CLOSE table",
            "CLOSE binary"})
    void rejectsWhatPostgreSqlRejects(String sql) {
        for (Consumer<CCJSqlParser> dialect : List.of(POSTGRESQL, NO_DIALECT)) {
            assertThrows(JSQLParserException.class, () -> CCJSqlParserUtil.parse(sql, dialect),
                    sql);
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "FETCH NEXT FROM c INTO @x",
            "FETCH ABSOLUTE @n FROM c",
            "FETCH NEXT FROM GLOBAL c",
            "FETCH c INTO x",
            "MOVE c d",
            "CLOSE GLOBAL c",
            "CLOSE table"})
    void otherProductsVariantsStayUnsupportedWithoutDialect(String sql)
            throws JSQLParserException {
        assertInstanceOf(UnsupportedStatement.class, CCJSqlParserUtil.parse(sql, UNSUPPORTED),
                sql);
        // the PostgreSQL dialect reads them as cursor statements, which they are not
        assertThrows(JSQLParserException.class, () -> CCJSqlParserUtil.parse(sql,
                POSTGRESQL.andThen(UNSUPPORTED)), sql);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "DECLARE c CURSOR FOR SELECT 1",
            "FETCH NEXT FROM c",
            "FETCH ALL c",
            "MOVE c",
            "CLOSE c",
            "CLOSE ALL"})
    void otherDialectsKeepTheirReadings(String sql) {
        for (Dialect dialect : List.of(Dialect.SQLSERVER, Dialect.MYSQL, Dialect.MARIADB,
                Dialect.ORACLE, Dialect.H2, Dialect.COCKROACHDB)) {
            Consumer<CCJSqlParser> consumer = parser -> parser.withDialect(dialect);
            assertThrows(JSQLParserException.class, () -> CCJSqlParserUtil.parse(sql, consumer),
                    sql + " " + dialect);
            if (!sql.startsWith("DECLARE")) {
                assertDoesParseAsUnsupported(sql, consumer.andThen(UNSUPPORTED));
            }
        }
    }

    @Test
    void sqlServerDeclareKeepsItsReading() throws JSQLParserException {
        for (Consumer<CCJSqlParser> dialect : List.of(NO_DIALECT, POSTGRESQL,
                (Consumer<CCJSqlParser>) parser -> parser.withDialect(Dialect.SQLSERVER))) {
            for (String sql : new String[] {"DECLARE @x INT", "DECLARE @x INT = 1, @y VARCHAR(10)",
                    "DECLARE @t TABLE (a INT)", "DECLARE @x AS t"}) {
                assertInstanceOf(DeclareStatement.class,
                        assertSqlCanBeParsedAndDeparsed(sql, true, dialect), sql);
            }
        }
        // the cursor forms of other products remain parse errors without a dialect
        assertThrows(JSQLParserException.class,
                () -> CCJSqlParserUtil.parse("DECLARE c CURSOR LOCAL FAST_FORWARD FOR SELECT 1"));
        assertThrows(JSQLParserException.class,
                () -> CCJSqlParserUtil.parse("DECLARE x INT", UNSUPPORTED));
    }

    @Test
    void endsAtStatementBoundaries() throws JSQLParserException {
        String script = "START TRANSACTION; DECLARE c SCROLL CURSOR FOR SELECT * FROM t;\n"
                + "FETCH NEXT FROM c;\n"
                + "MOVE c; FETCH PRIOR c; CLOSE c; COMMIT";
        for (Consumer<CCJSqlParser> dialect : List.of(POSTGRESQL, NO_DIALECT)) {
            assertEquals(List.of("StartTransaction", "DeclareCursorStatement",
                    "FetchCursorStatement", "FetchCursorStatement", "FetchCursorStatement",
                    "CloseCursorStatement", "Commit"), classes(script, dialect));
        }
        // without a semicolon in the PostgreSQL dialect, as other statements may follow
        assertEquals(List.of("PlainSelect", "Update", "FetchCursorStatement",
                "FetchCursorStatement", "CloseCursorStatement", "CloseCursorStatement",
                "FetchCursorStatement", "PlainSelect"),
                classes("SELECT 0; UPDATE t SET a = 1\nFETCH c\nMOVE ALL IN c\nCLOSE c\n"
                        + "CLOSE ALL\nFETCH next\n;SELECT 1", POSTGRESQL));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "DECLARE c CURSOR FOR SELECT 1",
            "FETCH c",
            "FETCH next",
            "FETCH FORWARD ALL FROM c",
            "MOVE ABSOLUTE -1 c",
            "CLOSE c",
            "CLOSE ALL"})
    void isAStatementStart(String sql) throws JSQLParserException {
        for (Consumer<CCJSqlParser> dialect : List.of(POSTGRESQL, NO_DIALECT)) {
            Statements statements =
                    CCJSqlParserUtil.parseStatements("SELECT 1;\n" + sql + ";\nSELECT 2", dialect);
            assertEquals(3, statements.size(), sql);
            assertEquals(sql, statements.get(1).toString());
        }
        Statements unsupported = CCJSqlParserUtil.parseStatements("FOO BAR; " + sql + "; BAZ",
                UNSUPPORTED);
        assertEquals(3, unsupported.size(), sql);
        assertInstanceOf(UnsupportedStatement.class, unsupported.get(0));
        assertEquals(sql, unsupported.get(1).toString());
        assertInstanceOf(UnsupportedStatement.class, unsupported.get(2));
    }

    @Test
    void selectFetchAndOtherUsesOfTheWordsAreUnchanged() throws JSQLParserException {
        for (Consumer<CCJSqlParser> dialect : List.of(POSTGRESQL, NO_DIALECT,
                (Consumer<CCJSqlParser>) parser -> parser.withDialect(Dialect.MYSQL))) {
            assertSqlCanBeParsedAndDeparsed("SELECT * FROM t FETCH FIRST 1 ROWS ONLY", true,
                    dialect);
            assertSqlCanBeParsedAndDeparsed("SELECT * FROM t OFFSET 1 ROWS FETCH NEXT 2 ROWS ONLY",
                    true, dialect);
            assertSqlCanBeParsedAndDeparsed(
                    "SELECT move, close, cursor, scroll, hold, absolute, relative, forward, "
                            + "backward, prior, insensitive, asensitive FROM move AS move",
                    true, dialect);
            assertSqlCanBeParsedAndDeparsed("SELECT move(1), t.move FROM t move", true, dialect);
            assertInstanceOf(CreateTable.class, assertSqlCanBeParsedAndDeparsed(
                    "CREATE TABLE move (move INT, close INT, cursor INT)", true, dialect));
        }
        assertInstanceOf(AlterSession.class, CCJSqlParserUtil.parse(
                "ALTER SESSION CLOSE DATABASE LINK x",
                parser -> parser.withDialect(Dialect.ORACLE)));
    }

    @Test
    void findsTheTablesOfTheQueryOnly() throws JSQLParserException {
        Statement declare = parse("DECLARE c CURSOR FOR WITH x AS (SELECT a FROM t) "
                + "SELECT x.a FROM x JOIN s.u ON true");
        assertThat(new TablesNamesFinder<>().getTables(declare))
                .containsExactlyInAnyOrder("t", "s.u");
        assertThat(new TablesNamesFinder<>().getTables(parse("DECLARE c CURSOR FOR TABLE t")))
                .containsExactly("t");
        for (String sql : new String[] {"FETCH NEXT FROM t", "MOVE t", "CLOSE t", "CLOSE ALL"}) {
            assertThat(new TablesNamesFinder<>().getTables(parse(sql))).isEmpty();
        }
    }

    @Test
    void reportsSessionFeatures() throws JSQLParserException {
        StatementFeatures declare = parse("DECLARE c CURSOR FOR SELECT a FROM t").getFeatures();
        assertThat(declare.getCertain()).containsExactlyInAnyOrder(StmtFeature.MODIFIES_SESSION,
                StmtFeature.READS_DATA);
        assertThat(parse("DECLARE c CURSOR FOR SELECT 1").getFeatures().getCertain())
                .containsExactly(StmtFeature.MODIFIES_SESSION);

        StatementFeatures fetch = parse("FETCH ALL c").getFeatures();
        assertThat(fetch.getCertain()).containsExactlyInAnyOrder(StmtFeature.RETURNS_RESULT_SET,
                StmtFeature.MODIFIES_SESSION);
        assertThat(fetch.getUncertain()).containsExactly(StmtFeature.READS_DATA);

        StatementFeatures move = parse("MOVE ALL c").getFeatures();
        assertThat(move.getCertain()).containsExactly(StmtFeature.MODIFIES_SESSION);
        assertThat(move.getUncertain()).containsExactly(StmtFeature.READS_DATA);

        assertThat(parse("CLOSE ALL").getFeatures().getCertain())
                .containsExactly(StmtFeature.MODIFIES_SESSION);
    }

    @Test
    void nodesSpanTheStatementsAndTheQuery() throws JSQLParserException {
        String sql = "DECLARE c SCROLL CURSOR WITH HOLD FOR SELECT a FROM t";
        DeclareCursorStatement declare = (DeclareCursorStatement) parse(sql);
        assertEquals(sql, text(sql, declare));
        assertEquals("SELECT a FROM t", text(sql, declare.getQuery()));
        for (String other : new String[] {"FETCH FORWARD 2 FROM c", "MOVE c", "CLOSE ALL"}) {
            assertEquals(other, text(other, (ASTNodeAccess) parse(other)));
        }
    }

    @Test
    void buildsStatementsInCode() {
        Select query = new PlainSelect().addSelectItems(new Column(new Table("t"), "a"))
                .withFromItem(new Table("t"));
        DeclareCursorStatement declare = new DeclareCursorStatement("c", query)
                .addOptions(CursorOption.BINARY, CursorOption.NO_SCROLL);
        declare.setHoldability(DeclareCursorStatement.Holdability.WITH_HOLD);
        assertEquals("DECLARE c BINARY NO SCROLL CURSOR WITH HOLD FOR SELECT t.a FROM t",
                declare.toString());
        assertEquals(declare.toString(), deparse(declare));
        declare.setOptions(null);
        declare.setHoldability(null);
        assertEquals("DECLARE c CURSOR FOR SELECT t.a FROM t", deparse(declare));

        FetchCursorStatement fetch = new FetchCursorStatement(FetchCursorStatement.Kind.MOVE, "c");
        assertEquals("MOVE c", fetch.toString());
        fetch.setDirection(FetchDirection.FORWARD);
        fetch.setCount(3L);
        fetch.setFromKeyword(FetchCursorStatement.FromKeyword.IN);
        assertEquals("MOVE FORWARD 3 IN c", deparse(fetch));
        fetch.setAll(true);
        assertNull(fetch.getCount());
        assertEquals("MOVE FORWARD ALL IN c", fetch.toString());
        fetch.setCount(-1L);
        assertFalse(fetch.isAll());
        fetch.setKind(FetchCursorStatement.Kind.FETCH);
        fetch.setDirection(null);
        fetch.setFromKeyword(null);
        assertEquals("FETCH -1 c", deparse(fetch));
        assertThrows(NullPointerException.class, () -> fetch.setKind(null));
        FetchCursorStatement plain = new FetchCursorStatement();
        plain.setCursorName("c");
        assertEquals("FETCH c", plain.toString());

        CloseCursorStatement close = new CloseCursorStatement();
        assertTrue(close.isAll());
        assertEquals("CLOSE ALL", deparse(close));
        close.setCursorName("c");
        assertEquals("CLOSE c", close.toString());
        assertEquals("CLOSE d", deparse(new CloseCursorStatement("d")));
    }

    private static void assertFetch(String sql, FetchCursorStatement.Kind kind,
            FetchDirection direction, Long count, boolean all,
            FetchCursorStatement.FromKeyword fromKeyword, String name) throws JSQLParserException {
        FetchCursorStatement fetch = (FetchCursorStatement) parse(sql);
        assertEquals(kind, fetch.getKind(), sql);
        assertEquals(direction, fetch.getDirection(), sql);
        assertEquals(count, fetch.getCount(), sql);
        assertEquals(all, fetch.isAll(), sql);
        assertEquals(fromKeyword, fetch.getFromKeyword(), sql);
        assertEquals(name, fetch.getCursorName(), sql);
    }

    private static void assertNormalized(String expected, String sql) throws JSQLParserException {
        for (Consumer<CCJSqlParser> dialect : List.of(POSTGRESQL, NO_DIALECT)) {
            Statement statement = CCJSqlParserUtil.parse(sql, dialect);
            assertEquals(expected, statement.toString());
            assertEquals(expected, deparse(statement));
        }
    }

    private static void assertDoesParseAsUnsupported(String sql, Consumer<CCJSqlParser> dialect) {
        try {
            assertInstanceOf(UnsupportedStatement.class, CCJSqlParserUtil.parse(sql, dialect),
                    sql);
        } catch (JSQLParserException e) {
            throw new AssertionError(sql, e);
        }
    }

    private static Statement parse(String sql) throws JSQLParserException {
        return assertSqlCanBeParsedAndDeparsed(sql, true, POSTGRESQL);
    }

    private static List<String> classes(String script, Consumer<CCJSqlParser> dialect)
            throws JSQLParserException {
        return CCJSqlParserUtil.parseStatements(script, dialect).stream()
                .map(statement -> statement.getClass().getSimpleName())
                .collect(Collectors.toList());
    }

    private static String deparse(Statement statement) {
        StringBuilder builder = new StringBuilder();
        statement.accept(new StatementDeParser(builder), null);
        return builder.toString();
    }

    private static String text(String sql, ASTNodeAccess access) {
        Node node = access.getASTNode();
        return sql.substring(node.jjtGetFirstToken().absoluteBegin - 1,
                node.jjtGetLastToken().absoluteEnd - 1);
    }
}
