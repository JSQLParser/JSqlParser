/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2024 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.expression.operators.relational;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.test.TestUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BetweenTest {
    @Test
    void testBetweenWithAdditionIssue1948() throws JSQLParserException {
        String sqlStr =
                "select col FROM tbl WHERE start_time BETWEEN 1706024185 AND MyFunc() - 734400";
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);
    }

    @Test
    void testBetweenSymmetricIssue2250() throws JSQLParserException {
        String sqlStr =
                "SELECT *\n"
                        + "FROM orders\n"
                        + "WHERE 100 BETWEEN SYMMETRIC total_price AND discount_price;\n";
        PlainSelect select = (PlainSelect) TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);
        Between between = (Between) select.getWhere();

        Assertions.assertTrue(between.isUsingSymmetric());
        Assertions.assertFalse(between.isUsingAsymmetric());
    }

    @Test
    void testBetweenASymmetricIssue2250() throws JSQLParserException {
        String sqlStr =
                "SELECT *\n"
                        + "FROM orders\n"
                        + "WHERE 100 BETWEEN ASYMMETRIC total_price AND discount_price;\n";
        PlainSelect select = (PlainSelect) TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);
        Between between = (Between) select.getWhere();

        Assertions.assertFalse(between.isUsingSymmetric());
        Assertions.assertTrue(between.isUsingAsymmetric());
    }
}
