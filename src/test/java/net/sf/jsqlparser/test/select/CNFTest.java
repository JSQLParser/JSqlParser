package net.sf.jsqlparser.test.select;

import static org.junit.Assert.*;

import org.junit.Test;

import net.sf.jsqlparser.expression.BinaryExpression;
import net.sf.jsqlparser.expression.DateValue;
import net.sf.jsqlparser.expression.DoubleValue;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.NotExpression;
import net.sf.jsqlparser.expression.Parenthesis;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.TimeValue;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.GreaterThan;
import net.sf.jsqlparser.expression.operators.relational.GreaterThanEquals;
import net.sf.jsqlparser.expression.operators.relational.LikeExpression;
import net.sf.jsqlparser.expression.operators.relational.MinorThan;
import net.sf.jsqlparser.expression.operators.relational.MinorThanEquals;
import net.sf.jsqlparser.expression.operators.relational.NotEqualsTo;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.CNFConverter;

/**
 * this class is mainly used for testing whether we generate the 
 * correct CNF form of an expression tree. We use the name of 
 * variables that is reflected in the steps defined in the class.
 * @author messfish
 *
 */
public class CNFTest {

    private Expression original1;
    private Expression original2;
    private Expression original3;
    private Expression original4;
    private Expression original5;
    // these expressions stores the original expression tree.
    private Expression stepone1;
    private Expression stepone2;
    private Expression stepone3;
    private Expression stepone4; 
    private Expression stepone5;
    // these expressions stores the step one of the expression tree.
    private Expression steptwo1;
    private Expression steptwo2;
    private Expression steptwo3;
    private Expression steptwo4;
    private Expression steptwo5;
    // these expression stores the step two of the expression tree.
    private Expression stepthree1;
    private Expression stepthree2;
    private Expression stepthree3;
    private Expression stepthree4;
    private Expression stepthree5;
    // these expression stores the step three of the expression tree.
    private Expression stepfour1;
    private Expression stepfour2;
    private Expression stepfour3;
    private Expression stepfour4;
    private Expression stepfour5;
    // these expression stores the step four of the expression tree.
    private Expression steplast1;
    private Expression steplast2;
    private Expression steplast3;
    private Expression steplast4;
    private Expression steplast5;
    // these expression stores the last step of the expression tree.
    private CNFConverter cnf1;
    private CNFConverter cnf2;
    private CNFConverter cnf3;
    private CNFConverter cnf4;
    private CNFConverter cnf5;
    // generate three CNF converter objects to handle the 
    // conversion of three expression trees.
    
    /**
     * Constructor: this constructor will be served to build several
     * original construction tree and several expected trees that
     * will be used in those test cases.
     */
    public CNFTest() {
        /* Here is the original part of the expression tree. */
        buildOriginalExpression1();
        buildOriginalExpression2();
        buildOriginalExpression3();
        buildOriginalExpression4();
        buildOriginalExpression5();
        stepone1 = StepOneHelper.buildStepOneExpression1();
        stepone2 = StepOneHelper.buildStepOneExpression2();
        stepone3 = StepOneHelper.buildStepOneExpression3();
        stepone4 = StepOneHelper.buildStepOneExpression4();
        stepone5 = StepOneHelper.buildStepOneExpression5();
        steptwo1 = StepTwoHelper.buildStepTwoExpression1();
        steptwo2 = StepTwoHelper.buildStepTwoExpression2();
        steptwo3 = StepTwoHelper.buildStepTwoExpression3();
        steptwo4 = StepTwoHelper.buildStepTwoExpression4();
        steptwo5 = StepTwoHelper.buildStepTwoExpression5();
        stepthree1 = StepThreeHelper.buildStepThreeExpression1();
        stepthree2 = StepThreeHelper.buildStepThreeExpression2();
        stepthree3 = StepThreeHelper.buildStepThreeExpression3();
        stepthree4 = StepThreeHelper.buildStepThreeExpression4();
        stepthree5 = StepThreeHelper.buildStepThreeExpression5();
        stepfour1 = StepFourHelper.buildStepLastExpression1();
        stepfour2 = StepFourHelper.buildStepLastExpression2();
        stepfour3 = StepFourHelper.buildStepLastExpression3();
        stepfour4 = StepFourHelper.buildStepLastExpression4();
        stepfour5 = StepFourHelper.buildStepLastExpression5();
        steplast1 = StepLastHelper.buildFinalStep1();
        steplast2 = StepLastHelper.buildFinalStep2();
        steplast3 = StepLastHelper.buildFinalStep3();
        steplast4 = StepLastHelper.buildFinalStep4();
        steplast5 = StepLastHelper.buildFinalStep5();
        cnf1 = new CNFConverter();
        cnf2 = new CNFConverter();
        cnf3 = new CNFConverter();
        cnf4 = new CNFConverter();
        cnf5 = new CNFConverter();
    }

