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

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import net.sf.jsqlparser.expression.ExpressionVisitorAdapter;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.RowPatternFunction;
import net.sf.jsqlparser.expression.operators.arithmetic.Addition;
import net.sf.jsqlparser.parser.AbstractJSqlParser.Dialect;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.StatementFeatureVisitor;
import net.sf.jsqlparser.util.TablesNamesFinder;
import net.sf.jsqlparser.util.deparser.ExpressionDeParser;
import net.sf.jsqlparser.util.deparser.StatementDeParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

class MatchRecognizeDialectTest {
    private static String read(String name) throws Exception {
        try (InputStream input = MatchRecognizeDialectTest.class.getResourceAsStream(
                "/net/sf/jsqlparser/statement/select/match-recognize/" + name)) {
            assertNotNull(input, name);
            return new String(input.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    static Stream<Arguments> executedCases() throws Exception {
        return read("execution-cases.txt").lines().map(line -> line.split(" "))
                .map(parts -> Arguments.of(Dialect.valueOf(parts[0]), parts[1]));
    }

    private static PlainSelect parse(String sql, Dialect dialect) throws Exception {
        return (PlainSelect) CCJSqlParserUtil.parse(sql, p -> p.withDialect(dialect));
    }

    private static String deparse(PlainSelect select) {
        StringBuilder builder = new StringBuilder();
        select.accept(new StatementDeParser(builder));
        return builder.toString();
    }

    private static PlainSelect roundTrip(String sql, Dialect dialect) throws Exception {
        PlainSelect select = parse(sql, dialect);
        assertEquals(select.toString(), deparse(select));
        assertEquals(select.toString(), parse(deparse(select), dialect).toString());
        return select;
    }

    @ParameterizedTest(name = "{0}: {1}")
    @MethodSource("executedCases")
    void executionFixturesRoundTrip(Dialect dialect, String file) throws Exception {
        roundTrip(read(file), dialect);
    }

    private static String snowflake(String clause) {
        return "SELECT * FROM events MATCH_RECOGNIZE (" + clause + ")";
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "PATTERN (A+) DEFINE A AS price > 0",
            "ALL ROWS PER MATCH PATTERN (A+) DEFINE A AS price > 0",
            "MEASURES MATCH_SEQUENCE_NUMBER() AS n ALL ROWS PER MATCH PATTERN (A B) DEFINE A AS price > 0",
            "MEASURES FINAL LAST(A.price) AS p ALL ROWS PER MATCH OMIT EMPTY MATCHES PATTERN (A*) DEFINE A AS price > 0",
            "ALL ROWS PER MATCH SHOW EMPTY MATCHES PATTERN (A*) DEFINE A AS price > 0",
            "ALL ROWS PER MATCH WITH UNMATCHED ROWS PATTERN (A+) DEFINE A AS price > 0",
            "AFTER MATCH SKIP TO FIRST B PATTERN (A B+) DEFINE A AS price > 0, B AS price < 0",
            "AFTER MATCH SKIP TO LAST B PATTERN (A B+) DEFINE A AS price > 0, B AS price < 0",
            "AFTER MATCH SKIP TO B PATTERN (A B+) DEFINE A AS price > 0, B AS price < 0",
            "PATTERN (PERMUTE(A, B){1,2}) DEFINE A AS price > 0, B AS price < 0",
            "PATTERN ({- A+ -} B) DEFINE A AS price > 0, B AS price < 0",
            "PATTERN (^ A B | C $) DEFINE A AS price > 0, B AS price = 0, C AS price < 0",
            "PATTERN (A+) DEFINE A AS price > LAG(price)",
            "MEASURES RUNNING SUM(A.price) + FINAL SUM(A.price) AS total PATTERN (A+) DEFINE A AS price > 0"
    })
    void documentedSnowflakeSyntax(String clause) throws Exception {
        roundTrip(snowflake(clause), Dialect.SNOWFLAKE);
    }

    @Test
    void snowflakeDocumentedPrecedenceIsExplicitInTheAst() throws Exception {
        String query = snowflake("PATTERN (A B | C) DEFINE A AS price > 0");
        MatchRecognize node = (MatchRecognize) roundTrip(query, Dialect.SNOWFLAKE).getFromItem();
        RowPattern.Operation sequence = (RowPattern.Operation) node.getPattern();
        assertEquals(RowPattern.Operation.Type.SEQUENCE, sequence.getType());
        RowPattern.Operation alternative = (RowPattern.Operation) sequence.getPatterns().get(1);
        assertEquals(RowPattern.Operation.Type.ALTERNATION, alternative.getType());
        assertEquals("A (B | C)", sequence.toString());
        MatchRecognize oracle = (MatchRecognize) parse(query, Dialect.ORACLE).getFromItem();
        assertEquals(RowPattern.Operation.Type.ALTERNATION,
                ((RowPattern.Operation) oracle.getPattern()).getType());
        assertEquals("(A B) | C", oracle.getPattern().toString());
    }

    @Test
    void prefixFunctionsRemainEditableAndVisibleToExistingVisitors() throws Exception {
        PlainSelect statement = roundTrip(read("oracle-running-final.sql"), Dialect.ORACLE);
        MatchRecognize node = (MatchRecognize) statement.getFromItem();
        Addition sum = (Addition) node.getMeasures().get(0).getExpression();
        RowPatternFunction running = (RowPatternFunction) sum.getLeftExpression();
        RowPatternFunction finalFunction = (RowPatternFunction) sum.getRightExpression();
        assertEquals(RowPatternFunction.EvaluationMode.RUNNING, running.getEvaluationMode());
        assertEquals(RowPatternFunction.EvaluationMode.FINAL, finalFunction.getEvaluationMode());
        running.getFunction().setName("custom_measure");
        running.getFunction().setParameters(
                new net.sf.jsqlparser.expression.operators.relational.ExpressionList<>(
                        new LongValue(42)));
        assertTrue(deparse(statement).contains("RUNNING custom_measure(42)"));
        assertEquals(statement.toString(), deparse(statement));
        assertTrue(StatementFeatureVisitor.analyse(statement).getUnresolvedReferences()
                .contains("custom_measure"));
        List<String> columns = new ArrayList<>();
        finalFunction.accept(new ExpressionVisitorAdapter<Void>() {
            @Override
            public <S> Void visit(Column column, S context) {
                assertEquals("context", context);
                columns.add(column.toString());
                return null;
            }
        }, "context");
        assertEquals(List.of("A.price"), columns);
        StringBuilder builder = new StringBuilder();
        finalFunction.accept(new ExpressionDeParser(null, builder) {
            @Override
            public <S> StringBuilder visit(Column column, S context) {
                return getBuilder().append("A.changed");
            }
        }, null);
        assertEquals("FINAL SUM(A.changed)", builder.toString());
    }

    @Test
    void subsetNamesAreNotDiscoveredAsTables() throws Exception {
        String query = read("oracle-subset.sql");
        net.sf.jsqlparser.expression.Expression expression =
                CCJSqlParserUtil.parseExpression("(" + query.replace(";", "") + ")");
        assertFalse(new TablesNamesFinder().getTables(expression).contains("U"));
        MatchRecognize node = (MatchRecognize) parse(query, Dialect.ORACLE).getFromItem();
        assertEquals("U", node.getSubsets().get(0).getName());
        assertEquals(List.of("A", "B"), node.getSubsets().get(0).getVariables());
    }

    @Test
    void permutationAndExclusionVisitorsKeepStructureWithoutExpansion() throws Exception {
        PlainSelect statement =
                roundTrip(snowflake("PATTERN (PERMUTE(A, {- B{2} -}, C)) DEFINE A AS price > 0"),
                        Dialect.ORACLE);
        MatchRecognize node = (MatchRecognize) statement.getFromItem();
        RowPattern.Permute permute = (RowPattern.Permute) node.getPattern();
        assertEquals(3, permute.getPatterns().size());
        assertInstanceOf(RowPattern.Exclusion.class, permute.getPatterns().get(1));
        List<String> variables = new ArrayList<>();
        permute.accept(new RowPatternVisitorAdapter<Void>() {
            @Override
            public <S> Void visit(RowPattern.Variable variable, S context) {
                assertEquals("pattern", context);
                variables.add(variable.getName());
                return null;
            }
        }, "pattern");
        assertEquals(List.of("A", "B", "C"), variables);
        permute.getPatterns().set(2, new RowPattern.Variable("D"));
        assertTrue(deparse(statement).contains("PERMUTE(A, {- B{2} -}, D)"));
    }

    @Test
    void outputPivotAliasAndSampleStayAfterMatchRecognize() throws Exception {
        String query = snowflake(
                "MEASURES CLASSIFIER() AS label, SUM(A.price) AS total ALL ROWS PER MATCH PATTERN (A+) DEFINE A AS price > 0")
                + " PIVOT (SUM(total) FOR label IN ('A')) p SAMPLE (10)";
        PlainSelect statement = roundTrip(query, Dialect.SNOWFLAKE);
        MatchRecognize node = (MatchRecognize) statement.getFromItem();
        assertNotNull(node.getPivot());
        assertNotNull(node.getSampleClause());
        List<String> columns = new ArrayList<>();
        node.accept(new FromItemVisitorAdapter<Void>(new ExpressionVisitorAdapter<Void>() {
            @Override
            public <S> Void visit(Column column, S context) {
                columns.add(column.toString());
                return null;
            }
        }), null);
        assertTrue(columns.containsAll(List.of("total", "label", "A.price")));
    }

    @ParameterizedTest
    @ValueSource(strings = {"SELECT running, \"final\" FROM t", "SELECT running(price) FROM t",
            "SELECT * FROM t FINAL", "SELECT 'RUNNING SUM(x)' FROM t"})
    void prefixWordsKeepExistingUses(String sql) throws Exception {
        roundTrip(sql, Dialect.ORACLE);
    }
}
