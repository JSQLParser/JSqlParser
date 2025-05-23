/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2023 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.expression.operators.relational;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.test.TestUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class InExpressionTest {

    @Test
    void testOracleInWithoutBrackets() throws JSQLParserException {
        String sqlStr = "select 1 from dual where a in 1 ";
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);
    }


    @Test
    void testOracleInWithBrackets() throws JSQLParserException {
        String sqlStr = "select 1 from dual where a in (1) ";
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);
    }

    @Test
    void testPrecedenceIssue2244() throws JSQLParserException {
        String sqlStr = "select * from `T_DEMO` where a in (1,3,2) or b = 2";
        PlainSelect select = (PlainSelect) TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);

        Assertions.assertInstanceOf(OrExpression.class, select.getWhere());
    }

}
