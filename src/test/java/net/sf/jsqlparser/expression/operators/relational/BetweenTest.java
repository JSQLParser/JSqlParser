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
import net.sf.jsqlparser.expression.operators.arithmetic.Addition;
import net.sf.jsqlparser.expression.operators.arithmetic.Subtraction;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.ParenthesedSelect;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.test.TestUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class BetweenTest {
    @Test
    void testBetweenWithAdditionIssue1948() throws JSQLParserException {
        String sqlStr =
                "select col FROM tbl WHERE start_time BETWEEN 1706024185 AND MyFunc() - 734400";
        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "SELECT 1 FROM t WHERE d BETWEEN (SELECT m FROM x) - 1 AND (SELECT m FROM x)",
            "SELECT 1 FROM t WHERE d BETWEEN (SELECT m FROM x) - INTERVAL '7 days' AND now()"
    })
    void testBetweenWithSubqueryArithmeticInLowerBoundIssue2944(String sql)
            throws JSQLParserException {
        PlainSelect select = (PlainSelect) TestUtils.assertSqlCanBeParsedAndDeparsed(sql, true);
        Between between = assertInstanceOf(Between.class, select.getWhere());
        Subtraction lower =
                assertInstanceOf(Subtraction.class, between.getBetweenExpressionStart());
        assertInstanceOf(ParenthesedSelect.class, lower.getLeftExpression());
    }

    @Test
    void testBetweenWithSubqueryArithmeticInUpperBoundIssue2944() throws JSQLParserException {
        String sql =
                "SELECT 1 FROM t WHERE d BETWEEN (SELECT m FROM x) AND (SELECT m FROM x) + 1";
        PlainSelect select = (PlainSelect) TestUtils.assertSqlCanBeParsedAndDeparsed(sql, true);
        Between between = assertInstanceOf(Between.class, select.getWhere());
        assertInstanceOf(ParenthesedSelect.class, between.getBetweenExpressionStart());
        Addition upper = assertInstanceOf(Addition.class, between.getBetweenExpressionEnd());
        assertInstanceOf(ParenthesedSelect.class, upper.getLeftExpression());
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

    @Test
    void testBetweenWithOldOracleJoinSyntaxOnBothOperandsIssue672() throws JSQLParserException {
        String sqlStr =
                "SELECT * FROM table1 t1, table2 t2 WHERE t1.col1 BETWEEN t2.col2(+) AND t2.col3(+)";
        PlainSelect select = (PlainSelect) TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);
        Between between = (Between) select.getWhere();

        assertEquals(EqualsTo.ORACLE_JOIN_RIGHT,
                ((Column) between.getBetweenExpressionStart()).getOldOracleJoinSyntax());
        assertEquals(EqualsTo.ORACLE_JOIN_RIGHT,
                ((Column) between.getBetweenExpressionEnd()).getOldOracleJoinSyntax());
    }

    @Test
    void testBetweenWithOldOracleJoinSyntaxOnStartOperandIssue672() throws JSQLParserException {
        TestUtils.assertSqlCanBeParsedAndDeparsed(
                "SELECT * FROM table1 t1, table2 t2 WHERE t1.col1 BETWEEN t2.col2(+) AND 5", true);
    }

    @Test
    void testBetweenWithOldOracleJoinSyntaxOnEndOperandIssue672() throws JSQLParserException {
        TestUtils.assertSqlCanBeParsedAndDeparsed(
                "SELECT * FROM table1 t1, table2 t2 WHERE t1.col1 BETWEEN 1 AND t2.col3(+)", true);
    }

    @Test
    void testNotBetweenWithOldOracleJoinSyntaxIssue672() throws JSQLParserException {
        String sqlStr =
                "SELECT * FROM table1 t1, table2 t2 WHERE t1.col1 NOT BETWEEN t2.col2(+) AND t2.col3(+)";
        PlainSelect select = (PlainSelect) TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);
        Between between = (Between) select.getWhere();

        assertTrue(between.isNot());
        assertEquals(EqualsTo.ORACLE_JOIN_RIGHT,
                ((Column) between.getBetweenExpressionStart()).getOldOracleJoinSyntax());
        assertEquals(EqualsTo.ORACLE_JOIN_RIGHT,
                ((Column) between.getBetweenExpressionEnd()).getOldOracleJoinSyntax());
    }

    @Test
    void testBetweenSymmetricWithOldOracleJoinSyntaxIssue672() throws JSQLParserException {
        TestUtils.assertSqlCanBeParsedAndDeparsed(
                "SELECT * FROM t1, t2 WHERE t1.c BETWEEN SYMMETRIC t2.c(+) AND t2.d(+)", true);
    }

    @Test
    void testBetweenWithOldOracleJoinSyntaxInJoinOnClauseIssue672() throws JSQLParserException {
        TestUtils.assertSqlCanBeParsedAndDeparsed(
                "SELECT * FROM t1 JOIN t2 ON t1.c BETWEEN t2.c(+) AND t2.d(+)", true);
    }

    @Test
    void testBetweenWithOldOracleJoinSyntaxOnLeftOperand() throws JSQLParserException {
        TestUtils.assertSqlCanBeParsedAndDeparsed(
                "SELECT * FROM table1 t1, table2 t2 WHERE t2.col2(+) BETWEEN t1.col1 AND t1.col3",
                true);
    }

    @Test
    void testBetweenWithOldOracleJoinSyntaxBeforeComparisonSuffixOnOperand()
            throws JSQLParserException {
        // (+) directly followed by a comparison operator keeps the pre-existing
        // RegularConditionRHS path, where the marker sits on the comparison itself
        String sqlStr =
                "SELECT * FROM t1, t2 WHERE t1.c BETWEEN t2.c(+) = 5 AND 1";
        PlainSelect select = (PlainSelect) TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);
        Between between = (Between) select.getWhere();
        EqualsTo comparison = (EqualsTo) between.getBetweenExpressionStart();

        assertEquals(EqualsTo.ORACLE_JOIN_RIGHT, comparison.getOldOracleJoinSyntax());
        assertEquals(EqualsTo.NO_ORACLE_JOIN,
                ((Column) comparison.getLeftExpression()).getOldOracleJoinSyntax());
    }
}
