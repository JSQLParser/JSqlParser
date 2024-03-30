package net.sf.jsqlparser.expression;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.test.TestUtils;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LambdaExpressionTest {

    @Test
    void testLambdaFunctionSingleParameter() throws JSQLParserException {
        String sqlStr = "select list_transform( split('test', ''),  x -> unicode(x) )";
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);
    }

    @Disabled
    @Test
    // wip, right now the Grammar works but collides with Multi Value Lists
    void testLambdaFunctionMultipleParameter() throws JSQLParserException {
        String sqlStr = "SELECT list_transform(\n" +
                "        [1, 2, 3],\n" +
                "        x -> list_reduce([4, 5, 6], (a, b) -> a + b) + x\n" +
                "    )";
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);
    }

}
