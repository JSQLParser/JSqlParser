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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.Set;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.parser.feature.Feature;
import net.sf.jsqlparser.test.TestUtils;
import net.sf.jsqlparser.util.TablesNamesFinder;
import net.sf.jsqlparser.util.deparser.ExpressionDeParser;
import net.sf.jsqlparser.util.deparser.SelectDeParser;
import net.sf.jsqlparser.util.validation.ValidationTestAsserts;
import net.sf.jsqlparser.util.validation.feature.FeaturesAllowed;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class AsOfJoinTest extends ValidationTestAsserts {
    @ParameterizedTest
    @ValueSource(strings = {"ASOF JOIN", "ASOF INNER JOIN", "ASOF LEFT JOIN",
            "ASOF LEFT OUTER JOIN", "ASOF RIGHT JOIN", "ASOF FULL OUTER JOIN"})
    void preservesAsOfAndOrdinaryJoinQualifiers(String joinType) throws Exception {
        String sql = "SELECT * FROM trades t " + joinType
                + " prices p ON t.symbol = p.symbol AND t.ts >= p.ts";
        PlainSelect select = (PlainSelect) TestUtils.assertSqlCanBeParsedAndDeparsed(sql);
        Join join = select.getJoins().get(0);
        assertTrue(join.isAsOf());
        assertEquals(joinType.contains("LEFT"), join.isLeft());
        assertEquals(joinType.contains("OUTER"), join.isOuter());
        assertTrue(((PlainSelect) CCJSqlParserUtil.parse(select.toString())).getJoins().get(0)
                .isAsOf());
        assertEquals(Set.of("trades", "prices"), TablesNamesFinder.findTables(sql));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "SELECT * FROM trades ASOF JOIN prices USING (symbol, ts)",
            "SELECT * FROM trades ASOF /* nearest */ LEFT JOIN prices USING (symbol, ts)",
            "SELECT * FROM (SELECT * FROM trades) ASOF JOIN prices ON trades.ts >= prices.ts",
            "SELECT * FROM trades t ASOF JOIN prices p ON t.ts >= p.ts JOIN symbols s ON s.id = t.id",
            "FROM trades t |> ASOF JOIN prices p ON t.ts >= p.ts |> SELECT *"})
    void worksWithoutAliasesAndWithinExistingJoinPaths(String sql) throws Exception {
        TestUtils.assertSqlCanBeParsedAndDeparsed(sql, true);
    }

    @Test
    void keepsUsingColumnsStructuredAndAllowsChangingTheJoinKind() throws Exception {
        PlainSelect select = (PlainSelect) CCJSqlParserUtil.parse(
                "SELECT * FROM trades ASOF LEFT JOIN prices USING (symbol, ts)");
        Join join = select.getJoins().get(0);
        assertEquals("symbol", join.getUsingColumns().get(0).getColumnName());
        assertEquals("ts", join.getUsingColumns().get(1).getColumnName());
        join.setAsOf(false);
        assertEquals("SELECT * FROM trades LEFT JOIN prices USING (symbol, ts)", select.toString());
        assertTrue(join.withAsOf(true).isAsOf());
    }

    @Test
    void visitsPredicatesThroughTheConfiguredDeparser() throws Exception {
        PlainSelect select = (PlainSelect) CCJSqlParserUtil.parse(
                "SELECT * FROM trades t ASOF LEFT JOIN prices p ON t.ts >= p.ts + 1");
        StringBuilder output = new StringBuilder();
        int[] visits = {0};
        ExpressionDeParser expressions = new ExpressionDeParser(null, output) {
            @Override
            public <S> StringBuilder visit(LongValue value, S context) {
                visits[0]++;
                return getBuilder().append(value.getValue() + 100);
            }
        };
        SelectDeParser deparser = new SelectDeParser(expressions, output);
        expressions.setSelectVisitor(deparser);
        select.accept((SelectVisitor<StringBuilder>) deparser, null);
        assertEquals("SELECT * FROM trades t ASOF LEFT JOIN prices p ON t.ts >= p.ts + 101",
                output.toString());
        assertEquals(1, visits[0]);
        assertTrue(select.toString().endsWith("p.ts + 1"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"SELECT asof FROM trades", "SELECT * FROM asof",
            "SELECT * FROM trades asof WHERE asof.ts > 0",
            "SELECT * FROM trades AS asof JOIN prices p ON asof.ts = p.ts",
            "SELECT * FROM trades \"ASOF\" JOIN prices p ON \"ASOF\".ts = p.ts"})
    void keepsAsOfUsableAsAnIdentifier(String sql) throws Exception {
        PlainSelect select = (PlainSelect) TestUtils.assertSqlCanBeParsedAndDeparsed(sql);
        if (select.getJoins() != null) {
            assertFalse(select.getJoins().get(0).isAsOf());
        }
    }

    @Test
    void validatesTheAsOfFeatureAndPredicateExpressions() throws Exception {
        String sql = "SELECT * FROM trades t ASOF JOIN prices p ON t.ts >= p.ts";
        validateNoErrors(sql, 1, FeaturesAllowed.SELECT);
        validateNotAllowed(sql, 1, 1,
                new FeaturesAllowed().add(FeaturesAllowed.SELECT).remove(Feature.joinAsOf),
                Feature.joinAsOf);
        validateNotAllowed(sql + " + ?", 1, 1,
                new FeaturesAllowed().add(FeaturesAllowed.SELECT).remove(Feature.jdbcParameter),
                Feature.jdbcParameter);
    }
}
