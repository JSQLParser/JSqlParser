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
import net.sf.jsqlparser.expression.operators.arithmetic.BitwiseAnd;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.test.TestUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class JdbcNamedParameterTest {
    @Test
    void testDoubleColon() throws JSQLParserException {
        String sqlStr = "select :test";
        PlainSelect select = (PlainSelect) TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);
        assertTrue(select.getSelectItems().get(0).getExpression() instanceof JdbcNamedParameter);
    }

    @Test
    void testAmpersand() throws JSQLParserException {
        String sqlStr = "select &test, 'a & b', a & b";
        PlainSelect select = (PlainSelect) TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);
        assertTrue(select.getSelectItems().get(0).getExpression() instanceof JdbcNamedParameter);
        assertTrue(select.getSelectItems().get(2).getExpression() instanceof BitwiseAnd);
    }

    @Test
    void testIssue1785() throws JSQLParserException {
        String sqlStr = "select * from all_tables\n"
                + "where owner = &myowner";
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);
    }

    @Test
    void testIssue1970() throws JSQLParserException {
        String sqlStr = "SELECT a from tbl where col = $2";
        PlainSelect select = (PlainSelect) TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);

        EqualsTo where = (EqualsTo) select.getWhere();
        Assertions.assertInstanceOf(JdbcParameter.class, where.getRightExpression());

        JdbcParameter p = (JdbcParameter) where.getRightExpression();
        Assertions.assertEquals(2, p.getIndex());
    }
}
