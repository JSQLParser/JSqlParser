package net.sf.jsqlparser.test.select;

import java.util.ArrayList;
import java.util.List;

import net.sf.jsqlparser.expression.BinaryExpression;
import net.sf.jsqlparser.expression.DateValue;
import net.sf.jsqlparser.expression.DoubleValue;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.NotExpression;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.TimeValue;
import net.sf.jsqlparser.expression.operators.conditional.MultiAndExpression;
import net.sf.jsqlparser.expression.operators.conditional.MultiOrExpression;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.GreaterThan;
import net.sf.jsqlparser.expression.operators.relational.GreaterThanEquals;
import net.sf.jsqlparser.expression.operators.relational.LikeExpression;
import net.sf.jsqlparser.expression.operators.relational.MinorThan;
import net.sf.jsqlparser.expression.operators.relational.NotEqualsTo;
import net.sf.jsqlparser.schema.Column;

public class StepFourHelper {

    /**
     * this method is used to build the last step expression tree.
     */
    public static Expression buildStepLastExpression1() {
        Expression e1 = new DoubleValue("1.2");
        Expression e2 = new DoubleValue("2.3");
        Expression e3 = new DoubleValue("1.1");
        Expression e4 = new DoubleValue("2.5");
        Expression e5 = new DoubleValue("1.2");
        Expression e6 = new DoubleValue("2.3");
        Expression e7 = new DoubleValue("8.0");
        Expression e8 = new DoubleValue("7.2");
        Expression e9 = new DoubleValue("3.5");
        Expression e10 = new DoubleValue("4.6");
        Expression e11 = new DoubleValue("1.1");
        Expression e12 = new DoubleValue("2.5");
        Expression e13 = new DoubleValue("3.5");
        Expression e14 = new DoubleValue("4.6");
        Expression e15 = new DoubleValue("8.0");
        Expression e16 = new DoubleValue("7.2");
        BinaryExpression e17 = new MinorThan();
        e17.setLeftExpression(e1);
        e17.setRightExpression(e2);
        BinaryExpression e18 = new NotEqualsTo();
        e18.setLeftExpression(e3);
        e18.setRightExpression(e4);
        BinaryExpression e19 = new MinorThan();
        e19.setLeftExpression(e5);
        e19.setRightExpression(e6);
        BinaryExpression e20 = new GreaterThanEquals();
        e20.setLeftExpression(e7);
        e20.setRightExpression(e8);
        BinaryExpression e21 = new EqualsTo();
        e21.setLeftExpression(e9);
        e21.setRightExpression(e10);
        BinaryExpression e22 = new NotEqualsTo();
        e22.setLeftExpression(e11);
        e22.setRightExpression(e12);
        BinaryExpression e23 = new EqualsTo();
        e23.setLeftExpression(e13);
        e23.setRightExpression(e14);
        BinaryExpression e24 = new GreaterThanEquals();
        e24.setLeftExpression(e15);
        e24.setRightExpression(e16);
        Expression e25 = new NotExpression(e17);
        Expression e26 = new NotExpression(e18);
        Expression e27 = new NotExpression(e19);
        Expression e28 = new NotExpression(e20);
        Expression e29 = new NotExpression(e21);
        Expression e30 = new NotExpression(e22);
        Expression e31 = new NotExpression(e23);
        Expression e32 = new NotExpression(e24);
        List<Expression> list1 = new ArrayList<Expression>();
        list1.add(e25);
        list1.add(e26);
        Expression e33 = new MultiOrExpression(list1);
        List<Expression> list2 = new ArrayList<Expression>();
        list2.add(e27);
        list2.add(e28);
        Expression e34 = new MultiOrExpression(list2);
        List<Expression> list3 = new ArrayList<Expression>();
        list3.add(e29);
        list3.add(e30);
        Expression e35 = new MultiOrExpression(list3);
        List<Expression> list4 = new ArrayList<Expression>();
        list4.add(e31);
        list4.add(e32);
        Expression e36 = new MultiOrExpression(list4);
        List<Expression> list5 = new ArrayList<Expression>();
        list5.add(e33);
        list5.add(e34);
        list5.add(e35);
        list5.add(e36);
        return new MultiAndExpression(list5);
    }
    
