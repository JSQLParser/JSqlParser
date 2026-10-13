/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.util.deparser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.JsonFunction;
import net.sf.jsqlparser.expression.JsonFunctionExpression;
import net.sf.jsqlparser.expression.JsonFunctionType;
import net.sf.jsqlparser.expression.JsonKeyValuePair;
import net.sf.jsqlparser.expression.JsonKeyValuePairSeparator;
import net.sf.jsqlparser.expression.JsonTableFunction;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.parser.AbstractJSqlParser.Dialect;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.Statement;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class JsonExpressionRenderingTest {
    private static final Object CONTEXT = new Object();

    @ParameterizedTest
    @ValueSource(strings = {
            "JSON_VALUE(doc, '$.a' RETURNING CHAR(20) DEFAULT 'old' ON EMPTY DEFAULT 'old' ON ERROR)",
            "JSON_QUERY(doc, '$.a' PASSING filter_value RETURNING VARCHAR(20) "
                    + "FORMAT JSON WITH ARRAY WRAPPER EMPTY ARRAY ON EMPTY ERROR ON ERROR)",
            "JSON_EXISTS(doc, '$.a' PASSING filter_value UNKNOWN ON ERROR)",
            "JSON_ARRAY(doc, JSON_VALUE(other_doc, '$.a') FORMAT JSON)",
            "JSON_OBJECT(KEY key_column VALUE JSON_VALUE(doc, '$.a') FORMAT JSON)"
    })
    void rewritesScalarChildrenWithoutMutatingTheAst(String sql) throws Exception {
        Expression expression = CCJSqlParserUtil.parseExpression(sql);
        String before = expression.toString();
        RecordingDeParser printer = new RecordingDeParser();
        expression.accept(printer, CONTEXT);
        String output = printer.getBuilder().toString();
        assertEquals(before.replace("key_column", "r_key_column")
                .replace("other_doc", "other_payload").replace("doc", "payload")
                .replace("filter_value", "r_filter_value").replace("'$.a'", "'$.b'")
                .replace("'old'", "'new'"), output);
        assertEquals(output, CCJSqlParserUtil.parseExpression(output).toString());
        assertEquals(before, expression.toString());
        assertTrue(printer.columns.contains("doc"));
        assertTrue(printer.strings.contains("$.a"));
    }

    @Test
    void rewritesMySqlGeneratedColumnExpressions() throws Exception {
        String sql = "CREATE TABLE t (doc JSON, payload JSON, value VARCHAR(20) AS "
                + "(JSON_VALUE(doc, '$.a' RETURNING CHAR(20) DEFAULT 'old' ON EMPTY)) STORED)";
        Statement statement = CCJSqlParserUtil.parse(sql, p -> p.withDialect(Dialect.MYSQL));
        String before = statement.toString();
        RecordingDeParser printer = new RecordingDeParser(null);
        statement.accept(new StatementDeParser(printer, new SelectDeParser(), printer.getBuilder()),
                null);
        String output = printer.getBuilder().toString();
        assertEquals(before.replace("JSON_VALUE(doc", "JSON_VALUE(payload")
                .replace("'$.a'", "'$.b'").replace("'old'", "'new'"), output);
        assertEquals(List.of("doc"), printer.columns);
        assertEquals(output, CCJSqlParserUtil.parse(output,
                p -> p.withDialect(Dialect.MYSQL)).toString());
        assertEquals(before, statement.toString());
    }

    @Test
    void rewritesNestedTablePathsDefaultsAndPassingValuesInSourceOrder() throws Exception {
        String sql = "SELECT * FROM JSON_TABLE(doc, '$.a' AS root "
                + "PASSING filter_value AS filter COLUMNS ("
                + "ord FOR ORDINALITY, value VARCHAR(20) PATH '$.a' "
                + "DEFAULT 'old' ON ERROR DEFAULT 'old' ON EMPTY, "
                + "NESTED PATH '$.a' AS nested COLUMNS (present INT EXISTS PATH '$.a')) "
                + "PLAN (root OUTER nested) EMPTY ON ERROR) AS jt";
        Statement statement = CCJSqlParserUtil.parse(sql);
        String before = statement.toString();
        RecordingDeParser printer = new RecordingDeParser();
        statement.accept(new StatementDeParser(printer, new SelectDeParser(), printer.getBuilder()),
                CONTEXT);
        String output = printer.getBuilder().toString();
        assertEquals(before.replace("JSON_TABLE(doc", "JSON_TABLE(payload")
                .replace("filter_value", "r_filter_value").replace("'$.a'", "'$.b'")
                .replace("'old'", "'new'"), output);
        assertEquals(List.of("doc", "filter_value"), printer.columns);
        assertEquals(List.of("$.a", "$.a", "old", "old", "$.a", "$.a"), printer.strings);
        assertEquals(output, CCJSqlParserUtil.parse(output).toString());
        assertEquals(before, statement.toString());
    }

    @Test
    void passesMySqlJsonTableDefaultsThroughTheExpressionPrinter() throws Exception {
        String sql = "SELECT * FROM JSON_TABLE(doc, '$.a' COLUMNS "
                + "(value VARCHAR(20) PATH '$.a' DEFAULT 'old' ON EMPTY "
                + "DEFAULT 'old' ON ERROR)) AS jt";
        Statement statement = CCJSqlParserUtil.parse(sql, p -> p.withDialect(Dialect.MYSQL));
        RecordingDeParser printer = new RecordingDeParser();
        statement.accept(new StatementDeParser(printer, new SelectDeParser(), printer.getBuilder()),
                CONTEXT);
        String expected = statement.toString().replace("JSON_TABLE(doc", "JSON_TABLE(payload")
                .replace("'$.a'", "'$.b'").replace("'old'", "'new'");
        assertEquals(expected, printer.getBuilder().toString());
        assertEquals(expected, CCJSqlParserUtil.parse(expected,
                p -> p.withDialect(Dialect.MYSQL)).toString());
    }

    @Test
    void rendersExpressionPlanTermsWithoutRewritingPathNames() {
        JsonTableFunction.JsonTablePlanExpression nested =
                new JsonTableFunction.JsonTablePlanExpression()
                        .addTerm(new JsonTableFunction.JsonTablePlanTerm()
                                .setExpression(new Column("doc")));
        JsonTableFunction.JsonTablePlanExpression plan =
                new JsonTableFunction.JsonTablePlanExpression()
                        .addTerm(new JsonTableFunction.JsonTablePlanTerm().setName("doc"))
                        .addOperator(JsonTableFunction.JsonTablePlanOperator.OUTER)
                        .addTerm(new JsonTableFunction.JsonTablePlanTerm()
                                .setNestedPlanExpression(nested));
        RecordingDeParser printer = new RecordingDeParser();
        plan.appendTo(printer.getBuilder(), expression -> expression.accept(printer, CONTEXT));
        assertEquals("doc OUTER (payload)", printer.getBuilder().toString());
        assertEquals(List.of("doc"), printer.columns);
        assertEquals("doc OUTER (doc)", plan.toString());
    }

    @Test
    void leavesOpaqueKeyValueObjectsAndAdditionalQueryPathsUnchanged() throws Exception {
        JsonKeyValuePair pair = new JsonKeyValuePair("raw_key", "raw_value", false,
                JsonKeyValuePairSeparator.COLON);
        RecordingDeParser printer = new RecordingDeParser();
        pair.appendTo(printer.getBuilder(), expression -> expression.accept(printer, CONTEXT));
        assertEquals(pair.toString(), printer.getBuilder().toString());
        assertTrue(printer.columns.isEmpty());
        JsonFunction query = (JsonFunction) CCJSqlParserUtil.parseExpression(
                "JSON_QUERY(doc, '$.a' WITH ARRAY WRAPPER, '$.a')");
        RecordingDeParser queryPrinter = new RecordingDeParser();
        query.accept(queryPrinter, CONTEXT);
        assertEquals("JSON_QUERY(payload, '$.b' WITH ARRAY WRAPPER, '$.a')",
                queryPrinter.getBuilder().toString());
        assertEquals(List.of("$.a"), queryPrinter.strings);
    }

    @Test
    void rewritesScalarSubqueriesThroughTheConnectedStatementDeparser() throws Exception {
        Statement statement = CCJSqlParserUtil.parse(
                "SELECT JSON_VALUE((SELECT doc FROM source_table), '$.a')");
        String before = statement.toString();
        RecordingDeParser printer = new RecordingDeParser(null);
        statement.accept(new StatementDeParser(printer, new SelectDeParser(), printer.getBuilder()),
                null);

        String expected = before.replace("SELECT doc", "SELECT payload")
                .replace("'$.a'", "'$.b'");
        assertEquals(expected, printer.getBuilder().toString());
        assertEquals(expected, CCJSqlParserUtil.parse(expected).toString());
        assertEquals(List.of("doc"), printer.columns);
        assertEquals(before, statement.toString());
    }

    @Test
    void preservesNullPassingExpressionsInIncompleteScalarBuilders() throws Exception {
        JsonFunction function = (JsonFunction) CCJSqlParserUtil.parseExpression(
                "JSON_VALUE(doc, '$.a')");
        function.addPassingExpression(null);
        function.addPassingExpression(new Column("filter_value"));
        RecordingDeParser printer = new RecordingDeParser();
        function.accept(printer, CONTEXT);

        assertEquals("JSON_VALUE(doc, '$.a' PASSING null, filter_value)", function.toString());
        assertEquals("JSON_VALUE(payload, '$.b' PASSING null, r_filter_value)",
                printer.getBuilder().toString());
        assertEquals(List.of("doc", "filter_value"), printer.columns);
    }

    @Test
    void preservesNullPassingClausesAndPlanTermsInIncompleteTableBuilders() {
        JsonTableFunction function = new JsonTableFunction()
                .setJsonInputExpression(new Column("doc"))
                .addPassingClause(new JsonTableFunction.JsonTablePassingClause(
                        new Column("filter_value"), "filter"))
                .setPlanClause(new JsonTableFunction.JsonTablePlanClause().setPlanExpression(
                        new JsonTableFunction.JsonTablePlanExpression().addTerm(null)
                                .addOperator(JsonTableFunction.JsonTablePlanOperator.OUTER)
                                .addTerm(null)));
        function.getPassingClauses().add(0, null);
        RecordingDeParser printer = new RecordingDeParser();
        function.accept(printer, CONTEXT);

        assertEquals("JSON_TABLE(doc PASSING null, filter_value AS filter null "
                + "PLAN (null OUTER null))", function.toString());
        assertEquals("JSON_TABLE(payload PASSING null, r_filter_value AS filter null "
                + "PLAN (null OUTER null))", printer.getBuilder().toString());
        assertEquals(List.of("doc", "filter_value"), printer.columns);
    }

    @Test
    void scalarRendererOverloadsCustomizeBothDefaultAndVisitorOutput() {
        JsonFunction function = new JsonFunction(JsonFunctionType.VALUE) {
            @Override
            public StringBuilder appendValue(StringBuilder builder,
                    Consumer<Expression> expressionPrinter) {
                return super.appendValue(builder, expressionPrinter).append(" /* custom */");
            }
        };
        function.setInputExpression(new JsonFunctionExpression(new Column("doc")));
        function.setJsonPathExpression(new StringValue("$.a"));
        RecordingDeParser printer = new RecordingDeParser();
        function.accept(printer, CONTEXT);

        assertEquals("JSON_VALUE(doc, '$.a') /* custom */", function.toString());
        assertEquals("JSON_VALUE(payload, '$.b') /* custom */", printer.getBuilder().toString());
        assertEquals(List.of("doc"), printer.columns);
    }

    @Test
    void columnAppendHookAndOpaqueCustomColumnsWorkInBothRenderers() {
        JsonTableFunction.JsonTableColumnDefinition opaque =
                new JsonTableFunction.JsonTableColumnDefinition() {
                    @Override
                    public void collectExpressions(List<Expression> expressions) {}

                    @Override
                    public String toString() {
                        return "legacy FOR ORDINALITY";
                    }
                };
        JsonTableFunction.JsonTableValueColumnDefinition structured =
                new JsonTableFunction.JsonTableValueColumnDefinition() {
                    @Override
                    public StringBuilder appendTo(StringBuilder builder,
                            Consumer<Expression> expressionPrinter) {
                        return super.appendTo(builder, expressionPrinter).append(" /* custom */");
                    }
                };
        structured.setColumnName("value").setPathExpression(new StringValue("$.a"));
        JsonTableFunction function = new JsonTableFunction()
                .setJsonInputExpression(new Column("doc"))
                .setColumnsClause(new JsonTableFunction.JsonTableColumnsClause()
                        .addColumnDefinition(opaque).addColumnDefinition(structured));
        RecordingDeParser printer = new RecordingDeParser();
        function.accept(printer, CONTEXT);

        assertEquals("JSON_TABLE(doc COLUMNS (legacy FOR ORDINALITY, "
                + "value PATH '$.a' /* custom */))", function.toString());
        assertEquals("JSON_TABLE(payload COLUMNS (legacy FOR ORDINALITY, "
                + "value PATH '$.b' /* custom */))", printer.getBuilder().toString());
        assertEquals(List.of("doc"), printer.columns);
        assertEquals(List.of("$.a"), printer.strings);
    }

    private static class RecordingDeParser extends ExpressionDeParser {
        private final List<String> columns = new ArrayList<>();
        private final List<String> strings = new ArrayList<>();
        private final Object expectedContext;

        RecordingDeParser() {
            this(CONTEXT);
        }

        RecordingDeParser(Object expectedContext) {
            this.expectedContext = expectedContext;
        }

        @Override
        public <S> StringBuilder visit(Column column, S context) {
            assertSame(expectedContext, context);
            columns.add(column.getColumnName());
            String name = column.getColumnName();
            String replacement = "doc".equals(name) ? "payload"
                    : "other_doc".equals(name) ? "other_payload" : "r_" + name;
            return super.visit(new Column(column.getTable(), replacement), context);
        }

        @Override
        public <S> StringBuilder visit(StringValue value, S context) {
            assertSame(expectedContext, context);
            strings.add(value.getValue());
            if ("$.a".equals(value.getValue()) || "old".equals(value.getValue())) {
                return super.visit(new StringValue("$.a".equals(value.getValue()) ? "$.b" : "new"),
                        context);
            }
            return super.visit(value, context);
        }
    }
}
