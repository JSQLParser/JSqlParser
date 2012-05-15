package net.sf.jsqlparser.expression;

/**
 *  
 * @author tw
 */
public class CastExpression implements Expression {
	private Expression leftExpression;
	private String typeName;

	public String getTypeName() {
		return typeName;
	}

	public void setTypeName(String typeName) {
		this.typeName = typeName;
	}

	public Expression getLeftExpression() {
		return leftExpression;
	}

	public void setLeftExpression(Expression expression) {
		leftExpression = expression;
	}

	public void accept(ExpressionVisitor expressionVisitor) {
		expressionVisitor.visit(this);
	}

	public String toString() {
        return "CAST(" + leftExpression + " AS " + typeName + ")";
    }
}
