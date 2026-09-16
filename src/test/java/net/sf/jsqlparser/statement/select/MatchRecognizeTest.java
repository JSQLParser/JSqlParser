/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.select;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.ExpressionVisitorAdapter;
import net.sf.jsqlparser.expression.JdbcParameter;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.parser.AbstractJSqlParser.Dialect;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.StatementFeatureVisitor;
import net.sf.jsqlparser.util.TablesNamesFinder;
import net.sf.jsqlparser.util.deparser.ExpressionDeParser;
import net.sf.jsqlparser.util.deparser.SelectDeParser;
import net.sf.jsqlparser.util.deparser.StatementDeParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class MatchRecognizeTest {
    private static String sql(String pattern) {
        return "SELECT * FROM events MATCH_RECOGNIZE (PARTITION BY account_id "
                + "ORDER BY seq DESC NULLS LAST MEASURES SUM(A.price) AS total, COUNT(*) AS n "
                + "PATTERN (" + pattern
                + ") DEFINE A AS price > 0, B AS price = 0, C AS price < 0)";
    }

    private static PlainSelect parse(String sql) throws JSQLParserException {
        return (PlainSelect) CCJSqlParserUtil.parse(sql, p -> p.withDialect(Dialect.BIGQUERY));
    }

    private static MatchRecognize match(PlainSelect select) {
        return assertInstanceOf(MatchRecognize.class, select.getFromItem());
    }

    private static String deparse(Select select) {
        StringBuilder builder = new StringBuilder();
        select.accept(new StatementDeParser(builder));
        return builder.toString();
    }

    private static PlainSelect roundTrip(String sql) throws JSQLParserException {
        PlainSelect statement = parse(sql);
        String rendered = deparse(statement);
        assertEquals(statement.toString(), rendered);
        assertEquals(rendered, parse(rendered).toString());
        return statement;
    }

    @Test
    void reportedQuery() throws Exception {
        String input;
        try (java.io.InputStream stream = getClass().getResourceAsStream(
                "/net/sf/jsqlparser/statement/select/match-recognize-2350.sql")) {
            assertNotNull(stream);
            input = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        }
        MatchRecognize match = match(roundTrip(input));
        assertEquals(4, match.getMeasures().size());
        assertEquals(3, match.getDefinitions().size());
        assertEquals(Boolean.FALSE, match.getOptions().getUseLongestMatch());
        assertEquals(MatchRecognize.SkipMode.PAST_LAST_ROW, match.getSkipMode());
        assertEquals("low mid+ high+", match.getPattern().toString());
    }

    @ParameterizedTest
    @ValueSource(strings = {"A", "A B | C", "(A B | C)+?", "A??", "A*?", "A+?",
            "A{3}", "A{2,}", "A{,4}?", "A{2,4}?", "A{@lo,@hi}", "A{?,?}",
            "A ()", "| A", "A |", "A||B", "||A||", "^ A+ $", "^A+$$", "A$", "(^)* A", "`$` A",
            "(A?)?"})
    void patternRoundTrip(String pattern) throws Exception {
        roundTrip(sql(pattern));
    }

    @Test
    void precedenceAndMutation() throws Exception {
        MatchRecognize match = match(roundTrip(sql("A B | C")));
        RowPattern.Operation alternative =
                assertInstanceOf(RowPattern.Operation.class, match.getPattern());
        assertEquals(RowPattern.Operation.Type.ALTERNATION, alternative.getType());
        RowPattern.Operation sequence =
                assertInstanceOf(RowPattern.Operation.class, alternative.getPatterns().get(0));
        assertEquals(RowPattern.Operation.Type.SEQUENCE, sequence.getType());
        assertEquals("A", ((RowPattern.Variable) sequence.getPatterns().get(0)).getName());
        sequence.getPatterns().set(1,
                new RowPattern.Operation(RowPattern.Operation.Type.ALTERNATION,
                        List.of(new RowPattern.Variable("B"), new RowPattern.Variable("C"))));
        assertEquals("(A (B | C)) | C", match.getPattern().toString());
        roundTrip("SELECT * FROM " + match);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " OPTIONS ()", " OPTIONS (use_longest_match = FALSE)",
            " OPTIONS (use_longest_match = TRUE)"})
    void optionsPreservePresence(String option) throws Exception {
        String query = sql("A");
        MatchRecognize match =
                match(roundTrip(query.substring(0, query.length() - 1) + option + ")"));
        if (option.isEmpty()) {
            assertNull(match.getOptions());
        } else if (option.equals(" OPTIONS ()")) {
            assertNull(match.getOptions().getUseLongestMatch());
        } else {
            assertEquals(option.contains("TRUE"), match.getOptions().getUseLongestMatch());
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"PAST LAST ROW", "TO NEXT ROW"})
    void skip(String skip) throws Exception {
        roundTrip(sql("A+").replace(" PATTERN", " AFTER MATCH SKIP " + skip + " PATTERN"));
    }

    @Test
    void aliasesAndJoinsBindToTheirOwnInput() throws Exception {
        String query = sql("A").replace("FROM events", "FROM events AS src") + " AS result";
        MatchRecognize node = match(roundTrip(query));
        assertEquals("src", node.getInput().getAlias().getName());
        assertEquals("result", node.getAlias().getName());
        PlainSelect join = roundTrip(query.replace("FROM events", "FROM accounts x JOIN events")
                + " ON x.id = result.n");
        assertInstanceOf(Table.class, join.getFromItem());
        assertInstanceOf(MatchRecognize.class, join.getJoins().get(0).getFromItem());
        node = match(roundTrip(sql("A").replace("events",
                "(events JOIN accounts ON events.account_id = accounts.id)")));
        assertInstanceOf(ParenthesedFromItem.class, node.getInput());
        List<String> inputTables = new ArrayList<>();
        node.accept(new FromItemVisitorAdapter<Void>() {
            @Override
            public <S> Void visit(Table table, S context) {
                inputTables.add(table.getName());
                return null;
            }
        }, null);
        assertEquals(List.of("events", "accounts"), inputTables);
    }

    @Test
    void nestedSetOperationLookaheadStillLexesAnchors() throws Exception {
        String nested = "SELECT * FROM ((" + sql("^A+$$") + ") UNION ALL (" + sql("A??") + ")) u";
        roundTrip(nested);
    }

    @Test
    void parameterIndicesExcludeQuantifiers() throws Exception {
        PlainSelect select = roundTrip("SELECT ? AS start_param FROM events MATCH_RECOGNIZE ("
                + "ORDER BY seq MEASURES COUNT(*) AS n PATTERN (A?? B{?}) "
                + "DEFINE A AS price > ?, B AS price < 0) WHERE n > ?");
        MatchRecognize node = match(select);
        List<Integer> indices = new ArrayList<>();
        node.forEachExpression(
                expression -> expression.accept(new ExpressionVisitorAdapter<Void>() {
                    @Override
                    public <S> Void visit(JdbcParameter parameter, S context) {
                        indices.add(parameter.getIndex());
                        return null;
                    }
                }, null));
        assertEquals(List.of(2, 3), indices);
        RowPattern.Quantified bound =
                (RowPattern.Quantified) ((RowPattern.Operation) node.getPattern()).getPatterns()
                        .get(1);
        bound.setLowerBound(new LongValue(4));
        assertTrue(deparse(select).contains("B{4}"));
        assertEquals(1, ((JdbcParameter) select.getSelectItem(0).getExpression()).getIndex());
    }

    @Test
    void expressionAndSourceCallbacksPreserveContext() throws Exception {
        PlainSelect statement = parse(sql("A{@lo,3}"));
        List<String> columns = new ArrayList<>();
        Object marker = new Object();
        MatchRecognize node = match(statement);
        node.accept(new FromItemVisitorAdapter<Void>(new ExpressionVisitorAdapter<Void>() {
            @Override
            public <S> Void visit(Column column, S context) {
                assertSame(marker, context);
                columns.add(column.toString());
                return null;
            }
        }) {
            @Override
            public <S> Void visit(Table table, S context) {
                assertSame(marker, context);
                columns.add("table:" + table.getName());
                return null;
            }
        }, marker);
        assertEquals(
                List.of("table:events", "account_id", "seq", "A.price", "price", "price", "price"),
                columns);
        StringBuilder builder = new StringBuilder();
        ExpressionDeParser expressions = new ExpressionDeParser() {
            @Override
            public <S> StringBuilder visit(Column column, S context) {
                return getBuilder().append("renamed_").append(column.getColumnName());
            }
        };
        expressions.setBuilder(builder);
        SelectDeParser selects = new SelectDeParser(expressions, builder);
        expressions.setSelectVisitor(selects);
        statement.accept((SelectVisitor<StringBuilder>) selects, marker);
        assertTrue(builder.toString().contains("SUM(renamed_price)"));
        assertTrue(builder.toString().contains("DEFINE A AS renamed_price > 0"));
        assertTrue(builder.toString().contains("ORDER BY renamed_seq DESC NULLS LAST"));
    }

    @Test
    void discoveryAndFeatureAnalysisReachClauseExpressions() throws Exception {
        PlainSelect statement = parse(sql("A").replace("SUM(A.price)", "custom_measure(A.price)")
                .replace("A AS price > 0",
                        "A AS custom_predicate(price) > (SELECT min(limit_value) FROM limits)"));
        assertEquals(Set.of("events", "limits"), new TablesNamesFinder()
                .getTables((net.sf.jsqlparser.statement.Statement) statement));
        assertTrue(StatementFeatureVisitor.analyse(statement).getUnresolvedReferences()
                .containsAll(Set.of("custom_measure", "custom_predicate", "min")));
    }

    @ParameterizedTest
    @ValueSource(strings = {"SELECT match_recognize FROM t", "SELECT * FROM t match_recognize",
            "SELECT * FROM match_recognize", "SELECT match_recognize(price) FROM t",
            "SELECT pattern('$$') FROM t", "SELECT $$body$$", "SELECT 'A' || 'B' FROM t",
            "SELECT * FROM t WHERE price > ?"})
    void existingNamesAndLiterals(String sql) throws Exception {
        roundTrip(sql);
    }

    @ParameterizedTest
    @ValueSource(strings = {"A{3}?", "^+ A", "A{1+2}", "A{2,3,4}", "A{}", "A{,}", "A**", "{- A }",
            "A+++"})
    void rejectsMalformedPatterns(String pattern) {
        assertThrows(JSQLParserException.class, () -> parse(sql(pattern)));
    }

    @Test
    void expressionTableDiscoveryKeepsNestedScopes() throws Exception {
        String query = sql("A").replace("SUM(A.price)",
                "SUM(A.price) + (SELECT external.price FROM lookup external)");
        net.sf.jsqlparser.expression.Expression expression =
                CCJSqlParserUtil.parseExpression("(" + query + ")");
        assertEquals(Set.of("events", "lookup", "external"),
                new TablesNamesFinder().getTables(expression));
    }

    @Test
    void inputUnionVisitorDoesNotRequirePlainSelect() throws Exception {
        MatchRecognize node = match(roundTrip(sql("A").replace("FROM events",
                "FROM (SELECT * FROM events UNION ALL SELECT * FROM archive_events)")));
        List<String> tables = new ArrayList<>();
        FromItemVisitorAdapter<Void> visitor = new FromItemVisitorAdapter<Void>() {
            @Override
            public <S> Void visit(Table table, S context) {
                tables.add(table.getName());
                return null;
            }
        };
        SelectVisitorAdapter<Void> selects =
                new SelectVisitorAdapter<>(new ExpressionVisitorAdapter<>(), visitor);
        visitor.setSelectVisitor(selects);
        node.accept(visitor, null);
        assertEquals(List.of("events", "archive_events"), tables);
    }

    @Test
    void nestedStructRendersEditedExpressionsInsteadOfOriginalTokens() throws Exception {
        PlainSelect statement = roundTrip(sql("A").replace("SUM(A.price)",
                "ARRAY_AGG(STRUCT(MATCH_ROW_NUMBER() AS row_no, CLASSIFIER() AS label, A.price))"));
        net.sf.jsqlparser.expression.Function array =
                (net.sf.jsqlparser.expression.Function) match(statement).getMeasures().get(0)
                        .getExpression();
        net.sf.jsqlparser.expression.StructType struct =
                (net.sf.jsqlparser.expression.StructType) array.getParameters().get(0);
        struct.getArguments().set(2, new SelectItem<>(new LongValue(99)));
        assertTrue(statement.toString().contains("99"));
        assertFalse(statement.toString().contains("A.price"));
        assertEquals(statement.toString(), deparse(statement));
        roundTrip(statement.toString());
    }

    @Test
    void boundsRespectNestingLimit() {
        String nested = "(".repeat(12) + "A" + ")".repeat(12);
        assertThrows(JSQLParserException.class, () -> parse(sql(nested)));
    }
}