    /** 
     * this method is used to check whether we clone the expression
     * tree in the correct form that is specified in the clone class.
     */
    @Test
    public void testStepOne() {
        cnf1.reorder(original1);
        assertEquals(stepone1.toString(), cnf1.getRoot().toString());
        cnf2.reorder(original2);
        assertEquals(stepone2.toString(), cnf2.getRoot().toString());
        cnf3.reorder(original3);
        assertEquals(stepone3.toString(), cnf3.getRoot().toString());
        cnf4.reorder(original4);
        assertEquals(stepone4.toString(), cnf4.getRoot().toString());
        cnf5.reorder(original5);
        assertEquals(stepone5.toString(), cnf5.getRoot().toString());
    }
    
    /**
     * this method is used to check whether we push the not operators
     * thoroughly. 
     */
    @Test
    public void testStepTwo() {
        cnf1.reorder(original1);
        cnf1.pushNotDown();
        assertEquals(steptwo1.toString(), cnf1.getRoot().toString());
        cnf2.reorder(original2);
        cnf2.pushNotDown();
        assertEquals(steptwo2.toString(), cnf2.getRoot().toString());
        cnf3.reorder(original3);
        cnf3.pushNotDown();
        assertEquals(steptwo3.toString(), cnf3.getRoot().toString());
        cnf4.reorder(original4);
        cnf4.pushNotDown();
        assertEquals(steptwo4.toString(), cnf4.getRoot().toString());
        cnf5.reorder(original5);
        cnf5.pushNotDown();
        assertEquals(steptwo5.toString(), cnf5.getRoot().toString());
    }
    
    /**
     * this method is used to check whether the expression tree is 
     * reformed in the correct order.
     */
    @Test
    public void testStepThree() {
        cnf1.reorder(original1);
        cnf1.pushNotDown();
        cnf1.gather();
        assertEquals(stepthree1.toString(), cnf1.getRoot().toString());
        cnf2.reorder(original2);
        cnf2.pushNotDown();
        cnf2.gather();
        assertEquals(stepthree2.toString(), cnf2.getRoot().toString());
        cnf3.reorder(original3);
        cnf3.pushNotDown();
        cnf3.gather();
        assertEquals(stepthree3.toString(), cnf3.getRoot().toString());
        cnf4.reorder(original4);
        cnf4.pushNotDown();
        cnf4.gather();
        assertEquals(stepthree4.toString(), cnf4.getRoot().toString());
        cnf5.reorder(original5);
        cnf5.pushNotDown();
        cnf5.gather();
        assertEquals(stepthree5.toString(), cnf5.getRoot().toString());
    }

    
    /**
     * this method is used to check whether the expression tree is 
     * converted into the CNF form.
     */
    @Test
    public void testStepFour() {
        cnf1.reorder(original1);
        cnf1.pushNotDown();
        cnf1.gather();
        cnf1.pushAndUp();
        assertEquals(stepfour1.toString(), cnf1.getRoot().toString());
        cnf2.reorder(original2);
        cnf2.pushNotDown();
        cnf2.gather();
        cnf2.pushAndUp();
        assertEquals(stepfour2.toString(), cnf2.getRoot().toString());
        cnf3.reorder(original3);
        cnf3.pushNotDown();
        cnf3.gather();
        cnf3.pushAndUp();
        assertEquals(stepfour3.toString(), cnf3.getRoot().toString());
        cnf4.reorder(original4);
        cnf4.pushNotDown();
        cnf4.gather();
        cnf4.pushAndUp();
        assertEquals(stepfour4.toString(), cnf4.getRoot().toString());
        cnf5.reorder(original5);
        cnf5.pushNotDown();
        cnf5.gather();
        cnf5.pushAndUp();
        assertEquals(stepfour5.toString(), cnf5.getRoot().toString());
    }
    
