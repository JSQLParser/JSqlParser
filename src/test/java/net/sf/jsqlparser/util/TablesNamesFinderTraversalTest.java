/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.ArrayExpression;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.JsonExpression;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.upsert.Upsert;
import net.sf.jsqlparser.test.TestUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

class TablesNamesFinderTraversalTest {
    @ParameterizedTest
    @MethodSource("nestedTables")
    void findsTablesInNestedChildren(String sql, List<String> expected) throws JSQLParserException {
        Statement statement = CCJSqlParserUtil.parse(sql);
        assertThat(TablesNamesFinder.findTables(sql)).containsExactlyInAnyOrderElementsOf(expected);
        assertThat(new TablesNamesFinder<>().getTables(statement))
                .containsExactlyInAnyOrderElementsOf(expected);
    }

    private static Stream<Arguments> nestedTables() {
        return Stream.of(
                tables("SELECT a[(SELECT max(x) FROM t2)] FROM t1", "t1", "t2"),
                tables("SELECT a[1][(SELECT max(x) FROM t2)] FROM t1", "t1", "t2"),
                tables("SELECT f()[(SELECT max(x) FROM t2)] FROM t1", "t1", "t2"),
                tables("SELECT f((SELECT x FROM t2))[(SELECT y FROM t3)] FROM t1", "t1", "t2",
                        "t3"),
                tables("SELECT a[OFFSET(1):2] FROM t1", "t1"),
                tables("SELECT f()[OFFSET(1):OFFSET(2)] FROM t1", "t1"),
                tables("SELECT f()[1:] FROM t1", "t1"),
                tables("SELECT f()[:2] FROM t1", "t1"),
                tables("SELECT f()[:] FROM t1", "t1"),
                tables("SELECT f()[OFFSET((SELECT x FROM t2)):OFFSET((SELECT y FROM t3))] FROM t1",
                        "t1", "t2",
                        "t3"),
                tables("FROM t1 JOIN t2 ON t1.id=t2.id |> SELECT t1.id", "t1", "t2"),
                tables("FROM t1 JOIN t2 ON t1.id=(SELECT id FROM t3) |> SELECT t1.id", "t1", "t2",
                        "t3"),
                tables("FROM t1 LEFT JOIN (SELECT id FROM t2) q ON t1.id=q.id |> SELECT t1.id",
                        "t1", "t2"),
                tables("FROM t1 LATERAL VIEW explode((SELECT a FROM t2)) e AS x |> SELECT x", "t1",
                        "t2"),
                tables("WITH c AS (SELECT a FROM t2) FROM t1 JOIN c ON t1.a=c.a |> SELECT t1.a",
                        "t1", "t2"),
                tables("UPDATE t1 SET a=1 OUTPUT inserted.* INTO t2", "t1", "t2"),
                tables("INSERT INTO t1 OUTPUT inserted.* INTO t2 VALUES (1)", "t1", "t2"),
                tables("DELETE t1 OUTPUT deleted.* INTO t2 FROM t1", "t1", "t2"),
                tables("MERGE INTO t1 USING t2 ON t1.a=t2.a WHEN MATCHED THEN DELETE OUTPUT deleted.* INTO t3",
                        "t1", "t2", "t3"),
                tables("UPDATE t1 SET a=1 OUTPUT (SELECT x FROM t2) INTO t3", "t1", "t2", "t3"),
                tables("MERGE INTO t1 USING t2 ON t1.a=t2.a WHEN MATCHED THEN DELETE OUTPUT (SELECT x FROM t3) INTO t4",
                        "t1", "t2", "t3", "t4"),
                tables("MERGE INTO t1 USING t2 ON t1.a=t2.a WHEN MATCHED THEN DELETE OUTPUT deleted.* INTO @tv",
                        "t1", "t2"),
                tables("UPSERT INTO t1 VALUES (1) ON DUPLICATE KEY UPDATE (a)=(SELECT max(x) FROM t2)",
                        "t1", "t2"),
                tables("UPSERT INTO t1 SELECT a FROM t2 ON DUPLICATE KEY UPDATE b=(SELECT x FROM t3)",
                        "t1", "t2", "t3"),
                tables("SELECT POSITION('x' IN (SELECT max(a) FROM t2)) FROM t1", "t1", "t2"),
                tables("SELECT SUBSTRING((SELECT a FROM t2) FROM (SELECT n FROM t3) FOR (SELECT n FROM t4)) FROM t1",
                        "t1", "t2", "t3", "t4"),
                tables("IF (SELECT count(*) FROM t2)>1 DELETE FROM t3", "t2", "t3"),
                tables("IF EXISTS (SELECT 1 FROM t1) UPDATE t2 SET a=1 ELSE DELETE FROM t3", "t1",
                        "t2", "t3"),
                tables("SELECT sum(x) OVER (PARTITION BY (SELECT a FROM t2)) FROM t1", "t1", "t2"),
                tables("SELECT sum(x) OVER (PARTITION BY (SELECT a FROM t2) ORDER BY (SELECT b FROM t3)) FROM t1",
                        "t1", "t2", "t3"),
                tables("SELECT a[1], f()[1] FROM t1", "t1"),
                tables("SELECT a FROM t1 JOIN t2 ON t1.a=t2.a", "t1", "t2"),
                tables("SELECT * FROM t1 LATERAL VIEW explode((SELECT a FROM t2)) e AS x", "t1",
                        "t2"),
                tables("FROM t1 |> JOIN t2 ON t1.a=t2.a |> SELECT t1.a", "t1", "t2"),
                tables("UPDATE t1 SET a=1 OUTPUT inserted.*, deleted.a INTO @tv", "t1"),
                tables("UPDATE t1 SET a=1 OUTPUT inserted.*", "t1"),
                tables("INSERT INTO t1 VALUES (1) ON DUPLICATE KEY UPDATE a=(SELECT x FROM t2)",
                        "t1", "t2"),
                tables("UPSERT INTO t1 VALUES (1)", "t1"),
                tables("SELECT POSITION('x' IN 'xx') FROM t1", "t1"),
                tables("IF a>1 DELETE FROM t3", "t3"),
                tables("SELECT sum(x) OVER (ORDER BY (SELECT a FROM t2)) FROM t1", "t1", "t2"));
    }

