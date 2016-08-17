/*
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2016 JSQLParser
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

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.ExpressionVisitor;

public class TeradataTrimExpression implements Expression
{
	String direction = "";
	String removalCharString = "";
	Expression targetStringExpression = null;
	Expression collationNameExpression = null;


	@Override
	public void accept(ExpressionVisitor expressionVisitor) {
		expressionVisitor.visit(this);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("TRIM(");
		if(direction != null)
		{
			sb.append(direction + " ");
		}
		if(removalCharString != null)
		{
			sb.append("'" + removalCharString + "' ");
		}
		if(direction != null || removalCharString != null)
		{
			sb.append("FROM ");
		}
		sb.append(targetStringExpression);
		if(collationNameExpression != null)
		{
			sb.append(" COLLATE " + collationNameExpression);
		}
		sb.append(")");
		return sb.toString();
	}

	public String getRemovalCharString()
	{
		return removalCharString;
	}

	public void setRemovalCharString(String removalCharExpression)
	{
		this.removalCharString = removalCharExpression;
	}

	public Expression getTargetStringExpression()
	{
		return targetStringExpression;
	}

	public void setTargetStringExpression(Expression targetStringExpression)
	{
		this.targetStringExpression = targetStringExpression;
	}

	public Expression getCollationNameExpression()
	{
		return collationNameExpression;
	}

	public void setCollationNameExpression(Expression collationNameExpression)
	{
		this.collationNameExpression = collationNameExpression;
	}

	public String getDirection()
	{
		return direction;
	}

	public void setDirection(String direction)
	{
		this.direction = direction;
	}
}
