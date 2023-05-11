package net.sf.jsqlparser.expression;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.operators.arithmetic.BitwiseAnd;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.test.TestUtils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class JdbcNamedParameterTest {
    @Test
    void testDoubleColon() throws JSQLParserException {
        String sqlStr = "select :test";
        PlainSelect select = (PlainSelect) TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);
        assertTrue(select.getSelectItems().get(0).getExpression() instanceof JdbcNamedParameter);
    }

    @Test
    void testAmpersand() throws JSQLParserException {
        String sqlStr = "select &test, 'a & b', a & b";
        PlainSelect select = (PlainSelect) TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);
        assertTrue(select.getSelectItems().get(0).getExpression() instanceof JdbcNamedParameter);
        assertTrue(select.getSelectItems().get(2).getExpression() instanceof BitwiseAnd);
    }

    @Test
    void testIssue1785() throws JSQLParserException {
        String sqlStr = "select * from all_tables\n"
                + "where owner = &myowner";
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);
    }
}
