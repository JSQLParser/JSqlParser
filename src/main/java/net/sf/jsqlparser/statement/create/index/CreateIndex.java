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
package net.sf.jsqlparser.statement.create.index;

import java.util.Iterator;

import lombok.Data;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitor;
import net.sf.jsqlparser.statement.create.table.Index;

/**
 * A "CREATE INDEX" statement
 *
 * @author Raymond Augé
 */
@Data
public class CreateIndex implements Statement {

	/**
	 * The table on which the index is to be created
	 */
	private Table table;
	/**
	 * The index to be created
	 */
	private Index index;

	@Override
	public void accept(StatementVisitor statementVisitor) {
		statementVisitor.visit(this);
	}

	@Override
	public String toString() {
		StringBuilder buffer = new StringBuilder();

		buffer.append("CREATE ");

		if (index.getType() != null) {
			buffer.append(index.getType());
			buffer.append(" ");
		}

		buffer.append("INDEX ");
		buffer.append(index.getName());
		buffer.append(" ON ");
		buffer.append(table.getFullyQualifiedName());

		if (index.getColumnsNames() != null) {
			buffer.append(" (");

			for (Iterator<String> iter = index.getColumnsNames().iterator(); iter.hasNext();) {
				String columnName = iter.next();

				buffer.append(columnName);

				if (iter.hasNext()) {
					buffer.append(", ");
				}
			}

			buffer.append(")");
		}

		return buffer.toString();
	}
}
