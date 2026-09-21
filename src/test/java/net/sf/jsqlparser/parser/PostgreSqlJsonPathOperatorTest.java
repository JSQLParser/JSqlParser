/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.parser;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.JdbcParameter;
import net.sf.jsqlparser.expression.JsonExpression;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.operators.arithmetic.Concat;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.IsBooleanExpression;
import net.sf.jsqlparser.expression.operators.relational.JsonOperator;
import net.sf.jsqlparser.parser.AbstractJSqlParser.Dialect;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.util.deparser.ExpressionDeParser;
import net.sf.jsqlparser.util.deparser.SelectDeParser;
import net.sf.jsqlparser.util.deparser.StatementDeParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PostgreSqlJsonPathOperatorTest {
    private PlainSelect parse(String sql) throws Exception {
        return (PlainSelect) CCJSqlParserUtil.parse(sql, p -> p.withDialect(Dialect.POSTGRESQL));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "SELECT '{\"a\":1}'::jsonb #- '{a}'",
            "SELECT doc#-'{a}' FROM events",
            "SELECT doc/*comment*/#-ARRAY['a', '0'] FROM events",
            "SELECT doc@?'$.a[*] ? (@ > 2)' FROM events",
            "SELECT NULL::jsonb @? '$.a'",
            "SELECT doc #- '{a}' #- '{b}' FROM events"
    })
    void buildsJsonOperatorsAndPreservesOutput(String sql) throws Exception {
        PlainSelect select = parse(sql);
        JsonOperator operator =
                assertInstanceOf(JsonOperator.class, select.getSelectItem(0).getExpression());
        assertEquals(sql.contains("@?") ? "@?" : "#-", operator.getStringExpression());
        StringBuilder output = new StringBuilder();
        select.accept(new StatementDeParser(output), null);
        assertEquals(select.toString(), output.toString());
        assertEquals(select.toString(), parse(output.toString()).toString());
        assertFalse(output.toString().contains("# -"));
    }

    @Test
    void preservesAssociativityAndComparisonPrecedence() throws Exception {
        PlainSelect select = parse("SELECT doc #- '{a}' #- '{b}' = '{}'::jsonb FROM events");
        EqualsTo comparison =
                assertInstanceOf(EqualsTo.class, select.getSelectItem(0).getExpression());
        JsonOperator deletion =
                assertInstanceOf(JsonOperator.class, comparison.getLeftExpression());
        assertEquals("#-", deletion.getStringExpression());
        assertInstanceOf(JsonOperator.class, deletion.getLeftExpression());
        IsBooleanExpression predicate = assertInstanceOf(IsBooleanExpression.class,
                parse("SELECT doc #- '{a}' @? '$.b' IS TRUE FROM events")
                        .getSelectItem(0).getExpression());
        JsonOperator exists = assertInstanceOf(JsonOperator.class, predicate.getLeftExpression());
        assertEquals("@?", exists.getStringExpression());
        assertInstanceOf(JsonOperator.class, exists.getLeftExpression());
    }

    @Test
    void sharesLeftAssociativityWithPostgresqlConcatenation() throws Exception {
        Concat concat = assertInstanceOf(Concat.class,
                parse("SELECT doc #- '{a}' || other FROM events").getSelectItem(0).getExpression());
        assertInstanceOf(JsonOperator.class, concat.getLeftExpression());
        JsonOperator deletion = assertInstanceOf(JsonOperator.class,
                parse("SELECT doc || other #- '{a}' FROM events").getSelectItem(0).getExpression());
        assertInstanceOf(Concat.class, deletion.getLeftExpression());
    }

    @Test
    void composesAccessAndPathOperatorsInBothParserModes() throws Exception {
        for (boolean complex : List.of(false, true)) {
            for (String operator : List.of("->", "#>")) {
                CCJSqlParser parser = CCJSqlParserUtil.newParser(
                        "SELECT doc " + operator + " '{a}' #- '{b}' FROM events")
                        .withDialect(Dialect.POSTGRESQL).withAllowComplexParsing(complex);
                JsonOperator deletion = assertInstanceOf(JsonOperator.class,
                        ((PlainSelect) parser.Statement()).getSelectItem(0).getExpression());
                JsonExpression access =
                        assertInstanceOf(JsonExpression.class, deletion.getLeftExpression());
                assertInstanceOf(StringValue.class, access.getIdentList().get(0).getKey());
            }
            CCJSqlParser parser = CCJSqlParserUtil.newParser(
                    "SELECT doc #- '{a}' ->> 'b' = 'x' FROM events")
                    .withDialect(Dialect.POSTGRESQL).withAllowComplexParsing(complex);
            EqualsTo comparison = assertInstanceOf(EqualsTo.class,
                    ((PlainSelect) parser.Statement()).getSelectItem(0).getExpression());
            JsonExpression access =
                    assertInstanceOf(JsonExpression.class, comparison.getLeftExpression());
            assertInstanceOf(JsonOperator.class, access.getExpression());
            assertInstanceOf(StringValue.class, access.getIdentList().get(0).getKey());
        }
    }

    @Test
    void distinguishesPathOperatorFromParametersAndVisitsOperands() throws Exception {
        PlainSelect select = parse("SELECT doc @? ? FROM events WHERE doc #- ? = ?::jsonb");
        JsonOperator exists = (JsonOperator) select.getSelectItem(0).getExpression();
        assertEquals(1, ((JdbcParameter) exists.getRightExpression()).getIndex());
        EqualsTo comparison = (EqualsTo) select.getWhere();
        JsonOperator deletion = (JsonOperator) comparison.getLeftExpression();
        assertEquals(2, ((JdbcParameter) deletion.getRightExpression()).getIndex());

        List<String> visited = new ArrayList<>();
        ExpressionDeParser expressions = new ExpressionDeParser() {
            @Override
            public <S> StringBuilder visit(StringValue value, S context) {
                visited.add(value.getValue());
                return getBuilder().append("'changed'");
            }
        };
        StringBuilder output = new StringBuilder();
        parse("SELECT doc #- '{a}' @? '$.b' FROM events")
                .accept(new StatementDeParser(expressions, new SelectDeParser(), output), null);
        assertEquals(List.of("{a}", "$.b"), visited);
        assertEquals("SELECT doc #- 'changed' @? 'changed' FROM events", output.toString());
    }

    @Test
    void retainsExistingHashAndJsonOperators() throws Exception {
        for (String sql : List.of("SELECT 5 # -1", "SELECT doc #> '{a}' FROM events",
                "SELECT doc #>> '{a}' FROM events", "SELECT doc ? 'a' FROM events",
                "SELECT doc @@ '$.a > 2' FROM events")) {
            assertEquals(parse(sql).toString(), parse(parse(sql).toString()).toString());
        }
        Expression expression = parse("SELECT doc #- '{a}' FROM events")
                .getSelectItem(0).getExpression();
        ((JsonOperator) expression).setRightExpression(new StringValue("{b}"));
        assertEquals("doc #- '{b}'", expression.toString());
    }

    @ParameterizedTest
    @ValueSource(strings = {"SELECT '{}'::jsonb #-", "SELECT '{}'::jsonb @?",
            "SELECT '{}'::jsonb @? , 1"})
    void requiresRightOperand(String sql) {
        assertThrows(net.sf.jsqlparser.JSQLParserException.class, () -> parse(sql));
    }

    @Test
    void retainsAtSignIdentifierBehaviorInOtherDialects() throws Exception {
        for (Dialect dialect : List.of(Dialect.SQLSERVER, Dialect.MYSQL)) {
            PlainSelect select = (PlainSelect) CCJSqlParserUtil.parse("SELECT name@host FROM users",
                    p -> p.withDialect(dialect));
            assertEquals("name@host", select.getSelectItem(0).getExpression().toString());
        }
        CCJSqlParser parser = CCJSqlParserUtil.newParser("doc@?'$.a'")
                .withDialect(Dialect.POSTGRESQL);
        assertEquals("doc", parser.getNextToken().image);
        Token operator = parser.getNextToken();
        assertEquals("@?", operator.image);
        assertEquals(4, operator.beginColumn);
        assertEquals(5, operator.endColumn);
    }
}
