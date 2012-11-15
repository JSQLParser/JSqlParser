package net.sf.jsqlparser.expression;

import java.util.List;

/**
 * Analytic function. The name of the function is variable but the parameters
 * following the special analytic function path.
 * e.g. row_number() over (order by test)
 * @author tw
 */
public class AnalyticExpression implements Expression {

	private List partitionByColumns;
	private List orderByElements;
	private String name;
	
	@Override
	public void accept(ExpressionVisitor expressionVisitor) {
		expressionVisitor.visit(this);
	}

	public List getOrderByElements() {
		return orderByElements;
	}

	public void setOrderByElements(List orderByElements) {
		this.orderByElements = orderByElements;
	}

	public List getPartitionByColumns() {
		return partitionByColumns;
	}

	public void setPartitionByColumns(List partitionByColumns) {
		this.partitionByColumns = partitionByColumns;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	@Override
	public String toString() {
		StringBuilder b = new StringBuilder();
		
		b.append(name).append("() OVER (");
		if (partitionByColumns!=null && !partitionByColumns.isEmpty()) {
			b.append("PARTITION BY ");
			for (int i=0;i<partitionByColumns.size();i++) {
				if (i>0)
					b.append(", ");
				b.append(partitionByColumns.get(i).toString());
			}
			b.append(" ");
		}
		
		if (orderByElements!=null && !orderByElements.isEmpty()) {
			b.append("ORDER BY ");
			for (int i=0;i<orderByElements.size();i++) {
				if (i>0)
					b.append(", ");
				b.append(orderByElements.get(i).toString());
			}
		}
		
        b.append(")");
		
		return b.toString();
    }
}
