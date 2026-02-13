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
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.test.TestUtils;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

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

    @Test
    void testSimpleFunctionIssue2059() throws JSQLParserException {
        String sqlStr = "select count(*) from zzz";
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true, parser -> {
            parser.withAllowComplexParsing(false);
        });
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "select LISTAGG(field, ',' on overflow truncate '...') from dual",
            "select LISTAGG(field, ',' on overflow truncate '...' with count) from dual",
            "select LISTAGG(field, ',' on overflow truncate '...' without count) from dual",
            "select LISTAGG(field, ',' on overflow error) from dual", "SELECT department, \n" +
                    "       LISTAGG(name, ', ' ON OVERFLOW TRUNCATE '... (truncated)' WITH COUNT) WITHIN GROUP (ORDER BY name)\n"
                    +
                    "       AS employee_names\n" +
                    "FROM employees\n" +
                    "GROUP BY department;"
    })
    void testListAggOnOverflow(String sqlStr) throws Exception {
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "select RTRIM('string')",
            "select LTRIM('string')",
            "select RTRIM(field) from dual",
            "select LTRIM(field) from dual"
    })
    void testTrimFunctions(String sqlStr) throws JSQLParserException {
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);
    }

    @Test
    void TestIntervalParameterIssue2272() throws JSQLParserException {
        String sqlStr =
                "SELECT DATE_SUB('2025-06-19', INTERVAL QUARTER(STR_TO_DATE('20250619', '%Y%m%d')) - 1 QUARTER) from dual";
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "select json_query('{\"a\":1}', 'strict $.keyvalue()' WITH WRAPPER) from tbl",
            "select json_query('{\"a\":1}', 'strict $.keyvalue()' WITH ARRAY WRAPPER) from tbl",
            "select json_query('{\"a\":1}', 'strict $.keyvalue()' WITHOUT WRAPPER) from tbl",
            "select json_query('{\"a\":1}', 'strict $.keyvalue()' WITHOUT ARRAY WRAPPER) from tbl",
            "select json_query('{\"a\":1}', 'strict $.keyvalue()' WITH CONDITIONAL ARRAY WRAPPER) from tbl",
            "select json_query('{\"a\":1}', '$' ERROR ON ERROR) from tbl",
            "select json_query('{\"a\":1}', '$' RETURNING VARCHAR(100) NULL ON EMPTY) from tbl"
    })
    void testJsonQueryWithWrapperClauseInsideFunctionParameters(String sqlStr)
            throws JSQLParserException {
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true,
                parser -> parser.withAllowComplexParsing(false));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "select json_query('{\"customer\" : 100, \"region\" : \"AFRICA\"}', 'strict $.keyvalue()' WITH ARRAY WRAPPER, '$.region') from tbl",
            "select json_query('{\"customer\" : 100, \"region\" : \"AFRICA\"}', '$' RETURNING VARCHAR(100), '$.region') from tbl",
            "select json_query('{\"customer\" : 100, \"region\" : \"AFRICA\"}', '$' ERROR ON ERROR, '$.region') from tbl"
    })
    void testJsonQueryTrailingClauseBeforeLastParameterKeepsAdditionalParameters(String sqlStr)
            throws JSQLParserException {
        PlainSelect select = (PlainSelect) TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true,
                parser -> parser.withAllowComplexParsing(false));
        Function function = select.getSelectItem(0).getExpression(Function.class);

        Assertions.assertThat(function.getParameters()).hasSize(3);
        if (sqlStr.contains("WITH ARRAY WRAPPER")) {
            Assertions.assertThat(function.getParameters().get(1).toString())
                    .isEqualTo("'strict $.keyvalue()' WITH ARRAY WRAPPER");
        } else if (sqlStr.contains("RETURNING")) {
            Assertions.assertThat(function.getParameters().get(1).toString())
                    .isEqualTo("'$' RETURNING VARCHAR ( 100 )");
        } else {
            Assertions.assertThat(function.getParameters().get(1).toString())
                    .isEqualTo("'$' ERROR ON ERROR");
        }
        Assertions.assertThat(function.getParameters().get(2)).isInstanceOf(StringValue.class);
        Assertions.assertThat(function.getParameters().get(2).toString()).isEqualTo("'$.region'");
        Assertions.assertThat(function.getParameterTrailingClauses()).hasSize(3);
    }

    @Test
    void testFunctionSupportsMultipleParameterLevelTrailingClauses() throws JSQLParserException {
        String sqlStr =
                "select json_query('{\"a\":1}', '$' ERROR ON ERROR, '$.x' RETURNING VARCHAR(10), '$.z' WITH ARRAY WRAPPER) from tbl";

        PlainSelect select = (PlainSelect) TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true,
                parser -> parser.withAllowComplexParsing(false));
        Function function = select.getSelectItem(0).getExpression(Function.class);

        Assertions.assertThat(function.getParameters()).hasSize(4);
        Assertions.assertThat(function.getParameters().get(1).toString())
                .isEqualTo("'$' ERROR ON ERROR");
        Assertions.assertThat(function.getParameters().get(2).toString())
                .isEqualTo("'$.x' RETURNING VARCHAR ( 10 )");
        Assertions.assertThat(function.getParameters().get(3).toString())
                .isEqualTo("'$.z' WITH ARRAY WRAPPER");

        Assertions.assertThat(function.getParameterExpression(1).toString()).isEqualTo("'$'");
        Assertions.assertThat(function.getParameterExpression(2).toString()).isEqualTo("'$.x'");
        Assertions.assertThat(function.getParameterExpression(3).toString()).isEqualTo("'$.z'");
        Assertions.assertThat(function.getParameterTrailingClause(1)).isEqualTo("ERROR ON ERROR");
        Assertions.assertThat(function.getParameterTrailingClause(2))
                .isEqualTo("RETURNING VARCHAR ( 10 )");
        Assertions.assertThat(function.getParameterTrailingClause(3))
                .isEqualTo("WITH ARRAY WRAPPER");
        Assertions.assertThat(function.getParameterTrailingClauses())
                .containsExactly(null, "ERROR ON ERROR", "RETURNING VARCHAR ( 10 )",
                        "WITH ARRAY WRAPPER");
    }
}
