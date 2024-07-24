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

    @Test
    void testChainedFunctions() throws JSQLParserException {
        String sqlStr =
                "select f1(a1=1).f2 = 1";
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);

        sqlStr =
                "select f1(a1=1).f2(b).f2 = 1";
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);
    }


    @Test
    void testDatetimeParameter() throws JSQLParserException {
        String sqlStr = "SELECT DATE(DATETIME '2016-12-25 23:59:59')";
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);
    }

    @Test
    void testFunctionArrayParameter() throws JSQLParserException {
        String sqlStr = "select unnest(ARRAY[1,2,3], nested >= true) as a";

        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);
    }

    @Test
    void testSubSelectArrayWithoutKeywordParameter() throws JSQLParserException {
        String sqlStr = "SELECT\n" +
                "  email,\n" +
                "  REGEXP_CONTAINS(email, r'@[a-zA-Z0-9-]+\\.[a-zA-Z0-9-.]+') AS is_valid\n" +
                "FROM\n" +
                "  (SELECT\n" +
                "    ['foo@example.com', 'bar@example.org', 'www.example.net']\n" +
                "    AS addresses),\n" +
                "  UNNEST(addresses) AS email";
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);
    }

    @Test
    void testSubSelectParameterWithoutParentheses() throws JSQLParserException {
        String sqlStr = "SELECT COALESCE(SELECT mycolumn FROM mytable, 0)";
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true,
                parser -> parser.withUnparenthesizedSubSelects(true));
    }
}
