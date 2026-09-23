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
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.create.table.ColDataType;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.TableFunction;
import net.sf.jsqlparser.test.TestUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class JsonTableExistsTest {
    @ParameterizedTest
    @ValueSource(strings = {
            "n INT EXISTS PATH '$.n'",
            "n VARCHAR(10) EXISTS PATH '$.n'",
            "NESTED PATH '$.items[*]' COLUMNS (n INT EXISTS PATH '$.n')"
    })
    void typedExistsRoundTrips(String column) throws JSQLParserException {
        String sql = sql(column);
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
    void typedExistsCanBeInspectedAndEdited() throws JSQLParserException {
        PlainSelect select = (PlainSelect) CCJSqlParserUtil.parse(sql("n INT EXISTS PATH '$.n'"));
        JsonTableFunction table =
                (JsonTableFunction) ((TableFunction) select.getFromItem()).getFunction();
        JsonTableValueColumnDefinition column =
                (JsonTableValueColumnDefinition) table.getColumnsClause().getColumnDefinitions()
                        .get(0);
        assertTrue(column.isExists());
        assertEquals("INT", column.getDataType().getBaseTypeName());
        column.setColumnName("present").setDataType(new ColDataType("BIGINT"))
                .setPathExpression(new StringValue("$.other"));
        TestUtils.assertStatementCanBeDeparsedAs(select,
                sql("present BIGINT EXISTS PATH '$.other'"), true);
        TestUtils.assertSqlCanBeParsedAndDeparsed(select.toString());
        column.setExistsKeyword(false);
        TestUtils.assertStatementCanBeDeparsedAs(select, sql("present BIGINT PATH '$.other'"),
                true);
    }

    @Test
    void constructedAndUntypedExistsColumnsRetainOrder() throws JSQLParserException {
        JsonTableValueColumnDefinition column = new JsonTableValueColumnDefinition()
                .setColumnName("n").setExistsKeyword(true).setDataType(new ColDataType("INT"))
                .setPathExpression(new StringValue("$.n"));
        assertEquals("n INT EXISTS PATH '$.n'", column.toString());
        column.setDataType(null);
        assertEquals("n EXISTS PATH '$.n'", column.toString());
        TestUtils.assertSqlCanBeParsedAndDeparsed(sql(column.toString()));
    }

    private static String sql(String column) {
        return "SELECT * FROM JSON_TABLE('[{\"n\":1},{}]', '$[*]' COLUMNS (" + column + ")) AS jt";
    }
}
