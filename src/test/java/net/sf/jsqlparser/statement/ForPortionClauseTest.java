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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.ExpressionVisitorAdapter;
import net.sf.jsqlparser.expression.JdbcParameter;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.parser.feature.Feature;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.update.Update;
import net.sf.jsqlparser.test.TestUtils;
import net.sf.jsqlparser.util.TablesNamesFinder;
import net.sf.jsqlparser.util.deparser.ExpressionDeParser;
import net.sf.jsqlparser.util.deparser.SelectDeParser;
import net.sf.jsqlparser.util.deparser.StatementDeParser;
import net.sf.jsqlparser.util.validation.ValidationTestAsserts;
import net.sf.jsqlparser.util.validation.feature.FeaturesAllowed;
import net.sf.jsqlparser.util.validation.feature.MariaDbVersion;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class ForPortionClauseTest extends ValidationTestAsserts {
    @ParameterizedTest
    @ValueSource(strings = {
            "UPDATE prices FOR PORTION OF VALID_TIME FROM '2020-01-01' TO '2021-01-01' SET price = 10 WHERE id = 1",
            "DELETE FROM prices FOR PORTION OF VALID_TIME FROM '2020-01-01' TO '2021-01-01' WHERE id = 1",
            "UPDATE prices p FOR PORTION OF valid_time FROM ? TO ? SET price = ? WHERE id = ?",
            "DELETE FROM prices AS p FOR PORTION OF \"valid time\" FROM :start_date TO :end_date WHERE id = :id",
            "UPDATE prices FOR PORTION OF p FROM DATE '2020-01-01' TO DATE '2021-01-01' SET price = 10",
            "DELETE FROM prices FOR PORTION OF p FROM CAST(? AS DATE) TO CAST(? AS DATE) ORDER BY id LIMIT 1",
            "UPDATE prices FOR PORTION OF p FROM (1 + 2) TO COALESCE(3, 4) SET price = 10 RETURNING id",
            "DELETE FROM prices FOR PORTION OF p FROM 1 TO 2 RETURNING id",
            "WITH ids AS (SELECT id FROM selected) UPDATE prices FOR PORTION OF p FROM 1 TO 2 SET price = 10 WHERE id IN (SELECT id FROM ids)"})
    void preservesTheIntervalThroughBothStatementRenderers(String sql) throws Exception {
        Statement statement = TestUtils.assertSqlCanBeParsedAndDeparsed(sql);
        ForPortionClause clause = portion(statement);
        assertEquals(clause.toString(),
                portion(CCJSqlParserUtil.parse(statement.toString())).toString());
    }

    @Test
    void exposesTypedBoundsAndPreservesGlobalParameterOrder() throws Exception {
        Update update = (Update) CCJSqlParserUtil.parse(
                "UPDATE prices FOR PORTION OF p FROM ? TO ? SET price = ? WHERE id = ?");
        ForPortionClause clause = update.getForPortionClause();
        assertEquals("p", clause.getPeriodName());
        assertEquals(1, ((JdbcParameter) clause.getFromExpression()).getIndex());
        assertEquals(2, ((JdbcParameter) clause.getToExpression()).getIndex());
        assertEquals(3,
                ((JdbcParameter) update.getUpdateSets().get(0).getValues().get(0)).getIndex());
        clause.setPeriodName("valid_time");
        clause.setFromExpression(new StringValue("2020-01-01"));
        clause.setToExpression(new StringValue("2021-01-01"));
        assertInstanceOf(StringValue.class, clause.getFromExpression());
        assertEquals(
                "UPDATE prices FOR PORTION OF valid_time FROM '2020-01-01' TO '2021-01-01' SET price = ? WHERE id = ?",
                update.toString());
        Delete delete = (Delete) CCJSqlParserUtil.parse("DELETE FROM prices");
        assertNull(delete.getForPortionClause());
        assertSame(delete, delete.withForPortionClause(clause));
        assertEquals("DELETE FROM prices " + clause, delete.toString());
        update.setForPortionClause(null);
        assertEquals("UPDATE prices SET price = ? WHERE id = ?", update.toString());
    }

    @ParameterizedTest
    @ValueSource(strings = {"UPDATE prices FOR PORTION OF p FROM 1 TO 2 SET price = 3 WHERE id = 4",
            "DELETE FROM prices FOR PORTION OF p FROM 1 TO 2 WHERE id = 4"})
    void customVisitorsSeeEachBoundExactlyOnce(String sql) throws Exception {
        Statement statement = CCJSqlParserUtil.parse(sql);
        Object context = new Object();
        List<Long> bounds = new ArrayList<>();
        portion(statement).accept(new ExpressionVisitorAdapter<Void>() {
            @Override
            public <S> Void visit(LongValue value, S actualContext) {
                assertSame(context, actualContext);
                bounds.add(value.getValue());
                return null;
            }
        }, context);
        assertEquals(List.of(1L, 2L), bounds);
        List<Long> rendered = new ArrayList<>();
        StringBuilder output = new StringBuilder();
        ExpressionDeParser expressions = new ExpressionDeParser() {
            @Override
            public <S> StringBuilder visit(LongValue value, S actualContext) {
                rendered.add(value.getValue());
                return getBuilder().append(value.getValue() + 100);
            }
        };
        statement.accept(new StatementDeParser(expressions, new SelectDeParser(), output), null);
        assertEquals(statement instanceof Update ? List.of(1L, 2L, 3L, 4L) : List.of(1L, 2L, 4L),
                rendered);
        String expected = sql.replace("FROM 1 TO 2", "FROM 101 TO 102")
                .replace("price = 3", "price = 103").replace("id = 4", "id = 104");
        assertEquals(expected, output.toString());
        assertEquals(sql, statement.toString());
        TestUtils.assertSqlCanBeParsedAndDeparsed(output.toString());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "UPDATE prices FOR PORTION OF p FROM (SELECT MIN(ts) FROM starts) TO (SELECT MAX(ts) FROM ends) SET price = 10",
            "DELETE FROM prices FOR PORTION OF p FROM (SELECT MIN(ts) FROM starts) TO (SELECT MAX(ts) FROM ends)"})
    void tableDiscoveryTraversesBothBoundExpressions(String sql) throws Exception {
        assertEquals(Set.of("prices", "starts", "ends"), TablesNamesFinder.findTables(sql));
    }

    @ParameterizedTest
    @ValueSource(strings = {"UPDATE prices FOR PORTION OF p FROM ? TO ? SET price = 10",
            "DELETE FROM prices FOR PORTION OF p FROM ? TO ?",
            "UPDATE prices FOR PORTION OF p FROM ? TO 2 SET price = 10",
            "UPDATE prices FOR PORTION OF p FROM 1 TO ? SET price = 10",
            "DELETE FROM prices FOR PORTION OF p FROM ? TO 2",
            "DELETE FROM prices FOR PORTION OF p FROM 1 TO ?"})
    void validatesTheClauseFeatureAndBothBoundExpressions(String sql) throws Exception {
        validateNoErrors(sql, 1,
                new FeaturesAllowed().add(FeaturesAllowed.DML).add(Feature.jdbcParameter));
        validateNoErrors(sql, 1, MariaDbVersion.V10_5_4);
        validateNotAllowed(sql, 1, 1,
                new FeaturesAllowed().add(FeaturesAllowed.DML).add(Feature.jdbcParameter)
                        .remove(Feature.forPortion),
                Feature.forPortion);
        validateNotAllowed(sql, 1, 1,
                new FeaturesAllowed().add(FeaturesAllowed.DML).remove(Feature.jdbcParameter),
                Feature.jdbcParameter);
    }

    @ParameterizedTest
    @ValueSource(strings = {"UPDATE prices FOR PORTION p FROM 1 TO 2 SET price = 10",
            "DELETE FROM prices FOR PORTION OF p FROM 1",
            "DELETE FROM prices FOR PORTION OF p FROM 1 TO",
            "DELETE FROM prices FOR PORTION OF p TO 2",
            "UPDATE prices FOR PORTION OF p FROM 1 TO 2",
            "DELETE FROM prices FOR PORTION OF p FROM 1 TO 2 garbage"})
    void rejectsIncompleteClausesAndTrailingInput(String sql) {
        assertThrows(JSQLParserException.class, () -> CCJSqlParserUtil.parse(sql));
    }

    @ParameterizedTest
    @ValueSource(strings = {"UPDATE portion SET valid_time = 1",
            "DELETE FROM portion WHERE valid_time = 1",
            "SELECT portion, valid_time FROM prices FOR UPDATE"})
    void preservesOrdinaryIdentifiersAndForUpdate(String sql) throws Exception {
        TestUtils.assertSqlCanBeParsedAndDeparsed(sql);
    }

    private static ForPortionClause portion(Statement statement) {
        return statement instanceof Update ? ((Update) statement).getForPortionClause()
                : ((Delete) statement).getForPortionClause();
    }
}