    /**
     * this method is used to build the last step expression tree 2.
     */
    public static Expression buildStepLastExpression2() {
        Expression e1 = new DoubleValue("1.1");
        Expression e2 = new DoubleValue("2.3");
        Expression e3 = new Column("S.A");
        Expression e4 = new StringValue("\"%%%\"");
        Expression e5 = new DoubleValue("1.1");
        Expression e6 = new DoubleValue("2.3");
        Expression e7 = new Column("S.B");
        Expression e8 = new StringValue("\"orz\"");
        Expression e9 = new DoubleValue("3.3");
        Expression e10 = new DoubleValue("4.5");
        Expression e11 = new Column("S.A");
        Expression e12 = new StringValue("\"%%%\"");
        Expression e13 = new DoubleValue("3.3");
        Expression e14 = new DoubleValue("4.5");
        Expression e15 = new Column("S.B");
        Expression e16 = new StringValue("\"orz\"");
        BinaryExpression e17 = new GreaterThanEquals();
        e17.setLeftExpression(e1);
        e17.setRightExpression(e2);
        BinaryExpression e18 = new LikeExpression();
        e18.setLeftExpression(e3);
        e18.setRightExpression(e4);
        BinaryExpression e19 = new GreaterThanEquals();
        e19.setLeftExpression(e5);
        e19.setRightExpression(e6);
        BinaryExpression e20 = new EqualsTo();
        e20.setLeftExpression(e7);
        e20.setRightExpression(e8);
        BinaryExpression e21 = new MinorThan();
        e21.setLeftExpression(e9);
        e21.setRightExpression(e10);
        BinaryExpression e22 = new LikeExpression();
        e22.setLeftExpression(e11);
        e22.setRightExpression(e12);
        BinaryExpression e23 = new MinorThan();
        e23.setLeftExpression(e13);
        e23.setRightExpression(e14);
        BinaryExpression e24 = new EqualsTo();
        e24.setLeftExpression(e15);
        e24.setRightExpression(e16);
        List<Expression> list1 = new ArrayList<Expression>();
        list1.add(e17);
        list1.add(e18);
        Expression e25 = new MultiOrExpression(list1);
        List<Expression> list2 = new ArrayList<Expression>();
        list2.add(e19);
        list2.add(e20);
        Expression e26 = new MultiOrExpression(list2);
        Expression e27 = new NotExpression(e21);
        List<Expression> list3 = new ArrayList<Expression>();
        list3.add(e27);
        list3.add(e22);
        Expression e28 = new MultiOrExpression(list3);
        Expression e29 = new NotExpression(e23);
        List<Expression> list4 = new ArrayList<Expression>();
        list4.add(e29);
        list4.add(e24);
        Expression e30 = new MultiOrExpression(list4);
        List<Expression> list5 = new ArrayList<Expression>();
        list5.add(e25);
        list5.add(e26);
        list5.add(e28);
        list5.add(e30);
        return new MultiAndExpression(list5);
    }
    
    /**
     * this method is used to build the last step expression tree 3.
     */
    public static Expression buildStepLastExpression3() {
        Expression[] part1 = LargeTreeHelper.buildBase1();
        Expression[] part2 = LargeTreeHelper.buildBase2();
        Expression[] part3 = LargeTreeHelper.buildBase3();
        Expression[] part4 = LargeTreeHelper.buildLogical1(part1);
        Expression[] part5 = LargeTreeHelper.buildLogical2(part2);
        Expression[] part6 = LargeTreeHelper.buildLogical3(part3);
        Expression[] part7 = buildMultiOrPart1(part4);
        Expression[] part8 = buildMultiOrPart2(part5);
        Expression[] part9 = buildMultiOrPart3(part6);
        List<Expression> list19 = new ArrayList<Expression>();
        list19.add(part7[0]);
        list19.add(part7[1]);
        list19.add(part7[2]);
        list19.add(part7[3]);
        list19.add(part7[4]);
        list19.add(part7[5]);
        list19.add(part8[0]);
        list19.add(part8[1]);
        list19.add(part8[2]);
        list19.add(part8[3]);
        list19.add(part8[4]);
        list19.add(part8[5]);
        list19.add(part9[0]);
        list19.add(part9[1]);
        list19.add(part9[2]);
        list19.add(part9[3]);
        list19.add(part9[4]);
        list19.add(part9[5]);
        return new MultiAndExpression(list19);
    }
    
    /**
     * this method is used to build the step last expression tree 4.
     */
    public static Expression buildStepLastExpression4() {
        Expression e1 = new Column("S.D");
        Expression e2 = new DateValue("\"2017-03-25\"");
        BinaryExpression e3 = new GreaterThan();
        e3.setLeftExpression(e1);
        e3.setRightExpression(e2);
        return new NotExpression(e3);
    }
    
