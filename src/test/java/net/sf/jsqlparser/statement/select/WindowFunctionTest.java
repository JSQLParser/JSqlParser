/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.select;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.AnalyticExpression;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.WindowElement;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.test.TestUtils;
import org.junit.jupiter.api.Test;

public class WindowFunctionTest {
    @Test
    public void testListAggOverIssue1652() throws JSQLParserException {
        String sqlString =
                "SELECT\n" +
                        "    LISTAGG (d.COL_TO_AGG, ' / ') WITHIN GROUP (ORDER BY d.COL_TO_AGG) OVER (PARTITION BY d.PART_COL) AS MY_LISTAGG\n"
                        +
                        "FROM cte_dummy_data d";

        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlString, true);
    }

    @Test
    public void RedshiftRespectIgnoreNulls() throws JSQLParserException {
        String sqlString =
                "select venuestate, venueseats, venuename,\n"
                        + "first_value(venuename) ignore nulls\n"
                        + "over(partition by venuestate\n"
                        + "order by venueseats desc\n"
                        + "rows between unbounded preceding and unbounded following) AS first\n"
                        + "from (select * from venue where venuestate='CA')\n"
                        + "order by venuestate;";

        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlString, true);
    }

    @Test
    public void testWindowFrameGroupsIssue2431() throws JSQLParserException {
        String sqlString =
                "SELECT SUM(value) OVER (ORDER BY ts "
                        + "GROUPS BETWEEN 1 PRECEDING AND CURRENT ROW) FROM events";

        WindowElement windowElement = parseWindowElement(sqlString, 0);
        assertEquals(WindowElement.Type.GROUPS, windowElement.getType());
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlString, true);
    }

    @Test
    public void testWindowFrameGroupsExcludeTiesIssue2431() throws JSQLParserException {
        String sqlString =
                "SELECT id, ts, value, SUM(value) OVER (ORDER BY ts "
                        + "GROUPS BETWEEN 1 PRECEDING AND CURRENT ROW EXCLUDE TIES) AS sum_excl_ties "
                        + "FROM events ORDER BY ts, id";

        WindowElement windowElement = parseWindowElement(sqlString, 3);
        assertEquals(WindowElement.Exclusion.TIES, windowElement.getExclusion());
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlString, true);
    }

    @Test
    public void testWindowFrameExclusionsIssue2431() throws JSQLParserException {
        String[] sqlStrings = {
                "SELECT SUM(value) OVER (ORDER BY ts ROWS UNBOUNDED PRECEDING EXCLUDE CURRENT ROW) FROM events",
                "SELECT SUM(value) OVER (ORDER BY ts RANGE CURRENT ROW EXCLUDE GROUP) FROM events",
                "SELECT SUM(value) OVER (ORDER BY ts GROUPS BETWEEN 1 PRECEDING AND CURRENT ROW EXCLUDE TIES) FROM events",
                "SELECT SUM(value) OVER (ORDER BY ts GROUPS BETWEEN 1 PRECEDING AND CURRENT ROW EXCLUDE NO OTHERS) FROM events"
        };

        for (String sqlString : sqlStrings) {
            TestUtils.assertSqlCanBeParsedAndDeparsed(sqlString, true);
        }
    }

    @Test
    public void testFrameExclusionIdentifierCompatibilityIssue2431() throws JSQLParserException {
        TestUtils.assertSqlCanBeParsedAndDeparsed("SELECT ties FROM ties", true);
        TestUtils.assertSqlCanBeParsedAndDeparsed("SELECT others FROM others", true);
    }

    @Test
    public void testWindowFrameGroupsVariantsIssue2431() throws JSQLParserException {
        String singleSidedSqlString =
                "SELECT SUM(value) OVER (ORDER BY ts GROUPS UNBOUNDED PRECEDING) FROM events";
        String namedWindowSqlString =
                "SELECT SUM(value) OVER w FROM events "
                        + "WINDOW w AS (ORDER BY ts GROUPS BETWEEN 1 PRECEDING AND CURRENT ROW)";

        TestUtils.assertSqlCanBeParsedAndDeparsed(singleSidedSqlString, true);
        TestUtils.assertSqlCanBeParsedAndDeparsed(namedWindowSqlString, true);
    }

    private WindowElement parseWindowElement(String sqlString, int selectItemIndex)
            throws JSQLParserException {
        PlainSelect plainSelect = (PlainSelect) CCJSqlParserUtil.parse(sqlString);
        Expression expression = plainSelect.getSelectItem(selectItemIndex).getExpression();
        assertInstanceOf(AnalyticExpression.class, expression);
        AnalyticExpression analyticExpression = (AnalyticExpression) expression;
        WindowElement windowElement = analyticExpression.getWindowElement();

        assertNotNull(windowElement);
        return windowElement;
    }
}
