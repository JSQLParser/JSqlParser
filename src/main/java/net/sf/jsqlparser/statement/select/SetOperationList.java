package net.sf.jsqlparser.statement.select;

import java.util.List;

/**
 * A database set operation. This operation consists of a list of plainSelects 
 * connected by set operations (UNION,INTERSECT,MINUS,EXCEPT). All these operations
 * have the same priority.
 * @author tw
 */
public class SetOperationList implements SelectBody {

	private List<PlainSelect> plainSelects;
	private List<SetOperation> operations;
	private List<OrderByElement> orderByElements;
	private Limit limit;

	@Override
	public void accept(SelectVisitor selectVisitor) {
		selectVisitor.visit(this);
	}

	public List<OrderByElement> getOrderByElements() {
		return orderByElements;
	}

	public List<PlainSelect> getPlainSelects() {
		return plainSelects;
	}

	public List<SetOperation> getOperations() {
		return operations;
	}

	public void setOrderByElements(List<OrderByElement> orderByElements) {
		this.orderByElements = orderByElements;
	}

	public void setOpsAndSelects(List<PlainSelect> select, List<SetOperation> ops) {
		plainSelects = select;
		operations = ops;

		if (select.size() - 1 != ops.size()) {
			throw new IllegalArgumentException("list sizes are not valid");
		}
	}

	public Limit getLimit() {
		return limit;
	}

	public void setLimit(Limit limit) {
		this.limit = limit;
	}

	@Override
	public String toString() {
		StringBuilder buffer = new StringBuilder();

		for (int i = 0; i < plainSelects.size(); i++) {
			if (i != 0) {
				buffer.append(" ").append(operations.get(i - 1).toString()).append(" ");
			}
			buffer.append("(").append(plainSelects.get(i).toString()).append(")");
		}

		if (orderByElements != null) {
			buffer.append(PlainSelect.orderByToString(orderByElements));
		}
		if (limit != null) {
			buffer.append(limit.toString());
		}
		return buffer.toString();
	}

	/**
	 * list of set operations.
	 */
	public enum SetOperationType {
		INTERSECT,
		EXCEPT,
		MINUS,
		UNION
	}

}
