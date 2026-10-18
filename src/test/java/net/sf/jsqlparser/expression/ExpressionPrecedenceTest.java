/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.expression;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.operators.arithmetic.Addition;
import net.sf.jsqlparser.expression.operators.arithmetic.BitwiseAnd;
import net.sf.jsqlparser.expression.operators.arithmetic.BitwiseLeftShift;
import net.sf.jsqlparser.expression.operators.arithmetic.BitwiseOr;
import net.sf.jsqlparser.expression.operators.arithmetic.BitwiseRightShift;
import net.sf.jsqlparser.expression.operators.arithmetic.BitwiseXor;
import net.sf.jsqlparser.expression.operators.arithmetic.Concat;
import net.sf.jsqlparser.expression.operators.arithmetic.Division;
import net.sf.jsqlparser.expression.operators.arithmetic.Modulo;
import net.sf.jsqlparser.expression.operators.arithmetic.Multiplication;
import net.sf.jsqlparser.expression.operators.arithmetic.Subtraction;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.relational.Between;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.ExistsExpression;
import net.sf.jsqlparser.expression.operators.relational.InExpression;
import net.sf.jsqlparser.expression.operators.relational.Intersects;
import net.sf.jsqlparser.expression.operators.relational.IsNullExpression;
import net.sf.jsqlparser.expression.operators.relational.LikeExpression;
import net.sf.jsqlparser.expression.operators.relational.ParenthesedExpressionList;
import net.sf.jsqlparser.parser.AbstractJSqlParser.Dialect;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.test.TestUtils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 *
 * @author tw
 */
public class ExpressionPrecedenceTest {

    @Test
    public void testGetSign() throws JSQLParserException {
        // || binds like +, so tighter than &
        Expression expr = CCJSqlParserUtil.parseExpression("1&2||3");
        Assertions.assertInstanceOf(BitwiseAnd.class, expr);
        Assertions.assertInstanceOf(LongValue.class, ((BitwiseAnd) expr).getLeftExpression());
        Assertions.assertInstanceOf(Concat.class, ((BitwiseAnd) expr).getRightExpression());

        // PostgreSQL puts || and & on one level
        expr = CCJSqlParserUtil.parseExpression("1&2||3", false,
                parser -> parser.withDialect(Dialect.POSTGRESQL));
        Assertions.assertInstanceOf(Concat.class, expr);
        Assertions.assertInstanceOf(BitwiseAnd.class, ((Concat) expr).getLeftExpression());
        Assertions.assertInstanceOf(LongValue.class, ((Concat) expr).getRightExpression());
    }

    @Test
    public void testNotNotConditionIssue1170() throws JSQLParserException {
        Expression expr = CCJSqlParserUtil.parseCondExpression("not not 1 = 1");
        NotExpression outer = Assertions.assertInstanceOf(NotExpression.class, expr);
        NotExpression inner = Assertions.assertInstanceOf(NotExpression.class,
                outer.getExpression());
        Assertions.assertInstanceOf(EqualsTo.class, inner.getExpression());
        Assertions.assertEquals("NOT NOT 1 = 1", expr.toString());
    }

    @Test
    public void testNotNotNotConditionIssue1170() throws JSQLParserException {
        Expression expr = CCJSqlParserUtil.parseCondExpression("not not not 1 = 1");
        NotExpression first = Assertions.assertInstanceOf(NotExpression.class, expr);
        NotExpression second = Assertions.assertInstanceOf(NotExpression.class,
                first.getExpression());
        NotExpression third = Assertions.assertInstanceOf(NotExpression.class,
                second.getExpression());
        Assertions.assertInstanceOf(EqualsTo.class, third.getExpression());
        Assertions.assertEquals("NOT NOT NOT 1 = 1", expr.toString());
    }

    @Test
    public void testNotNotExistsIssue1170() throws JSQLParserException {
        Expression expr =
                CCJSqlParserUtil.parseCondExpression("not not exists (select 1 from mytable)");
        NotExpression outer = Assertions.assertInstanceOf(NotExpression.class, expr);
        NotExpression inner = Assertions.assertInstanceOf(NotExpression.class,
                outer.getExpression());
        Assertions.assertInstanceOf(ExistsExpression.class, inner.getExpression());
    }

