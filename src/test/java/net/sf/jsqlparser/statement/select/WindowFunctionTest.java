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

import net.sf.jsqlparser.JSQLParserException;
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
}
