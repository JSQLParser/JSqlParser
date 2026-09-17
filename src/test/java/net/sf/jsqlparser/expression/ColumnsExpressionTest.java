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
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static net.sf.jsqlparser.test.TestUtils.assertSqlCanBeParsedAndDeparsed;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.operators.arithmetic.Addition;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.util.TablesNamesFinder;
import net.sf.jsqlparser.util.deparser.ExpressionDeParser;
import net.sf.jsqlparser.util.deparser.SelectDeParser;
import net.sf.jsqlparser.util.deparser.StatementDeParser;
import net.sf.jsqlparser.util.validation.ValidationContext;
import net.sf.jsqlparser.util.validation.validator.ExpressionValidator;
import org.junit.jupiter.api.Test;

class ColumnsExpressionTest {

    private static final String SQL = "SELECT COLUMNS('m') APPLY(x -> x + 1)"
            + " EXCEPT (skipped) REPLACE(value + 2 AS value) FROM metrics";

    @Test
    void testDeparserVisitsTransformerChildren() throws JSQLParserException {
        PlainSelect select = (PlainSelect) CCJSqlParserUtil.parse(SQL);
        StringBuilder buffer = new StringBuilder();
        List<String> columns = new ArrayList<>();
        ExpressionDeParser expressions = new ExpressionDeParser() {
            @Override
            public <S> StringBuilder visit(StringValue value, S context) {
                return getBuilder().append("'renamed'");
            }

            @Override
            public <S> StringBuilder visit(LongValue value, S context) {
                return getBuilder().append('?');
            }

            @Override
            public <S> StringBuilder visit(Column column, S context) {
                columns.add(column.getColumnName());
                return super.visit(column, context);
            }
        };
        select.accept(new StatementDeParser(expressions, new SelectDeParser(), buffer), null);

        assertEquals("SELECT COLUMNS('renamed') APPLY(x -> x + ?)"
                + " EXCEPT (skipped) REPLACE(value + ? AS value) FROM metrics", buffer.toString());
        assertEquals(List.of("x", "skipped", "value"), columns);
        assertEquals(SQL, select.toString());
        assertSqlCanBeParsedAndDeparsed(SQL, true);
    }

    @Test
    void testAdapterVisitsEveryTransformerInOrder() throws JSQLParserException {
        PlainSelect select = (PlainSelect) CCJSqlParserUtil.parse(SQL);
        ColumnsExpression expression = assertInstanceOf(ColumnsExpression.class,
                select.getSelectItems().get(0).getExpression());
        List<String> visited = new ArrayList<>();
        Object marker = new Object();
        expression.accept(new ExpressionVisitorAdapter<Void>() {
            @Override
            public <S> Void visit(StringValue value, S context) {
                assertSame(marker, context);
                visited.add(value.getValue());
                return null;
            }

            @Override
            public <S> Void visit(Column column, S context) {
                assertSame(marker, context);
                visited.add(column.getColumnName());
                return null;
            }
        }, marker);
        assertEquals(List.of("m", "x", "skipped", "value"), visited);
    }

    @Test
    void testAdapterPreservesCollectedResult() throws JSQLParserException {
        PlainSelect select = (PlainSelect) CCJSqlParserUtil.parse(
                "SELECT COLUMNS('m') EXCEPT (skipped) FROM metrics");
        Expression expression = select.getSelectItems().get(0).getExpression();
        ExpressionVisitorAdapter<List<String>> collector = new ExpressionVisitorAdapter<>() {
            @Override
            protected <S> List<String> applyExpression(Expression value, S context) {
                return List.of(value.toString());
            }

            @Override
            protected <S> List<String> visitExpressions(Expression value, S context,
                    Collection<Expression> children) {
                List<String> result = new ArrayList<>();
                for (Expression child : children) {
                    if (child != null) {
                        result.addAll(child.accept(this, context));
                    }
                }
                return result;
            }
        };
        assertEquals(List.of("'m'", "skipped"), expression.accept(collector, null));
    }

    @Test
    void testVisitorDefaultDispatchesChildren() throws JSQLParserException {
        PlainSelect select = (PlainSelect) CCJSqlParserUtil.parse(SQL);
        ColumnsExpression expression = assertInstanceOf(ColumnsExpression.class,
                select.getSelectItems().get(0).getExpression());
        ExpressionVisitor<Void> visitor = mock(ExpressionVisitor.class, CALLS_REAL_METHODS);
        Object context = new Object();
        expression.accept(visitor, context);

        verify(visitor).visit((Function) expression.getColumns(), context);
        verify(visitor).visit((LambdaExpression) expression.getTransformers().get(0)
                .getApplyExpression(), context);
        verify(visitor).visit(expression.getTransformers().get(1).getExceptColumns().get(0),
                context);
        verify(visitor).visit(
                (Addition) expression
                        .getTransformers().get(2).getReplaceItems().get(0).getExpression(),
                context);
    }

    @Test
    void testValidatorVisitsExceptColumns() throws JSQLParserException {
        PlainSelect select = (PlainSelect) CCJSqlParserUtil.parse(SQL);
        ColumnsExpression expression = assertInstanceOf(ColumnsExpression.class,
                select.getSelectItems().get(0).getExpression());
        List<String> visited = new ArrayList<>();
        ExpressionValidator validator = new ExpressionValidator() {
            @Override
            public <S> Void visit(Column column, S context) {
                visited.add(column.getColumnName());
                return null;
            }
        };
        validator.setContext(new ValidationContext().setCapabilities(Collections.emptyList()));
        expression.accept(validator, null);
        assertEquals(List.of("x", "skipped", "value"), visited);
    }

    @Test
    void testFinderVisitsExceptColumns() throws JSQLParserException {
        List<String> visited = new ArrayList<>();
        TablesNamesFinder<Void> finder = new TablesNamesFinder<>() {
            @Override
            public <S> Void visit(Column column, S context) {
                visited.add(column.getColumnName());
                return super.visit(column, context);
            }
        };
        assertEquals(Set.of("metrics"), finder.getTables(CCJSqlParserUtil.parse(SQL)));
        assertEquals(List.of("x", "skipped", "value"), visited);
    }

    @Test
    void testAstTokenRangeIncludesMatcher() throws JSQLParserException {
        PlainSelect select = (PlainSelect) CCJSqlParserUtil.parse(
                "SELECT COLUMNS('m') APPLY(sum) AS result FROM metrics");
        ColumnsExpression expression = assertInstanceOf(ColumnsExpression.class,
                select.getSelectItem(0).getExpression());
        assertEquals("COLUMNS", expression.getASTNode().jjtGetFirstToken().image);
        assertEquals(")", expression.getASTNode().jjtGetLastToken().image);
        assertEquals("AS", expression.getASTNode().jjtGetLastToken().next.image);
        assertEquals("COLUMNS", expression.getColumns().getASTNode().jjtGetFirstToken().image);
        assertEquals("APPLY", expression.getColumns().getASTNode().jjtGetLastToken().next.image);
        assertSame(expression, ((Function) expression.getColumns()).getParent());
        assertSame(expression, ((Function) expression.getColumns())
                .getParent(ColumnsExpression.class));
    }

    @Test
    void testFinderVisitsTransformerSubqueries() throws JSQLParserException {
        String sql = "SELECT COLUMNS('m') APPLY(x -> (SELECT max(v) FROM applied))"
                + " REPLACE((SELECT v FROM replacement) AS c) FROM metrics";
        assertEquals(Set.of("metrics", "applied", "replacement"),
                TablesNamesFinder.findTables(sql));
        assertSqlCanBeParsedAndDeparsed(sql, true);
    }
}
