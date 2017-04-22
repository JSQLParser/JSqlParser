package net.sf.jsqlparser.test.select;

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
import net.sf.jsqlparser.expression.operators.relational.NotEqualsTo;
import net.sf.jsqlparser.schema.Column;

public class StepLastHelper {

	/**
	 * This is the case for building the final CNF form.
	 */
	public static Expression buildFinalStep1() {
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
		Expression e33 = new OrExpression(e25, e26);
		Expression e34 = new OrExpression(e27, e28);
		Expression e35 = new OrExpression(e29, e30);
		Expression e36 = new OrExpression(e31, e32);
		Expression e37 = new Parenthesis(e33);
		Expression e38 = new Parenthesis(e34);
		Expression e39 = new Parenthesis(e35);
		Expression e40 = new Parenthesis(e36);
		Expression e41 = new AndExpression(e37, e38);
		Expression e42 = new AndExpression(e41, e39);
		return new AndExpression(e42, e40);
	}
	
	/**
	 * This is the case for building the final CNF form.
	 */
	public static Expression buildFinalStep2() {
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
		Expression e25 = new OrExpression(e17, e18);
		Expression e26 = new OrExpression(e19, e20);
		Expression e27 = new NotExpression(e21);
		Expression e28 = new OrExpression(e27, e22);
		Expression e29 = new NotExpression(e23);
		Expression e30 = new OrExpression(e29, e24);
		Expression e31 = new Parenthesis(e25);
		Expression e32 = new Parenthesis(e26);
		Expression e33 = new Parenthesis(e28);
		Expression e34 = new Parenthesis(e30);
		Expression e35 = new AndExpression(e31, e32);
		Expression e36 = new AndExpression(e35, e33);
		return new AndExpression(e36, e34);
	}
	
	/**
	 * This is the case for building the final CNF form.
	 */
	public static Expression buildFinalStep3() {
		Expression[] part1 = LargeTreeHelper.buildBase1();
		Expression[] part2 = LargeTreeHelper.buildBase2();
		Expression[] part3 = LargeTreeHelper.buildBase3();
		Expression[] part4 = LargeTreeHelper.buildLogical1(part1);
		Expression[] part5 = LargeTreeHelper.buildLogical2(part2);
		Expression[] part6 = LargeTreeHelper.buildLogical3(part3);
		Expression[] part7 = buildOr1(part4);
		Expression[] part8 = buildOr2(part5);
		Expression[] part9 = buildOr3(part6);
		Expression e295 = new AndExpression(part7[0], part7[1]);
		Expression e296 = new AndExpression(e295, part7[2]);
		Expression e297 = new AndExpression(e296, part7[3]);
		Expression e298 = new AndExpression(e297, part7[4]);
		Expression e299 = new AndExpression(e298, part7[5]);
		Expression e300 = new AndExpression(e299, part8[0]);
		Expression e301 = new AndExpression(e300, part8[1]);
		Expression e302 = new AndExpression(e301, part8[2]);
		Expression e303 = new AndExpression(e302, part8[3]);
		Expression e304 = new AndExpression(e303, part8[4]);
		Expression e305 = new AndExpression(e304, part8[5]);
		Expression e306 = new AndExpression(e305, part9[0]);
		Expression e307 = new AndExpression(e306, part9[1]);
		Expression e308 = new AndExpression(e307, part9[2]);
		Expression e309 = new AndExpression(e308, part9[3]);
		Expression e310 = new AndExpression(e309, part9[4]);
		return new AndExpression(e310, part9[5]);
	}
	
	/**
	 * This is the case for building the final CNF form.
	 */
	public static Expression buildFinalStep4() {
		Expression e1 = new Column("S.D");
		Expression e2 = new DateValue("\"2017-03-25\"");
		BinaryExpression e3 = new GreaterThan();
		e3.setLeftExpression(e1);
		e3.setRightExpression(e2);
		return new NotExpression(e3);
	}
	
	/**
	 * This is the case for building the final CNF form.
	 */
	public static Expression buildFinalStep5() {
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
		Expression e15 = new AndExpression(e9, e10);
		Expression e16 = new AndExpression(e15, e13);
		return new AndExpression(e16, e14);
	}
	
