package net.sf.jsqlparser.expression;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.FromItem;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.test.TestUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.*;

public class JsonTableTest {

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
        JsonTable table = parseTable(jsonTableStr);

        assertThat(table.getColumns()).hasSize(1);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "JSON_TABLE(document COLUMNS( hasValue EXISTS PATH '$.pathTest'))",
            "JSON_TABLE(document COLUMNS( hasValue EXISTS PATH '$.pathTest' TRUE ON ERROR TRUE ON EMPTY))",
            "JSON_TABLE(document COLUMNS( hasValue EXISTS PATH '$.pathTest' FALSE ON ERROR FALSE ON EMPTY))",
            "JSON_TABLE(document COLUMNS( hasValue EXISTS PATH '$.pathTest' ERROR ON ERROR ERROR ON EMPTY))",
            "JSON_TABLE(document COLUMNS( hasValue EXISTS PATH '$.pathTest' ERROR ON ERROR))",
            "JSON_TABLE(document COLUMNS( hasValue EXISTS PATH '$.pathTest' ERROR ON EMPTY))",
            "JSON_TABLE(document COLUMNS( hasValue EXISTS ERROR ON EMPTY))",
            "JSON_TABLE(document COLUMNS( hasValue EXISTS))",
    })
    void testExistsColumns(String jsonTableStr) throws JSQLParserException {
        JsonTable table = parseTable(jsonTableStr);

        assertThat(table.getColumns()).hasSize(1);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "JSON_TABLE(document COLUMNS( val PATH '$.pathTest'))",
            "JSON_TABLE(document COLUMNS( val FORMAT JSON PATH '$.pathTest'))",
            "JSON_TABLE(document COLUMNS( val ALLOW SCALARS PATH '$.pathTest'))",
            "JSON_TABLE(document COLUMNS( val DISALLOW SCALARS PATH '$.pathTest'))",
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
            "JSON_TABLE(document COLUMNS( val VARCHAR2 PATH '$.pathTest'))",
            "JSON_TABLE(document COLUMNS( val VARCHAR2(240) PATH '$.pathTest'))",
            "JSON_TABLE(document COLUMNS( val VARCHAR2(240) FORMAT JSON PATH '$.pathTest'))",
            "JSON_TABLE(document COLUMNS( val VARCHAR2(500 BYTE) PATH '$.pathTest'))",
            "JSON_TABLE(document COLUMNS( val VARCHAR2(100 CHAR) PATH '$.pathTest'))",
            "JSON_TABLE(document COLUMNS( val VARCHAR2(500 BYTE) FORMAT JSON DISALLOW SCALARS WITH UNCONDITIONAL ARRAY WRAPPER PATH '$.pathTest' EMPTY OBJECT ON ERROR))",
    })
    void testQueryColumns(String jsonTableStr) throws JSQLParserException {
        JsonTable table = parseTable(jsonTableStr);

        assertThat(table.getColumns()).hasSize(1);
    }

    @Test
    void testFormatJson() throws JSQLParserException {
        String expression = "JSON_TABLE(document FORMAT JSON COLUMNS( id FOR ORDINALITY))";
        JsonTable table = parseTable(expression);

        assertThat(table.isFormatJson()).isTrue();
    }

    @Test
    void testPathExpression() throws JSQLParserException {
        String expression = "JSON_TABLE(document, '$.SubPath' COLUMNS( id FOR ORDINALITY))";
        JsonTable table = parseTable(expression);

        assertThat(table.getPathExpression()).isEqualTo("$.SubPath");
    }

    @Test
    void testNullOnError() throws JSQLParserException {
        String expression = "JSON_TABLE(document NULL ON ERROR COLUMNS( id FOR ORDINALITY))";
        JsonTable table = parseTable(expression);

        assertThat(table.getOnErrorType()).isEqualTo(JsonOnErrorType.NULL);
    }

    @Test
    void testErrorOnError() throws JSQLParserException {
        String expression = "JSON_TABLE(document ERROR ON ERROR COLUMNS( id FOR ORDINALITY))";
        JsonTable table = parseTable(expression);

        assertThat(table.getOnErrorType()).isEqualTo(JsonOnErrorType.ERROR);
    }

    @Test
    void testNullOnEmpty() throws JSQLParserException {
        String expression = "JSON_TABLE(document NULL ON EMPTY COLUMNS( id FOR ORDINALITY))";
        JsonTable table = parseTable(expression);

        assertThat(table.getOnEmptyType()).isEqualTo(JsonOnEmptyType.NULL);
    }

    @Test
    void testErrorOnEmpty() throws JSQLParserException {
        String expression = "JSON_TABLE(document ERROR ON EMPTY COLUMNS( id FOR ORDINALITY))";
        JsonTable table = parseTable(expression);

        assertThat(table.getOnEmptyType()).isEqualTo(JsonOnEmptyType.ERROR);
    }

    @Test
    void testTableTypeLax() throws JSQLParserException {
        String expression = "JSON_TABLE(document TYPE(LAX) COLUMNS( id FOR ORDINALITY))";
        JsonTable table = parseTable(expression);

        assertThat(table.getType()).isEqualTo(JsonTableType.LAX);
    }

    @Test
    void testTableTypeStrict() throws JSQLParserException {
        String expression = "JSON_TABLE(document TYPE(STRICT) COLUMNS( id FOR ORDINALITY))";
        JsonTable table = parseTable(expression);

        assertThat(table.getType()).isEqualTo(JsonTableType.STRICT);
    }

    @Test
    void testColumnTypeExists() throws JSQLParserException {
        String expression = "JSON_TABLE(document COLUMNS( hasValue EXISTS PATH '$.pathTest'))";
        JsonTable table = parseTable(expression);

        assertThat(table.getColumns()).hasSize(1);

        JsonTableColumn col = table.getColumns().get(0);
        assertThat(col.getType()).isEqualTo(JsonTableColumnType.JSON_EXISTS);
    }

    @Test
    void testBuilder() {
        Column c = new Column("document");

        JsonTable table = new JsonTable().withExpression(c)
                .withPathExpression("$.subPath")
                .withFormatJson(true)
                .withType(JsonTableType.STRICT)
                .withOnEmptyType(JsonOnEmptyType.NULL)
                .withOnErrorType(JsonOnErrorType.ERROR)
                .withColumn(new JsonTableColumn().withName("id")
                        .withType(JsonTableColumnType.ORDINALITY));

        assertThat(table.toString()).isEqualTo(
                "JSON_TABLE(document FORMAT JSON, '$.subPath' ERROR ON ERROR TYPE(STRICT) NULL ON EMPTY COLUMNS(id FOR ORDINALITY))");
    }

    @Test
    void testValidSetters() {
        JsonTable table = new JsonTable();

        assertThatNoException().isThrownBy(() -> {
            table.setOnEmptyType(null);
            table.setOnEmptyType(JsonOnEmptyType.NULL);
            table.setOnEmptyType(JsonOnEmptyType.ERROR);

            table.setOnErrorType(null);
            table.setOnErrorType(JsonOnErrorType.NULL);
            table.setOnErrorType(JsonOnErrorType.ERROR);

            table.setType(null);
            table.setType(JsonTableType.LAX);
            table.setType(JsonTableType.STRICT);
        });
    }

    @Test
    void testInvalidSetters() {
        JsonTable table = new JsonTable();

        assertThatThrownBy(() -> table.setOnEmptyType(JsonOnEmptyType.EMPTY))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> table.setOnEmptyType(JsonOnEmptyType.EMPTY_ARRAY))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> table.setOnEmptyType(JsonOnEmptyType.EMPTY_OBJECT))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> table.setOnEmptyType(JsonOnEmptyType.FALSE))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> table.setOnEmptyType(JsonOnEmptyType.TRUE))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> table.setOnEmptyType(JsonOnEmptyType.DEFAULT))
                .isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> table.setOnErrorType(JsonOnErrorType.EMPTY))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> table.setOnErrorType(JsonOnErrorType.EMPTY_ARRAY))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> table.setOnErrorType(JsonOnErrorType.EMPTY_OBJECT))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> table.setOnErrorType(JsonOnErrorType.FALSE))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> table.setOnErrorType(JsonOnErrorType.TRUE))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> table.setOnErrorType(JsonOnErrorType.DEFAULT))
                .isInstanceOf(IllegalArgumentException.class);

        JsonTableColumn column = new JsonTableColumn();

        assertThatThrownBy(() -> {
            column.setType(JsonTableColumnType.JSON_EXISTS);
            column.setFormatJson(true);
        }).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> {
            column.setType(JsonTableColumnType.JSON_VALUE);
            column.setFormatJson(true);
        }).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> {
            column.setType(JsonTableColumnType.ORDINALITY);
            column.setFormatJson(true);
        }).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> {
            column.setType(JsonTableColumnType.JSON_NESTED_PATH);
            column.setFormatJson(true);
        }).isInstanceOf(IllegalArgumentException.class);
    }

    private JsonTable parseTable(String jsonTableStr) throws JSQLParserException {
        String sql = "SELECT * FROM " + jsonTableStr;
        Statement stmt = CCJSqlParserUtil.parse(sql);

        TestUtils.assertSqlCanBeParsedAndDeparsed(sql, true);

        FromItem fromItem = ((PlainSelect) stmt).getFromItem();
        return (JsonTable) fromItem;
    }


}
