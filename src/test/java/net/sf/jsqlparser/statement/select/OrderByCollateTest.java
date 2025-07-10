package net.sf.jsqlparser.statement.select;

import static net.sf.jsqlparser.test.TestUtils.assertSqlCanBeParsedAndDeparsed;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.expression.CollateExpression;
import org.junit.jupiter.api.Test;

public class OrderByCollateTest {

    @Test
    public void testOrderByWithCollate() throws JSQLParserException {
        String sql = "SELECT * FROM a ORDER BY CAST(a.xyz AS TEXT) COLLATE \"und-x-icu\" ASC NULLS FIRST";
        assertSqlCanBeParsedAndDeparsed(sql);
    }

    @Test
    public void testOrderByWithCollateSimple() throws JSQLParserException {
        String sql = "SELECT * FROM a ORDER BY col COLLATE \"C\" ASC";
        assertSqlCanBeParsedAndDeparsed(sql);
    }

    @Test
    public void testOrderByWithCollateMultiple() throws JSQLParserException {
        String sql = "SELECT * FROM a ORDER BY col1 COLLATE \"C\" ASC, col2 COLLATE \"POSIX\" DESC";
        assertSqlCanBeParsedAndDeparsed(sql);
    }

    @Test
    public void testOrderByWithCollateAndNulls() throws JSQLParserException {
        String sql = "SELECT * FROM a ORDER BY col COLLATE \"C\" DESC NULLS LAST";
        assertSqlCanBeParsedAndDeparsed(sql);
    }

    @Test
    public void testOrderByCollateStructure() throws JSQLParserException {
        String sql = "SELECT * FROM a ORDER BY col COLLATE \"C\" ASC";
        Select select = (Select) CCJSqlParserUtil.parse(sql);
        PlainSelect plainSelect = (PlainSelect) select;
        
        assertNotNull(plainSelect.getOrderByElements());
        assertEquals(1, plainSelect.getOrderByElements().size());
        
        OrderByElement orderByElement = plainSelect.getOrderByElements().get(0);
        assertNotNull(orderByElement.getExpression());
        
        // The expression should be a CollateExpression
        if (orderByElement.getExpression() instanceof CollateExpression) {
            CollateExpression collateExpr = (CollateExpression) orderByElement.getExpression();
            assertEquals("\"C\"", collateExpr.getCollate());
        }
    }
}