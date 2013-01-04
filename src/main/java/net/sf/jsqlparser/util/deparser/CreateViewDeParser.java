package net.sf.jsqlparser.util.deparser;

import net.sf.jsqlparser.statement.create.view.CreateView;

/**
 * A class to de-parse (that is, tranform from JSqlParser hierarchy into a
 * string) a {@link net.sf.jsqlparser.statement.create.view.CreateView}
 */
public class CreateViewDeParser {

	protected StringBuilder buffer;

	/**
	 * @param buffer the buffer that will be filled with the select
	 */
	public CreateViewDeParser(StringBuilder buffer) {
		this.buffer = buffer;
	}

	public void deParse(CreateView createView) {
		buffer.append("CREATE ");
		if (createView.isOrReplace()) {
			buffer.append("OR REPLACE ");
		}
		buffer.append("VIEW ").append(createView.getView().getWholeTableName());
		buffer.append(" AS ");
		buffer.append(createView.getSelectBody().toString());
	}

	public StringBuilder getBuffer() {
		return buffer;
	}

	public void setBuffer(StringBuilder buffer) {
		this.buffer = buffer;
	}
}
