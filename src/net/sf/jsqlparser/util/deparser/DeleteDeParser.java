package net.sf.jsqlparser.util.deparser;

import net.sf.jsqlparser.expression.ExpressionVisitor;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.select.SelectVisitor;

/**
 * A class to de-parse (that is, tranform from JSqlParser hierarchy into a string) a
 * {@link net.sf.jsqlparser.statement.delete.Delete}
 */
public class DeleteDeParser {
	protected StringBuffer buffer;
	protected ExpressionVisitor expressionVisitor;

	public DeleteDeParser() {
	}

	/**
	 * @param expressionVisitor
	 *            a {@link ExpressionVisitor} to de-parse expressions. It has to share the same<br>
	 *            StringBuffer (buffer parameter) as this object in order to work
	 * @param buffer
	 *            the buffer that will be filled with the select
	 */
	public DeleteDeParser(ExpressionVisitor expressionVisitor, StringBuffer buffer) {
		this.buffer = buffer;
		this.expressionVisitor = expressionVisitor;
	}

	public StringBuffer getBuffer() {
		return buffer;
	}

	public void setBuffer(StringBuffer buffer) {
		this.buffer = buffer;
	}

	public void deParse(Delete delete) {
		buffer.append("DELETE FROM " + delete.getTable().getWholeTableName());
		if (delete.getWhere() != null) {
			buffer.append(" WHERE ");
			delete.getWhere().accept(expressionVisitor);
		}

	}

	public ExpressionVisitor getExpressionVisitor() {
		return expressionVisitor;
	}

	public void setExpressionVisitor(ExpressionVisitor visitor) {
		expressionVisitor = visitor;
	}

}
