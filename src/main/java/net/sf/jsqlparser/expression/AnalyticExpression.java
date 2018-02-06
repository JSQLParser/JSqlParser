/*
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2013 JSQLParser
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as 
 * published by the Free Software Foundation, either version 2.1 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */
package net.sf.jsqlparser.expression;

import java.util.List;

import lombok.Data;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.parser.ASTNodeAccessImpl;
import net.sf.jsqlparser.statement.select.OrderByElement;
import net.sf.jsqlparser.statement.select.PlainSelect;

/**
 * Analytic function. The name of the function is variable but the parameters following the special
 * analytic function path. e.g. row_number() over (order by test). Additional there can be an
 * expression for an analytical aggregate like sum(col) or the "all collumns" wildcard like
 * count(*).
 *
 * @author tw
 */
@Data
public class AnalyticExpression extends ASTNodeAccessImpl implements Expression {

	private ExpressionList partitionExpressionList;
	private List<OrderByElement> orderByElements;
	private String name;
	private Expression expression;
	private Expression offset;
	private Expression defaultValue;
	private boolean allColumns = false;
	private WindowElement windowElement;
	private KeepExpression keep = null;
	private AnalyticType type = AnalyticType.OVER;

	@Override
	public void accept(ExpressionVisitor expressionVisitor) {
		expressionVisitor.visit(this);
	}

	@Override
	public String toString() {
		StringBuilder b = new StringBuilder();

		b.append(name).append("(");
		if (expression != null) {
			b.append(expression.toString());
			if (offset != null) {
				b.append(", ").append(offset.toString());
				if (defaultValue != null) {
					b.append(", ").append(defaultValue.toString());
				}
			}
		} else if (isAllColumns()) {
			b.append("*");
		}
		b.append(") ");
		if (keep != null) {
			b.append(keep.toString()).append(" ");
		}

		switch (type) {
			case WITHIN_GROUP:
				b.append("WITHIN GROUP");
				break;
			default:
				b.append("OVER");
		}
		b.append(" (");

		toStringPartitionBy(b);
		toStringOrderByElements(b);

		b.append(")");

		return b.toString();
	}

	private void toStringPartitionBy(StringBuilder b) {
		if (partitionExpressionList != null && !partitionExpressionList.getExpressions().isEmpty()) {
			b.append("PARTITION BY ");
			b.append(PlainSelect.getStringList(partitionExpressionList.getExpressions(), true, false));
			b.append(" ");
		}
	}

	private void toStringOrderByElements(StringBuilder b) {
		if (orderByElements != null && !orderByElements.isEmpty()) {
			b.append("ORDER BY ");
			for (int i = 0; i < orderByElements.size(); i++) {
				if (i > 0) {
					b.append(", ");
				}
				b.append(orderByElements.get(i).toString());
			}

			if (windowElement != null) {
				b.append(' ');
				b.append(windowElement);
			}
		}
	}
}
