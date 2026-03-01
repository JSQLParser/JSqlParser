/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2023 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.select;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.test.TestUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;


class SampleClauseTest {

    @ParameterizedTest
    @ValueSource(strings = {
            "SELECT * FROM fact_halllogin_detail TABLESAMPLE BERNOULLI (10) where dt>=20220710 limit 10",
            "SELECT * FROM fact_halllogin_detail TABLESAMPLE BERNOULLI (10.1) where dt>=20220710 limit 10",
            "SELECT * FROM fact_halllogin_detail TABLESAMPLE SYSTEM (10) where dt>=20220710 limit 10",
            "SELECT * FROM fact_halllogin_detail TABLESAMPLE SYSTEM (10) REPEATABLE (10) where dt>=20220710 limit 10",
            "SELECT * FROM fact_halllogin_detail TABLESAMPLE SYSTEM (10.0) REPEATABLE (10.1) where dt>=20220710 limit 10"
    })
    void standardTestIssue1593(String sqlStr) throws JSQLParserException {
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "SELECT * from table_name SAMPLE(99)", "SELECT * from table_name SAMPLE(99.1)",
            "SELECT * from table_name SAMPLE BLOCK (99)",
            "SELECT * from table_name SAMPLE BLOCK (99.1)",
            "SELECT * from table_name SAMPLE BLOCK (99) SEED (10) ",
            "SELECT * from table_name SAMPLE BLOCK (99.1) SEED (10.1)"
    })
    void standardOracleIssue1826(String sqlStr) throws JSQLParserException {
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "SELECT * FROM events SAMPLE 0.1",
            "SELECT * FROM events SAMPLE 10000",
            "SELECT * FROM events SAMPLE 0.1 OFFSET 1000"
    })
    void clickHouseSampleClause(String sqlStr) throws JSQLParserException {
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);
    }

    @Test
    void testDuckDB() throws JSQLParserException {
        String sqlStr = "SELECT *\n" +
                "FROM (SELECT * FROM addresses)\n" +
                "USING SAMPLE SYSTEM (10 PERCENT);";
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);
    }

    @Test
    void testBigQuery() throws JSQLParserException {
        String sqlStr = "SELECT *\n" +
                "FROM (SELECT * FROM addresses)\n" +
                "TABLESAMPLE SYSTEM (10 PERCENT);";
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);
    }
}
