/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.handler;

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
import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.expression.operators.relational.GreaterThan;
import net.sf.jsqlparser.expression.operators.relational.ParenthesedExpressionList;
import net.sf.jsqlparser.parser.ASTNodeAccess;
import net.sf.jsqlparser.parser.AbstractJSqlParser.Dialect;
import net.sf.jsqlparser.parser.CCJSqlParser;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.parser.Node;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.Statements;
import net.sf.jsqlparser.statement.StmtFeature;
import net.sf.jsqlparser.statement.UnsupportedStatement;
import net.sf.jsqlparser.statement.create.accessmethod.CreateAccessMethod;
import net.sf.jsqlparser.statement.handler.HandlerStatement.Action;
import net.sf.jsqlparser.statement.handler.HandlerStatement.Direction;
import net.sf.jsqlparser.statement.handler.HandlerStatement.KeyComparison;
import net.sf.jsqlparser.statement.select.Limit;
import net.sf.jsqlparser.statement.select.Offset;
import net.sf.jsqlparser.util.TablesNamesFinder;
import net.sf.jsqlparser.util.deparser.StatementDeParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class MySqlHandlerStatementTest {
    private static final Consumer<CCJSqlParser> MYSQL = parser -> parser.withDialect(Dialect.MYSQL);
    private static final Consumer<CCJSqlParser> NO_DIALECT = parser -> {
    };

    @ParameterizedTest
    @ValueSource(strings = {
            "HANDLER t OPEN",
            "HANDLER t OPEN AS h",
            "HANDLER db1.t OPEN h2",
            "HANDLER `db1`.`t` OPEN AS `h`",
            "HANDLER h READ FIRST",
            "HANDLER h READ NEXT LIMIT 10",
            "HANDLER h READ FIRST WHERE a > 1 LIMIT 5",
            "HANDLER h READ i1 = (1)",
            "HANDLER h READ i1 >= (1, 2) WHERE a > 0 LIMIT 3",
            "HANDLER h READ i1 <= (1)",
            "HANDLER h READ i1 < (1)",
            "HANDLER h READ i1 > ('a', 2)",
            "HANDLER h READ `PRIMARY` = (1, DEFAULT)",
            "HANDLER h READ i1 FIRST",
            "HANDLER h READ i1 NEXT",
            "HANDLER h READ i1 PREV",
            "HANDLER h READ i1 LAST WHERE a IS NULL LIMIT 2, 5",
            "HANDLER h READ i1 NEXT LIMIT 5 OFFSET 2",
            "HANDLER h READ first NEXT",
            "HANDLER `read` READ NEXT",
            "HANDLER h CLOSE"})
    void parsesMySqlFormsWithAndWithoutDialect(String sql) throws JSQLParserException {
        for (Consumer<CCJSqlParser> dialect : List.of(NO_DIALECT, MYSQL)) {
            Statement statement = assertSqlCanBeParsedAndDeparsed(sql, false, dialect);
            assertInstanceOf(HandlerStatement.class, statement);
            assertEquals(sql, deparse(statement));
            assertEquals(sql, CCJSqlParserUtil.parse(statement.toString(), dialect).toString());
        }
    }

    @Test
    void opensATableUnderItsHandlerName() throws JSQLParserException {
        HandlerStatement open = parse("HANDLER db1.t OPEN AS h");
        assertEquals(Action.OPEN, open.getAction());
        assertEquals("db1.t", open.getTable().getFullyQualifiedName());
        assertEquals("h", open.getTable().getAlias().getName());
        assertTrue(open.getTable().getAlias().isUseAs());
        assertEquals("h", open.getHandlerName());

        HandlerStatement withoutAs = parse("HANDLER t OPEN h2");
        assertFalse(withoutAs.getTable().getAlias().isUseAs());
        assertEquals("h2", withoutAs.getHandlerName());

        HandlerStatement plain = parse("HANDLER t OPEN");
        assertNull(plain.getTable().getAlias());
        assertEquals("t", plain.getHandlerName());
    }

    @Test
    void readsThroughAnIndexWithKeyValues() throws JSQLParserException {
        HandlerStatement read = parse("HANDLER h READ i1 >= (1, 2) WHERE a > 0 LIMIT 3");
        assertEquals(Action.READ, read.getAction());
        assertEquals("h", read.getHandlerName());
        assertEquals("i1", read.getIndexName());
        assertEquals(KeyComparison.GREATER_THAN_OR_EQUALS, read.getKeyComparison());
        assertInstanceOf(ParenthesedExpressionList.class, read.getKeyValues());
        assertEquals(List.of("1", "2"), read.getKeyValues().stream().map(Object::toString)
                .collect(Collectors.toList()));
        assertNull(read.getDirection());
        assertInstanceOf(GreaterThan.class, read.getWhere());
        assertEquals("3", read.getLimit().getRowCount().toString());
        assertNull(read.getOffset());

        for (KeyComparison comparison : KeyComparison.values()) {
            HandlerStatement compared =
                    parse("HANDLER h READ i1 " + comparison.getOperator() + " (1)");
            assertEquals(comparison, compared.getKeyComparison());
        }
    }

    @Test
    void readsInIndexOrStorageOrder() throws JSQLParserException {
        HandlerStatement index = parse("HANDLER h READ i1 PREV");
        assertEquals("i1", index.getIndexName());
        assertEquals(Direction.PREV, index.getDirection());
        assertNull(index.getKeyComparison());
        assertNull(index.getKeyValues());

        HandlerStatement storage = parse("HANDLER h READ next WHERE a > 1");
        assertNull(storage.getIndexName());
        assertEquals(Direction.NEXT, storage.getDirection());

        // a direction after the word makes it the name of an index, as in MySQL's grammar
        HandlerStatement named = parse("HANDLER h READ first NEXT");
        assertEquals("first", named.getIndexName());
        assertEquals(Direction.NEXT, named.getDirection());

        HandlerStatement offset = parse("HANDLER h READ i1 LAST LIMIT 5 OFFSET 2");
        assertEquals(Direction.LAST, offset.getDirection());
        assertEquals("5", offset.getLimit().getRowCount().toString());
        assertEquals("2", offset.getOffset().getOffset().toString());

        HandlerStatement comma = parse("HANDLER h READ FIRST LIMIT 2, 5");
        assertEquals("2", comma.getLimit().getOffset().toString());
        assertNull(comma.getOffset());
    }

    @Test
    void closesAHandler() throws JSQLParserException {
        HandlerStatement close = parse("HANDLER h CLOSE");
        assertEquals(Action.CLOSE, close.getAction());
        assertEquals("h", close.getHandlerName());
        assertEquals("h", close.getTable().getName());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "HANDLER",
            "HANDLER t",
            "HANDLER t OPEN AS",
            "HANDLER t OPEN h AS x",
            "HANDLER t OPEN AS h(a)",
            "HANDLER t OPEN AS 'h'",
            "HANDLER db1.t CLOSE",
            "HANDLER db1.t READ FIRST",
            "HANDLER h READ",
            "HANDLER h READ PREV",
            "HANDLER h READ LAST",
            "HANDLER h READ i1",
            "HANDLER h READ i1 = 1",
            "HANDLER h READ i1 = ()",
            "HANDLER h READ i1 != (1)",
            "HANDLER h READ i1 <> (1)",
            "HANDLER h READ i1 FIRST NEXT",
            "HANDLER h READ FIRST OFFSET 2",
            "HANDLER h READ FIRST LIMIT 2, 5 OFFSET 1",
            "HANDLER h READ FIRST WHERE",
            "HANDLER h READ FIRST LIMIT 5 WHERE a > 1",
            "HANDLER h CLOSE h",
            "HANDLER h OPEN AS x CLOSE",
            // MySQL parses HANDLER READ with subqueries disallowed
            "HANDLER h READ FIRST WHERE a IN (SELECT b FROM s)",
            "HANDLER h READ FIRST WHERE a = (SELECT 1)",
            "HANDLER h READ FIRST WHERE EXISTS (SELECT 1)",
            "HANDLER h READ FIRST WHERE a IN (TABLE s)",
            "HANDLER h READ FIRST WHERE f((SELECT 1)) > 0",
            "HANDLER h READ i1 = ((SELECT 1))",
            "HANDLER h READ FIRST LIMIT (SELECT 1)"})
    void rejectsFormsMySqlRejects(String sql) {
        for (Consumer<CCJSqlParser> dialect : List.of(NO_DIALECT, MYSQL)) {
            assertThrows(JSQLParserException.class, () -> CCJSqlParserUtil.parse(sql, dialect),
                    sql);
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"HANDLER t OPEN", "HANDLER h READ FIRST", "HANDLER h CLOSE"})
    void otherDialectsKeepRejectingHandler(String sql) {
        for (Dialect dialect : List.of(Dialect.POSTGRESQL, Dialect.MARIADB, Dialect.ORACLE,
                Dialect.SQLSERVER)) {
            assertThrows(JSQLParserException.class,
                    () -> CCJSqlParserUtil.parse(sql, parser -> parser.withDialect(dialect)),
                    sql + " " + dialect);
        }
    }

    @Test
    void wordsRemainUsableAsNames() throws JSQLParserException {
        for (Consumer<CCJSqlParser> dialect : List.of(NO_DIALECT, MYSQL)) {
            assertSqlCanBeParsedAndDeparsed("SELECT handler, prev FROM handler", true, dialect);
            assertSqlCanBeParsedAndDeparsed("SELECT handler.a FROM db.handler AS handler", true,
                    dialect);
            assertSqlCanBeParsedAndDeparsed("SELECT * FROM t handler", true, dialect);
            assertSqlCanBeParsedAndDeparsed("CREATE TABLE handler (handler INT)", true, dialect);
            assertSqlCanBeParsedAndDeparsed("HANDLER handler OPEN AS handler", true, dialect);
        }
        CreateAccessMethod accessMethod = (CreateAccessMethod) assertSqlCanBeParsedAndDeparsed(
                "CREATE ACCESS METHOD heap2 TYPE TABLE HANDLER heap_tableam_handler", true,
                parser -> parser.withDialect(Dialect.POSTGRESQL));
        assertEquals("heap_tableam_handler", accessMethod.getHandler());
    }

    @Test
    void endsTheStatementsAroundIt() throws JSQLParserException {
        Statements statements = CCJSqlParserUtil.parseStatements(
                "HANDLER t OPEN AS h; HANDLER h READ FIRST; HANDLER h CLOSE; SELECT 1", MYSQL);
        assertEquals(List.of("HandlerStatement", "HandlerStatement", "HandlerStatement",
                "PlainSelect"), names(statements));

        Statements unsupported = CCJSqlParserUtil.parseStatements(
                "FOO; HANDLER t OPEN; BAR; HANDLER t CLOSE",
                parser -> parser.withUnsupportedStatements(true));
        assertEquals(List.of("UnsupportedStatement", "HandlerStatement", "UnsupportedStatement",
                "HandlerStatement"), names(unsupported));

        Statements postgres = CCJSqlParserUtil.parseStatements("HANDLER t OPEN",
                parser -> parser.withDialect(Dialect.POSTGRESQL).withUnsupportedStatements(true));
        assertInstanceOf(UnsupportedStatement.class, postgres.get(0));
    }

    @Test
    void findsTheOpenedTableButNotTheHandlerName() throws JSQLParserException {
        assertThat(new TablesNamesFinder<>()
                .getTables(CCJSqlParserUtil.parse("HANDLER db1.t OPEN AS h")))
                .containsExactly("db1.t");
        // READ and CLOSE name the handler, which may be the alias that OPEN gave the table
        assertThat(new TablesNamesFinder<>()
                .getTables(CCJSqlParserUtil.parse("HANDLER h READ i1 = (1) WHERE a > 1")))
                .isEmpty();
        assertThat(new TablesNamesFinder<>().getTables(CCJSqlParserUtil.parse("HANDLER h CLOSE")))
                .isEmpty();
    }

    @Test
    void reportsReadsAndSessionChanges() throws JSQLParserException {
        assertThat(CCJSqlParserUtil.parse("HANDLER h READ FIRST").getFeatures().getCertain())
                .containsExactlyInAnyOrder(StmtFeature.READS_DATA,
                        StmtFeature.RETURNS_RESULT_SET);
        for (String sql : new String[] {"HANDLER t OPEN", "HANDLER h CLOSE"}) {
            assertThat(CCJSqlParserUtil.parse(sql).getFeatures().getCertain())
                    .containsExactly(StmtFeature.MODIFIES_SESSION);
        }
    }

    @Test
    void nodesSpanTheStatementAndItsParts() throws JSQLParserException {
        String sql = "HANDLER db1.t OPEN AS h";
        HandlerStatement open = parse(sql);
        assertEquals(sql, text(sql, open));
        assertEquals("db1.t", text(sql, open.getTable()));

        sql = "HANDLER h READ i1 >= (1, 2) WHERE a > 0 LIMIT 3";
        HandlerStatement read = parse(sql);
        assertEquals(sql, text(sql, read));
        assertEquals("h", text(sql, read.getTable()));
        assertEquals("(1, 2)", text(sql, read.getKeyValues()));
        assertEquals("a > 0", text(sql, read.getWhere()));
        assertEquals("LIMIT 3", text(sql, read.getLimit()));
    }

    @Test
    void buildsStatementsInCode() {
        Table table = new Table("db", "t");
        table.setAlias(new Alias("h"));
        HandlerStatement open = new HandlerStatement(Action.OPEN, table);
        assertEquals("HANDLER db.t OPEN AS h", open.toString());
        assertEquals("h", open.getHandlerName());
        assertNull(new HandlerStatement().getHandlerName());
        assertEquals(Action.OPEN, new HandlerStatement().getAction());

        HandlerStatement read = new HandlerStatement().withAction(Action.READ)
                .withTable(new Table("h")).withIndexName("i1")
                .withKeyComparison(KeyComparison.EQUALS)
                .withKeyValues(new ParenthesedExpressionList<>(new LongValue(1)))
                .withWhere(new GreaterThan(new Column("a"), new LongValue(0)))
                .withLimit(new Limit().withRowCount(new LongValue(5)))
                .withOffset(new Offset().withOffset(new LongValue(2)));
        assertEquals("HANDLER h READ i1 = (1) WHERE a > 0 LIMIT 5 OFFSET 2", read.toString());
        assertEquals(read.toString(), deparse(read));

        read.withKeyComparison(null).withKeyValues((ExpressionList<?>) null)
                .withDirection(Direction.FIRST).withIndexName(null).withWhere(null)
                .withLimit(null).withOffset(null);
        assertEquals("HANDLER h READ FIRST", read.toString());

        HandlerStatement close = new HandlerStatement(Action.CLOSE, new Table("h"));
        assertEquals("HANDLER h CLOSE", deparse(close));
        assertThrows(NullPointerException.class, () -> close.setAction(null));
    }

    private static HandlerStatement parse(String sql) throws JSQLParserException {
        return (HandlerStatement) assertSqlCanBeParsedAndDeparsed(sql, true, MYSQL);
    }

    private static List<String> names(Statements statements) {
        return statements.stream().map(statement -> statement.getClass().getSimpleName())
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
