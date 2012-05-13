package net.sf.jsqlparser.util.deparser;

import java.util.Iterator;
import java.util.List;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.ExpressionVisitor;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.AllColumns;
import net.sf.jsqlparser.statement.select.AllTableColumns;
import net.sf.jsqlparser.statement.select.FromItem;
import net.sf.jsqlparser.statement.select.FromItemVisitor;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.Limit;
import net.sf.jsqlparser.statement.select.OrderByElement;
import net.sf.jsqlparser.statement.select.OrderByVisitor;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.SelectExpressionItem;
import net.sf.jsqlparser.statement.select.SelectItem;
import net.sf.jsqlparser.statement.select.SelectItemVisitor;
import net.sf.jsqlparser.statement.select.SelectVisitor;
import net.sf.jsqlparser.statement.select.SubJoin;
import net.sf.jsqlparser.statement.select.SubSelect;
import net.sf.jsqlparser.statement.select.Top;
import net.sf.jsqlparser.statement.select.Union;

/**
 * A class to de-parse (that is, tranform from JSqlParser hierarchy into a string) a
 * {@link net.sf.jsqlparser.statement.select.Select}
 */
public class SelectDeParser implements SelectVisitor, OrderByVisitor, SelectItemVisitor, FromItemVisitor {
	protected StringBuilder buffer;
	protected ExpressionVisitor expressionVisitor;

	public SelectDeParser() {
	}

	/**
	 * @param expressionVisitor
	 *            a {@link ExpressionVisitor} to de-parse expressions. It has to share the same<br>
	 *            StringBuilder (buffer parameter) as this object in order to work
	 * @param buffer
	 *            the buffer that will be filled with the select
	 */
	public SelectDeParser(ExpressionVisitor expressionVisitor, StringBuilder buffer) {
		this.buffer = buffer;
		this.expressionVisitor = expressionVisitor;
	}

	public void visit(PlainSelect plainSelect) {
		buffer.append("SELECT ");
		Top top = plainSelect.getTop();
		if (top != null)
			buffer.append(top).append(" ");
		if (plainSelect.getDistinct() != null) {
			buffer.append("DISTINCT ");
			if (plainSelect.getDistinct().getOnSelectItems() != null) {
				buffer.append("ON (");
				for (Iterator<SelectItem> iter = plainSelect.getDistinct().getOnSelectItems().iterator(); iter.hasNext();) {
					SelectItem selectItem = iter.next();
					selectItem.accept(this);
					if (iter.hasNext()) {
						buffer.append(", ");
					}
				}
				buffer.append(") ");
			}

		}

		for (Iterator<SelectItem> iter = plainSelect.getSelectItems().iterator(); iter.hasNext();) {
			SelectItem selectItem = iter.next();
			selectItem.accept(this);
			if (iter.hasNext()) {
				buffer.append(", ");
			}
		}

		if (plainSelect.getFromItem() != null) {
			buffer.append(" FROM ");
			plainSelect.getFromItem().accept(this);
		}

		if (plainSelect.getJoins() != null) {
			for (Iterator<Join> iter = plainSelect.getJoins().iterator(); iter.hasNext();) {
				Join join = iter.next();
				deparseJoin(join);
			}
		}

		if (plainSelect.getWhere() != null) {
			buffer.append(" WHERE ");
			plainSelect.getWhere().accept(expressionVisitor);
		}

		if (plainSelect.getGroupByColumnReferences() != null) {
			buffer.append(" GROUP BY ");
			for (Iterator<Expression> iter = plainSelect.getGroupByColumnReferences().iterator(); iter.hasNext();) {
				Expression columnReference = iter.next();
				columnReference.accept(expressionVisitor);
				if (iter.hasNext()) {
					buffer.append(", ");
				}
			}
		}

		if (plainSelect.getHaving() != null) {
			buffer.append(" HAVING ");
			plainSelect.getHaving().accept(expressionVisitor);
		}

		if (plainSelect.getOrderByElements() != null) {
			deparseOrderBy(plainSelect.getOrderByElements());
		}

		if (plainSelect.getLimit() != null) {
			deparseLimit(plainSelect.getLimit());
		}

	}