    /**
     * this method is used to test the full progress from the original
     * expression tree to the expression tree in CNF form. In other words,
     * this is used to test the last case.
     */
    @Test
    public void testAll() {
        cnf1.convert(original1);
        assertEquals(steplast1.toString(), cnf1.getRoot().toString());
        cnf2.convert(original2);
        assertEquals(steplast2.toString(), cnf2.getRoot().toString());
        cnf3.convert(original3);
        assertEquals(steplast3.toString(), cnf3.getRoot().toString());
        cnf4.convert(original4);
        assertEquals(steplast4.toString(), cnf4.getRoot().toString());
        cnf5.convert(original5);
        assertEquals(steplast5.toString(), cnf5.getRoot().toString());
    }
    
    /**
     * this method is used to build the original expression tree 1.
     * The purpose of this method is to check when there is a Not Operator
     * at the root. Which means the root must be switched.
     * Here is the structure of the expression tree:
     *                             NOT
     *                              |
     *                             ( )
     *                              |
     *                             AND
     *                 /                           \
     *                OR                         OR
     *          /            \              /           \
     *         <               =            !=            >=
     *      /     \        /     \      /      \      /      \
     *  1.2    2.3     3.5    4.6   1.1     2.5   8.0     7.2            
     */
    private void buildOriginalExpression1() {
        Expression e1 = new DoubleValue("1.2");
        Expression e2 = new DoubleValue("2.3");
        Expression e3 = new DoubleValue("3.5");
        Expression e4 = new DoubleValue("4.6");
        Expression e5 = new DoubleValue("1.1");
        Expression e6 = new DoubleValue("2.5");
        Expression e7 = new DoubleValue("8.0");
        Expression e8 = new DoubleValue("7.2");
        BinaryExpression e9 = new MinorThan();
        e9.setLeftExpression(e1);
        e9.setRightExpression(e2);
        BinaryExpression e10 = new EqualsTo();
        e10.setLeftExpression(e3);
        e10.setRightExpression(e4);
        BinaryExpression e11 = new NotEqualsTo();
        e11.setLeftExpression(e5);
        e11.setRightExpression(e6);
        BinaryExpression e12 = new GreaterThanEquals();
        e12.setLeftExpression(e7);
        e12.setRightExpression(e8);
        BinaryExpression e13 = new OrExpression(e9, e10);
        BinaryExpression e14 = new OrExpression(e11, e12);
        BinaryExpression e15 = new AndExpression(e13, e14);
        original1 = new Parenthesis(e15);
        ((Parenthesis)original1).setNot();
    }
    
