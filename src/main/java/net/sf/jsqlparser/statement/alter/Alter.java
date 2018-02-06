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
package net.sf.jsqlparser.statement.alter;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import lombok.Data;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitor;

/**
 *
 * @author toben & wrobstory
 */
@Data
public class Alter implements Statement {

	private Table table;

	private List<AlterExpression> alterExpressions;

	public void addAlterExpression(AlterExpression alterExpression) {
		if (alterExpressions == null) {
			alterExpressions = new ArrayList<AlterExpression>();
		}
		alterExpressions.add(alterExpression);
	}

	@Override
	public void accept(StatementVisitor statementVisitor) {
		statementVisitor.visit(this);
	}

	@Override
	public String toString() {

		StringBuilder b = new StringBuilder();
		b.append("ALTER TABLE ").append(table.getFullyQualifiedName()).append(" ");

		Iterator<AlterExpression> altIter = alterExpressions.iterator();

		while (altIter.hasNext()) {
			b.append(altIter.next().toString());

			// Need to append whitespace after each ADD or DROP statement
			// but not the last one
			if (altIter.hasNext()) {
				b.append(", ");
			}
		}

		return b.toString();
	}
}