    @Test
    public void testNotNotInIssue1170() throws JSQLParserException {
        Expression expr = CCJSqlParserUtil.parseCondExpression("not not a in (1, 2)");
        NotExpression outer = Assertions.assertInstanceOf(NotExpression.class, expr);
        NotExpression inner = Assertions.assertInstanceOf(NotExpression.class,
                outer.getExpression());
        Assertions.assertInstanceOf(InExpression.class, inner.getExpression());
    }

    @Test
    public void testNotNotNotBetweenIssue1170() throws JSQLParserException {
        Expression expr = CCJSqlParserUtil.parseCondExpression("not not not a between 1 and 2");
        NotExpression first = Assertions.assertInstanceOf(NotExpression.class, expr);
        NotExpression second = Assertions.assertInstanceOf(NotExpression.class,
                first.getExpression());
        NotExpression third = Assertions.assertInstanceOf(NotExpression.class,
                second.getExpression());
        Assertions.assertInstanceOf(Between.class, third.getExpression());
    }

    @Test
    public void testNotNotNotWhereClauseIssue1170() throws JSQLParserException {
        TestUtils.assertSqlCanBeParsedAndDeparsed("SELECT * FROM mytable WHERE NOT NOT NOT a = 1");
    }

    @Test
    public void testMixedNotAndExclamationMarkIssue1170() throws JSQLParserException {
        Expression expr = CCJSqlParserUtil.parseCondExpression("not !1 = 1");
        NotExpression outer = Assertions.assertInstanceOf(NotExpression.class, expr);
        Assertions.assertFalse(outer.isExclamationMark());
        NotExpression inner = Assertions.assertInstanceOf(NotExpression.class,
                outer.getExpression());
        Assertions.assertTrue(inner.isExclamationMark());
        Assertions.assertInstanceOf(EqualsTo.class, inner.getExpression());
        Assertions.assertEquals("NOT ! 1 = 1", expr.toString());
    }

    @Test
    public void testSingleNotConditionStaysFlat() throws JSQLParserException {
        Expression expr = CCJSqlParserUtil.parseCondExpression("not 1 = 1");
        NotExpression not = Assertions.assertInstanceOf(NotExpression.class, expr);
        Assertions.assertInstanceOf(EqualsTo.class, not.getExpression());
    }

    @Test
    public void testNotNotLikeIssue1170() throws JSQLParserException {
        Expression expr = CCJSqlParserUtil.parseCondExpression("not not a like 'x'");
        NotExpression outer = Assertions.assertInstanceOf(NotExpression.class, expr);
        NotExpression inner = Assertions.assertInstanceOf(NotExpression.class,
                outer.getExpression());
        LikeExpression like = Assertions.assertInstanceOf(LikeExpression.class,
                inner.getExpression());
        Assertions.assertFalse(like.isNot());
        Assertions.assertInstanceOf(Column.class, like.getLeftExpression());
    }

    @Test
    public void testNotNotIsNullIssue1170() throws JSQLParserException {
        Expression expr = CCJSqlParserUtil.parseCondExpression("not not a is null");
        NotExpression outer = Assertions.assertInstanceOf(NotExpression.class, expr);
        NotExpression inner = Assertions.assertInstanceOf(NotExpression.class,
                outer.getExpression());
        IsNullExpression isNull = Assertions.assertInstanceOf(IsNullExpression.class,
                inner.getExpression());
        Assertions.assertFalse(isNull.isNot());
        Assertions.assertInstanceOf(Column.class, isNull.getLeftExpression());
    }

    @Test
    public void testNotNotSimilarToIssue1170() throws JSQLParserException {
        Expression expr = CCJSqlParserUtil.parseCondExpression("not not a similar to 'x'");
        NotExpression outer = Assertions.assertInstanceOf(NotExpression.class, expr);
        NotExpression inner = Assertions.assertInstanceOf(NotExpression.class,
                outer.getExpression());
        LikeExpression similar = Assertions.assertInstanceOf(LikeExpression.class,
                inner.getExpression());
        Assertions.assertFalse(similar.isNot());
        Assertions.assertInstanceOf(Column.class, similar.getLeftExpression());
    }