    /**
     * this method is used to build the original expression tree 2.
     * The purpose is to test the double negation law. As you can 
     * see when you build the tree, there will be two Not Operators 
     * together on the line. It is there when we use the double negation law.
     * Here is the structure of the expression tree:
     *                                 ( )
     *                                  |
     *                                  OR
     *                   /                                  \
     *                NOT                                 ( )
     *              |                                   |
     *             OR                                  AND
     *        /          \                        /           \
     *      NOT          <                      LIKE           =    
     *          |         /    \                 /      \      /     \
     *      >=       3.3    4.5              S.A    "%%%"  S.B    "orz"
     *    /    \
     *  1.1    2.3
     */
    private void buildOriginalExpression2() {
        Expression e1 = new DoubleValue("1.1");
        Expression e2 = new DoubleValue("2.3");
        Expression e3 = new DoubleValue("3.3");
        Expression e4 = new DoubleValue("4.5");
        Expression e5 = new Column("S.A");
        Expression e6 = new StringValue("\"%%%\"");
        Expression e7 = new Column("S.B");
        Expression e8 = new StringValue("\"orz\"");
        BinaryExpression e9 = new GreaterThanEquals();
        e9.setLeftExpression(e1);
        e9.setRightExpression(e2);
        BinaryExpression e10 = new MinorThan();
        e10.setLeftExpression(e3);
        e10.setRightExpression(e4);
        BinaryExpression e11 = new LikeExpression();
        e11.setLeftExpression(e5);
        e11.setRightExpression(e6);
        BinaryExpression e12 = new EqualsTo();
        e12.setLeftExpression(e7);
        e12.setRightExpression(e8);
        Expression e13 = new NotExpression(e9);
        BinaryExpression e14 = new OrExpression(e13, e10);
        Expression e15 = new NotExpression(e14);
        BinaryExpression e16 = new AndExpression(e11, e12);
        Expression e17 = new Parenthesis(e16);
        BinaryExpression e18 = new OrExpression(e15, e17);
        original2 = new Parenthesis(e18);
    }
    
    /**
     * this method is used to build the original expression tree 3.
     * This is the case when we test a more complex tree structure,
     * Notice you could see the amount of line to build up the CNF tree.
     * You could tell how complicated the CNF could be.
     * 
     * This is the structure of the expression tree:
     *                             OR
     *                     /                    \
     *                   ( )                   ( )
     *                 |                     |
     *                AND                    OR
     *            /         \             /                   \
     *          >=          <=           AND                  NOT
     *       /     \      /     \      /       \               |
     *      3.0    4.0   5.0    6.0   AND        =             OR
     *                              /     \    /   \         /       \
     *                             <       > 11.0 12.0     !=         AND
     *                           /   \   /   \            /   \      /         \
     *                          7.0 8.0 9.0 10.0        13.0 14.0  LIKE        AND
     *                                                            /    \      /       \
     *                                                           15.0 16.0   =          >
     *                                                                     /   \      /   \
     *                                                                    17.0 18.0 19.0 20.0
     */
    private void buildOriginalExpression3() {
        Expression e1 = new DoubleValue("3.0");
        Expression e2 = new DoubleValue("4.0");
        Expression e3 = new DoubleValue("5.0");
        Expression e4 = new DoubleValue("6.0");
        Expression e5 = new DoubleValue("7.0");
        Expression e6 = new DoubleValue("8.0");
        Expression e7 = new DoubleValue("9.0");
        Expression e8 = new DoubleValue("10.0");
        Expression e9 = new DoubleValue("11.0");
        Expression e10 = new DoubleValue("12.0");
        Expression e11 = new DoubleValue("13.0");
        Expression e12 = new DoubleValue("14.0");
        Expression e13 = new DoubleValue("15.0");
        Expression e14 = new DoubleValue("16.0");
        Expression e15 = new DoubleValue("17.0");
        Expression e16 = new DoubleValue("18.0");
        Expression e17 = new DoubleValue("19.0");
        Expression e18 = new DoubleValue("20.0");
        BinaryExpression e19 = new GreaterThanEquals();
        e19.setLeftExpression(e1);
        e19.setRightExpression(e2);
        BinaryExpression e20 = new MinorThanEquals();
        e20.setLeftExpression(e3);
        e20.setRightExpression(e4);
        BinaryExpression e21 = new MinorThan();
        e21.setLeftExpression(e5);
        e21.setRightExpression(e6);
        BinaryExpression e22 = new GreaterThan();
        e22.setLeftExpression(e7);
        e22.setRightExpression(e8);
        BinaryExpression e23 = new EqualsTo();
        e23.setLeftExpression(e9);
        e23.setRightExpression(e10);
        BinaryExpression e24 = new NotEqualsTo();
        e24.setLeftExpression(e11);
        e24.setRightExpression(e12);
        BinaryExpression e25 = new LikeExpression();
        e25.setLeftExpression(e13);
        e25.setRightExpression(e14);
        BinaryExpression e26 = new EqualsTo();
        e26.setLeftExpression(e15);
        e26.setRightExpression(e16);
        BinaryExpression e27 = new GreaterThan();
        e27.setLeftExpression(e17);
        e27.setRightExpression(e18);
        BinaryExpression e28 = new AndExpression(e19, e20);
        BinaryExpression e29 = new AndExpression(e21, e22);
        BinaryExpression e30 = new OrExpression(e26, e27);
        BinaryExpression e31 = new AndExpression(e29, e23);
        BinaryExpression e32 = new AndExpression(e25, e30);
        BinaryExpression e33 = new OrExpression(e24, e32);
        Expression e34 = new NotExpression(e33);
        BinaryExpression e35 = new OrExpression(e31, e34);
        Expression e36 = new Parenthesis(e28);
        Expression e37 = new Parenthesis(e35);
        BinaryExpression e38 = new OrExpression(e36, e37);
        original3 = e38;
    }
    
