/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.util.cnfexpression;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class CNFTest {

    /**
     * The purpose of this method is to check when there is a Not Operator at the root. Which means
     * the root must be switched.
     *
     * Here is the expression tree:
     *
     * NOT | ( ) | AND / \ ( ) ( ) | | OR OR / \ / \ < = != >= / \ / \ / \ / \ 1.2 2.3 3.5 4.6 1.1
     * 2.5 8.0 7.2
     *
     * Here is the converted expression tree:
     *
     * AND / \ AND ( ) / \ | AND ( ) OR / \ | / \ ( ) ( ) OR NOT NOT | | / \ | | OR OR NOT NOT = >=
     * / \ / \ | | / \ / \ NOT NOT NOT NOT = != 3.5 4.6 8.0 7.2 | | | | / \ / \ < != < >= / \ / \ /
     * \ / \ 1.2 2.3 1.1 2.5 1.2 2.3 8.0 7.2
     *
     */
    @Test
    public void test1() throws Exception {
        Expression expr = CCJSqlParserUtil.parseCondExpression(
                "NOT ((1.2 < 2.3 OR 3.5 = 4.6) AND (1.1 <> 2.5 OR 8.0 >= 7.2))");
        Expression expected = CCJSqlParserUtil.parseCondExpression(
                "(NOT 1.2 < 2.3 OR NOT 1.1 <> 2.5) AND (NOT 1.2 < 2.3 OR NOT 8.0 >= 7.2) AND"
                        + " (NOT 3.5 = 4.6 OR NOT 1.1 <> 2.5) AND (NOT 3.5 = 4.6 OR NOT 8.0 >= 7.2)");
        Expression result = CNFConverter.convertToCNF(expr);
        assertEquals(expected.toString(), result.toString());
    }

    /**
     * The purpose is to test the double negation law. As you can see when you build the tree, there
     * will be two Not Operators together on the line. It is there when we use the double negation
     * law.
     *
     * Here is the expression tree: ( ) | OR / \ ( ) ( ) | | NOT AND | / \ ( ) LIKE = | / \ / \ OR
     * S.A "%%%" S.B "orz" / \ NOT < | / \ >= 3.3 4.5 / \ 1.1 2.3
     *
     * Here is the converted expression tree:
     *
     * AND / \ AND ( ) / \ | AND ( ) OR / \ | / \ ( ) ( ) OR NOT = | | / \ | / \ OR OR NOT LIKE <
     * S.B "orz" / \ / \ | / \ / \ >= LIKE >= = < S.A "%%%" 3.3 4.5 / \ / \ / \ / \ 1.1 2.3 S.A
     * "%%%" 1.1 2.3 S.B "orz"
     *
     */
    @Test
    public void test2() throws Exception {
        Expression expr = CCJSqlParserUtil.parseCondExpression(
                "((NOT (NOT 1.1 >= 2.3 OR 3.3 < 4.5)) OR "
                        + "(S.A LIKE '\"%%%\"' AND S.B = '\"orz\"'))");
        Expression expected = CCJSqlParserUtil.parseCondExpression(
                "(1.1 >= 2.3 OR S.A LIKE '\"%%%\"') AND (1.1 >= 2.3 OR S.B = '\"orz\"')"
                        + " AND (NOT 3.3 < 4.5 OR S.A LIKE '\"%%%\"') AND (NOT 3.3 < 4.5 OR S.B = '\"orz\"')");
        Expression result = CNFConverter.convertToCNF(expr);
        assertEquals(expected.toString(), result.toString());
    }

    /**
     * This is the case when we test a more complex tree structure, Notice you could see the amount
     * of line to build up the CNF tree. You could tell how complicated the CNF could be.
     *
     * OR / \ ( ) ( ) | | AND OR / \ / \ >= <= ( ) NOT / \ / \ | | 7.0 8.0 9.0 10.0 AND OR / \ / \ (
     * ) = != ( ) | / \ / \ | AND 11.0 12.0 13.0 14.0 AND / \ / \ < > = ( ) / \ / \ / \ | 7.0 8.0
     * 9.0 10.0 15.0 16.0 OR / \ = > / \ / \ 17.0 18.0 19.0 20.0
     *
     * Here is the converted expression tree:
     *
     * AND / \ AND ( ) / \ | AND ( ) part18 / \ | AND ( ) part17 / \ | AND ( ) part16 / \ | AND ( )
     * part15 / \ | AND ( ) part14 / \ | AND ( ) part13 / \ | AND ( ) part12 / \ | AND ( ) part11 /
     * \ | AND ( ) part10 / \ | AND ( ) part9 / \ | AND ( ) part8 / \ | AND ( ) part7 / \ | AND ( )
     * part6 / \ | AND ( ) part5 / \ | AND ( ) part4 / \ | ( ) ( ) part3 | | part1 part2
     *
     * part1: OR / \ OR NOT / \ | >= < != / \ / \ / \ 3.0 4.0 7.0 8.0 13.0 14.0
     *
     * part2: OR / \ OR NOT / \ | OR NOT = / \ | / \ >= < = 17.0 18.0 / \ / \ / \ 3.0 4.0 7.0 8.0
     * 15.0 16.0
     *
     * part3: OR / \ OR NOT / \ | OR NOT = / \ | / \ >= < = 19.0 20.0 / \ / \ / \ 3.0 4.0 7.0 8.0
     * 15.0 16.0
     *
     * part4: OR / \ OR NOT / \ | >= < != / \ / \ / \ 3.0 4.0 9.0 10.0 13.0 14.0
     *
     * part5: OR / \ OR NOT / \ | OR NOT = / \ | / \ >= < = 17.0 18.0 / \ / \ / \ 3.0 4.0 9.0 10.0
     * 15.0 16.0
     *
     * part6: OR / \ OR NOT / \ | OR NOT = / \ | / \ >= < = 19.0 20.0 / \ / \ / \ 3.0 4.0 9.0 10.0
     * 15.0 16.0
     *
     * part7: OR / \ OR NOT / \ | >= < != / \ / \ / \ 3.0 4.0 11.0 12.0 13.0 14.0
     *
     * part8: OR / \ OR NOT / \ | OR NOT = / \ | / \ >= < = 17.0 18.0 / \ / \ / \ 3.0 4.0 11.0 12.0
     * 15.0 16.0
     *
     * part9: OR / \ OR NOT / \ | OR NOT = / \ | / \ >= < = 19.0 20.0 / \ / \ / \ 3.0 4.0 11.0 12.0
     * 15.0 16.0
     *
     * part10: OR / \ OR NOT / \ | >= < != / \ / \ / \ 5.0 6.0 7.0 8.0 13.0 14.0
     *
     * part11: OR / \ OR NOT / \ | OR NOT = / \ | / \ >= < = 17.0 18.0 / \ / \ / \ 5.0 6.0 7.0 8.0
     * 15.0 16.0
     *
     * part12: OR / \ OR NOT / \ | OR NOT = / \ | / \ >= < = 19.0 20.0 / \ / \ / \ 5.0 6.0 7.0 8.0
     * 15.0 16.0
     *
     * part13: OR / \ OR NOT / \ | >= < != / \ / \ / \ 5.0 6.0 9.0 10.0 13.0 14.0
     *
     * part14: OR / \ OR NOT / \ | OR NOT = / \ | / \ >= < = 17.0 18.0 / \ / \ / \ 5.0 6.0 9.0 10.0
     * 15.0 16.0
     *
     * part15: OR / \ OR NOT / \ | OR NOT = / \ | / \ >= < = 19.0 20.0 / \ / \ / \ 5.0 6.0 9.0 10.0
     * 15.0 16.0
     *
     * part16: OR / \ OR NOT / \ | >= < != / \ / \ / \ 5.0 6.0 11.0 12.0 13.0 14.0
     *
     * part17: OR / \ OR NOT / \ | OR NOT = / \ | / \ >= < = 17.0 18.0 / \ / \ / \ 5.0 6.0 11.0 12.0
     * 15.0 16.0
     *
     * part18: OR / \ OR NOT / \ | OR NOT = / \ | / \ >= < = 19.0 20.0 / \ / \ / \ 5.0 6.0 11.0 12.0
     * 15.0 16.0
     *
     */
    @Test
    public void test3() throws Exception {
        Expression expr = CCJSqlParserUtil.parseCondExpression(
                "(3.0 >= 4.0 AND 5.0 <= 6.0) OR "
                        + "(((7.0 < 8.0 AND 9.0 > 10.0) AND 11.0 = 12.0) OR "
                        + "NOT (13.0 <> 14.0 OR (15.0 = 16.0 AND (17.0 = 18.0 OR 19.0 > 20.0))))");
        Expression expected = CCJSqlParserUtil.parseCondExpression(
                "(3.0 >= 4.0 OR 7.0 < 8.0 OR NOT 13.0 <> 14.0) AND "
                        + "(3.0 >= 4.0 OR 7.0 < 8.0 OR NOT 15.0 = 16.0 OR NOT 17.0 = 18.0) AND "
                        + "(3.0 >= 4.0 OR 7.0 < 8.0 OR NOT 15.0 = 16.0 OR NOT 19.0 > 20.0) AND "
                        + "(3.0 >= 4.0 OR 9.0 > 10.0 OR NOT 13.0 <> 14.0) AND "
                        + "(3.0 >= 4.0 OR 9.0 > 10.0 OR NOT 15.0 = 16.0 OR NOT 17.0 = 18.0) AND "
                        + "(3.0 >= 4.0 OR 9.0 > 10.0 OR NOT 15.0 = 16.0 OR NOT 19.0 > 20.0) AND "
                        + "(3.0 >= 4.0 OR 11.0 = 12.0 OR NOT 13.0 <> 14.0) AND "
                        + "(3.0 >= 4.0 OR 11.0 = 12.0 OR NOT 15.0 = 16.0 OR NOT 17.0 = 18.0) AND "
                        + "(3.0 >= 4.0 OR 11.0 = 12.0 OR NOT 15.0 = 16.0 OR NOT 19.0 > 20.0) AND "
                        + "(5.0 <= 6.0 OR 7.0 < 8.0 OR NOT 13.0 <> 14.0) AND "
                        + "(5.0 <= 6.0 OR 7.0 < 8.0 OR NOT 15.0 = 16.0 OR NOT 17.0 = 18.0) AND "
                        + "(5.0 <= 6.0 OR 7.0 < 8.0 OR NOT 15.0 = 16.0 OR NOT 19.0 > 20.0) AND "
                        + "(5.0 <= 6.0 OR 9.0 > 10.0 OR NOT 13.0 <> 14.0) AND "
                        + "(5.0 <= 6.0 OR 9.0 > 10.0 OR NOT 15.0 = 16.0 OR NOT 17.0 = 18.0) AND "
                        + "(5.0 <= 6.0 OR 9.0 > 10.0 OR NOT 15.0 = 16.0 OR NOT 19.0 > 20.0) AND "
                        + "(5.0 <= 6.0 OR 11.0 = 12.0 OR NOT 13.0 <> 14.0) AND "
                        + "(5.0 <= 6.0 OR 11.0 = 12.0 OR NOT 15.0 = 16.0 OR NOT 17.0 = 18.0) AND "
                        + "(5.0 <= 6.0 OR 11.0 = 12.0 OR NOT 15.0 = 16.0 OR NOT 19.0 > 20.0)");
        Expression result = CNFConverter.convertToCNF(expr);
        assertEquals(expected.toString(), result.toString());
    }

    /**
     * This is the case when we test a very simple tree structure that has neither AND operator or
     * OR operator.
     *
     * Here is the expression tree:
     *
     * NOT | > / \ S.D {d '2017-03-25'}
     *
     * Here is the converted expression tree:
     *
     * NOT | > / \ S.D {d '2017-03-25'}
     *
     */
    @Test
    public void test4() throws Exception {
        Expression expr = CCJSqlParserUtil.parseCondExpression("NOT S.D > {d '2017-03-25'}");
        Expression expected = CCJSqlParserUtil.parseCondExpression("NOT S.D > {d '2017-03-25'}");
        Expression result = CNFConverter.convertToCNF(expr);
        assertEquals(expected.toString(), result.toString());
    }

    /**
     * This is the case when we test the tree that only contains AND operator without having an OR
     * operator.
     *
     * Here is the original expression tree: NOT | ( ) | OR / \ ( ) ( ) | | NOT OR | / \ AND LIKE =
     * / \ / \ / \ > < S.C "%%" S.D {t '12:04:34'} / \ / \ S.A 3.5 S.B 4
     *
     * Here is the converted expression tree:
     *
     * AND / \ AND = / \ / \ AND NOT LIKE S.D {t '12:04:34'} / \ / \ > < S.C "%%" / \ / \ S.A 3.5
     * S.B 4
     *
     */
    @Test
    public void test5() throws Exception {
        Expression expr = CCJSqlParserUtil.parseCondExpression(
                "NOT ((NOT (S.A > 3.5 AND S.B < 4)) OR "
                        + "(S.C LIKE '\"%%\"' OR S.D = {t '12:04:34'}))");
        Expression expected = CCJSqlParserUtil.parseCondExpression(
                "S.A > 3.5 AND S.B < 4 AND NOT S.C LIKE '\"%%\"' "
                        + "AND NOT S.D = {t '12:04:34'}");
        Expression result = CNFConverter.convertToCNF(expr);
        assertEquals(expected.toString(), result.toString());
    }

    @Test
    public void testStackOverflowIssue1576() throws JSQLParserException {
        Expression expr = CCJSqlParserUtil.parseCondExpression(
                "((3.0 >= 4.0 AND 5.0 <= 6.0) OR "
                        + "(7.0 < 8.0 AND 9.0 > 10.0) OR "
                        + "(11.0 = 11.0 AND 19.0 > 20.0) OR "
                        + "(17.0 = 14.0 AND 19.0 > 17.0) OR "
                        + "(17.0 = 18.0 AND 20.0 > 20.0) OR "
                        + "(17.0 = 16.0 AND 19.0 > 20.0) OR "
                        + "(17.0 = 18.0 AND 19.0 > 20.0) OR "
                        + "(17.0 = 18.0 AND 19.0 > 20.0) OR "
                        + "(17.0 = 22.0 AND 19.0 > 20.0) OR "
                        + "(18.0 = 18.0 AND 22.0 > 20.0) OR "
                        + "(17.0 = 18.0 AND 19.0 > 20.0) OR "
                        + "(18.0 = 18.0 AND 22.0 > 20.0) OR "
                        + "(18.0 = 19.0 AND 22.0 > 20.0) OR "
                        + "(17.0 = 18.0 AND 19.0 > 20.0))");
        Expression result = CNFConverter.convertToCNF(expr);
        assertThat(result).asString().hasSize(3448827);
    }


    @Test
    @Disabled
    public void testStackOverflowIssue1576_veryLarge() throws JSQLParserException {
        Expression expr = CCJSqlParserUtil.parseCondExpression(
                "((3.0 >= 4.0 AND 5.0 <= 6.0) OR "
                        + "(7.0 < 8.0 AND 9.0 > 10.0) OR "
                        + "(11.0 = 11.0 AND 19.0 > 20.0) OR "
                        + "(17.0 = 14.0 AND 19.0 > 17.0) OR "
                        + "(17.0 = 18.0 AND 20.0 > 20.0) OR "
                        + "(17.0 = 16.0 AND 19.0 > 20.0) OR "
                        + "(17.0 = 18.0 AND 19.0 > 20.0) OR "
                        + "(17.0 = 18.0 AND 19.0 > 20.0) OR "
                        + "(17.0 = 22.0 AND 19.0 > 20.0) OR "
                        + "(18.0 = 18.0 AND 22.0 > 20.0) OR "
                        + "(17.0 = 18.0 AND 19.0 > 20.0) OR "
                        + "(18.0 = 18.0 AND 22.0 > 20.0) OR "
                        + "(18.0 = 19.0 AND 22.0 > 20.0) OR "
                        + "(117.0 = 22.0 AND 19.0 > 20.0) OR "
                        + "(118.0 = 18.0 AND 22.0 > 20.0) OR "
                        + "(117.0 = 18.0 AND 19.0 > 20.0) OR "
                        // + "(118.0 = 18.0 AND 22.0 > 20.0) OR "
                        // + "(118.0 = 19.0 AND 22.0 > 20.0) OR "
                        + "(17.0 = 18.0 AND 19.0 > 20.0))");
        Expression result = CNFConverter.convertToCNF(expr);
        assertThat(result).asString().hasSize(33685499);
    }

    @Test
    public void testStackOverflowIssue1576_2() throws JSQLParserException {
        Expression expr = CCJSqlParserUtil.parseCondExpression(
                "((3.0 >= 4.0 AND 5.0 <= 6.0) OR "
                        + "(7.0 < 8.0 AND 9.0 > 10.0) OR "
                        + "(11.0 = 11.0 AND 19.0 > 20.0) OR "
                        + "(17.0 = 14.0 AND 19.0 > 17.0) OR "
                        + "(17.0 = 18.0 AND 20.0 > 20.0) OR "
                        + "(17.0 = 16.0 AND 19.0 > 20.0))");

        Expression result = CNFConverter.convertToCNF(expr);
        assertThat(result).asString().isEqualTo(
                "(3.0 >= 4.0 OR 7.0 < 8.0 OR 11.0 = 11.0 OR 17.0 = 14.0 OR 17.0 = 18.0 OR 17.0 = 16.0) AND (3.0 >= 4.0 OR 7.0 < 8.0 OR 11.0 = 11.0 OR 17.0 = 14.0 OR 17.0 = 18.0 OR 19.0 > 20.0) AND (3.0 >= 4.0 OR 7.0 < 8.0 OR 11.0 = 11.0 OR 17.0 = 14.0 OR 20.0 > 20.0 OR 17.0 = 16.0) AND (3.0 >= 4.0 OR 7.0 < 8.0 OR 11.0 = 11.0 OR 17.0 = 14.0 OR 20.0 > 20.0 OR 19.0 > 20.0) AND (3.0 >= 4.0 OR 7.0 < 8.0 OR 11.0 = 11.0 OR 19.0 > 17.0 OR 17.0 = 18.0 OR 17.0 = 16.0) AND (3.0 >= 4.0 OR 7.0 < 8.0 OR 11.0 = 11.0 OR 19.0 > 17.0 OR 17.0 = 18.0 OR 19.0 > 20.0) AND (3.0 >= 4.0 OR 7.0 < 8.0 OR 11.0 = 11.0 OR 19.0 > 17.0 OR 20.0 > 20.0 OR 17.0 = 16.0) AND (3.0 >= 4.0 OR 7.0 < 8.0 OR 11.0 = 11.0 OR 19.0 > 17.0 OR 20.0 > 20.0 OR 19.0 > 20.0) AND (3.0 >= 4.0 OR 7.0 < 8.0 OR 19.0 > 20.0 OR 17.0 = 14.0 OR 17.0 = 18.0 OR 17.0 = 16.0) AND (3.0 >= 4.0 OR 7.0 < 8.0 OR 19.0 > 20.0 OR 17.0 = 14.0 OR 17.0 = 18.0 OR 19.0 > 20.0) AND (3.0 >= 4.0 OR 7.0 < 8.0 OR 19.0 > 20.0 OR 17.0 = 14.0 OR 20.0 > 20.0 OR 17.0 = 16.0) AND (3.0 >= 4.0 OR 7.0 < 8.0 OR 19.0 > 20.0 OR 17.0 = 14.0 OR 20.0 > 20.0 OR 19.0 > 20.0) AND (3.0 >= 4.0 OR 7.0 < 8.0 OR 19.0 > 20.0 OR 19.0 > 17.0 OR 17.0 = 18.0 OR 17.0 = 16.0) AND (3.0 >= 4.0 OR 7.0 < 8.0 OR 19.0 > 20.0 OR 19.0 > 17.0 OR 17.0 = 18.0 OR 19.0 > 20.0) AND (3.0 >= 4.0 OR 7.0 < 8.0 OR 19.0 > 20.0 OR 19.0 > 17.0 OR 20.0 > 20.0 OR 17.0 = 16.0) AND (3.0 >= 4.0 OR 7.0 < 8.0 OR 19.0 > 20.0 OR 19.0 > 17.0 OR 20.0 > 20.0 OR 19.0 > 20.0) AND (3.0 >= 4.0 OR 9.0 > 10.0 OR 11.0 = 11.0 OR 17.0 = 14.0 OR 17.0 = 18.0 OR 17.0 = 16.0) AND (3.0 >= 4.0 OR 9.0 > 10.0 OR 11.0 = 11.0 OR 17.0 = 14.0 OR 17.0 = 18.0 OR 19.0 > 20.0) AND (3.0 >= 4.0 OR 9.0 > 10.0 OR 11.0 = 11.0 OR 17.0 = 14.0 OR 20.0 > 20.0 OR 17.0 = 16.0) AND (3.0 >= 4.0 OR 9.0 > 10.0 OR 11.0 = 11.0 OR 17.0 = 14.0 OR 20.0 > 20.0 OR 19.0 > 20.0) AND (3.0 >= 4.0 OR 9.0 > 10.0 OR 11.0 = 11.0 OR 19.0 > 17.0 OR 17.0 = 18.0 OR 17.0 = 16.0) AND (3.0 >= 4.0 OR 9.0 > 10.0 OR 11.0 = 11.0 OR 19.0 > 17.0 OR 17.0 = 18.0 OR 19.0 > 20.0) AND (3.0 >= 4.0 OR 9.0 > 10.0 OR 11.0 = 11.0 OR 19.0 > 17.0 OR 20.0 > 20.0 OR 17.0 = 16.0) AND (3.0 >= 4.0 OR 9.0 > 10.0 OR 11.0 = 11.0 OR 19.0 > 17.0 OR 20.0 > 20.0 OR 19.0 > 20.0) AND (3.0 >= 4.0 OR 9.0 > 10.0 OR 19.0 > 20.0 OR 17.0 = 14.0 OR 17.0 = 18.0 OR 17.0 = 16.0) AND (3.0 >= 4.0 OR 9.0 > 10.0 OR 19.0 > 20.0 OR 17.0 = 14.0 OR 17.0 = 18.0 OR 19.0 > 20.0) AND (3.0 >= 4.0 OR 9.0 > 10.0 OR 19.0 > 20.0 OR 17.0 = 14.0 OR 20.0 > 20.0 OR 17.0 = 16.0) AND (3.0 >= 4.0 OR 9.0 > 10.0 OR 19.0 > 20.0 OR 17.0 = 14.0 OR 20.0 > 20.0 OR 19.0 > 20.0) AND (3.0 >= 4.0 OR 9.0 > 10.0 OR 19.0 > 20.0 OR 19.0 > 17.0 OR 17.0 = 18.0 OR 17.0 = 16.0) AND (3.0 >= 4.0 OR 9.0 > 10.0 OR 19.0 > 20.0 OR 19.0 > 17.0 OR 17.0 = 18.0 OR 19.0 > 20.0) AND (3.0 >= 4.0 OR 9.0 > 10.0 OR 19.0 > 20.0 OR 19.0 > 17.0 OR 20.0 > 20.0 OR 17.0 = 16.0) AND (3.0 >= 4.0 OR 9.0 > 10.0 OR 19.0 > 20.0 OR 19.0 > 17.0 OR 20.0 > 20.0 OR 19.0 > 20.0) AND (5.0 <= 6.0 OR 7.0 < 8.0 OR 11.0 = 11.0 OR 17.0 = 14.0 OR 17.0 = 18.0 OR 17.0 = 16.0) AND (5.0 <= 6.0 OR 7.0 < 8.0 OR 11.0 = 11.0 OR 17.0 = 14.0 OR 17.0 = 18.0 OR 19.0 > 20.0) AND (5.0 <= 6.0 OR 7.0 < 8.0 OR 11.0 = 11.0 OR 17.0 = 14.0 OR 20.0 > 20.0 OR 17.0 = 16.0) AND (5.0 <= 6.0 OR 7.0 < 8.0 OR 11.0 = 11.0 OR 17.0 = 14.0 OR 20.0 > 20.0 OR 19.0 > 20.0) AND (5.0 <= 6.0 OR 7.0 < 8.0 OR 11.0 = 11.0 OR 19.0 > 17.0 OR 17.0 = 18.0 OR 17.0 = 16.0) AND (5.0 <= 6.0 OR 7.0 < 8.0 OR 11.0 = 11.0 OR 19.0 > 17.0 OR 17.0 = 18.0 OR 19.0 > 20.0) AND (5.0 <= 6.0 OR 7.0 < 8.0 OR 11.0 = 11.0 OR 19.0 > 17.0 OR 20.0 > 20.0 OR 17.0 = 16.0) AND (5.0 <= 6.0 OR 7.0 < 8.0 OR 11.0 = 11.0 OR 19.0 > 17.0 OR 20.0 > 20.0 OR 19.0 > 20.0) AND (5.0 <= 6.0 OR 7.0 < 8.0 OR 19.0 > 20.0 OR 17.0 = 14.0 OR 17.0 = 18.0 OR 17.0 = 16.0) AND (5.0 <= 6.0 OR 7.0 < 8.0 OR 19.0 > 20.0 OR 17.0 = 14.0 OR 17.0 = 18.0 OR 19.0 > 20.0) AND (5.0 <= 6.0 OR 7.0 < 8.0 OR 19.0 > 20.0 OR 17.0 = 14.0 OR 20.0 > 20.0 OR 17.0 = 16.0) AND (5.0 <= 6.0 OR 7.0 < 8.0 OR 19.0 > 20.0 OR 17.0 = 14.0 OR 20.0 > 20.0 OR 19.0 > 20.0) AND (5.0 <= 6.0 OR 7.0 < 8.0 OR 19.0 > 20.0 OR 19.0 > 17.0 OR 17.0 = 18.0 OR 17.0 = 16.0) AND (5.0 <= 6.0 OR 7.0 < 8.0 OR 19.0 > 20.0 OR 19.0 > 17.0 OR 17.0 = 18.0 OR 19.0 > 20.0) AND (5.0 <= 6.0 OR 7.0 < 8.0 OR 19.0 > 20.0 OR 19.0 > 17.0 OR 20.0 > 20.0 OR 17.0 = 16.0) AND (5.0 <= 6.0 OR 7.0 < 8.0 OR 19.0 > 20.0 OR 19.0 > 17.0 OR 20.0 > 20.0 OR 19.0 > 20.0) AND (5.0 <= 6.0 OR 9.0 > 10.0 OR 11.0 = 11.0 OR 17.0 = 14.0 OR 17.0 = 18.0 OR 17.0 = 16.0) AND (5.0 <= 6.0 OR 9.0 > 10.0 OR 11.0 = 11.0 OR 17.0 = 14.0 OR 17.0 = 18.0 OR 19.0 > 20.0) AND (5.0 <= 6.0 OR 9.0 > 10.0 OR 11.0 = 11.0 OR 17.0 = 14.0 OR 20.0 > 20.0 OR 17.0 = 16.0) AND (5.0 <= 6.0 OR 9.0 > 10.0 OR 11.0 = 11.0 OR 17.0 = 14.0 OR 20.0 > 20.0 OR 19.0 > 20.0) AND (5.0 <= 6.0 OR 9.0 > 10.0 OR 11.0 = 11.0 OR 19.0 > 17.0 OR 17.0 = 18.0 OR 17.0 = 16.0) AND (5.0 <= 6.0 OR 9.0 > 10.0 OR 11.0 = 11.0 OR 19.0 > 17.0 OR 17.0 = 18.0 OR 19.0 > 20.0) AND (5.0 <= 6.0 OR 9.0 > 10.0 OR 11.0 = 11.0 OR 19.0 > 17.0 OR 20.0 > 20.0 OR 17.0 = 16.0) AND (5.0 <= 6.0 OR 9.0 > 10.0 OR 11.0 = 11.0 OR 19.0 > 17.0 OR 20.0 > 20.0 OR 19.0 > 20.0) AND (5.0 <= 6.0 OR 9.0 > 10.0 OR 19.0 > 20.0 OR 17.0 = 14.0 OR 17.0 = 18.0 OR 17.0 = 16.0) AND (5.0 <= 6.0 OR 9.0 > 10.0 OR 19.0 > 20.0 OR 17.0 = 14.0 OR 17.0 = 18.0 OR 19.0 > 20.0) AND (5.0 <= 6.0 OR 9.0 > 10.0 OR 19.0 > 20.0 OR 17.0 = 14.0 OR 20.0 > 20.0 OR 17.0 = 16.0) AND (5.0 <= 6.0 OR 9.0 > 10.0 OR 19.0 > 20.0 OR 17.0 = 14.0 OR 20.0 > 20.0 OR 19.0 > 20.0) AND (5.0 <= 6.0 OR 9.0 > 10.0 OR 19.0 > 20.0 OR 19.0 > 17.0 OR 17.0 = 18.0 OR 17.0 = 16.0) AND (5.0 <= 6.0 OR 9.0 > 10.0 OR 19.0 > 20.0 OR 19.0 > 17.0 OR 17.0 = 18.0 OR 19.0 > 20.0) AND (5.0 <= 6.0 OR 9.0 > 10.0 OR 19.0 > 20.0 OR 19.0 > 17.0 OR 20.0 > 20.0 OR 17.0 = 16.0) AND (5.0 <= 6.0 OR 9.0 > 10.0 OR 19.0 > 20.0 OR 19.0 > 17.0 OR 20.0 > 20.0 OR 19.0 > 20.0)");
    }
}
