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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.util.ArrayList;
import java.util.List;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.ExpressionVisitorAdapter;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.expression.JsonTableFunction;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.StatementFeatureVisitor;
import net.sf.jsqlparser.statement.StatementFeatures;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class FromItemVisitorAdapterTest {

    @Test
    void visitsEveryRowsFromFunctionWithTheSameContext() throws JSQLParserException {
        PlainSelect select = parse("SELECT * FROM ROWS FROM (f(source_col), g(other_col)) "
                + "WITH ORDINALITY AS rows");
        RecordingExpressions expressions = new RecordingExpressions();

        select.getFromItem().accept(new FromItemVisitorAdapter<>(expressions),
                expressions.context);

        assertEquals(List.of("f", "g"), expressions.functions);
        assertEquals(List.of("source_col", "other_col"), expressions.columns);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "SELECT * FROM f(g(source_col))",
            "SELECT * FROM (SELECT * FROM f(g(source_col))) nested",
            "SELECT * FROM base JOIN f(g(source_col)) AS derived ON 1 = 1"
    })
    void selectAdapterUsesTheSuppliedExpressionVisitorForTableFunctions(String sql)
            throws JSQLParserException {
        RecordingExpressions expressions = new RecordingExpressions();
        SelectVisitorAdapter<Void> visitor = new SelectVisitorAdapter<>(expressions);

        parse(sql).accept(visitor, expressions.context);

        assertEquals(List.of("f", "g"), expressions.functions);
        assertEquals(List.of("source_col"), expressions.columns);
    }

    @Test
    void dispatchesJsonTableAndVisitsItsNestedExpressions() throws JSQLParserException {
        PlainSelect select = parse("SELECT * FROM JSON_TABLE(payload, '$' "
                + "PASSING filter_expr AS filter COLUMNS ("
                + "id INT PATH '$.id' DEFAULT fallback_value ON EMPTY, "
                + "NESTED PATH '$.items[*]' COLUMNS ("
                + "value INT PATH '$.value' DEFAULT nested_default ON ERROR))) AS jt");
        RecordingExpressions expressions = new RecordingExpressions();

        select.getFromItem().accept(new FromItemVisitorAdapter<>(expressions),
                expressions.context);

        assertEquals(1, expressions.jsonTables.size());
        assertSame(((TableFunction) select.getFromItem()).getFunction(),
                expressions.jsonTables.get(0));
        assertEquals(List.of("payload", "filter_expr", "fallback_value", "nested_default"),
                expressions.columns);
    }

    @Test
    void traversesSubqueriesInFunctionArgumentsThroughTheConfiguredVisitorChain()
            throws JSQLParserException {
        RecordingExpressions expressions = new RecordingExpressions();
        SelectVisitorAdapter<Void> visitor = new SelectVisitorAdapter<>(expressions);
        expressions.setSelectVisitor(visitor);

        parse("SELECT * FROM f((SELECT selected_col FROM g(argument_col)))")
                .accept(visitor, expressions.context);

        assertEquals(List.of("f", "g"), expressions.functions);
        assertEquals(List.of("selected_col", "argument_col"), expressions.columns);
    }

    @Test
    void emptyTableFunctionDoesNotInvokeTheExpressionVisitor() {
        RecordingExpressions expressions = new RecordingExpressions();
        TableFunction tableFunction = new TableFunction((Function) null);

        assertNull(tableFunction.accept(new FromItemVisitorAdapter<>(expressions),
                expressions.context));
        assertEquals(List.of(), expressions.functions);
    }

    @Test
    void featureAnalysisChecksEachTableFunctionOnce() throws JSQLParserException {
        List<String> checkedFunctions = new ArrayList<>();
        StatementFeatures features = StatementFeatureVisitor.analyse(
                CCJSqlParserUtil.parse("SELECT * FROM ROWS FROM (f(g(1)), h(2))"), name -> {
                    checkedFunctions.add(name);
                    return true;
                });

        assertEquals(List.of("f", "g", "h"), checkedFunctions);
        assertFalse(features.mayModifyData());
    }

    private static PlainSelect parse(String sql) throws JSQLParserException {
        return ((Select) CCJSqlParserUtil.parse(sql)).getPlainSelect();
    }

    private static final class RecordingExpressions extends ExpressionVisitorAdapter<Void> {
        private final Object context = new Object();
        private final List<String> columns = new ArrayList<>();
        private final List<String> functions = new ArrayList<>();
        private final List<JsonTableFunction> jsonTables = new ArrayList<>();

        @Override
        public <S> Void visit(Column column, S context) {
            assertSame(this.context, context);
            columns.add(column.getColumnName());
            return super.visit(column, context);
        }

        @Override
        public <S> Void visit(Function function, S context) {
            assertSame(this.context, context);
            functions.add(function.getName());
            return super.visit(function, context);
        }

        @Override
        public <S> Void visit(JsonTableFunction function, S context) {
            assertSame(this.context, context);
            jsonTables.add(function);
            return super.visit(function, context);
        }
    }
}