    /**
     * This method is used for building the original expression tree 4.
     * This is the case when we test a very simple tree structure that 
     * has neither AND operator or OR operator.
     * 
     * Here is the structure of the expression tree:
     * 
     *                             >
     *                   /             \
     *                  S.D           2017-03-25
     */
    private void buildOriginalExpression4() {
        Expression e1 = new Column("S.D");
        Expression e2 = new DateValue("\"2017-03-25\"");
        BinaryExpression e3 = new GreaterThan();
        e3.setLeftExpression(e1);
        e3.setRightExpression(e2);
        original4 = new NotExpression(e3);
    }
    
    /**
     * This method is used for building the original expression tree 5.
     * This is the case when we test the tree that only contains AND
     * operator without having an OR operator.
     * 
     * Here is the structure of the expression tree:
     * 
     *                            NOT
     *                          |
     *                          OR
     *              /                   \
     *             NOT                   OR
     *              |                 /              \
     *             AND              LIKE              =
     *        /          \        /       \        /      \
     *       >           <      S.C   "12:04:34"  S.D    "sddsds"
     *    /     \      /   \
     *  S.A     3.5   S.B   4
     */
    private void buildOriginalExpression5() {
        Expression e1 = new Column("S.A");
        Expression e2 = new DoubleValue("3.5");
        Expression e3 = new Column("S.B");
        Expression e4 = new LongValue(4);
        Expression e5 = new Column("S.C");
        Expression e6 = new TimeValue("\"12:04:34\"");
        Expression e7 = new Column("S.D");
        Expression e8 = new StringValue("\"sddsds\"");
        BinaryExpression e9 = new GreaterThan();
        e9.setLeftExpression(e1);
        e9.setRightExpression(e2);
        BinaryExpression e10 = new MinorThan();
        e10.setLeftExpression(e3);
        e10.setRightExpression(e4);
        BinaryExpression e11 = new LikeExpression();
        e11.setLeftExpression(e5);
        e11.setRightExpression(e6);
        BinaryExpression e12 = new EqualsTo();
        e12.setLeftExpression(e7);
        e12.setRightExpression(e8);
        BinaryExpression e13 = new AndExpression(e9, e10);
        BinaryExpression e14 = new OrExpression(e11, e12);
        Expression e15 = new NotExpression(e13);
        BinaryExpression e16 = new OrExpression(e15, e14);
        original5 = new NotExpression(e16);
    }
    
}
