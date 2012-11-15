package net.sf.jsqlparser.expression;

import net.sf.jsqlparser.statement.select.SubSelect;

public class AnyComparisonExpression implements Expression {
	private SubSelect subSelect;

	public AnyComparisonExpression(SubSelect subSelect) {
		this.subSelect = subSelect;
	}

	public SubSelect GetSubSelect() {
		return subSelect;
	}

	@Override
	public void accept(ExpressionVisitor expressionVisitor) {
		expressionVisitor.visit(this);
	}
}