	public void visit(Union union) {
		for (Iterator<PlainSelect> iter = union.getPlainSelects().iterator(); iter.hasNext();) {
			buffer.append("(");
			PlainSelect plainSelect = iter.next();
			plainSelect.accept(this);
			buffer.append(")");
			if (iter.hasNext()) {
				buffer.append(" UNION ");
				if (union.isAll()) {
					buffer.append("ALL ");// should UNION be a BinaryExpression ?
				}
			}

		}

		if (union.getOrderByElements() != null) {
			deparseOrderBy(union.getOrderByElements());
		}

		if (union.getLimit() != null) {
			deparseLimit(union.getLimit());
		}

	}

	public void visit(OrderByElement orderBy) {
		orderBy.getExpression().accept(expressionVisitor);
		if (!orderBy.isAsc())
			buffer.append(" DESC");
	}

	public void visit(Column column) {
		buffer.append(column.getWholeColumnName());
	}

	public void visit(AllColumns allColumns) {
		buffer.append("*");
	}

	public void visit(AllTableColumns allTableColumns) {
		buffer.append(allTableColumns.getTable().getWholeTableName() + ".*");
	}

	public void visit(SelectExpressionItem selectExpressionItem) {
		selectExpressionItem.getExpression().accept(expressionVisitor);
		if (selectExpressionItem.getAlias() != null) {
			buffer.append(" AS " + selectExpressionItem.getAlias());
		}

	}

	public void visit(SubSelect subSelect) {
		buffer.append("(");
		subSelect.getSelectBody().accept(this);
		buffer.append(")");
		String alias = subSelect.getAlias();
		if (alias != null) {
			buffer.append(" AS ").append(alias);
		}
	}

	public void visit(Table tableName) {
		buffer.append(tableName.getWholeTableName());
		String alias = tableName.getAlias();
		if (alias != null && !alias.isEmpty()) {
			buffer.append(" AS " + alias);
		}
	}

	public void deparseOrderBy(List<OrderByElement> orderByElements) {
		buffer.append(" ORDER BY ");
		for (Iterator<OrderByElement> iter = orderByElements.iterator(); iter.hasNext();) {
			OrderByElement orderByElement = iter.next();
			orderByElement.accept(this);
			if (iter.hasNext()) {
				buffer.append(", ");
			}
		}
	}

	public void deparseLimit(Limit limit) {
		// LIMIT n OFFSET skip
		if (limit.isRowCountJdbcParameter()) {
			buffer.append(" LIMIT ");
			buffer.append("?");
		} else if (limit.getRowCount() != 0) {
			buffer.append(" LIMIT ");
			buffer.append(limit.getRowCount());
		}

		if (limit.isOffsetJdbcParameter()) {
			buffer.append(" OFFSET ?");
		} else if (limit.getOffset() != 0) {
			buffer.append(" OFFSET " + limit.getOffset());
		}

	}

	public StringBuilder getBuffer() {
		return buffer;
	}

	public void setBuffer(StringBuilder buffer) {
		this.buffer = buffer;
	}

	public ExpressionVisitor getExpressionVisitor() {
		return expressionVisitor;
	}

	public void setExpressionVisitor(ExpressionVisitor visitor) {
		expressionVisitor = visitor;
	}

	public void visit(SubJoin subjoin) {
		buffer.append("(");
		subjoin.getLeft().accept(this);
		deparseJoin(subjoin.getJoin());
		buffer.append(")");
	}

	public void deparseJoin(Join join) {
		if (join.isSimple())
			buffer.append(", ");
		else {

			if (join.isRight())
				buffer.append(" RIGHT");
			else if (join.isNatural())
				buffer.append(" NATURAL");
			else if (join.isFull())
				buffer.append(" FULL");
			else if (join.isLeft())
				buffer.append(" LEFT");

			if (join.isOuter())
				buffer.append(" OUTER");
			else if (join.isInner())
				buffer.append(" INNER");

			buffer.append(" JOIN ");

		}

		FromItem fromItem = join.getRightItem();
		fromItem.accept(this);
		if (join.getOnExpression() != null) {
			buffer.append(" ON ");
			join.getOnExpression().accept(expressionVisitor);
		}
		if (join.getUsingColumns() != null) {
			buffer.append(" USING (");
			for (Iterator<Column> iterator = join.getUsingColumns().iterator(); iterator.hasNext();) {
				Column column = iterator.next();
				buffer.append(column.getWholeColumnName());
				if (iterator.hasNext()) {
					buffer.append(", ");
				}
			}
			buffer.append(")");
		}

	}

}