    /**
     * this method is used to build the step last expression tree 5.
     */
    public static Expression buildStepLastExpression5() {
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
        Expression e13 = new NotExpression(e11);
        Expression e14 = new NotExpression(e12);
        List<Expression> list = new ArrayList<Expression>();
        list.add(e9);
        list.add(e10);
        list.add(e13);
        list.add(e14);
        return new MultiAndExpression(list);
    }
    
    public static Expression[] buildMultiOrPart1(Expression[] array) {
        Expression[] result = new Expression[6];
        List<Expression> list1 = new ArrayList<Expression>();
        list1.add(array[0]);
        list1.add(array[1]);
        list1.add(array[2]);
        result[0] = new MultiOrExpression(list1);
        List<Expression> list2 = new ArrayList<Expression>();
        list2.add(array[3]);
        list2.add(array[4]);
        list2.add(array[5]);
        list2.add(array[6]);
        result[1] = new MultiOrExpression(list2);
        List<Expression> list3 = new ArrayList<Expression>();
        list3.add(array[7]);
        list3.add(array[8]);
        list3.add(array[9]);
        list3.add(array[10]);
        result[2] = new MultiOrExpression(list3);
        List<Expression> list4 = new ArrayList<Expression>();
        list4.add(array[11]);
        list4.add(array[12]);
        list4.add(array[13]);
        result[3] = new MultiOrExpression(list4);
        List<Expression> list5 = new ArrayList<Expression>();
        list5.add(array[14]);
        list5.add(array[15]);
        list5.add(array[16]);
        list5.add(array[17]);
        result[4] = new MultiOrExpression(list5);
        List<Expression> list6 = new ArrayList<Expression>();
        list6.add(array[18]);
        list6.add(array[19]);
        list6.add(array[20]);
        list6.add(array[21]);
        result[5] = new MultiOrExpression(list6);
        return result;
    }
    
    public static Expression[] buildMultiOrPart2(Expression[] array) {
        Expression[] result = new Expression[6];
        List<Expression> list7 = new ArrayList<Expression>();
        list7.add(array[0]);
        list7.add(array[1]);
        list7.add(array[2]);
        result[0] = new MultiOrExpression(list7);
        List<Expression> list8 = new ArrayList<Expression>();
        list8.add(array[3]);
        list8.add(array[4]);
        list8.add(array[5]);
        list8.add(array[6]);
        result[1] = new MultiOrExpression(list8);
        List<Expression> list9 = new ArrayList<Expression>();
        list9.add(array[7]);
        list9.add(array[8]);
        list9.add(array[9]);
        list9.add(array[10]);
        result[2] = new MultiOrExpression(list9);
        List<Expression> list10 = new ArrayList<Expression>();
        list10.add(array[11]);
        list10.add(array[12]);
        list10.add(array[13]);
        result[3] = new MultiOrExpression(list10);
        List<Expression> list11 = new ArrayList<Expression>();
        list11.add(array[14]);
        list11.add(array[15]);
        list11.add(array[16]);
        list11.add(array[17]);
        result[4] = new MultiOrExpression(list11);
        List<Expression> list12 = new ArrayList<Expression>();
        list12.add(array[18]);
        list12.add(array[19]);
        list12.add(array[20]);
        list12.add(array[21]);
        result[5] = new MultiOrExpression(list12);
        return result;
    }
    
    public static Expression[] buildMultiOrPart3(Expression[] array) {
        Expression[] result = new Expression[6];
        List<Expression> list13 = new ArrayList<Expression>();
        list13.add(array[0]);
        list13.add(array[1]);
        list13.add(array[2]);
        result[0] = new MultiOrExpression(list13);
        List<Expression> list14 = new ArrayList<Expression>();
        list14.add(array[3]);
        list14.add(array[4]);
        list14.add(array[5]);
        list14.add(array[6]);
        result[1] = new MultiOrExpression(list14);
        List<Expression> list15 = new ArrayList<Expression>();
        list15.add(array[7]);
        list15.add(array[8]);
        list15.add(array[9]);
        list15.add(array[10]);
        result[2] = new MultiOrExpression(list15);
        List<Expression> list16 = new ArrayList<Expression>();
        list16.add(array[11]);
        list16.add(array[12]);
        list16.add(array[13]);
        result[3] = new MultiOrExpression(list16);
        List<Expression> list17 = new ArrayList<Expression>();
        list17.add(array[14]);
        list17.add(array[15]);
        list17.add(array[16]);
        list17.add(array[17]);
        result[4] = new MultiOrExpression(list17);
        List<Expression> list18 = new ArrayList<Expression>();
        list18.add(array[18]);
        list18.add(array[19]);
        list18.add(array[20]);
        list18.add(array[21]);
        result[5] = new MultiOrExpression(list18);
        return result;
    }
    
}