    @Test
    public void testNotNotInsideAndChainIssue1170() throws JSQLParserException {
        Expression expr = CCJSqlParserUtil.parseCondExpression("a and not not b = 1");
        AndExpression and = Assertions.assertInstanceOf(AndExpression.class, expr);
        Assertions.assertInstanceOf(Column.class, and.getLeftExpression());
        NotExpression outer = Assertions.assertInstanceOf(NotExpression.class,
                and.getRightExpression());
        NotExpression inner = Assertions.assertInstanceOf(NotExpression.class,
                outer.getExpression());
        EqualsTo equalsTo = Assertions.assertInstanceOf(EqualsTo.class, inner.getExpression());
        Assertions.assertInstanceOf(Column.class, equalsTo.getLeftExpression());
    }

    @Test
    public void testNotNotCaseInsensitiveIssue1170() throws JSQLParserException {
        Expression expr = CCJSqlParserUtil.parseCondExpression("NoT nOt 1 = 1");
        NotExpression outer = Assertions.assertInstanceOf(NotExpression.class, expr);
        NotExpression inner = Assertions.assertInstanceOf(NotExpression.class,
                outer.getExpression());
        Assertions.assertInstanceOf(EqualsTo.class, inner.getExpression());
    }

    @Test
    public void testEightNotIssue1170() throws JSQLParserException {
        Expression expr = CCJSqlParserUtil
                .parseCondExpression("not not not not not not not not 1 = 1");
        int depth = 0;
        while (expr instanceof NotExpression) {
            expr = ((NotExpression) expr).getExpression();
            depth++;
        }
        Assertions.assertEquals(8, depth);
        Assertions.assertInstanceOf(EqualsTo.class, expr);
    }

    @Test
    public void testNotNotParenthesizedConditionIssue1170() throws JSQLParserException {
        Expression expr = CCJSqlParserUtil.parseCondExpression("not not (a = 1 and b = 2)");
        NotExpression outer = Assertions.assertInstanceOf(NotExpression.class, expr);
        NotExpression inner = Assertions.assertInstanceOf(NotExpression.class,
                outer.getExpression());
        Assertions.assertInstanceOf(ParenthesedExpressionList.class, inner.getExpression());
    }

    @Test
    public void testNotNotInStatementPositionsIssue1170() throws JSQLParserException {
        TestUtils.assertSqlCanBeParsedAndDeparsed(
                "SELECT * FROM t WHERE a IN (SELECT id FROM u WHERE NOT NOT x = 1)");
        TestUtils.assertSqlCanBeParsedAndDeparsed("UPDATE t SET a = 1 WHERE NOT NOT b = 2");
        TestUtils.assertSqlCanBeParsedAndDeparsed("DELETE FROM t WHERE NOT NOT b = 2");
    }

    @Test
    public void testValuePositionNotStaysOnOperand() throws JSQLParserException {
        Expression equalsTo = CCJSqlParserUtil.parseCondExpression("a = not 1");
        EqualsTo eq = Assertions.assertInstanceOf(EqualsTo.class, equalsTo);
        Assertions.assertInstanceOf(Column.class, eq.getLeftExpression());
        Assertions.assertInstanceOf(NotExpression.class, eq.getRightExpression());

        Expression addition = CCJSqlParserUtil.parseCondExpression("1 + not 2");
        Addition add = Assertions.assertInstanceOf(Addition.class, addition);
        Assertions.assertInstanceOf(NotExpression.class, add.getRightExpression());
    }

