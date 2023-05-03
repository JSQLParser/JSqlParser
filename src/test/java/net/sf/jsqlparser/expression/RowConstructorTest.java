package net.sf.jsqlparser.expression;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.test.TestUtils;
import org.junit.jupiter.api.Test;

class RowConstructorTest {
    @Test
    public void testRowConstructor() throws JSQLParserException {
        TestUtils.assertExpressionCanBeParsedAndDeparsed("ROW(dataid, value, calcMark)", true);
    }

}
