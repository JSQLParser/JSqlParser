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
package net.sf.jsqlparser.schema;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.ExpressionVisitor;
import net.sf.jsqlparser.parser.ASTNodeAccessImpl;

/**
 * A column. It can have the table name it belongs to.
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public final class Column extends ASTNodeAccessImpl implements Expression, MultiPartName {

	private Table table;
	private String columnName;

	public Column(Table table, String columnName) {
		setTable(table);
		setColumnName(columnName);
	}

	public Column(String columnName) {
		this(null, columnName);
	}

	@Override
	public String getFullyQualifiedName() {
		return getName(false);
	}

	/**
	 * Get name with out without using aliases.
	 *
	 * @param aliases
	 * @return
	 */
	public String getName(boolean aliases) {
		StringBuilder fqn = new StringBuilder();

		if (table != null) {
			if (table.getAlias() != null && aliases) {
				fqn.append(table.getAlias().getName());
			} else {
				fqn.append(table.getFullyQualifiedName());
			}
		}
		if (fqn.length() > 0) {
			fqn.append('.');
		}
		if (columnName != null) {
			fqn.append(columnName);
		}
		return fqn.toString();
	}

	@Override
	public void accept(ExpressionVisitor expressionVisitor) {
		expressionVisitor.visit(this);
	}

	@Override
	public String toString() {
		return getName(true);
	}
}