	private static Expression[] buildOr1(Expression[] array) {
		Expression[] result = new Expression[6];
		Expression e229 = new OrExpression(array[0], array[1]);
		Expression e230 = new OrExpression(e229, array[2]);
		result[0] = new Parenthesis(e230);
		Expression e232 = new OrExpression(array[3], array[4]);
		Expression e233 = new OrExpression(e232, array[5]);
		Expression e234 = new OrExpression(e233, array[6]);
		result[1] = new Parenthesis(e234);
		Expression e236 = new OrExpression(array[7], array[8]);
		Expression e237 = new OrExpression(e236, array[9]);
		Expression e238 = new OrExpression(e237, array[10]);
		result[2] = new Parenthesis(e238);
		Expression e240 = new OrExpression(array[11], array[12]);
		Expression e241 = new OrExpression(e240, array[13]);
		result[3] = new Parenthesis(e241);
		Expression e243 = new OrExpression(array[14], array[15]);
		Expression e244 = new OrExpression(e243, array[16]);
		Expression e245 = new OrExpression(e244, array[17]);
		result[4] = new Parenthesis(e245);
		Expression e247 = new OrExpression(array[18], array[19]);
		Expression e248 = new OrExpression(e247, array[20]);
		Expression e249 = new OrExpression(e248, array[21]);
		result[5] = new Parenthesis(e249);
		return result;
	}
	
	private static Expression[] buildOr2(Expression[] array) {
		Expression[] result = new Expression[6];
		Expression e251 = new OrExpression(array[0], array[1]);
		Expression e252 = new OrExpression(e251, array[2]);
		result[0] = new Parenthesis(e252);
		Expression e254 = new OrExpression(array[3], array[4]);
		Expression e255 = new OrExpression(e254, array[5]);
		Expression e256 = new OrExpression(e255, array[6]);
		result[1] = new Parenthesis(e256);
		Expression e258 = new OrExpression(array[7], array[8]);
		Expression e259 = new OrExpression(e258, array[9]);
		Expression e260 = new OrExpression(e259, array[10]);
		result[2] = new Parenthesis(e260);
		Expression e262 = new OrExpression(array[11], array[12]);
		Expression e263 = new OrExpression(e262, array[13]);
		result[3] = new Parenthesis(e263);
		Expression e265 = new OrExpression(array[14], array[15]);
		Expression e266 = new OrExpression(e265, array[16]);
		Expression e267 = new OrExpression(e266, array[17]);
		result[4] = new Parenthesis(e267);
		Expression e269 = new OrExpression(array[18], array[19]);
		Expression e270 = new OrExpression(e269, array[20]);
		Expression e271 = new OrExpression(e270, array[21]);
		result[5] = new Parenthesis(e271);
		return result;
	}
	
	private static Expression[] buildOr3(Expression[] array) {
		Expression[] result = new Expression[6];
		Expression e273 = new OrExpression(array[0], array[1]);
		Expression e274 = new OrExpression(e273, array[2]);
		result[0] = new Parenthesis(e274);
		Expression e276 = new OrExpression(array[3], array[4]);
		Expression e277 = new OrExpression(e276, array[5]);
		Expression e278 = new OrExpression(e277, array[6]);
		result[1] = new Parenthesis(e278);
		Expression e280 = new OrExpression(array[7], array[8]);
		Expression e281 = new OrExpression(e280, array[9]);
		Expression e282 = new OrExpression(e281, array[10]);
		result[2] = new Parenthesis(e282);
		Expression e284 = new OrExpression(array[11], array[12]);
		Expression e285 = new OrExpression(e284, array[13]);
		result[3] = new Parenthesis(e285);
		Expression e287 = new OrExpression(array[14], array[15]);
		Expression e288 = new OrExpression(e287, array[16]);
		Expression e289 = new OrExpression(e288, array[17]);
		result[4] = new Parenthesis(e289);
		Expression e291 = new OrExpression(array[18], array[19]);
		Expression e292 = new OrExpression(e291, array[20]);
		Expression e293 = new OrExpression(e292, array[21]);
		result[5] = new Parenthesis(e293);
		return result;
	}
	
}
