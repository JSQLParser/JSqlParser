package net.sf.jsqlparser.expression;

import net.sf.jsqlparser.statement.select.SubSelect;

public class AllComparisonExpression implements Expression {
	private SubSelect subSelect;

	public AllComparisonExpression(SubSelect subSelect) {
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
