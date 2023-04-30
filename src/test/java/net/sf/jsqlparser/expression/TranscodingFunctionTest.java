package net.sf.jsqlparser.expression;

import net.sf.jsqlparser.JSQLParserException;
import org.junit.jupiter.api.Test;

import static net.sf.jsqlparser.test.TestUtils.assertSqlCanBeParsedAndDeparsed;
import static org.junit.jupiter.api.Assertions.*;

class TranscodingFunctionTest {

    @Test
    void testTranscoding() throws JSQLParserException {
        String functionStr = "CONVERT( 'abc' USING utf8mb4 )";
        String sqlStr = "SELECT " + functionStr;
        assertSqlCanBeParsedAndDeparsed(sqlStr, true);

        TranscodingFunction transcodingFunction = new TranscodingFunction()
                .withExpression(new StringValue("abc"))
                .withTranscodingName("utf8mb4");
        assertEquals(functionStr, transcodingFunction.toString());
    }

}
