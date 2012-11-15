package net.sf.jsqlparser.statement.select;

import java.util.List;

/**
 * A DISTINCT [ON (expression, ...)] clause
 */
public class Distinct {
	private List<SelectItem> onSelectItems;

	/**
	 * A list of {@link SelectItem}s expressions, as in "select DISTINCT ON (a,b,c) a,b FROM..."
	 * 
	 * @return a list of {@link SelectItem}s expressions
	 */
	public List<SelectItem> getOnSelectItems() {
		return onSelectItems;
	}

	public void setOnSelectItems(List<SelectItem> list) {
		onSelectItems = list;
	}

	@Override
	public String toString() {
		String sql = "DISTINCT";

		if (onSelectItems != null && onSelectItems.size() > 0) {
			sql += " ON (" + PlainSelect.getStringList(onSelectItems) + ")";
		}

		return sql;
	}
}
