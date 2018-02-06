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
package net.sf.jsqlparser.expression;

import lombok.Data;
import net.sf.jsqlparser.parser.ASTNodeAccessImpl;

/**
 *
 * @author toben
 */
@Data
public class OracleHierarchicalExpression extends ASTNodeAccessImpl implements Expression {

	private Expression startExpression;
	private Expression connectExpression;
	private boolean noCycle = false;
	boolean connectFirst = false;

	@Override
	public void accept(ExpressionVisitor expressionVisitor) {
		expressionVisitor.visit(this);
	}

	@Override
	public String toString() {
		StringBuilder b = new StringBuilder();
		if (isConnectFirst()) {
			b.append(" CONNECT BY ");
			if (isNoCycle()) {
				b.append("NOCYCLE ");
			}
			b.append(connectExpression.toString());
			if (startExpression != null) {
				b.append(" START WITH ").append(startExpression.toString());
			}
		} else {
			if (startExpression != null) {
				b.append(" START WITH ").append(startExpression.toString());
			}
			b.append(" CONNECT BY ");
			if (isNoCycle()) {
				b.append("NOCYCLE ");
			}
			b.append(connectExpression.toString());
		}
		return b.toString();
	}
}
