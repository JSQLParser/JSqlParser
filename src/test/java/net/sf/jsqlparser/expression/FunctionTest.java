/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2023 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.expression;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.test.TestUtils;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

class FunctionTest {
    @Test
    @Disabled
    // @Todo: Implement the Prediction(... USING ...) functions
    // https://docs.oracle.com/en/database/oracle/oracle-database/19/sqlrf/PREDICTION.html
    void testNestedFunctions() throws JSQLParserException {
        String sqlStr =
                "select cust_gender, count(*) as cnt, round(avg(age)) as avg_age\n"
                        + "   from mining_data_apply_v\n"
                        + "   where prediction(dt_sh_clas_sample cost model\n"
                        + "      using cust_marital_status, education, household_size) = 1\n"
                        + "   group by cust_gender\n"
                        + "   order by cust_gender";

        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);
    }

    @Test
    void testCallFunction() throws JSQLParserException {
        String sqlStr =
                "call dbms_scheduler.auto_purge ( ) ";
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);
    }

}
