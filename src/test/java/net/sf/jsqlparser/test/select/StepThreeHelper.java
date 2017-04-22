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
import net.sf.jsqlparser.expression.operators.relational.MinorThanEquals;
import net.sf.jsqlparser.expression.operators.relational.NotEqualsTo;
import net.sf.jsqlparser.schema.Column;

public class StepThreeHelper {

	/**
	 * this method is used to build the step three expression tree 1.
	 */
	public static Expression buildStepThreeExpression1() {
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
		Expression e13 = new NotExpression(e9);
		Expression e14 = new NotExpression(e10);
		Expression e15 = new NotExpression(e11);
		Expression e16 = new NotExpression(e12);
		List<Expression> list1 = new ArrayList<Expression>();
		list1.add(e13);
		list1.add(e14);
		Expression e17 = new MultiAndExpression(list1);
		List<Expression> list2 = new ArrayList<Expression>();
		list2.add(e15);
		list2.add(e16);
		Expression e18 = new MultiAndExpression(list2);
		List<Expression> list3 = new ArrayList<Expression>();
		list3.add(e17);
		list3.add(e18);
		return new MultiOrExpression(list3);
	}
	
	/**
	 * this method is used to build the step three expression tree 2.
	 */
	public static Expression buildStepThreeExpression2() {
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
		Expression e13 = new NotExpression(e10);
		List<Expression> list1 = new ArrayList<Expression>();
		list1.add(e9);
		list1.add(e13);
		Expression e14 = new MultiAndExpression(list1);
		List<Expression> list2 = new ArrayList<Expression>();
		list2.add(e11);
		list2.add(e12);
		Expression e15 = new MultiAndExpression(list2);
		List<Expression> list3 = new ArrayList<Expression>();
		list3.add(e14);
		list3.add(e15);
		return new MultiOrExpression(list3);
	}
	
	/**
	 * this method is used to build the step three expression tree 3.
	 */
	public static Expression buildStepThreeExpression3() {
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
		List<Expression> list1 = new ArrayList<Expression>();
		list1.add(e19);
		list1.add(e20);
		Expression e28 = new MultiAndExpression(list1);
		List<Expression> list2 = new ArrayList<Expression>();
		list2.add(e21);
		list2.add(e22);
		list2.add(e23);
		Expression e29 = new MultiAndExpression(list2);
		Expression e30 = new NotExpression(e26);
		Expression e31 = new NotExpression(e27);
		List<Expression> list3 = new ArrayList<Expression>();
		list3.add(e30);
		list3.add(e31);
		Expression e32 = new MultiAndExpression(list3);
		Expression e33 = new NotExpression(e25);
		List<Expression> list4 = new ArrayList<Expression>();
		list4.add(e33);
		list4.add(e32);
		Expression e34 = new MultiOrExpression(list4);
		Expression e35 = new NotExpression(e24);
		List<Expression> list5 = new ArrayList<Expression>();
		list5.add(e35);
		list5.add(e34);
		Expression e36 = new MultiAndExpression(list5);
		List<Expression> list6 = new ArrayList<Expression>();
		list6.add(e28);
		list6.add(e29);
		list6.add(e36);
		return new MultiOrExpression(list6);
	}
	
	/**
	 * this method is used to build the step three expression tree 4.
	 */
	public static Expression buildStepThreeExpression4() {
		Expression e1 = new Column("S.D");
		Expression e2 = new DateValue("\"2017-03-25\"");
		BinaryExpression e3 = new GreaterThan();
		e3.setLeftExpression(e1);
		e3.setRightExpression(e2);
		return new NotExpression(e3);
	}
	
	/**
	 * this method is used to build the step three expression tree 5.
	 */
	public static Expression buildStepThreeExpression5() {
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
	
}
