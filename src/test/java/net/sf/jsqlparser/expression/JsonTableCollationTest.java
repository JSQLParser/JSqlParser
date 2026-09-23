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

class JsonTableCollationTest {
    @ParameterizedTest
    @ValueSource(strings = {
            "n VARCHAR(10) COLLATE utf8mb4_bin PATH '$.n'",
            "n VARCHAR(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin PATH '$.n'",
            "n VARCHAR(10) COLLATE `utf8mb4_bin` PATH '$.n'",
            "n VARCHAR(10) COLLATE 'utf8mb4_bin' PATH '$.n'",
            "n VARCHAR(10) COLLATE \"utf8mb4_bin\" PATH '$.n'",
            "NESTED PATH '$.items[*]' COLUMNS (n VARCHAR(10) COLLATE utf8mb4_bin PATH '$.n')"
    })
    void collatedColumnsRoundTrip(String column) throws JSQLParserException {
        PlainSelect select = parse(sql(column));
        parse(select.toString());
    }

    @Test
    void collationCanBeInspectedEditedRemovedAndConstructed() throws JSQLParserException {
        PlainSelect select = parse(sql("n VARCHAR(10) COLLATE utf8mb4_bin PATH '$.n'"));
        JsonTableFunction table =
                (JsonTableFunction) ((TableFunction) select.getFromItem()).getFunction();
        JsonTableValueColumnDefinition column =
                (JsonTableValueColumnDefinition) table.getColumnsClause().getColumnDefinitions()
                        .get(0);
        assertEquals("utf8mb4_bin", column.getCollation());
        assertEquals("VARCHAR", column.getDataType().getBaseTypeName());
        column.setCollation("utf8mb4_general_ci");
        TestUtils.assertStatementCanBeDeparsedAs(select,
                sql("n VARCHAR(10) COLLATE utf8mb4_general_ci PATH '$.n'"), true);
        parse(select.toString());
        column.setCollation(null);
        TestUtils.assertStatementCanBeDeparsedAs(select, sql("n VARCHAR(10) PATH '$.n'"), true);
        parse(select.toString());
        JsonTableValueColumnDefinition constructed = new JsonTableValueColumnDefinition()
                .setColumnName("v")
                .setDataType(new ColDataType("VARCHAR").addArgumentsStringList("10"))
                .setCollation("utf8mb4_bin").setPathExpression(new StringValue("$.v"));
        table.getColumnsClause().getColumnDefinitions().set(0, constructed);
        TestUtils.assertStatementCanBeDeparsedAs(select,
                sql("v VARCHAR(10) COLLATE utf8mb4_bin PATH '$.v'"), true);
        parse(select.toString());
    }

    @Test
    void columnCollationRequiresMySqlDialect() {
        String sql = sql("n VARCHAR(10) COLLATE utf8mb4_bin PATH '$.n'");
        assertThrows(JSQLParserException.class, () -> CCJSqlParserUtil.parse(sql));
        assertThrows(JSQLParserException.class,
                () -> CCJSqlParserUtil.parse(sql,
                        parser -> parser.withDialect(Dialect.POSTGRESQL)));
    }

    private static PlainSelect parse(String sql) throws JSQLParserException {
        return (PlainSelect) TestUtils.assertSqlCanBeParsedAndDeparsed(sql, true,
                parser -> parser.withDialect(Dialect.MYSQL));
    }

    private static String sql(String column) {
        return "SELECT * FROM JSON_TABLE('[{\"n\":\"ab\"}]', '$[*]' COLUMNS (" + column
                + ")) AS jt";
    }
}
