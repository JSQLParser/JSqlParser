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
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.ArrayList;
import java.util.List;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.operators.relational.ParenthesedExpressionList;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.AllColumns;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.util.deparser.StatementDeParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class ColumnsTransformerTest {

    @ParameterizedTest
    @ValueSource(strings = {"*", "t.*", "COLUMNS('m')"})
    void testExceptColumnsReplacePattern(String matcher) throws JSQLParserException {
        PlainSelect select = (PlainSelect) CCJSqlParserUtil.parse(
                "SELECT " + matcher + " EXCEPT STRICT '^tmp_' FROM t");
        Expression expression = select.getSelectItem(0).getExpression();
        ColumnsTransformer transformer = getTransformer(expression);
        transformer.setExceptColumns(new ParenthesedExpressionList<>(new Column("keep")));

        assertOutput("SELECT " + matcher + " EXCEPT STRICT (keep) FROM t", select);
        assertNull(transformer.getExceptPattern());
        if (expression instanceof ColumnsExpression) {
            assertVisitedValues(List.of("m", "keep"), expression);
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"*", "t.*", "COLUMNS('m')"})
    void testExceptPatternReplacesColumns(String matcher) throws JSQLParserException {
        PlainSelect select = (PlainSelect) CCJSqlParserUtil.parse(
                "SELECT " + matcher + " EXCEPT STRICT (keep) FROM t");
        Expression expression = select.getSelectItem(0).getExpression();
        ColumnsTransformer transformer = getTransformer(expression);
        transformer.setExceptPattern(new StringValue("'^tmp_'"));

        assertOutput("SELECT " + matcher + " EXCEPT STRICT '^tmp_' FROM t", select);
        if (expression instanceof ColumnsExpression) {
            assertVisitedValues(List.of("m", "^tmp_"), expression);
        }
        assertNull(transformer.getExceptColumns());
    }

    @Test
    void testClearingPatternPreservesColumns() {
        ColumnsTransformer transformer =
                new ColumnsTransformer(ColumnsTransformer.ColumnsTransformerType.EXCEPT)
                        .setExceptColumns(new ParenthesedExpressionList<>(new Column("keep")))
                        .setExceptPattern(null);
        assertEquals("EXCEPT (keep)", transformer.toString());
    }

    @Test
    void testClearingColumnsPreservesPattern() {
        ColumnsTransformer transformer =
                new ColumnsTransformer(ColumnsTransformer.ColumnsTransformerType.EXCEPT)
                        .setExceptPattern(new StringValue("'^tmp_'"))
                        .setExceptColumns(null);
        assertEquals("EXCEPT '^tmp_'", transformer.toString());
    }

    private static ColumnsTransformer getTransformer(Expression expression) {
        return expression instanceof AllColumns
                ? ((AllColumns) expression).getTransformers().get(0)
                : ((ColumnsExpression) expression).getTransformers().get(0);
    }

    private static void assertOutput(String expected, PlainSelect select)
            throws JSQLParserException {
        assertEquals(expected, select.toString());
        StatementDeParser deparser = new StatementDeParser(new StringBuilder());
        select.accept(deparser, null);
        assertEquals(expected, deparser.getBuilder().toString());
        assertEquals(expected, CCJSqlParserUtil.parse(expected).toString());
    }

    private static void assertVisitedValues(List<String> expected, Expression expression) {
        List<String> visited = new ArrayList<>();
        expression.accept(new ExpressionVisitorAdapter<Void>() {
            @Override
            public <S> Void visit(StringValue value, S context) {
                visited.add(value.getValue());
                return null;
            }

            @Override
            public <S> Void visit(Column column, S context) {
                visited.add(column.getColumnName());
                return null;
            }
        }, null);
        assertEquals(expected, visited);
    }
}
