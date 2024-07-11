/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2024 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.expression;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.test.TestUtils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LambdaExpressionTest {

    @Test
    void testLambdaFunctionSingleParameter() throws JSQLParserException {
        String sqlStr = "select list_transform( split('test', ''),  x -> unicode(x) )";
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);
    }

    @Test
    void testNestedLambdaFunctionMultipleParameter() throws JSQLParserException {
        String sqlStr = "SELECT list_transform(\n" +
                "        [1, 2, 3],\n" +
                "        x -> list_reduce([4, 5, 6], (a, b) -> a + b) + x\n" +
                "    )";
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);
    }

    @Test
    void testLambdaMultiParameterIssue2030() throws JSQLParserException {
        String sqlStr = "SELECT map_filter(my_column, v -> v.my_inner_column = 'some_value')\n" +
                "FROM my_table";
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);
    }

    @Test
    void testLambdaMultiParameterIssue2032() throws JSQLParserException {
        String sqlStr = "SELECT  array_sort(array_agg(named_struct('depth', events_union.depth, 'eventtime',events_union.eventtime)), (left, right) -> case when(left.eventtime, left.depth) <(right.eventtime, right.depth) then -1 when(left.eventtime, left.depth) >(right.eventtime, right.depth) then 1 else 0 end) as col1 FROM your_table;";
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);
    }

}
