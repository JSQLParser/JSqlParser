/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.expression;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.AbstractJSqlParser.Dialect;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.util.deparser.ExpressionDeParser;
import net.sf.jsqlparser.util.deparser.StatementDeParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

class LambdaRoundTripTest {

    @ParameterizedTest
    @ValueSource(strings = {"SELECT arrayMap((x) -> x * 2, [1, 2])",
            "SELECT list_transform([1, 2], (x) -> x + 1)",
            "SELECT f(1, [1, 2], (x) -> CASE WHEN x > 1 THEN x ELSE 0 END)",
            "SELECT arrayMap((x, y) -> x + y, [1], [2])",
            "SELECT list_transform([1], (x) -> list_transform([2], (y) -> x + y))"})
    void preservesParameterGroupingAndNestedAst(String sql) throws JSQLParserException {
        PlainSelect select = (PlainSelect) CCJSqlParserUtil.parse(sql);
        List<LambdaExpression> before = lambdas(select);
        assertFalse(before.isEmpty());
        assertTrue(before.stream().allMatch(LambdaExpression::isParenthesized));
        StringBuilder visitorSql = new StringBuilder();
        select.accept(new StatementDeParser(visitorSql));
        for (String rendered : List.of(select.toString(), visitorSql.toString())) {
            PlainSelect reparsed = (PlainSelect) CCJSqlParserUtil.parse(rendered);
            List<LambdaExpression> after = lambdas(reparsed);
            assertEquals(before.size(), after.size(), rendered);
            for (int i = 0; i < before.size(); i++) {
                assertEquals(before.get(i).getIdentifiers(), after.get(i).getIdentifiers());
                assertEquals(before.get(i).getExpression().getClass(),
                        after.get(i).getExpression().getClass());
                assertTrue(after.get(i).isParenthesized());
            }
            assertEquals(select.toString(), reparsed.toString());
        }
    }

    @Test
    void keepsUnparenthesizedSingleParameterRendering() throws JSQLParserException {
        PlainSelect select = (PlainSelect) CCJSqlParserUtil.parse(
                "SELECT list_transform([1], x -> x + 1)");
        assertFalse(lambdas(select).get(0).isParenthesized());
        assertEquals("SELECT list_transform([1], x -> x + 1)", select.toString());
        LambdaExpression constructed = new LambdaExpression("x", new LongValue(1));
        assertEquals("x -> 1", constructed.toString());
        constructed.setParenthesized(true);
        assertEquals("( x ) -> 1", constructed.toString());
    }

    @Test
    void sharedRendererStillVisitsAndRewritesLambdaBody() throws JSQLParserException {
        PlainSelect select = (PlainSelect) CCJSqlParserUtil.parse(
                "SELECT arrayMap((x) -> x + 1, [2])");
        StringBuilder sql = new StringBuilder();
        ExpressionDeParser expressions = new ExpressionDeParser() {
            @Override
            public <S> StringBuilder visit(LongValue value, S context) {
                return getBuilder().append(value.getValue() + 10);
            }
        };
        select.accept(new StatementDeParser(expressions,
                new net.sf.jsqlparser.util.deparser.SelectDeParser(), sql));
        assertEquals("SELECT arrayMap(( x ) -> x + 11, [12])", sql.toString());
        assertEquals("SELECT arrayMap(( x ) -> x + 1, [2])", select.toString());
        assertEquals(1, lambdas((PlainSelect) CCJSqlParserUtil.parse(sql.toString())).size());
    }

    // All fixture statements execute on PostgreSQL 18.6 or MySQL 8.4.11, respectively.
    @ParameterizedTest
    @CsvFileSource(resources = "/net/sf/jsqlparser/expression/arrow-dialect-cases.tsv",
            delimiter = '\t')
    void jsonArrowRemainsJsonInEveryArgumentPosition(Dialect dialect, String sql)
            throws JSQLParserException {
        for (boolean complex : List.of(false, true)) {
            PlainSelect select = parseJson(sql, dialect, complex);
            StringBuilder visitorSql = new StringBuilder();
            select.accept(new StatementDeParser(visitorSql));
            for (String rendered : List.of(sql, select.toString(), visitorSql.toString())) {
                PlainSelect reparsed = parseJson(rendered, dialect, complex);
                assertTrue(lambdas(reparsed).isEmpty(), rendered);
                Function function = assertInstanceOf(Function.class,
                        reparsed.getSelectItem(0).getExpression());
                assertTrue(function.getParameters().stream()
                        .anyMatch(JsonExpression.class::isInstance), rendered);
            }
        }
    }

    @ParameterizedTest
    @EnumSource(value = Dialect.class, names = {"MYSQL", "POSTGRESQL"})
    void rejectsMissingJsonOperand(Dialect dialect) {
        assertThrows(JSQLParserException.class,
                () -> parseJson("SELECT COALESCE(NULL, payload ->)", dialect, true));
    }

    private static PlainSelect parseJson(String sql, Dialect dialect, boolean complex)
            throws JSQLParserException {
        return (PlainSelect) CCJSqlParserUtil.parse(sql,
                parser -> parser.withDialect(dialect).withAllowComplexParsing(complex));
    }

    private static List<LambdaExpression> lambdas(PlainSelect select) {
        List<LambdaExpression> result = new ArrayList<>();
        select.getSelectItem(0).getExpression().accept(new ExpressionVisitorAdapter<Void>() {
            @Override
            public <S> Void visit(LambdaExpression expression, S context) {
                result.add(expression);
                return super.visit(expression, context);
            }
        });
        return result;
    }
}
