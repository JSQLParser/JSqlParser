/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2025 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.select;

import java.util.stream.Stream;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.test.TestUtils;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

class BigQueryArraySubselectTest {

    /**
     * IF is a reserved keyword and a function name. Directly after "[" it has to be read as a
     * function call, not rejected as a keyword.
     */
    @ParameterizedTest
    @ValueSource(strings = {
            // IF as the first and only array element
            "SELECT [IF(a > 1, 'x', NULL)] AS arr FROM t",
            // IF as the first of several elements
            "SELECT [IF(a > 1, 'x', NULL), 'y'] AS arr FROM t",
            // IF as a later element (the comma already allowed it)
            "SELECT ['a', IF(a > 1, 'x', NULL)] AS arr FROM t",
            // inside UNNEST
            "SELECT * FROM UNNEST([IF(a > 1, 'x', NULL)]) w",
            "SELECT * FROM UNNEST([IF(a > 1, 'x', NULL), 'y']) w",
            // inside another function call
            "SELECT ARRAY_LENGTH([IF(a > 1, 'x', NULL)]) FROM t",
            // inside an ARRAY subquery
            "SELECT ARRAY(SELECT w FROM UNNEST([IF(a > 1, 'x', NULL)]) w) AS flags FROM t",
            "SELECT ARRAY(SELECT w FROM UNNEST(['a', IF(a > 1, 'x', NULL)]) w) AS flags FROM t",
            // other expressions first
            "SELECT ARRAY(SELECT w FROM UNNEST([CASE WHEN a > 1 THEN 'x' END, 'y']) w) AS flags "
                    + "FROM t"})
    void parsesIfAsFirstArrayElement(String sqlStr) throws JSQLParserException {
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);
    }

    /**
     * A keyword right after "[" in a subscript must keep its keyword meaning.
     */
    @ParameterizedTest
    @ValueSource(strings = {
            "SELECT arr[OFFSET(1)] FROM t",
            "SELECT arr[SAFE_OFFSET(1)] FROM t",
            "SELECT arr[ORDINAL(1)] FROM t"})
    void subscriptKeywordsAreUnaffected(String sqlStr) throws JSQLParserException {
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);
    }

    /**
     * A backquoted dotted name is split into its parts and quoted again on output.
     */
    @ParameterizedTest
    @MethodSource("createTableAsSelect")
    void parsesCreateTableAsSelect(String sqlStr, String expected) throws JSQLParserException {
        TestUtils.assertStatementCanBeDeparsedAs(CCJSqlParserUtil.parse(sqlStr), expected, true);
    }

    static Stream<Arguments> createTableAsSelect() {
        return Stream.of(
                Arguments.of(
                        "CREATE OR REPLACE TABLE `p.d.t` AS\n"
                                + "SELECT ARRAY(SELECT w FROM UNNEST([IF(a > 1, 'x', NULL)]) w) AS flags\n"
                                + "FROM `p.d.src`",
                        "CREATE OR REPLACE TABLE \"p\".\"d\".\"t\" AS\n"
                                + "SELECT ARRAY(SELECT w FROM UNNEST([IF(a > 1, 'x', NULL)]) w) AS flags\n"
                                + "FROM \"p\".\"d\".\"src\""),
                Arguments.of(
                        "CREATE OR REPLACE TABLE `p.d.t` AS "
                                + "SELECT [IF(a > 1, 'x', NULL), 'y'] AS arr FROM `p.d.src`",
                        "CREATE OR REPLACE TABLE \"p\".\"d\".\"t\" AS "
                                + "SELECT [IF(a > 1, 'x', NULL), 'y'] AS arr FROM \"p\".\"d\".\"src\""));
    }
}
