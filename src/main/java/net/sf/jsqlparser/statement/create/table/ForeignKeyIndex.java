/*
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2013 JSQLParser
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as 
 * published by the Free Software Foundation, either version 2.1 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */
package net.sf.jsqlparser.statement.create.table;

import java.util.List;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.PlainSelect;

/**
 * Foreign Key Index
 * @author toben
 */
public class ForeignKeyIndex extends NamedConstraint {
	private Table table;
	private List<String> referencedColumnNames;

	public Table getTable() {
		return table;
	}

	public void setTable(Table table) {
		this.table = table;
	}

	public List<String> getReferencedColumnNames() {
		return referencedColumnNames;
	}

	public void setReferencedColumnNames(List<String> referencedColumnNames) {
		this.referencedColumnNames = referencedColumnNames;
	}

	@Override
	public String toString() {
		return super.toString()
				+ " REFERENCES " + table + PlainSelect.getStringList(getReferencedColumnNames(), true, true);
	}
}
