package net.sf.jsqlparser.statement.create.table;

import java.util.List;

import net.sf.jsqlparser.statement.select.PlainSelect;

/**
 * An index (unique, primary etc.) in a CREATE TABLE statement
 */
public class Index {

	private String type;
	private List<String> columnsNames;
	private String name;

	/**
	 * A list of strings of all the columns regarding this index
	 */
	public List<String> getColumnsNames() {
		return columnsNames;
	}

	public String getName() {
		return name;
	}

	/**
	 * The type of this index: "PRIMARY KEY", "UNIQUE", "INDEX"
	 */
	public String getType() {
		return type;
	}

	public void setColumnsNames(List<String> list) {
		columnsNames = list;
	}

	public void setName(String string) {
		name = string;
	}

	public void setType(String string) {
		type = string;
	}

	@Override
	public String toString() {
		return type + " " + PlainSelect.getStringList(columnsNames, true, true) + (name != null ? " " + name : "");
	}
}