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
import net.sf.jsqlparser.parser.AbstractJSqlParser.Dialect;
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
    private static final String SUBQUERY = "(SELECT max(x) FROM t2)";

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
                tables("SELECT sum(x) OVER (ORDER BY (SELECT a FROM t2)) FROM t1", "t1", "t2"),
                tables("SELECT max(a) KEEP (DENSE_RANK FIRST ORDER BY (SELECT x FROM t2)) FROM t1",
                        "t1", "t2"),
                tables("SELECT max(a) KEEP (DENSE_RANK FIRST ORDER BY (SELECT x FROM t2)) OVER (PARTITION BY b) FROM t1",
                        "t1", "t2"),
                tables("SELECT array_agg(a LIMIT (SELECT x FROM t2)) FROM t1", "t1", "t2"),
                tables("SELECT any_value(a HAVING MAX (SELECT x FROM t2)) FROM t1", "t1", "t2"),
                tables("SELECT any_value(a HAVING MAX (SELECT x FROM t2)) OVER () FROM t1", "t1",
                        "t2"),
                tables("SELECT group_concat(a SEPARATOR (SELECT x FROM t2)) FROM t1", "t1", "t2"),
                tables("SELECT group_concat(a ORDER BY (SELECT x FROM t2)) FROM t1", "t1", "t2"),
                tables("SELECT f(a).g((SELECT x FROM t2)) FROM t1", "t1", "t2"),
                tables("SELECT a FROM t1 WHERE (SELECT x FROM t2) IS UNKNOWN", "t1", "t2"),
                tables("SELECT a FROM t1 WHERE MATCH (a) AGAINST ((SELECT x FROM t2))", "t1",
                        "t2"),
                tables("SELECT JSON_OBJECTAGG(KEY a VALUE (SELECT x FROM t2)) FROM t1", "t1",
                        "t2"),
                tables("SELECT JSON_ARRAYAGG(a ORDER BY (SELECT x FROM t2)) FROM t1", "t1", "t2"),
                tables("SELECT JSON_ARRAYAGG(a) OVER (PARTITION BY (SELECT x FROM t2) ORDER BY (SELECT y FROM t3)) FROM t1",
                        "t1", "t2", "t3"),
                tables("SELECT JSON_ARRAYAGG(a) OVER (ORDER BY b ROWS (SELECT x FROM t2) PRECEDING) FROM t1",
                        "t1", "t2"),
                tables("SELECT * FROM XMLTABLE('/r' PASSING (SELECT x FROM t2) COLUMNS a int PATH 'a') x",
                        "t2"),
                tables("SELECT * FROM JSON_TABLE((SELECT x FROM t2), '$' COLUMNS (a int PATH '$.a')) j",
                        "t2"),
                tables("SELECT * FROM generate_series(1, (SELECT max(x) FROM t2)) g", "t2"));
    }

    @ParameterizedTest
    @MethodSource("postgresTables")
    void findsTablesInPostgresExpressionChildren(String sql, List<String> expected)
            throws JSQLParserException {
        Statement statement = CCJSqlParserUtil.parse(sql,
                parser -> parser.withDialect(Dialect.POSTGRESQL));
        assertThat(new TablesNamesFinder<>().getTables(statement))
                .containsExactlyInAnyOrderElementsOf(expected);
        assertThat(new TablesNamesFinder<>().getTablesOrOtherSources(statement))
                .containsAll(expected);
    }

    private static Stream<Arguments> postgresTables() {
        return Stream.of(
                // aggregate ORDER BY inside the call
                tables("SELECT string_agg(a, ',' ORDER BY " + SUBQUERY + ") FROM t1", "t1",
                        "t2"),
                tables("SELECT array_agg(a ORDER BY " + SUBQUERY + ") FROM t1", "t1",
                        "t2"),
                // LIKE ... ESCAPE
                tables("SELECT a FROM t1 WHERE a LIKE 'a' ESCAPE " + SUBQUERY, "t1",
                        "t2"),
                // AT TIME ZONE, zone operand
                tables("SELECT now() AT TIME ZONE " + SUBQUERY, "t2"),
                // JSON operators, right operand
                tables("SELECT j -> " + SUBQUERY + " FROM t1", "t1", "t2"),
                tables("SELECT j ->> " + SUBQUERY + " FROM t1", "t1", "t2"),
                tables("SELECT j #> ARRAY[" + SUBQUERY + "] FROM t1", "t1", "t2"),
                tables("SELECT j #>> ARRAY[" + SUBQUERY + "] FROM t1", "t1", "t2"),
                // array slice bounds and subscript
                tables("SELECT a[1:(SELECT count(*)::int FROM t2)] FROM t1", "t1",
                        "t2"),
                tables("SELECT a[(SELECT count(*)::int FROM t2):2] FROM t1", "t1",
                        "t2"),
                tables("SELECT a[(SELECT count(*)::int FROM t2)] FROM t1", "t1",
                        "t2"),
                // positions that were already traversed
                tables("SELECT f(" + SUBQUERY + ") FROM t1", "t1", "t2"),
                tables("SELECT count(*) FILTER (WHERE a = " + SUBQUERY + ") FROM t1", "t1",
                        "t2"),
                tables("SELECT percentile_cont(0.5) WITHIN GROUP (ORDER BY " + SUBQUERY
                        + ") FROM t1", "t1", "t2"),
                tables("SELECT sum(a) OVER (PARTITION BY " + SUBQUERY + ") FROM t1", "t1",
                        "t2"),
                tables("SELECT sum(a) OVER (ORDER BY " + SUBQUERY + ") FROM t1", "t1",
                        "t2"),
                tables("SELECT sum(a) OVER (ORDER BY b ROWS BETWEEN " + SUBQUERY
                        + " PRECEDING AND CURRENT ROW) FROM t1", "t1", "t2"),
                tables("SELECT a FROM t1 LIMIT " + SUBQUERY, "t1", "t2"),
                tables("SELECT a FROM t1 OFFSET " + SUBQUERY, "t1", "t2"),
                tables("SELECT a FROM t1 FETCH FIRST " + SUBQUERY + " ROWS ONLY", "t1",
                        "t2"),
                tables("SELECT a FROM t1 WHERE a = ANY (SELECT x FROM t2)", "t1",
                        "t2"),
                tables("SELECT a FROM t1 WHERE a > ALL (SELECT x FROM t2)", "t1",
                        "t2"),
                tables("SELECT a FROM t1 WHERE a = SOME (SELECT x FROM t2)", "t1",
                        "t2"),
                tables("SELECT ARRAY(SELECT x FROM t2)", "t2"),
                tables("SELECT * FROM (VALUES (" + SUBQUERY + ")) v(x)", "t2"),
                tables("SELECT substring(a FROM " + SUBQUERY + ") FROM t1", "t1", "t2"),
                tables("SELECT trim(BOTH " + SUBQUERY + " FROM a) FROM t1", "t1", "t2"),
                tables("SELECT position(" + SUBQUERY + " IN a) FROM t1", "t1", "t2"),
                tables("SELECT overlay(a PLACING " + SUBQUERY + " FROM 1) FROM t1", "t1",
                        "t2"),
                tables("SELECT CASE WHEN a = 1 THEN " + SUBQUERY + " END FROM t1", "t1",
                        "t2"),
                tables("SELECT CAST(" + SUBQUERY + " AS text) FROM t1", "t1", "t2"),
                tables("SELECT " + SUBQUERY + "::text FROM t1", "t1", "t2"),
                tables("SELECT count(*) FROM t1 GROUP BY " + SUBQUERY, "t1", "t2"),
                tables("SELECT count(*) FROM t1 GROUP BY GROUPING SETS ((" + SUBQUERY + "))", "t1",
                        "t2"),
                tables("SELECT DISTINCT ON (" + SUBQUERY + ") a FROM t1", "t1", "t2"),
                tables("SELECT * FROM t1, LATERAL (SELECT x FROM t2) p", "t1",
                        "t2"));
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
                tables("string_agg(a, ',' ORDER BY (SELECT x FROM t2))", "t2"),
                tables("a LIKE 'a' ESCAPE (SELECT x FROM t2)", "t2"),
                tables("now() AT TIME ZONE (SELECT x FROM t2)", "t2"),
                tables("(SELECT x FROM t2) IS UNKNOWN", "t2"),
                tables("a[1]"), tables("f()[1]"), tables("f()[:2]"), tables("f()[:]"),
                tables("f(a).b.c"));
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
    void visitsAggregateChildrenOnce() throws JSQLParserException {
        CountingFinder finder = new CountingFinder();
        Statement statement = CCJSqlParserUtil.parse(
                "SELECT max(a) KEEP (DENSE_RANK FIRST ORDER BY (SELECT x FROM t2)) OVER (PARTITION BY (SELECT y FROM t3)), string_agg(a, ',' ORDER BY (SELECT z FROM t4)) FROM t1");
        assertThat(finder.getTables(statement)).containsExactlyInAnyOrder("t1", "t2", "t3",
                "t4");
        assertThat(finder.visits).isEqualTo(Map.of("t1", 1, "t2", 1, "t3", 1, "t4", 1));
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
    void traversesSliceBounds() throws JSQLParserException {
        Expression expression = CCJSqlParserUtil.parseExpression("f()[1:2]");
        assertThat(expression).isInstanceOf(ArrayExpression.class);
        ArrayExpression slice = (ArrayExpression) expression;
        assertThat(slice.getIndexExpression()).isNull();
        assertThat(slice.getStartIndexExpression()).hasToString("1");
        assertThat(slice.getStopIndexExpression()).hasToString("2");
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
