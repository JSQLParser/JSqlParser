package net.sf.jsqlparser.expression.operators.arithmetic;

import net.sf.jsqlparser.expression.BinaryExpression;
import net.sf.jsqlparser.expression.ExpressionVisitor;

/**
 * Modulo expression (a % b).
 * @author toben
 */
public class Modulo extends BinaryExpression {
	public Modulo() {
	}
	
	@Override
	public void accept(ExpressionVisitor expressionVisitor) {
		expressionVisitor.visit(this);
	}
	
	@Override
	public String getStringExpression() {
		return "%";
	}
}
