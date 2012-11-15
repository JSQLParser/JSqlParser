package net.sf.jsqlparser.util.deparser;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.ExpressionVisitor;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.update.Update;

/**
 * A class to de-parse (that is, tranform from JSqlParser hierarchy into a string) an
 * {@link net.sf.jsqlparser.statement.update.Update}
 */
public class UpdateDeParser {
	protected StringBuilder buffer;
	protected ExpressionVisitor expressionVisitor;

	public UpdateDeParser() {
	}

	/**
	 * @param expressionVisitor
	 *            a {@link ExpressionVisitor} to de-parse expressions. It has to share the same<br>
	 *            StringBuilder (buffer parameter) as this object in order to work
	 * @param buffer
	 *            the buffer that will be filled with the select
	 */
	public UpdateDeParser(ExpressionVisitor expressionVisitor, StringBuilder buffer) {
		this.buffer = buffer;
		this.expressionVisitor = expressionVisitor;
	}

	public StringBuilder getBuffer() {
		return buffer;
	}

	public void setBuffer(StringBuilder buffer) {
		this.buffer = buffer;
	}

	public void deParse(Update update) {
		buffer.append("UPDATE ").append(update.getTable().getWholeTableName()).append(" SET ");
		for (int i = 0; i < update.getColumns().size(); i++) {
			Column column = (Column) update.getColumns().get(i);
			buffer.append(column.getWholeColumnName()).append("=");

			Expression expression = (Expression) update.getExpressions().get(i);
			expression.accept(expressionVisitor);
			if (i < update.getColumns().size() - 1) {
				buffer.append(", ");
			}

		}

		if (update.getWhere() != null) {
			buffer.append(" WHERE ");
			update.getWhere().accept(expressionVisitor);
		}

	}

	public ExpressionVisitor getExpressionVisitor() {
		return expressionVisitor;
	}

	public void setExpressionVisitor(ExpressionVisitor visitor) {
		expressionVisitor = visitor;
	}

}
