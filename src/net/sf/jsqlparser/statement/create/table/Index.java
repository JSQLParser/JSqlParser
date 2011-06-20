package net.sf.jsqlparser.statement.create.table;

import java.util.List;

import net.sf.jsqlparser.statement.select.PlainSelect;

/**
 * An index (unique, primary etc.) in a CREATE TABLE statement
 */
public class Index {

	private String type;
	private List columnsNames;
	private String name;

	/**
	 * A list of strings of all the columns regarding this index
	 */
	public List getColumnsNames() {
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

	public void setColumnsNames(List list) {
		columnsNames = list;
	}

	public void setName(String string) {
		name = string;
	}

	public void setType(String string) {
		type = string;
	}

	public String toString() {
		return type + " " + PlainSelect.getStringList(columnsNames, true, true) + (name != null ? " " + name : "");
	}
}