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

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.AnalyticExpression;
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

        PlainSelect plainSelect = (PlainSelect) CCJSqlParserUtil.parse(sqlString);
        AnalyticExpression analyticExpression =
                plainSelect.getSelectItem(0).getExpression(AnalyticExpression.class);

        assertEquals(WindowElement.Type.GROUPS, analyticExpression.getWindowElement().getType());
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlString, true);
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
}
