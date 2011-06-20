package net.sf.jsqlparser.util.deparser;

import java.util.Iterator;

import net.sf.jsqlparser.statement.create.table.ColumnDefinition;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.sf.jsqlparser.statement.create.table.Index;

/**
 * A class to de-parse (that is, tranform from JSqlParser hierarchy into a string)
 * a {@link net.sf.jsqlparser.statement.create.table.CreateTable}
 */
public class CreateTableDeParser {
	protected StringBuffer buffer;

	/**
	 * @param buffer the buffer that will be filled with the select
	 */
	public CreateTableDeParser(StringBuffer buffer) {
		this.buffer = buffer;
	}

	public void deParse(CreateTable createTable) {
		buffer.append("CREATE TABLE " + createTable.getTable().getWholeTableName());
		if (createTable.getColumnDefinitions() != null) {
			buffer.append(" { ");
			for (Iterator iter = createTable.getColumnDefinitions().iterator(); iter.hasNext();) {
				ColumnDefinition columnDefinition = (ColumnDefinition) iter.next();
				buffer.append(columnDefinition.getColumnName());
				buffer.append(" ");
				buffer.append(columnDefinition.getColDataType().getDataType());
				if (columnDefinition.getColDataType().getArgumentsStringList() != null) {
					for (Iterator iterator = columnDefinition.getColDataType().getArgumentsStringList().iterator(); iterator.hasNext();) {
						buffer.append(" ");
						buffer.append((String) iterator.next());
					}
				}
				if (columnDefinition.getColumnSpecStrings() != null) {
					for (Iterator iterator = columnDefinition.getColumnSpecStrings().iterator(); iterator.hasNext();) {
						buffer.append(" ");
						buffer.append((String) iterator.next());
					}
				}

				if (iter.hasNext())
					buffer.append(",\n");

			}

			for (Iterator iter = createTable.getIndexes().iterator(); iter.hasNext();) {
				buffer.append(",\n");
				Index index = (Index) iter.next();
				buffer.append(index.getType() + " " + index.getName());
				buffer.append("(");
				for (Iterator iterator = index.getColumnsNames().iterator(); iterator.hasNext();) {
					buffer.append((String) iterator.next());
					if (iterator.hasNext()) {
						buffer.append(", ");
					}
				}
				buffer.append(")");

				if (iter.hasNext())
					buffer.append(",\n");
			}

			buffer.append(" \n} ");
		}
	}
	
	public StringBuffer getBuffer() {
		return buffer;
	}

	public void setBuffer(StringBuffer buffer) {
		this.buffer = buffer;
	}

}