    private static Arguments tables(String sql, String... names) {
        return Arguments.of(sql, List.of(names));
    }

    @ParameterizedTest
    @MethodSource("expressionTables")
    void findsTablesThroughExpressionEntryPoints(String sql, List<String> expected)
            throws JSQLParserException {
        Expression expression = CCJSqlParserUtil.parseExpression(sql);
        assertThat(TablesNamesFinder.findTablesInExpression(sql))
                .containsExactlyInAnyOrderElementsOf(expected);
        assertThat(new TablesNamesFinder<>().getTables(expression))
                .containsExactlyInAnyOrderElementsOf(expected);
    }

    private static Stream<Arguments> expressionTables() {
        return Stream.of(tables("a[(SELECT x FROM t2)]", "t2"),
                tables("f()[(SELECT x FROM t2)]", "t2"),
                tables("f()[OFFSET((SELECT x FROM t2)):OFFSET((SELECT y FROM t3))]", "t2", "t3"),
                tables("POSITION('x' IN (SELECT a FROM t2))", "t2"),
                tables("sum(x) OVER (PARTITION BY (SELECT a FROM t2))", "t2"),
                tables("a[1]"), tables("f()[1]"), tables("f()[:2]"), tables("f()[:]"));
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2, 3, 4, 5, 6, 7})
    void visitsArrayOperandsOnce(int fields) throws JSQLParserException {
        Expression index = (fields & 1) == 0 ? null
                : CCJSqlParserUtil.parseExpression("(SELECT x FROM index_table)");
        Expression start = (fields & 2) == 0 ? null
                : CCJSqlParserUtil.parseExpression("(SELECT x FROM start_table)");
        Expression stop = (fields & 4) == 0 ? null
                : CCJSqlParserUtil.parseExpression("(SELECT x FROM stop_table)");
        ArrayExpression expression = new ArrayExpression(
                CCJSqlParserUtil.parseExpression("f((SELECT x FROM object_table))"), index, start,
                stop);
        Map<String, Integer> expected = new HashMap<>();
        expected.put("object_table", 1);
        if (index != null) {
            expected.put("index_table", 1);
        }
        if (start != null) {
            expected.put("start_table", 1);
        }
        if (stop != null) {
            expected.put("stop_table", 1);
        }
        CountingFinder finder = new CountingFinder();
        assertThat(finder.getTables(expression))
                .containsExactlyInAnyOrderElementsOf(expected.keySet());
        assertThat(finder.visits).isEqualTo(expected);
    }

    @Test
    void visitsNewChildrenWithoutRepeatingExistingChildren() throws JSQLParserException {
        CountingFinder finder = new CountingFinder();
        Statement statement = CCJSqlParserUtil.parse(
                "FROM t1 JOIN t2 ON t1.a=(SELECT a FROM t3) |> SELECT sum(a) OVER (PARTITION BY (SELECT a FROM t4) ORDER BY (SELECT a FROM t5))");
        assertThat(finder.getTables(statement)).containsExactlyInAnyOrder("t1", "t2", "t3", "t4",
                "t5");
        assertThat(finder.visits).isEqualTo(Map.of("t1", 1, "t2", 1, "t3", 1, "t4", 1, "t5", 1));
    }

    @Test
    void resetsStateBetweenStatementAndExpressionVisits() throws JSQLParserException {
        TablesNamesFinder<?> finder = new TablesNamesFinder<>();
        assertThat(finder.getTables(CCJSqlParserUtil.parse("SELECT a FROM t1")))
                .containsExactlyInAnyOrder("t1");
        assertThat(finder.getTables(CCJSqlParserUtil.parseExpression("qualified.a")))
                .containsExactlyInAnyOrder("qualified");
        assertThat(finder
                .getTables(CCJSqlParserUtil.parse("UPDATE t3 SET a=1 OUTPUT inserted.* INTO @tv")))
                .containsExactlyInAnyOrder("t3");
        assertThat(finder.getTables(CCJSqlParserUtil.parse("SELECT a FROM t4")))
                .containsExactlyInAnyOrder("t4");
    }

    @Test
    void visitsParsedSliceBounds() throws JSQLParserException {
        String sql = "f()[OFFSET((SELECT x FROM t1)):OFFSET((SELECT y FROM t2))]";
        Expression expression = CCJSqlParserUtil.parseExpression(sql);
        assertThat(expression).isInstanceOf(ArrayExpression.class);
        ArrayExpression array = (ArrayExpression) expression;
        assertThat(array.getIndexExpression()).isNull();
        assertThat(array.getStartIndexExpression()).isNotNull();
        assertThat(array.getStopIndexExpression()).isNotNull();
        assertThat(new TablesNamesFinder<>().getTables(array)).containsExactlyInAnyOrder("t1",
                "t2");
    }

    @Test
    void preservesJsonArrayIndexTraversal() throws JSQLParserException {
        Expression expression = CCJSqlParserUtil.parseExpression("f()[1:2]");
        assertThat(expression).isInstanceOf(ArrayExpression.class);
        assertThat(((ArrayExpression) expression).getIndexExpression())
                .isInstanceOf(JsonExpression.class);
        assertThat(new TablesNamesFinder<>().getTables(expression)).isEmpty();
    }

    @Test
    void findsTablesInUpsertActionCondition() throws JSQLParserException {
        Upsert upsert = (Upsert) CCJSqlParserUtil
                .parse("UPSERT INTO t1 VALUES (1) ON DUPLICATE KEY UPDATE a=(SELECT x FROM t2)");
        upsert.getDuplicateAction().setWhereExpression(
                CCJSqlParserUtil.parseCondExpression("EXISTS (SELECT 1 FROM t3)"));
        assertThat(new TablesNamesFinder<>().getTables(upsert)).containsExactlyInAnyOrder("t1",
                "t2", "t3");
    }

    @Test
    void preservesCteNamesInOtherSourcesEntryPoints() throws JSQLParserException {
        String sql = "WITH c AS (SELECT a FROM t2) FROM t1 JOIN c ON t1.a=c.a |> SELECT t1.a";
        Statement statement = CCJSqlParserUtil.parse(sql);
        assertThat(TablesNamesFinder.findTablesOrOtherSources(sql)).containsExactlyInAnyOrder("t1",
                "t2", "c");
        assertThat(new TablesNamesFinder<>().getTablesOrOtherSources(statement))
                .containsExactlyInAnyOrder("t1", "t2", "c");
    }

    @Test
    void preservesParseAndDeparse() throws JSQLParserException {
        String sql = "SELECT a[(SELECT max(x) FROM t2)] FROM t1";
        Statement statement = TestUtils.assertSqlCanBeParsedAndDeparsed(sql);
        assertThat(new TablesNamesFinder<>().getTables(statement)).containsExactlyInAnyOrder("t1",
                "t2");
    }

    private static class CountingFinder extends TablesNamesFinder<Void> {
        private final Map<String, Integer> visits = new HashMap<>();

        @Override
        public <S> Void visit(Table table, S context) {
            visits.merge(table.getFullyQualifiedName(), 1, Integer::sum);
            return super.visit(table, context);
        }
    }
}