    @Test
    public void testCaretBindsTighterThanMultiplication() throws JSQLParserException {
        // PostgreSQL's power and MySQL's XOR both evaluate 2 * 3 ^ 2 as 2 * (3 ^ 2)
        Multiplication multiplication = Assertions.assertInstanceOf(Multiplication.class,
                CCJSqlParserUtil.parseExpression("2 * 3 ^ 2"));
        Assertions.assertInstanceOf(LongValue.class, multiplication.getLeftExpression());
        Assertions.assertInstanceOf(BitwiseXor.class, multiplication.getRightExpression());

        Division division = Assertions.assertInstanceOf(Division.class,
                CCJSqlParserUtil.parseExpression("10 / 2 ^ 2"));
        Assertions.assertInstanceOf(BitwiseXor.class, division.getRightExpression());

        Modulo modulo = Assertions.assertInstanceOf(Modulo.class,
                CCJSqlParserUtil.parseExpression("18 % 4 ^ 2"));
        Assertions.assertInstanceOf(BitwiseXor.class, modulo.getRightExpression());

        multiplication = Assertions.assertInstanceOf(Multiplication.class,
                CCJSqlParserUtil.parseExpression("2 ^ 3 * 2"));
        Assertions.assertInstanceOf(BitwiseXor.class, multiplication.getLeftExpression());

        TestUtils.assertSqlCanBeParsedAndDeparsed("SELECT pi() * r ^ 2 FROM circles");
    }

    @Test
    public void testCaretIsLeftAssociative() throws JSQLParserException {
        BitwiseXor outer = Assertions.assertInstanceOf(BitwiseXor.class,
                CCJSqlParserUtil.parseExpression("2 ^ 3 ^ 2"));
        Assertions.assertInstanceOf(BitwiseXor.class, outer.getLeftExpression());
        Assertions.assertInstanceOf(LongValue.class, outer.getRightExpression());
    }

    @Test
    public void testBitwiseOperatorsBindLooserThanAddition() throws JSQLParserException {
        // PostgreSQL, MySQL and SQLite all evaluate 1 & 7 - 2 as 1 & (7 - 2)
        BitwiseAnd and = Assertions.assertInstanceOf(BitwiseAnd.class,
                CCJSqlParserUtil.parseExpression("1 & 7 - 2"));
        Assertions.assertInstanceOf(LongValue.class, and.getLeftExpression());
        Assertions.assertInstanceOf(Subtraction.class, and.getRightExpression());

        BitwiseOr or = Assertions.assertInstanceOf(BitwiseOr.class,
                CCJSqlParserUtil.parseExpression("a | b + c"));
        Assertions.assertInstanceOf(Addition.class, or.getRightExpression());

        BitwiseLeftShift leftShift = Assertions.assertInstanceOf(BitwiseLeftShift.class,
                CCJSqlParserUtil.parseExpression("1 << n - 1"));
        Assertions.assertInstanceOf(Subtraction.class, leftShift.getRightExpression());

        BitwiseRightShift rightShift = Assertions.assertInstanceOf(BitwiseRightShift.class,
                CCJSqlParserUtil.parseExpression("a + b >> 2"));
        Assertions.assertInstanceOf(Addition.class, rightShift.getLeftExpression());

        Intersects xor = Assertions.assertInstanceOf(Intersects.class,
                CCJSqlParserUtil.parseExpression("a # b + c", false,
                        parser -> parser.withDialect(Dialect.POSTGRESQL)));
        Assertions.assertInstanceOf(Addition.class, xor.getRightExpression());

        TestUtils.assertSqlCanBeParsedAndDeparsed("SELECT flags & 1 << bit - 1 FROM t");
    }

    @Test
    public void testBitwiseOperatorsShareOneLeftAssociativeLevel() throws JSQLParserException {
        // as in PostgreSQL and SQLite: 4 | 1 & 0 is (4 | 1) & 0
        BitwiseAnd and = Assertions.assertInstanceOf(BitwiseAnd.class,
                CCJSqlParserUtil.parseExpression("4 | 1 & 0"));
        Assertions.assertInstanceOf(BitwiseOr.class, and.getLeftExpression());
    }

    @Test
    public void testSpaceSeparatedConcatStillBindsLikeAddition() throws JSQLParserException {
        BitwiseAnd and = Assertions.assertInstanceOf(BitwiseAnd.class,
                CCJSqlParserUtil.parseExpression("a & b | | c"));
        Assertions.assertInstanceOf(Concat.class, and.getRightExpression());
    }
}
