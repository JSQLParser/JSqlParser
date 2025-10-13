package net.sf.jsqlparser.expression;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.test.TestUtils;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

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
        "JSON_TABLE(document COLUMNS( id FOR ORDINALITY))"
    })
    void testExpression(String expressionStr) throws JSQLParserException {
        JsonTable table = (JsonTable) CCJSqlParserUtil.parseExpression(expressionStr);

        assertThat(table.getColumns()).hasSize(1);

    }

}
