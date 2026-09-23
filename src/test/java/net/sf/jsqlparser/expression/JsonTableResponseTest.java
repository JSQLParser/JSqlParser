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

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.JsonTableFunction.JsonTableValueColumnDefinition;
import net.sf.jsqlparser.parser.AbstractJSqlParser.Dialect;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.TableFunction;
import net.sf.jsqlparser.test.TestUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JsonTableResponseTest {
    @ParameterizedTest
    @ValueSource(strings = {
            "DEFAULT '9' ON ERROR",
            "DEFAULT '0' ON EMPTY DEFAULT '9' ON ERROR",
            "DEFAULT '9' ON ERROR DEFAULT '0' ON EMPTY",
            "NULL ON EMPTY DEFAULT '9' ON ERROR",
            "ERROR ON EMPTY DEFAULT '9' ON ERROR"
    })
    void defaultsRoundTripInEitherOrder(String response) throws JSQLParserException {
        String sql = sql("n INT PATH '$.n' " + response);
        for (Dialect dialect : new Dialect[] {null, Dialect.MYSQL, Dialect.ORACLE}) {
            PlainSelect select = (PlainSelect) TestUtils.assertSqlCanBeParsedAndDeparsed(sql,
                    true, parser -> {
                        if (dialect != null) {
                            parser.withDialect(dialect);
                        }
                    });
            TestUtils.assertSqlCanBeParsedAndDeparsed(select.toString(), false,
                    parser -> {
                        if (dialect != null) {
                            parser.withDialect(dialect);
                        }
                    });
        }
    }

    @Test
    void defaultExpressionsAreVisitedAndEditable() throws JSQLParserException {
        PlainSelect select = (PlainSelect) TestUtils.assertSqlCanBeParsedAndDeparsed(
                sql("n INT PATH '$.n' DEFAULT '0' ON EMPTY DEFAULT '9' ON ERROR"));
        JsonTableFunction table =
                (JsonTableFunction) ((TableFunction) select.getFromItem()).getFunction();
        JsonTableValueColumnDefinition column =
                (JsonTableValueColumnDefinition) table.getColumnsClause().getColumnDefinitions()
                        .get(0);
        assertEquals(JsonFunction.JsonOnResponseBehaviorType.DEFAULT,
                column.getOnErrorBehavior().getType());
        assertEquals("9", ((StringValue) column.getOnErrorBehavior().getExpression()).getValue());
        List<String> visited = new ArrayList<>();
        table.accept(new ExpressionVisitorAdapter<Void>() {
            @Override
            public <S> Void visit(StringValue value, S context) {
                visited.add(value.getValue());
                if ("9".equals(value.getValue())) {
                    value.setValue("7");
                }
                return null;
            }
        }, null);
        assertTrue(visited.containsAll(List.of("$.n", "0", "9")));
        TestUtils.assertStatementCanBeDeparsedAs(select,
                sql("n INT PATH '$.n' DEFAULT '0' ON EMPTY DEFAULT '7' ON ERROR"), true);
        column.setOnErrorBehavior(new JsonFunction.JsonOnResponseBehavior(
                JsonFunction.JsonOnResponseBehaviorType.DEFAULT, new StringValue("8")));
        column.setOnEmptyAfterOnError(true);
        TestUtils.assertStatementCanBeDeparsedAs(select,
                sql("n INT PATH '$.n' DEFAULT '8' ON ERROR DEFAULT '0' ON EMPTY"), true);
        TestUtils.assertSqlCanBeParsedAndDeparsed(select.toString());
    }

    @Test
    void nestedDefaultsRoundTrip() throws JSQLParserException {
        TestUtils.assertSqlCanBeParsedAndDeparsed(sql(
                "NESTED PATH '$.items[*]' COLUMNS (n INT PATH '$.n' DEFAULT '9' ON ERROR)"));
    }

    @ParameterizedTest
    @ValueSource(
            strings = {"NULL", "ERROR", "TRUE", "FALSE", "EMPTY", "EMPTY ARRAY", "EMPTY OBJECT"})
    void sharedNonDefaultResponsesRoundTrip(String behavior) throws JSQLParserException {
        TestUtils.assertSqlCanBeParsedAndDeparsed(sql("n PATH '$.n' " + behavior + " ON ERROR"));
        TestUtils.assertSqlCanBeParsedAndDeparsed(sql("n PATH '$.n' " + behavior + " ON EMPTY"));
    }

    private static String sql(String column) {
        return "SELECT * FROM JSON_TABLE('[{\"n\":1},{},{\"n\":\"bad\"}]', '$[*]' COLUMNS ("
                + column + ")) AS jt";
    }
}
