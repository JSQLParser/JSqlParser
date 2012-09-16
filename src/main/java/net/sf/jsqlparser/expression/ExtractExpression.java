package net.sf.jsqlparser.expression;

/**
 * Extract value from date/time expression. The name stores the part - name 
 * to get from the following date/time expression.
 * @author tw
 */
public class ExtractExpression implements Expression {
	private String name;
	private Expression expression;
	
	@Override
	public void accept(ExpressionVisitor expressionVisitor) {
		expressionVisitor.visit(this);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Expression getExpression() {
		return expression;
	}

	public void setExpression(Expression expression) {
		this.expression = expression;
	}

	@Override
	public String toString() {
		return "EXTRACT(" + name + " FROM " + expression + ')';
	}
}
