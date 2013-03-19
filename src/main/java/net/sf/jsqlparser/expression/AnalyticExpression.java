package net.sf.jsqlparser.expression;

import java.util.List;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.OrderByElement;

/**
 * Analytic function. The name of the function is variable but the parameters
 * following the special analytic function path. e.g. row_number() over (order
 * by test)
 *
 * @author tw
 */
public class AnalyticExpression implements Expression {

	private List<Column> partitionByColumns;
	private List<OrderByElement> orderByElements;
	private String name;
	private Expression expression;

	@Override
	public void accept(ExpressionVisitor expressionVisitor) {
		expressionVisitor.visit(this);
	}

	public List<OrderByElement> getOrderByElements() {
		return orderByElements;
	}

	public void setOrderByElements(List<OrderByElement> orderByElements) {
		this.orderByElements = orderByElements;
	}

	public List<Column> getPartitionByColumns() {
		return partitionByColumns;
	}

	public void setPartitionByColumns(List<Column> partitionByColumns) {
		this.partitionByColumns = partitionByColumns;
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
		StringBuilder b = new StringBuilder();

		b.append(name).append("(");
		if (expression != null) {
			b.append(expression.toString());
		}
		b.append(") OVER (");
		if (partitionByColumns != null && !partitionByColumns.isEmpty()) {
			b.append("PARTITION BY ");
			for (int i = 0; i < partitionByColumns.size(); i++) {
				if (i > 0) {
					b.append(", ");
				}
				b.append(partitionByColumns.get(i).toString());
			}
			b.append(" ");
		}

		if (orderByElements != null && !orderByElements.isEmpty()) {
			b.append("ORDER BY ");
			for (int i = 0; i < orderByElements.size(); i++) {
				if (i > 0) {
					b.append(", ");
				}
				b.append(orderByElements.get(i).toString());
			}
		}

		b.append(")");

		return b.toString();
	}
}
