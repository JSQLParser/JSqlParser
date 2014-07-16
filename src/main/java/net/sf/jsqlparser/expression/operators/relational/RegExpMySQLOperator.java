/*
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2014 JSQLParser
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
package net.sf.jsqlparser.expression.operators.relational;

import net.sf.jsqlparser.expression.BinaryExpression;
import net.sf.jsqlparser.expression.ExpressionVisitor;

public class RegExpMySQLOperator extends BinaryExpression {

	private RegExpMatchOperatorType operatorType;

	public RegExpMySQLOperator(RegExpMatchOperatorType operatorType) {
		if (operatorType == null) {
			throw new NullPointerException();
		}
		this.operatorType = operatorType;
	}

	public RegExpMatchOperatorType getOperatorType() {
		return operatorType;
	}

	@Override
	public void accept(ExpressionVisitor expressionVisitor) {
		expressionVisitor.visit(this);
	}

	@Override
	public String getStringExpression() {
		switch (operatorType) {
			case MATCH_CASESENSITIVE:
				return "REGEXP BINARY";
			case MATCH_CASEINSENSITIVE:
				return "REGEXP";
			default:
		}
		return null;
	}
}
