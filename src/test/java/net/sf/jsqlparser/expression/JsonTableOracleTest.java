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
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.FromItem;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.TableFunction;
import net.sf.jsqlparser.test.TestUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.*;

public class JsonTableOracleTest {

    @ParameterizedTest
    @ValueSource(strings = {
            "SELECT jt.phones FROM j_purchaseorder,\n" +
                    "JSON_TABLE(po_document, '$.ShippingInstructions'\n" +
                    "COLUMNS(phones VARCHAR2(100) FORMAT JSON PATH '$.Phone')) AS jt",
            "SELECT jt.phones FROM j_purchaseorder,\n" +
                    "JSON_TABLE(po_document, '$.ShippingInstructions'\n" +
                    "COLUMNS(phones FORMAT JSON PATH '$.Phone')) AS jt"
    })
    void testObjectOracle(String sqlStr) throws JSQLParserException {
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "JSON_TABLE(document COLUMNS( id FOR ORDINALITY))",
            "JSON_TABLE(document FORMAT JSON COLUMNS( id FOR ORDINALITY))",
            "JSON_TABLE(document, '$.SubPath' COLUMNS( id FOR ORDINALITY))",
            "JSON_TABLE(document NULL ON ERROR COLUMNS( id FOR ORDINALITY))",
            "JSON_TABLE(document ERROR ON ERROR COLUMNS( id FOR ORDINALITY))",
            "JSON_TABLE(document TYPE(LAX) COLUMNS( id FOR ORDINALITY))",
            "JSON_TABLE(document TYPE(STRICT) COLUMNS( id FOR ORDINALITY))",
            "JSON_TABLE(document NULL ON EMPTY COLUMNS( id FOR ORDINALITY))",
            "JSON_TABLE(document ERROR ON EMPTY COLUMNS( id FOR ORDINALITY))",
    })
    void testExpression(String jsonTableStr) throws JSQLParserException {
        JsonTableFunction table = parseTable(jsonTableStr);

        assertThat(table.getColumnsClause().getColumnDefinitions()).hasSize(1);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "JSON_TABLE(document COLUMNS( hasValue EXISTS PATH '$.pathTest'))",
            "JSON_TABLE(document COLUMNS( hasValue EXISTS PATH '$.pathTest' TRUE ON ERROR TRUE ON EMPTY))",
            "JSON_TABLE(document COLUMNS( hasValue EXISTS PATH '$.pathTest' FALSE ON ERROR FALSE ON EMPTY))",
            "JSON_TABLE(document COLUMNS( hasValue EXISTS PATH '$.pathTest' ERROR ON ERROR ERROR ON EMPTY))",
            "JSON_TABLE(document COLUMNS( hasValue EXISTS PATH '$.pathTest' ERROR ON ERROR))",
            "JSON_TABLE(document COLUMNS( hasValue EXISTS PATH '$.pathTest' ERROR ON EMPTY))",
            "JSON_TABLE(document COLUMNS( hasValue EXISTS PATH '$.pathTest' TRUE ON ERROR))",
            "JSON_TABLE(document COLUMNS( hasValue EXISTS PATH '$.pathTest' TRUE ON EMPTY))",
            "JSON_TABLE(document COLUMNS( hasValue EXISTS PATH '$.pathTest' FALSE ON ERROR))",
            "JSON_TABLE(document COLUMNS( hasValue EXISTS PATH '$.pathTest' FALSE ON EMPTY))",
            "JSON_TABLE(document COLUMNS( hasValue EXISTS ERROR ON EMPTY))",
            "JSON_TABLE(document COLUMNS( hasValue EXISTS))",
    })
    void testExistsColumns(String jsonTableStr) throws JSQLParserException {
        JsonTableFunction table = parseTable(jsonTableStr);

        assertThat(table.getColumnsClause().getColumnDefinitions()).hasSize(1);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "JSON_TABLE(document COLUMNS( val PATH '$.pathTest'))",
            "JSON_TABLE(document COLUMNS( val FORMAT JSON PATH '$.pathTest'))",
            "JSON_TABLE(document COLUMNS( val ALLOW SCALARS PATH '$.pathTest'))",
            "JSON_TABLE(document COLUMNS( val DISALLOW SCALARS PATH '$.pathTest'))",
            "JSON_TABLE(document COLUMNS( val VARCHAR(240) ALLOW SCALARS PATH '$.pathTest'))",
            "JSON_TABLE(document COLUMNS( val INT DISALLOW SCALARS PATH '$.pathTest'))",
            "JSON_TABLE(document COLUMNS( val FORMAT JSON DISALLOW SCALARS PATH '$.pathTest'))",
            "JSON_TABLE(document COLUMNS( val WITH WRAPPER PATH '$.pathTest'))",
            "JSON_TABLE(document COLUMNS( val WITHOUT WRAPPER PATH '$.pathTest'))",
            "JSON_TABLE(document COLUMNS( val WITH ARRAY WRAPPER PATH '$.pathTest'))",
            "JSON_TABLE(document COLUMNS( val WITHOUT ARRAY WRAPPER PATH '$.pathTest'))",
            "JSON_TABLE(document COLUMNS( val WITH CONDITIONAL WRAPPER PATH '$.pathTest'))",
            "JSON_TABLE(document COLUMNS( val WITH CONDITIONAL ARRAY WRAPPER PATH '$.pathTest'))",
            "JSON_TABLE(document COLUMNS( val WITH UNCONDITIONAL WRAPPER PATH '$.pathTest'))",
            "JSON_TABLE(document COLUMNS( val WITH UNCONDITIONAL ARRAY WRAPPER PATH '$.pathTest'))",
            "JSON_TABLE(document COLUMNS( val WITH UNCONDITIONAL ARRAY WRAPPER PATH '$.pathTest'))",
            "JSON_TABLE(document COLUMNS( val PATH '$.pathTest' ERROR ON ERROR))",
            "JSON_TABLE(document COLUMNS( val PATH '$.pathTest' NULL ON ERROR))",
            "JSON_TABLE(document COLUMNS( val PATH '$.pathTest' EMPTY ON ERROR))",
            "JSON_TABLE(document COLUMNS( val PATH '$.pathTest' EMPTY ARRAY ON ERROR))",
            "JSON_TABLE(document COLUMNS( val PATH '$.pathTest' EMPTY OBJECT ON ERROR))",
            "JSON_TABLE(document COLUMNS( val CLOB PATH '$.pathTest'))",
            "JSON_TABLE(document COLUMNS( val BLOB PATH '$.pathTest'))",
            "JSON_TABLE(document COLUMNS( val JSON PATH '$.pathTest'))",
            "JSON_TABLE(document COLUMNS( val VECTOR PATH '$.pathTest'))",
            "JSON_TABLE(document COLUMNS( val VARCHAR PATH '$.pathTest'))",
            "JSON_TABLE(document COLUMNS( val VARCHAR(240) PATH '$.pathTest'))",
            "JSON_TABLE(document COLUMNS( val VARCHAR(240) FORMAT JSON PATH '$.pathTest'))",

            // These would require adapting ColDataType in Line 10176
            // "JSON_TABLE(document COLUMNS( val VARCHAR2(500 BYTE) PATH '$.pathTest'))",
            // "JSON_TABLE(document COLUMNS( val VARCHAR2(100 CHAR) PATH '$.pathTest'))",
            "JSON_TABLE(document COLUMNS( val VARCHAR2 FORMAT JSON DISALLOW SCALARS WITH UNCONDITIONAL ARRAY WRAPPER PATH '$.pathTest' EMPTY OBJECT ON ERROR))",
    })
    void testQueryColumns(String jsonTableStr) throws JSQLParserException {
        JsonTableFunction table = parseTable(jsonTableStr);

        assertThat(table.getColumnsClause().getColumnDefinitions()).hasSize(1);
    }

    @Test
    void testFormatJson() throws JSQLParserException {
        String expression = "JSON_TABLE(document FORMAT JSON COLUMNS( id FOR ORDINALITY))";
        JsonTableFunction table = parseTable(expression);

        assertThat(table.getFormatJson()).isTrue();
    }

    @Test
    void testPathExpression() throws JSQLParserException {
        String expression = "JSON_TABLE(document, '$.SubPath' COLUMNS( id FOR ORDINALITY))";
        JsonTableFunction table = parseTable(expression);

        assertThat(table.getJsonPathExpression().toString()).isEqualTo("'$.SubPath'");
    }

    @Test
    void testNullOnError() throws JSQLParserException {
        String expression = "JSON_TABLE(document NULL ON ERROR COLUMNS( id FOR ORDINALITY))";
        JsonTableFunction table = parseTable(expression);

        assertThat(table.getOnErrorClause().getType())
                .isEqualTo(JsonTableFunction.JsonTableOnErrorType.NULL);
    }

    @Test
    void testErrorOnError() throws JSQLParserException {
        String expression = "JSON_TABLE(document ERROR ON ERROR COLUMNS( id FOR ORDINALITY))";
        JsonTableFunction table = parseTable(expression);

        assertThat(table.getOnErrorClause().getType())
                .isEqualTo(JsonTableFunction.JsonTableOnErrorType.ERROR);
    }

    @Test
    void testNullOnEmpty() throws JSQLParserException {
        String expression = "JSON_TABLE(document NULL ON EMPTY COLUMNS( id FOR ORDINALITY))";
        JsonTableFunction table = parseTable(expression);

        assertThat(table.getOnEmptyClause().getType())
                .isEqualTo(JsonTableFunction.JsonTableOnEmptyType.NULL);
    }

    @Test
    void testErrorOnEmpty() throws JSQLParserException {
        String expression = "JSON_TABLE(document ERROR ON EMPTY COLUMNS( id FOR ORDINALITY))";
        JsonTableFunction table = parseTable(expression);

        assertThat(table.getOnEmptyClause().getType())
                .isEqualTo(JsonTableFunction.JsonTableOnEmptyType.ERROR);
    }

    @Test
    void testParsingTypeLax() throws JSQLParserException {
        String expression = "JSON_TABLE(document TYPE(LAX) COLUMNS( id FOR ORDINALITY))";
        JsonTableFunction table = parseTable(expression);

        assertThat(table.getParsingTypeClause().getType())
                .isEqualTo(JsonTableFunction.JsonTableParsingType.LAX);
    }

    @Test
    void testParsingTypeStrict() throws JSQLParserException {
        String expression = "JSON_TABLE(document TYPE(STRICT) COLUMNS( id FOR ORDINALITY))";
        JsonTableFunction table = parseTable(expression);

        assertThat(table.getParsingTypeClause().getType())
                .isEqualTo(JsonTableFunction.JsonTableParsingType.STRICT);
    }

    @Test
    void testColumnTypeExists() throws JSQLParserException {
        String expression = "JSON_TABLE(document COLUMNS( hasValue EXISTS PATH '$.pathTest'))";
        JsonTableFunction table = parseTable(expression);

        assertThat(table.getColumnsClause().getColumnDefinitions()).hasSize(1);

        JsonTableFunction.JsonTableColumnDefinition col =
                table.getColumnsClause().getColumnDefinitions().get(0);
        assertThat(col).isInstanceOf(JsonTableFunction.JsonTableValueColumnDefinition.class);

        JsonTableFunction.JsonTableValueColumnDefinition valueCol =
                (JsonTableFunction.JsonTableValueColumnDefinition) col;

        assertThat(valueCol.isExists()).isTrue();
    }

    private JsonTableFunction parseTable(String jsonTableStr) throws JSQLParserException {
        String sql = "SELECT * FROM " + jsonTableStr;
        Statement stmt = CCJSqlParserUtil.parse(sql);

        TestUtils.assertSqlCanBeParsedAndDeparsed(sql, true);

        FromItem fromItem = ((PlainSelect) stmt).getFromItem();
        assertThat(fromItem).isInstanceOf(TableFunction.class);
        Function function = ((TableFunction) fromItem).getFunction();
        assertThat(function).isInstanceOf(JsonTableFunction.class);

        return (JsonTableFunction) function;
    }


}
